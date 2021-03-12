package com.predict.plus.core.platform.predict;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.predict.plus.algo.common.FeatureConfig;
import com.predict.plus.algo.model.LightGBMModel;
import com.predict.plus.common.utils.TimeMonitorUtils;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.core.platform.feature.model.FeatureVectorModel;
import com.predict.plus.facade.response.PredictScore;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 10:10
 */
@Component
@Slf4j
public class LightGBMPredict implements IPredict {

    private static ForkJoinPool forkJoinPool = new ForkJoinPool(16);

    @Override
    public List<PredictScore> modelPredict(PredictContext context,
                                            List<FeatureVectorModel> prdTotalFeatureList,
                                            BoosterAndFeatureConfigModel boosterAndFeatureConfigModel,
                                            Float missingValue) {
        String modelName = context.getModelName();
        int batchCount = context.getBatchCount();
        try {
            LightGBMModel lightGBMModel = boosterAndFeatureConfigModel.getLightGBMModel();
            FeatureConfig featureConfig = boosterAndFeatureConfigModel.getFeatureConfig();
            int colNum = featureConfig.getVectorLength();

            // 分桶预测
            TimeMonitorUtils.start();
            List<List<FeatureVectorModel>> bucketList = Lists.partition(prdTotalFeatureList, batchCount);
            Future<List<PredictScore>> future = forkJoinPool.submit(() -> {
                List<PredictScore> predictResultList = Collections.synchronizedList(new ArrayList<>());
                bucketList.parallelStream().forEach(subList -> {
                    List<PredictScore> subPredictList = predict(lightGBMModel, modelName, colNum, subList);
                    predictResultList.addAll(subPredictList);
                });

                return predictResultList;
            });
            List<PredictScore> pidScoreModels = future.get();
            TimeMonitorUtils.finish("lgbPredict", context.getCostTimeMap());
            log.info("LightGBMPredict-modelName:{}-bucketListSize:{}-rawSize:{}-colNum:{}-pidScoreModels:{}",
                    modelName, bucketList.size(), prdTotalFeatureList.size(), colNum, pidScoreModels.size());


            return pidScoreModels;
        } catch (Exception e) {
            log.warn("LightGBMPredict-modelPredict-modelName:{}-Exception", modelName, e);
        }

        log.warn("LightGBMPredict-modelPredict-modelName:{}-pidScoreModels is null", modelName);
        return null;
    }

    /**
     * 预测
     * @param lightGBMModel
     * @param modelName
     * @param colNum
     * @param prdTotalFeatureList
     * @return
     */
    private List<PredictScore> predict(LightGBMModel lightGBMModel,
                                        String modelName,
                                        int colNum,
                                        List<FeatureVectorModel> prdTotalFeatureList) {
        List<PredictScore> predictScoreModelList = new ArrayList<>();
        try {
            if (CollectionUtils.isNotEmpty(prdTotalFeatureList)) {
                int rowNum = prdTotalFeatureList.size();
                double[] modelTotalCols = new double[colNum * rowNum];
                //目标数组的起始位置
                int startIndex = 0;
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < prdTotalFeatureList.size(); i++) {
                    FeatureVectorModel featureVector = prdTotalFeatureList.get(i);
                    float[] feature = featureVector.getFeature();
                    for (float ele : feature) {
                        modelTotalCols[startIndex++] = ele;
                    }
                }
                long endTime = System.currentTimeMillis();
                log.info("LightGBMPredict-predict-modelName:{}-rowNum:{}, colNum:{}, costTime:{}, modelCols:{}",
                        modelName, rowNum, colNum, (endTime - startTime), Arrays.toString(modelTotalCols));

                // 预测
                double[] predicts = lightGBMModel.predict(modelTotalCols, colNum);
                for (int i = 0; i < predicts.length; i++) {
                    double xgbScore = predicts[i];
                    String pid = prdTotalFeatureList.get(i).getPid();
                    PredictScore predictScoreModel = new PredictScore();
                    predictScoreModel.setPid(pid);
                    predictScoreModel.setScore((float) xgbScore);

                    predictScoreModelList.add(predictScoreModel);
                }
            }
        } catch (Exception e) {
            log.warn("LightGBMPredict-predict-modelName:{}-Exception", modelName, e);
        }


