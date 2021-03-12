package com.predict.plus.core.flow.phase;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.predict.plus.algo.common.FeatureConfig;
import com.predict.plus.common.utils.ConfigResourceLoad;
import com.predict.plus.common.utils.LogUtils;
import com.predict.plus.common.utils.TimeMonitorUtils;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;

import lombok.extern.slf4j.Slf4j;
 
/**
 * 5. 原始特征转模型特征
 *
 * @Author: fc.w
 * @Date: 2020/11/06 16:16
 */
@Component
@Slf4j
public class RawFeature2ModelFeaturePhase implements PlatformPhase {

    private static ForkJoinPool forkJoinPool = new ForkJoinPool(16);

    @Override
    public void execute(PredictContext context) {
		System.out.println("-----------RawFeature2ModelFeaturePhase----------------");
  
        try {
            TimeMonitorUtils.start();
            Map<String, Map<String, Object>> prdRawFeatureMap1 = context.getPrdRawFeatureMap();
            Map<String, Map<String, Object>>   prdRawFeatureMap =   ConfigResourceLoad.readJsonFile(Map.class, new HashMap<String, Map<String, Object>>(), "prdRawFeatureMap.json");
            if (MapUtils.isNotEmpty(prdRawFeatureMap)) {
                BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = context.getBoosterAndFeatureConfigModel();
                FeatureConfig featureConfig = boosterAndFeatureConfigModel.getFeatureConfig();

                Set<String> pidSet = prdRawFeatureMap.keySet();
                List<String> pidList = new ArrayList<>(pidSet);
                int featureBatchCount = context.getFeatureBatchCount();
                List<List<String>> bucketPidList = Lists.partition(pidList, featureBatchCount);
                Future<Map<String, float[]>> future = forkJoinPool.submit(() -> {
                    long startTime = System.currentTimeMillis();
                    Map<String, float[]> subPrdFeatureMap = Maps.newConcurrentMap();
                    bucketPidList.parallelStream().forEach(subPidList -> {
                        for (String pid : subPidList) {
                            Map<String, Object> valueMap = prdRawFeatureMap.get(pid);
                            double[] prdFeatureDouble = featureConfig.transformToArray(valueMap);
                            float[] prdFeature = double2floatArray(prdFeatureDouble);
                            subPrdFeatureMap.put(pid, prdFeature);
                        }
                    });

                    Map<String, Long> costTimeMap = context.getCostTimeMap();
                    costTimeMap.put("inner_RawFeature2ModelFeaturePhase", System.currentTimeMillis() - startTime);
                    return subPrdFeatureMap;
                });
                Map<String, float[]> prdFeatureMap = future.get();

//                Set<String> pidSet = prdRawFeatureMap.keySet();
//                Future<Map<String, float[]>> future = forkJoinPool.submit(() -> {
//                    Map<String, float[]> subPrdFeatureMap = pidSet.parallelStream()
//                            .map(pid -> {
//                                Map<String, Object> valueMap = prdRawFeatureMap.get(pid);
//                                double[] prdFeatureDouble = featureConfig.transformToArray(valueMap);
//                                float[] prdFeature = ArrayUtils.double2floatArray(prdFeatureDouble);
//                                return new Pair<String, float[]>(pid, prdFeature);
//                            })
//                            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
//
//                    return subPrdFeatureMap;
//                });
//                Map<String, float[]> prdFeatureMap = future.get();
 
                context.setPrdModelFeatureMap(prdFeatureMap);
                LogUtils.logInfo(context.isLogSwitch(), "RawFeature2ModelFeaturePhase", "execute",
                        LogUtils.parse1("bucket:{}-featureBatchCount:{}-prdRawFeatureMap size:{}-prdFeatureMapSize:{}",
                                bucketPidList.size(), featureBatchCount, prdRawFeatureMap.size(), prdFeatureMap.size()));

                LogUtils.logDebug(context.isLogDebugSwitch(), "RawFeature2ModelFeaturePhase-prdFeatureMap:{}", JSON.toJSONString(prdFeatureMap));
            }
        } catch (Exception e) {
            log.warn("RawFeature2ModelFeaturePhase-Exception", e);
        } finally {
            TimeMonitorUtils.finish("RawFeature2ModelFeaturePhase", context.getCostTimeMap());
        }

    }
    
    public static float[] double2floatArray(double[] doubleArray) {
        int len = doubleArray.length;
        float[] floatArray = new float[len];
        for (int i = 0; i < len; i++) {
            double value = doubleArray[i];
            floatArray[i] = (float) value;
//            value = (int) (value * 1000000) / 1000000.0;
//            floatArray[i] = (float) value;
        }

        return floatArray;
    }
}