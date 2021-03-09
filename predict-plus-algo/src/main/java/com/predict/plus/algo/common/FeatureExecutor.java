package com.predict.plus.algo.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.predict.plus.algo.exception.FeatureException;
import com.predict.plus.algo.features.RawFeatureType;
import com.predict.plus.algo.operator.NumberOperator;
import com.predict.plus.algo.operator.TextOperator;
import com.predict.plus.algo.operator.UtilsOperator;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
public class FeatureExecutor implements Serializable {
    Feature feature;
    Object expression;
    Object outlierCheckExpression;
    private double[] defaultVector;
    Map<String, Object> defaultRawMap;
    Map<String, Pair<RawFeatureType, Object>> rawFeatureType;

    boolean fastReturn = false;

    /**
     * 是否需要重新定位，false，不需要，true需要
     */
    boolean reLocation = false;

    public FeatureExecutor(Feature feature,
                           Map<String, RawFeatureType> allType) {
        this.feature = feature;
        String validateMessage = validate();

        if (validateMessage != null) {
            throw new FeatureException(validateMessage);
        }

        ParserContext parserContext = ParserContext.create();
        if (StringUtils.isNotBlank(feature.getOperator()) && feature.getOperator() != null) {

            Method[] methods = UtilsOperator.class.getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (Modifier.isStatic(methods[i].getModifiers())) {
                    parserContext.addImport(methods[i].getName(), methods[i]);
                }
            }

            parserContext.addImport("number", NumberOperator.class);
            parserContext.addImport("text", TextOperator.class);
        }

        expression = MVEL.compileExpression(feature.getOperator(), parserContext);

        if (StringUtils.isNotBlank(feature.getOutlierCheck()) && feature.getOutlierCheck() != null) {
            outlierCheckExpression = MVEL.compileExpression(feature.getOutlierCheck(), parserContext);
        }

        //如果只有一个且没有计算就直接快速返回
        if (feature.getRawList().size() == 1
                && feature.getOperator().equals(feature.getRawList().get(0).trim())) {
            fastReturn = true;
        }

        if (CollectionUtils.isNotEmpty(feature.getReturnValueLoc())) {
            reLocation = true;
        }

        this.rawFeatureType = new HashMap<>();

