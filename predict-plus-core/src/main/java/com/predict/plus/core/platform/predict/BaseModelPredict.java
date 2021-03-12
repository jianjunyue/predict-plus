package com.predict.plus.core.platform.predict;

import com.alibaba.fastjson.JSON;
import com.predict.plus.core.platform.feature.model.FeatureVec;
import com.predict.plus.core.platform.feature.model.FeatureVectorModel;
import com.predict.plus.facade.response.PredictScore;

import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/07/23 16:19
 */
@Slf4j
public abstract class BaseModelPredict {

    /**
     * 模型加载
     * @param modelPath 模型路径
     * @throws Exception
     */
    public Booster loadModel(String modelPath) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource(modelPath);
        return XGBoost.loadModel(classPathResource.getInputStream());
    }

    /**
     * 预测接口
     * @param businessType 业务类型
     * @param booster 模型
     * @param missingValue 指定缺失值， null: Float.NaN、0: Float.valueOf(0.0);
     * @param colNum 单条向量长度
     * @param prdTotalFeatureList 模型特征
     * @return 预测结果
     */
    public List<PredictScore> predict(String businessType,
                                       Booster booster,
                                       Float missingValue,
                                       int colNum,
                                       List<FeatureVectorModel> prdTotalFeatureList) {
        List<PredictScore> predictScoreModelList = new ArrayList<>();
        try {
            if (CollectionUtils.isNotEmpty(prdTotalFeatureList)) {
                int rowNum = prdTotalFeatureList.size();
                float[] modelTotalCols = new float[colNum * rowNum];
                //目标数组的起始位置
                int startIndex = 0;
                for (int i = 0; i < prdTotalFeatureList.size(); i++) {
                    FeatureVectorModel featureVector = prdTotalFeatureList.get(i);
                    float[] feature = featureVector.getFeature();
                    if(i > 0) {
                        //i为0时，目标数组的起始位置为0 ,i为1时，目标数组的起始位置为第一个数组长度
                        //i为2时，目标数组的起始位置为第一个数组长度+第二个数组长度
                        startIndex = startIndex + feature.length;
                    }
                    System.arraycopy(feature, 0, modelTotalCols, startIndex, feature.length);
                }
//                log.info("BaseModelPredict-predict-{}-rowNum:{}, colNum:{}, modelCols:{}",
//                        businessType, rowNum, colNum, Arrays.toString(modelTotalCols));
                log.info("BaseModelPredict-predict-{}-rowNum:{}, colNum:{}",
                        businessType, rowNum, colNum);

                // 预测
                DMatrix dataMat = new DMatrix(modelTotalCols, rowNum, colNum, missingValue);
                float[][] predicts = booster.predict(dataMat, Boolean.FALSE, 0);
                for (int i = 0; i < predicts.length; i++) {
                	PredictScore predictScoreModel = new PredictScore();
                    Float xgbScore = predicts[i][0];
                    String pid = prdTotalFeatureList.get(i).getPid();
                    predictScoreModel.setPid(pid);
                    predictScoreModel.setScore(xgbScore);

                    predictScoreModelList.add(predictScoreModel);
                }
            }
        } catch (Exception e) {
            log.error("BaseModelPredict-predict-{}-Exception", businessType, e);
        }

        log.info("BaseModelPredict-predict-{}-pidScoreModelList: {}", businessType, JSON.toJSONString(predictScoreModelList));
        return predictScoreModelList;
    }

    /**
     * 预测：Map格式
     * @param businessType 业务类型
     * @param booster 模型
     * @param missingValue 缺省值
     * @param prdFeatureMap 特征
     * @return
     */
    public List<PredictScore> predict(String businessType,
                                       Booster booster,
                                       Float missingValue,
                                       Map<String, Map<Integer, Float>> prdFeatureMap) {
        List<PredictScore> predictScoreModelList = new ArrayList<>();
        try {
            List<FeatureVec> featureVectorList = featureIdxToVector(businessType, prdFeatureMap);
            if (CollectionUtils.isNotEmpty(featureVectorList)) {
                int rowNum = featureVectorList.size();
                int colNum = featureVectorList.get(0).getFeature().size();

                List<Float> totalVectorList = new ArrayList<>();
                for (FeatureVec featureVec : featureVectorList) {
                    totalVectorList.addAll(featureVec.getFeature());
                }

                float[] modelCols = new float[totalVectorList.size()];
                for (int i = 0; i < totalVectorList.size(); i++) {
                    modelCols[i] = totalVectorList.get(i);
                }
                log.info("BaseModelPredict-predict-{}-rowNum:{}, colNum:{}",
                        businessType, rowNum, colNum);

                // 预测
                DMatrix dataMat = new DMatrix(modelCols, rowNum, colNum, missingValue);
                float[][] predicts = booster.predict(dataMat, Boolean.FALSE, 0);
                for (int i = 0; i < predicts.length; i++) {
                	PredictScore predictScoreModel = new PredictScore();
                    Float xgbScore = predicts[i][0];
                    String pid = featureVectorList.get(i).getPid();
                    predictScoreModel.setPid(pid);
                    predictScoreModel.setScore(xgbScore);

                    predictScoreModelList.add(predictScoreModel);
                }
            }
        } catch (Exception e) {
            log.warn("BaseModelPredict-predict-{}-Exception", businessType, e);
        }

        log.info("BaseModelPredict-predict-{}-pidScoreModelList: {}", businessType, JSON.toJSONString(predictScoreModelList));
        return predictScoreModelList;
    }

    /**
     * 特征Map转Vector
     * @param businessType
     * @param prdFeatureMap
     * @return
     */
    private List<FeatureVec> featureIdxToVector(String businessType,
                                                Map<String, Map<Integer, Float>> prdFeatureMap) {
        List<FeatureVec> featureList = new ArrayList<>();
        try {
            prdFeatureMap.forEach((prd, vecSortMap) -> {
                List<Float> modelFeatureList = new ArrayList<>(vecSortMap.values());

                FeatureVec featureVec = new FeatureVec();
                featureVec.setPid(prd);
                featureVec.setFeature(modelFeatureList);

                featureList.add(featureVec);
            });
        } catch (Exception e) {
            log.warn("BaseModelPredict-featureIdxToVector-{}-Exception", businessType, e);
        }

        log.info("BaseModelPredict-featureIdxToVector-{}-featureVec: {}", businessType, JSON.toJSONString(featureList));
        return featureList;
    }

    /**
     * 特征名转ID
     * @param businessType
     * @param featureMap
     * @param feature2IdMap
     * @return
     */
    public Map<Integer, Float> featureNameToIdx(String businessType,
                                                Map<String, Float> featureMap,
                                                Map<String, Integer> feature2IdMap) {
        Map<Integer, Float> featureIdxMap = new HashMap<>();
        try {
            if (featureMap != null && featureMap.size() > 0) {
                for (Map.Entry<String, Float> entry : featureMap.entrySet()) {
                    if (feature2IdMap.containsKey(entry.getKey())) {
                        int idx = feature2IdMap.get(entry.getKey());
                        featureIdxMap.put(idx,entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("BaseModelPredict-featureNameToIdx-{}-Exception", businessType, e);
        }

        return featureIdxMap;
    }


}