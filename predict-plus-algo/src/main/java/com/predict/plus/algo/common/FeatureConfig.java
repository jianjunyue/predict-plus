package com.predict.plus.algo.common;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.predict.plus.algo.exception.FeatureException;
import com.predict.plus.algo.features.RawFeatureType;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
public class FeatureConfig implements Serializable {

    int modelVersion;

    List<FeatureExecutor> featureExecutors;

    int vectorLength = 0;
    /**
     * featureFieldIndex
     * featureName -> Pair<VectorLeftBound,VectorRightBound>
     * [VectorLeftBound,VectorRightBound)
     */
    Map<String, Pair<Integer, Integer>> featureFieldIndex = new HashMap<>();

    Map<String, RawFeatureType> rawFeatureTypeMap = new HashMap<>();

    PreprocessFeatureExecutor preprocessFeatureExecutor;

    DecimalFormat decimalFormat = new DecimalFormat(".######");

    /**
     * 构造方法
     *
     * @param modelVersion 特征清单版本
     * @param features     特征清单列表
     * @param allType      特征库
     */
    public FeatureConfig(int modelVersion, List<Feature> features, Map<String, RawFeatureType> allType, List<PreprocessFeature> preprocessFeatures) {
        this.modelVersion = modelVersion;

        preprocessFeatureExecutor = new PreprocessFeatureExecutor(preprocessFeatures);

        if (CollectionUtils.isNotEmpty(features)) {
            features.sort(Comparator.comparingInt(Feature::getId));
            Set<Integer> collect = features.stream().map(Feature::getId).collect(Collectors.toSet());
            if (collect.size() != features.size()) {
                throw new FeatureException("featureId duplicates! check the featureId list");
            }
            featureExecutors = Lists.newArrayList();

            for (Feature feature : features) {
                try {
                    FeatureExecutor featureExecutor = new FeatureExecutor(feature, allType);
                    if (featureFieldIndex.containsKey(feature.getName())) {
                        throw new FeatureException(String.format("featureName duplicates! check the featureName : %s", feature.getName()));
                    }
                    featureFieldIndex.put(feature.getName(),
                            Pair.of(vectorLength, vectorLength + feature.getReturnValueNum()));
                    vectorLength += feature.getReturnValueNum();

                    if (CollectionUtils.isEmpty(feature.getRawList())) {
                        throw new FeatureException("raw feature list is empty !");
                    }

                    for (String rawFeatureName : feature.getRawList()) {
                        if (allType.containsKey(rawFeatureName)) {
                            rawFeatureTypeMap.put(rawFeatureName, allType.get(rawFeatureName));
                        } else {
                            throw new FeatureException(String.format("feature not register! check featureType or check featureConfig.rawList field error featureName :%s", rawFeatureName));
                        }
                    }

                    featureExecutors.add(featureExecutor);
                } catch (Exception e) {
                    log.error("featureConfig catch exception {}", feature.getName(), e);
                    throw new FeatureException("featureConfig catch exception");
                }
            }

            if (featureFieldIndex.size() != features.size()) {
                throw new FeatureException("featureName duplicates! check the featureName list ");
            }
        } else {
            throw new FeatureException("features is empty");
        }

    }

    /**
     * 将原始特征转换成向量的形式，字段类型默认为double
     *
     * @param jsonData 输入原始特征的json字符串
     * @return double特征向量
     */
    public double[] transformToArray(String jsonData) {
        Map<String, Object> rawDataMap = JSON.parseObject(jsonData).getInnerMap();
        return transformToArray(rawDataMap);
    }

    /**
     * 将原始特征转换成向量的形式，字段类型为float
     *
     * @param jsonData 输入原始特征的json字符串
     * @return float特征向量
     */
    public float[] transformToFloatArray(String jsonData) {
        Map<String, Object> rawDataMap = JSON.parseObject(jsonData).getInnerMap();
        return transformToFloatArray(rawDataMap);
    }

    /**
     * 将原始特征转换成name -> vector的形式，一般可以用于debug
     *
     * @param jsonData
     * @return
     */
    public Map<String, double[]> transformToMap(String jsonData) {
        double[] array = transformToArray(jsonData);
        Map<String, double[]> result = new HashMap<>();

        for (FeatureExecutor featureExecutor : featureExecutors) {
            Feature feature = featureExecutor.getFeature();
            String name = feature.getName();
            Pair<Integer, Integer> indexPair = featureFieldIndex.get(name);
            double[] doubles = Arrays.copyOfRange(array, indexPair.getLeft(), indexPair.getRight());
            result.put(name, doubles);
        }
        return result;
    }