        log.info("LightGBMPredict-predict-modelName:{}-pidScoreModelList: {}", modelName, JSON.toJSONString(predictScoreModelList));
        return predictScoreModelList;
    }

    public static void main(String[] args) {
//        String a = "[{\"pid\": \"11291794\",\"feature\":[null,null,null,null,null,null,null,null,null,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,5.0,63.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,2.0,37.0,0.0,700.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,null,null,null,null,null,null,null,null,null,null,null,null,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,2.9258275,0.0,1.462398,1.763428,1.1264561,1.2024883,null,null,null,null,null,null,null,null,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,null,null,null,null,null,null,null,null,null,null,null,0.124061525,1.1179905,0.25295225,0.09557663,-1.6285821,2.9762397,2.0294654,-1.207125,-3.8891478,4.602048,0.11225486,-0.06643553,3.2566957,1.295326,1.339092,1.8818884,-0.19855756,1.3424854,3.3687987,-0.2859363,3.5259326,1.874264,1.142615,-1.3613124,-0.38842204,-4.4818625,-1.6776114,1.4036107,-1.0981262,-0.32049483,1.5534751,0.7335502,2.5232868,-0.029875875,-2.227871,0.77716744,-4.211196,1.6626039,-1.209401,-1.457327,1.4972739,1.0703881,0.31659824,0.057185307,-1.090136,0.48771903,0.6589015,-4.5335526,3.4499588,-1.5425459,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,0.0,1.0,1.0,1.0,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null]},{\"pid\": \"11291761\",\"feature\":[null,null,null,null,null,null,null,null,null,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,406.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,3.0,406.0,7.0,556.0,1.0,0.0,1.0,2.0,1.0,0.0,0.0,0.0,null,null,null,null,null,null,null,null,null,null,null,null,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,2.9258275,0.0,1.462398,1.763428,1.1264561,1.2024883,null,null,null,null,null,null,null,null,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,null,null,null,null,null,null,null,null,null,null,null,-0.43896493,2.1958442,1.9657365,-4.5578303,-6.098678,1.8553662,1.8227156,2.6959677,-0.4869259,-9.069191,-6.316761,-1.4103757,8.570725,-0.5030512,7.476685,-1.2915409,-8.167981,-0.5797753,2.0684977,8.068643,0.66998184,5.935228,8.685612,0.13697678,-2.0664294,6.7407093,-3.5598238,-10.958569,0.79455554,6.441706,3.765365,-1.7482972,3.265177,-3.9938717,-4.9911737,3.764984,-5.295676,-1.6580757,-1.4148692,-4.722754,-3.281823,0.38124767,-8.136314,7.705714,-2.4664557,1.5532416,-2.5077856,0.98022145,6.442338,-5.990629,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,0.0,1.0,0.0,0.0,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null]}]";
//        List<FeatureVectorModel> prdTotalFeatureList =  JSON.parseObject(a, new TypeReference<List<FeatureVectorModel>>(){});
//
//        int rowNum = prdTotalFeatureList.size();
//        double[] modelTotalCols = new double[colNum * rowNum];
//        //目标数组的起始位置
//        int startIndex = 0;
//        for (int i = 0; i < prdTotalFeatureList.size(); i++) {
//            FeatureVectorModel featureVector = prdTotalFeatureList.get(i);
//            float[] feature = featureVector.getFeature();
////            if (i > 0) {
////                //i为0时，目标数组的起始位置为0 ,i为1时，目标数组的起始位置为第一个数组长度
////                //i为2时，目标数组的起始位置为第一个数组长度+第二个数组长度
////                startIndex = startIndex + feature.length;
////            }
//            for (float ele : feature) {
//                modelTotalCols[startIndex++] = ele;
//            }
//
////            System.arraycopy(feature, 0, modelTotalCols, startIndex, feature.length);
//        }
//
//        System.out.println(Arrays.toString(modelTotalCols));
    }


}
