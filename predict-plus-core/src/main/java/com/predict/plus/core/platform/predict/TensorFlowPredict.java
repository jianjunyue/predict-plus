package com.predict.plus.core.platform.predict;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.predict.plus.algo.common.FeatureConfig;
import com.predict.plus.algo.model.TensorflowModel;
import com.predict.plus.common.utils.TimeMonitorUtils;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.core.platform.feature.model.FeatureVectorModel;
import com.predict.plus.facade.response.PredictScore;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import scala.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 10:11
 */
@Component
@Slf4j
public class TensorFlowPredict implements IPredict {

    private static ForkJoinPool forkJoinPool = new ForkJoinPool(16);

    @Override
    public List<PredictScore> modelPredict(PredictContext context,
                                            List<FeatureVectorModel> prdTotalFeatureList,
                                            BoosterAndFeatureConfigModel boosterAndFeatureConfigModel,
                                            Float missingValue) {
        String modelName = context.getModelName();
        int batchCount = context.getBatchCount();
        try {
            TensorflowModel tensorFlowModel = boosterAndFeatureConfigModel.getTensorFlowModel();
            FeatureConfig featureConfig = boosterAndFeatureConfigModel.getFeatureConfig();
            int colNum = featureConfig.getVectorLength();

            // 分桶预测
            TimeMonitorUtils.start();
            List<List<FeatureVectorModel>> bucketList = Lists.partition(prdTotalFeatureList, batchCount);
            Future<List<PredictScore>> future = forkJoinPool.submit(() -> {
                List<PredictScore> predictResultList = Collections.synchronizedList(new ArrayList<>());
                bucketList.parallelStream().forEach(subList -> {
                    List<PredictScore> subPredictList = predict(tensorFlowModel, modelName, colNum, prdTotalFeatureList);
                    predictResultList.addAll(subPredictList);
                });

                return predictResultList;
            });

            List<PredictScore> pidScoreModels = future.get();
            log.info("TensorFlowModelPredict-modelName:{}-bucketListSize:{}-rawSize:{}-colNum:{}-pidScoreModels:{}",
                    modelName, bucketList.size(), prdTotalFeatureList.size(), colNum, pidScoreModels.size());
            TimeMonitorUtils.finish("deepPredict", context.getCostTimeMap());

            return pidScoreModels;
        } catch (Exception e) {
            log.warn("TensorFlowModelPredict-modelPredict-modelName:{}-Exception", modelName, e);
        }

        log.warn("TensorFlowModelPredict-modelPredict-modelName:{}-pidScoreModels is null", modelName);
        return null;
    }

    /**
     *
     * @param modelName
     * @param colNum
     * @param prdTotalFeatureList
     * @return
     */
    private List<PredictScore> predict(TensorflowModel tensorFlowModel,
                                        String modelName,
                                        int colNum,
                                        List<FeatureVectorModel> prdTotalFeatureList) {
        List<PredictScore> predictScoreModelList = new ArrayList<>();
        try {
            if (CollectionUtils.isNotEmpty(prdTotalFeatureList)) {
                int rowNum = prdTotalFeatureList.size();
//                double[] modelTotalCols = new double[colNum * rowNum];
//                //目标数组的起始位置
//                int startIndex = 0;
//                for (int i = 0; i < prdTotalFeatureList.size(); i++) {
//                    FeatureVectorModel featureVector = prdTotalFeatureList.get(i);
//                    float[] feature = featureVector.getFeature();
//                    if (i > 0) {
//                        //i为0时，目标数组的起始位置为0 ,i为1时，目标数组的起始位置为第一个数组长度
//                        //i为2时，目标数组的起始位置为第一个数组长度+第二个数组长度
//                        startIndex = startIndex + feature.length;
//                    }
//                    System.arraycopy(feature, 0, modelTotalCols, startIndex, feature.length);
//                }
//                log.info("TensorFlowModelPredict-predict-modelName:{}-rowNum:{}, colNum:{}, modelCols:{}",
//                        modelName, rowNum, colNum, Arrays.toString(modelTotalCols));

                // 预测
//                double[] predicts = tensorFlowModel.predict(modelTotalCols, colNum);
                double[][] mat = new double[rowNum][colNum];
                for (int i = 0; i < rowNum; i++) {
                    FeatureVectorModel featureVector = prdTotalFeatureList.get(i);
                    float[] feature = featureVector.getFeature();
                    mat[i] = IntStream.range(0, feature.length).mapToDouble(f -> feature[f]).toArray();
                }
                double[] predicts = tensorFlowModel.output(mat);
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
            log.warn("TensorFlowModelPredict-predict-modelName:{}-Exception", modelName, e);
        }

        log.info("TensorFlowModelPredict-predict-modelName:{}-pidScoreModelList: {}", modelName, JSON.toJSONString(predictScoreModelList));
        return predictScoreModelList;
    }


    public static void main(String[] args) {
        float[] feature = {0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F};
        double[] featureDouble = IntStream.range(0, feature.length).mapToDouble(f -> feature[f]).toArray();
        System.out.println(Arrays.toString(featureDouble));

    }
}