    /**
     * 转化成libsvm的扩展形式，一般可用于learning to ranking 的原始数据
     *
     * @param jsonStr           json字符串
     * @param targetColumnsName label字段名称
     * @param defaultTarget     label为空时的默认值
     * @param qid               查询id
     * @return
     */
    public String transformToLibSVMExtension(String jsonStr, String targetColumnsName, String defaultTarget, String qid) {
        Map<String, Object> rawDataMap = JSON.parseObject(jsonStr).getInnerMap();
        String target = rawDataMap.get(targetColumnsName) != null ? String.valueOf(rawDataMap.get(targetColumnsName)) : defaultTarget;
        StringBuilder sb = new StringBuilder();
        sb.append(target);
        sb.append(" ");
        sb.append("qid:");
        sb.append(qid);
        sb.append(" ");
        sb.append(transformToLibSVMVector(rawDataMap));
        return sb.toString();
    }

    /**
     * 转换成libsvm 的形式，但是不包含target
     *
     * @param rawDataMap
     * @return
     */
    private String transformToLibSVMVector(Map<String, Object> rawDataMap) {
        double[] vector = transformToArray(rawDataMap);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (Math.abs(vector[i]) > 0.000001) {
                sb.append(" ");
                sb.append(i);
                sb.append(":");
                sb.append(decimalFormat.format(vector[i]));
            }
        }
        return sb.toString();
    }

    /**
     * 转化成libsvm的格式字符串
     *
     * @param jsonStr
     * @param targetColumnsName
     * @param defaultTarget
     * @return
     */
    public String transformToLibSVM(String jsonStr, String targetColumnsName, String defaultTarget) {
        try {
            Map<String, Object> rawDataMap = JSON.parseObject(jsonStr);

            StringBuilder sb = new StringBuilder();
            String target = rawDataMap.get(targetColumnsName) != null ? String.valueOf(rawDataMap.get(targetColumnsName)) : defaultTarget;

            sb.append(target);
            sb.append(" ");
            sb.append(transformToLibSVMVector(rawDataMap));
            return sb.toString();
        } catch (Exception e) {
            throw new FeatureException(e, jsonStr);
        }

    }

    /**
     * 获取特征清单名称列表，按照ID的顺序
     *
     * @return
     */
    public String[] featureIndexMap() {
        String[] result = new String[vectorLength];

        for (Map.Entry<String, Pair<Integer, Integer>> indexPair : featureFieldIndex.entrySet()) {
            Pair<Integer, Integer> value = indexPair.getValue();
            int loc = 0;
            boolean isSingle = value.getRight() - value.getLeft() == 1;
            for (int i = value.getLeft(); i < value.getRight(); i++) {
                result[i] = isSingle ? indexPair.getKey() : String.format("%s[%d]", indexPair.getKey(), loc++);
            }
        }

        return result;
    }

    /**
     * 获取指定位置的特征清单列表
     *
     * @param loc
     * @return
     */
    public String featureIndex(int loc) {
        return featureIndexMap()[loc];
    }

    /**
     * base function
     *
     * @param rawDataMap
     * @return
     */
    public double[] transformToArray(Map<String, Object> rawDataMap) {
        double[] resultArray = new double[vectorLength];

        int current = 0;

        if (preprocessFeatureExecutor != null) {
            rawDataMap = preprocessFeatureExecutor.transform(rawDataMap);
        }

        for (FeatureExecutor featureExecutor : featureExecutors) {
            try {
                Feature feature = featureExecutor.getFeature();
                Pair<Integer, Integer> indexPair = featureFieldIndex.get(feature.getName());
                double[] partVector = featureExecutor.toVector(rawDataMap);
                System.arraycopy(partVector,
                        0,
                        resultArray,
                        current,
                        indexPair.getRight() - indexPair.getLeft());
                current += indexPair.getRight() - indexPair.getLeft();
            } catch (Exception e) {
                log.error("featureExecutor error,{}", featureExecutor.getFeature(), e);
                throw e;
            }
        }

        return resultArray;
    }

    public float[] transformToFloatArray(Map<String, Object> rawDataMap) {
        double[] doubleArray = transformToArray(rawDataMap);

        int len = doubleArray.length;
        float[] resultArray = new float[len];

        for (int i = 0; i < len; i++) {
            resultArray[i] = (float) doubleArray[i];
        }
        return resultArray;
    }

    public static void main(String args[]) {
        double[] d = new double[10];
        double[] d2 = new double[10];

        System.arraycopy(d,
                0,
                d2,
                0,
                10);
    }

    public double[] normalizeTransform(Map<String, Object> normalizeMap) {
        double[] resultArray = new double[normalizeMap.size()];

        return resultArray;
    }

    public double[] quantileTransform(Map<String, Object> quantileMap) {
        double[] resultArray = new double[quantileMap.size()];

        return resultArray;
    }
}