        defaultVector = buildDefaultVector();
        defaultRawMap = buildDefaultRowMap();
        List<String> rawList = feature.getRawList();
        for (String s : rawList) {
            this.rawFeatureType.put(s, Pair.of(allType.get(s), defaultRawMap.get(s)));
        }
    }

    private String validate() {
        if (feature.getName() == null) {
            return String.format("特征名称为空 featureid=%d", feature.getId());
        }
        if (CollectionUtils.isEmpty(feature.getRawList())) {
            return "原始特征列表为空";
        }
        if (CollectionUtils.isNotEmpty(feature.getReturnValueLoc()) && feature.getReturnValueLoc().size() != feature.getReturnValueNum()) {
            return "如果设置了ReturnValueLoc，则ReturnValueLoc.size 必须等于 ReturnValueNum";
        }
        return null;
    }

    /**
     * @param rawDataMap
     * @return
     */
    public double[] toVector(Map<String, Object> rawDataMap) {

        List<String> rawList = feature.getRawList();

        Map<String, Object> paramEnv = Maps.newHashMapWithExpectedSize(rawList.size());

        for (String s : rawList) {
            Object rawData = rawDataMap.get(s);
            Pair<RawFeatureType, Object> context = this.rawFeatureType.get(s);
            Object param = null;
            if (rawData != null) {
                switch (context.getLeft()) {
                    case Number:
                        String s1 = rawData.toString();
                        if (StringUtils.isNotBlank(s1)) {
                            param = Double.valueOf(s1);
                        }
                        break;
                    case Text:
                        param = String.valueOf(rawData);
                        break;
                    default:
                        throw new UnsupportedOperationException("不支持的原始数据类型");
                }
            }
            if (param != null) {
                paramEnv.put(s, param);
            } else if (context.getRight() == null) {
                return getDefaultVector();
            } else {
                paramEnv.put(s, context.getRight());
            }
        }

        boolean isNormal = isNormal(paramEnv);
        //正常数据进入计算
        if (isNormal) {
            // 无特征计算的数据返回特征值本身
            if (fastReturn) {
                double[] result = getDefaultVector();
                result[0] = (double) (paramEnv.get(rawList.get(0)));
                return result;
                // 有特征计算的数据返回计算后的特征
            } else {
                double[] eval = evalOperator(paramEnv);
                return calcEval(eval);
            }
        }
        // 异常数据返回默认值
        return getDefaultVector();
    }

    private boolean isNormal(Map<String, Object> paramEnv) {
        if (outlierCheckExpression == null) {
            return true;
        } else {
            return outlierCheck(paramEnv, this);
        }
    }

    private double[] buildDefaultVector() {
        double[] result = new double[feature.getReturnValueNum()];

        if ( ! CollectionUtils.isEmpty(feature.getDefaultValues())) {
            int min = Math.min(feature.getDefaultValues().size(), result.length);

            /**
             * 如果default只有一个的时候，defaultVector会把所有位置都填充defaultValue
             * 如果有多个位置，但是按位置填充defaultVector，不够填0
             */
            if (min == 1) {
                for (int i = 0; i < result.length; i++) {
                    result[i] = feature.getDefaultValues().get(0);
                }
            } else {
                for (int i = 0; i < min; i++) {
                    result[i] = feature.getDefaultValues().get(i);
                }
            }
        }

        return result;
    }

    private Map<String, Object> buildDefaultRowMap(){
        Map<String, Object> defaultMap = new HashMap<>();
        String defaultRawMapStr = feature.getDefaultRawMap();

        if (StringUtils.isNotBlank(defaultRawMapStr)) {
            defaultMap.putAll(JSON.parseObject(defaultRawMapStr,
                    new TypeReference<Map<String, Object>>() {
                    }.getType()));
        }
        return defaultMap;

    }


    private double[] calcEval(double[] evalResult) {
        if (reLocation) {
            List<Integer> returnValueLoc = feature.getReturnValueLoc();
            double[] result = new double[feature.getReturnValueNum()];
            int i = 0;
            for (Integer loc : returnValueLoc) {
                result[i++] = evalResult[loc];
            }
            return result;
        } else {
            return evalResult;
        }
    }

    /**
     * @param env 参数上下文
     * @return 向量
     */
    public double[] evalOperator(Map<String, Object> env) {
        double[] vector = getDefaultVector();
        try {
            Object result = MVEL.executeExpression(expression, env);
            if (feature.getReturnValueNum() == 1 && result instanceof Number) {
                vector[0] = ((Number) result).doubleValue();
            } else {
                double[] resultArray = ((double[]) result);
                System.arraycopy(resultArray, 0, vector, 0, Math.min(resultArray.length, vector.length));
            }
            return vector;
        } catch (Exception e) {
            FeatureException featureException = new FeatureException(e, e.getMessage() + "\n" + JSON.toJSONString(getFeature()));
            log.error("error message", featureException);
            throw featureException;
        }
    }

    public boolean outlierCheck(Map<String, Object> env, FeatureExecutor executor) {
        try {
            Object result = MVEL.executeExpression(outlierCheckExpression, env);
            return !((Boolean) result).booleanValue();
        } catch (Exception e) {
            log.error("executor outlierCheck expression error, featureName:{}, param:{}", executor.getFeature().getName(), env, e);
        }
        return false;
    }


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Object getExpression() {
        return expression;
    }

    public void setExpression(Object expression) {
        this.expression = expression;
    }

    public Object getOutlierCheckExpression() {
        return outlierCheckExpression;
    }

    public void setOutlierCheckExpression(Object outlierCheckExpression) {
        this.outlierCheckExpression = outlierCheckExpression;
    }

    /**
     * 深度复制
     *
     * @return
     */
    public double[] getDefaultVector() {
        double[] temp = new double[defaultVector.length];
        System.arraycopy(defaultVector, 0, temp, 0, defaultVector.length);
        return temp;
    }

    public void setDefaultVector(double[] defaultVector) {
        this.defaultVector = defaultVector;
    }

    public Map<String, Object> getDefaultRawMap() {
        return defaultRawMap;
    }

    public void setDefaultRawMap(Map<String, Object> defaultRawMap) {
        this.defaultRawMap = defaultRawMap;
    }

}
