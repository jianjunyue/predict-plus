package com.predict.plus.core.platform.feature;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import com.google.common.collect.Maps;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.facade.request.ProductRequest;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/06 16:32
 */
@Slf4j
public abstract class AbsFeatureName2Id {

    /**
     * 特征ID转名称
     * @param context
     * @param prdRedisFeatureMap
     */
    public void featureId2Name(PredictContext context, Map<String, Map<String, Object>> prdRedisFeatureMap) {
        try {
            Map<String, Long> costTimeMap = context.getCostTimeMap();
            long s1 = System.currentTimeMillis();
            BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = context.getBoosterAndFeatureConfigModel();
            if (null != boosterAndFeatureConfigModel) {
                Map<String, String> featureId2NameMap = boosterAndFeatureConfigModel.getFeatureId2NameMap();

                Map<String, Map<String, Object>> prdRawFeatureMap = prdRedisFeatureMap.entrySet()
                        .stream()
                        .map(entry -> {
                            Map<String, Object> rawFeatureNameMap = featureName2IdTransfer(entry.getValue(), featureId2NameMap, context.getModelName());
                            return new Pair<String, Map<String, Object>>(entry.getKey(), rawFeatureNameMap);
                        })
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
                long s2 = System.currentTimeMillis();
                costTimeMap.put("featureId2Name_0", s2 - s1);

                // 在线API特征和离线特征合并
                Map<String, Map<String, Object>> finalPrdRawFeatureMap = context.getPrdRawFeatureMap();
                if (MapUtils.isNotEmpty(finalPrdRawFeatureMap) && MapUtils.isNotEmpty(prdRawFeatureMap)) {
                    Iterator<String> pidIterator = prdRawFeatureMap.keySet().iterator();
                    while (pidIterator.hasNext()) {
                        String pid = pidIterator.next();
                        Map<String, Object> featureColMap = prdRawFeatureMap.get(pid);
                        Map<String, Object> featureMap = finalPrdRawFeatureMap.getOrDefault(pid, new HashMap<>());
                        featureMap.putAll(featureColMap);
                        finalPrdRawFeatureMap.put(pid, featureMap);
                    }
                } else {
                    finalPrdRawFeatureMap = Maps.newHashMap();
                    finalPrdRawFeatureMap.putAll(prdRawFeatureMap);
                }
                context.setPrdRawFeatureMap(finalPrdRawFeatureMap);
                long s3 = System.currentTimeMillis();
                costTimeMap.put("featureId2Name_1", s3 - s2);

                // 处理没有查询到数据的prd
                List<ProductRequest> pidModelList = context.getRequest().getProductIds();
                if (CollectionUtils.isNotEmpty(pidModelList) && MapUtils.isNotEmpty(finalPrdRawFeatureMap) && pidModelList.size() != finalPrdRawFeatureMap.size()) {
                    Set<String> prdRawKeySet = finalPrdRawFeatureMap.keySet();
                    List<String> missPrdList = pidModelList.stream()
                            .map(ProductRequest::getProductId)
                            .filter(pid -> !prdRawKeySet.contains(pid)).collect(Collectors.toList());

//                    List<String> pids = pidModelList.stream().map(PidNeedScoreModel::getProductId).collect(Collectors.toList());
//                    LogUtils.logInfo(context.isLogSwitch(), "AbsFeatureName2Id", "featureId2Name", "prdRawKeySet:{}", JSON.toJSONString(prdRawKeySet));
//                    LogUtils.logInfo(context.isLogSwitch(), "AbsFeatureName2Id", "featureId2Name", "pids:{}", JSON.toJSONString(pids));

//                    LogUtils.logInfo(context.isLogSwitch(), "AbsFeatureName2Id", "featureId2Name",
//                            LogUtils.parse1("reqSize:{}-size:{}-missPrdList:{}",  pidModelList.size(), finalPrdRawFeatureMap.size(), JSON.toJSONString(missPrdList)));

                    context.setMissPrdList(missPrdList);
                }
                long s4 = System.currentTimeMillis();
                costTimeMap.put("featureId2Name_2", s4 - s3);
            } else {
                log.warn("AbsFeatureName2Id-featureId2Name-boosterAndFeatureConfigModel为空");
            }
        } catch (Exception e) {
            log.warn("AbsFeatureName2Id-featureId2Name-{}-Exception", context.getModelName(), e);
        }

    }



    /**
     * 特证名转ID
     * @param rawFeatureMap
     * @param featureId2NameMap
     */
    public Map<String, Object> featureName2IdTransfer(Map<String, Object> rawFeatureMap, Map<String, String> featureId2NameMap, String modelName) {
        Map<String, Object> rawFeatureNameMap = Maps.newHashMap();
        Map<String, Object> missFeatureIdMap = Maps.newHashMap();
        try {
            if (MapUtils.isNotEmpty(rawFeatureMap) && MapUtils.isNotEmpty(featureId2NameMap)) {
                for (Map.Entry<String, Object> entry : rawFeatureMap.entrySet()) {
                    String featureId = entry.getKey();
                    if (featureId2NameMap.containsKey(featureId)) {
                        String featureName = featureId2NameMap.get(featureId);
                        rawFeatureNameMap.put(featureName, entry.getValue());
                    } else {
                        missFeatureIdMap.put(featureId, entry.getValue());
                    }
                }
            }

            if (MapUtils.isNotEmpty(missFeatureIdMap)) {
                // 没有匹配上ID的特征名， 这部分特征可能是API传的，但不是原始特征，为了拼接组合KEY使用的，所以需要保留，
                rawFeatureNameMap.putAll(missFeatureIdMap);
//                log.info("AbsFeatureName2Id-modelName:{}-特征ID无法获取到特征名missFeatureIdMap:{}", modelName, JSON.toJSONString(missFeatureIdMap));
            }
        } catch (NumberFormatException e) {
            log.warn("AbsFeatureName2Id-NumberFormatException-NumberFormatException", e);
        }
        return rawFeatureNameMap;
    }




}