package com.predict.plus.core.platform.predict;

import com.google.common.collect.Lists;
import com.predict.plus.algo.common.FeatureConfig;
import com.predict.plus.common.utils.TimeMonitorUtils;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.core.platform.feature.model.FeatureVectorModel;
import com.predict.plus.facade.response.PredictScore; 
import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 10:07
 */
@Component
@Slf4j
public class XgboostPredict extends BaseModelPredict implements IPredict {

    private static ForkJoinPool forkJoinPool = new ForkJoinPool(16);

    /**
     *
     * @param prdTotalFeatureList
     * @param boosterAndFeatureConfigModel
     * @param missingValue
     * @return
     */
    public List<PredictScore> modelPredict(PredictContext context,
                                            List<FeatureVectorModel> prdTotalFeatureList,
                                            BoosterAndFeatureConfigModel boosterAndFeatureConfigModel,
                                            Float missingValue) {
        String modelName = context.getModelName();
        int batchCount = context.getBatchCount();
        try {
            Booster booster = boosterAndFeatureConfigModel.getBooster();
            FeatureConfig featureConfig = boosterAndFeatureConfigModel.getFeatureConfig();
            int colNum = featureConfig.getVectorLength();

            // 分桶预测
            TimeMonitorUtils.start();
            List<List<FeatureVectorModel>> bucketList = Lists.partition(prdTotalFeatureList, batchCount);
            Future<List<PredictScore>> future = forkJoinPool.submit(() -> {
                List<PredictScore> predictResultList = Collections.synchronizedList(new ArrayList<>());
                bucketList.parallelStream().forEach(subList -> {
                    List<PredictScore> subPredictList = super.predict(modelName, booster, missingValue,
                            colNum, subList);
                    predictResultList.addAll(subPredictList);
                });

                return predictResultList;
            });

            List<PredictScore> pidScoreModels = future.get();
            log.info("XgboostPredict-modelName:{}-bucketListSize:{}-rawSize:{}-colNum:{}-pidScoreModels:{}",
                    modelName, bucketList.size(), prdTotalFeatureList.size(), colNum, pidScoreModels.size());
            TimeMonitorUtils.finish("xgbPredict", context.getCostTimeMap());

            return pidScoreModels;
        } catch (Exception e) {
            log.warn("XgboostPredict-modelName:{}-Exception", modelName, e);
        }

        log.warn("XgboostPredict-modelPredict-modelName:{}-pidScoreModels is null", modelName);
        return null;
    }

}
