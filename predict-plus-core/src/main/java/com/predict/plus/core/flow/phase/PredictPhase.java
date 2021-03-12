package com.predict.plus.core.flow.phase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.predict.plus.common.utils.TimeMonitorUtils;
import com.predict.plus.core.cache.DataCacheManagerHelper;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
import com.predict.plus.core.model.PlatformModelConfig;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.core.platform.feature.model.FeatureVectorModel;
import com.predict.plus.core.platform.predict.IPredict;
import com.predict.plus.core.platform.predict.PredictModelStrategyBuilder;
import com.predict.plus.facade.request.ModelPredictRequest;
import com.predict.plus.facade.response.PredictScore;

import lombok.extern.slf4j.Slf4j;
/**
 * 6. 预测
 */  
@Service
@Slf4j
public class PredictPhase implements PlatformPhase {

    @Autowired
    private PredictModelStrategyBuilder predictModelStrategyBuilder;
 
    @Override
    public void execute(PredictContext context) {

		System.out.println("-----------PredictPhase----------------");
        // 判断上个任务执行状态
//        boolean preExecuteState = context.getExecuteStateMap().getOrDefault(rawFeature2ModelFeaturePhase, false);
//        if (! preExecuteState) {
//            log.warn("PredictPhase-上一阶段执行失败:rawFeature2ModelFeaturePhase");
//            return;
//        }

        try {
            TimeMonitorUtils.start();
            ModelPredictRequest request = context.getRequest();
            PlatformModelConfig config = DataCacheManagerHelper.platformModelConfigMap.getOrDefault(request.getModelName(), null);
            Float missingValue = Float.NaN;
            String modelType = null;
            if (null != config) {
                String missing  = config.getMissingValue();
                if (StringUtils.isNotEmpty(missing)) {
                    if (! missing.equalsIgnoreCase("NaN")) {
                        missingValue = 0.0F;
                    }
                }
                modelType = config.getModelType();
            }

            Map<String, float[]> totalFeatureMap = context.getPrdModelFeatureMap();
            List<FeatureVectorModel> prdTotalFeatureList = Lists.newArrayList();
            if (MapUtils.isNotEmpty(totalFeatureMap)) {
                prdTotalFeatureList = totalFeatureMap.entrySet()
                        .stream()
                        .map(entry -> new FeatureVectorModel(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            }
            BoosterAndFeatureConfigModel boosterConfigModel = context.getBoosterAndFeatureConfigModel();
            TimeMonitorUtils.finish("modelPredict0", context.getCostTimeMap());

            // 根据模型类型执行
            TimeMonitorUtils.start();
            if (null != modelType) {
                IPredict iPredict = predictModelStrategyBuilder.build(modelType);
                if (null != iPredict) {
                    List<PredictScore> predictScores = iPredict.modelPredict(context, prdTotalFeatureList, boosterConfigModel, missingValue);
                    context.setPredictScores(predictScores); 
                }
            }
            TimeMonitorUtils.finish("modelPredict1", context.getCostTimeMap());
//            context.getExecuteStateMap().put(predictPhase, true);
        } catch (Exception e) {
           log.warn("PredictPhase-execute-Exception", e);
        }

    }

}
