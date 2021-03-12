package com.predict.plus.core.flow.phase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.predict.plus.common.constant.BusinessConstant;
import com.predict.plus.common.utils.LogUtils;
import com.predict.plus.common.utils.TimeMonitorUtils;
import com.predict.plus.core.cache.DataCacheManagerHelper;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
import com.predict.plus.core.model.ExecutorPriority;
import com.predict.plus.core.model.FeatureDependModel;
import com.predict.plus.core.model.PlatformModelConfig;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.core.platform.feature.AbsFeatureName2Id;
import com.predict.plus.core.platform.feature.model.CombineKeyDimension;
import com.predict.plus.facade.request.ModelPredictRequest;
import com.predict.plus.facade.request.ProductRequest;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

/**
 * 3. redis查询各维度原始特征数据
 *
 * @Author: fc.w
 * @Date: 2020/11/05 11:55
 */
@Service
@Slf4j
public class GetDimensionDataPhase extends AbsFeatureName2Id implements PlatformPhase {
//    @Autowired
//    private Cache<String, String> predictCache;
//
//    private static ForkJoinPool forkJoinPool = new ForkJoinPool(32);
//
//    @Autowired
//    private CacheBuilder cacheBuilder;
//
//    @Autowired
//    FeatureCacheHelper featureCacheHelper;

  

    @Override
    public void execute(PredictContext context) {
        TimeMonitorUtils.start();
        // 判断上个任务执行状态
//        boolean preExecuteState = context.getExecuteStateMap().getOrDefault(featurePlatformPhase, false);
//        if (! preExecuteState) {
//            log.warn("GetDimensionDataPhase-上一阶段执行失败:featurePlatformPhase");
//            return;
//        }

        try {
            // 获取特征版本号
            BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = context.getBoosterAndFeatureConfigModel();
            if (null != boosterAndFeatureConfigModel) {
                Integer featureVersion = boosterAndFeatureConfigModel.getVersion();
                if (null != featureVersion) {
                    context.setVersion(featureVersion);
                }
            }

           // 获取key分隔符
            String modelName = context.getModelName();
            PlatformModelConfig platformModelConfig = DataCacheManagerHelper.platformModelConfigMap.getOrDefault(modelName, null);
            String keySeparator = "";
            String keyPrefix = "";
            Map<String, String> dimensionIdentifierMap = null;
            Map<String, FeatureDependModel> featureDependMap = null;
            if (null != platformModelConfig) {
                dimensionIdentifierMap = platformModelConfig.getDimensionIdentifierMap();
                keySeparator = platformModelConfig.getKeySeparator();
                keyPrefix = platformModelConfig.getKeyPrefix();
                featureDependMap = platformModelConfig.getFeatureDependMap();
            }
            LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase",
                    LogUtils.parse1("keySeparator:{}-keyPrefix{}-dimensionIdentifierMap:{}", keySeparator, keyPrefix, JSON.toJSONString(dimensionIdentifierMap)));
            // 执行优先级，获取各维度的原始特征数据
            ExecutorPriority executorPriority = DataCacheManagerHelper.executorPriorityMap.get(modelName);
            LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase-executorPriority", JSON.toJSONString(executorPriority));
            if (null != executorPriority && MapUtils.isNotEmpty(dimensionIdentifierMap)) {
                // 执行优先级最高的维度
                List<String> highestPriorityList = executorPriority.getHighestPriorityList();
                if (CollectionUtils.isNotEmpty(highestPriorityList)) {
                    for (String dimension : highestPriorityList) {
                        switch (dimension) {
                            case BusinessConstant.USER:
                                getUserDimensionV2(context, keySeparator, keyPrefix, dimensionIdentifierMap.get(dimension));
                                break;
                            case BusinessConstant.PRODUCT:
                                getPrdDimension(context, keySeparator, dimensionIdentifierMap.get(dimension));
                                break;
                        }
                    }
                }

                // 该组合维度依赖其他维度，优先级中
                List<String> mediumPriorityList = executorPriority.getMediumPriorityList();
                if (CollectionUtils.isNotEmpty(mediumPriorityList)) {
                    for (String dimension : mediumPriorityList) {
                        FeatureDependModel featureDependModel = featureDependMap.get(dimension);
                        if (null != featureDependModel) {
                            // 组合维度特征组装
                            CombineKeyDimension combineKeyDimension = getFeatureDependModelKey(context, featureDependModel, keySeparator, dimensionIdentifierMap.get(dimension));
                            if (null != combineKeyDimension) {
                                // 查询组合维度redis
                                getOtherDimension(context, combineKeyDimension);
                            }
                        }
                    }
                }

                // TODO 暂时不支持：待开发 该组合维度依赖其他组合维度，优先级低
//                List<String> lowPriorityList = executorPriority.getLowPriorityList();
//                if (CollectionUtils.notEmpty(lowPriorityList)) {
//                    for (String dimension : lowPriorityList) {
//
//                    }
//                }

//                context.getExecuteStateMap().put(getDimensionDataPhase, true);
            }
        } catch (Exception e) {
            log.error("GetDimensionDataPhase-Exception", e);
        } finally {
            TimeMonitorUtils.finish("GetDimensionData", context.getCostTimeMap());
        }
    }

    /**
     * 用户维度原始特征查询
     * @param context
     * @param keySeparator
     */
    private void getUserDimension(PredictContext context, String keySeparator, String dimensionIdentifier) {
//        TimeMonitorUtils.start();
//        try {
//            // 特征版本号+分隔符+维度关键字+分隔符+uid
//            String uidKey = new StringBuilder()
//                    .append(context.getVersion())
//                    .append(keySeparator)
//                    .append(dimensionIdentifier)
//                    .append(keySeparator)
//                    .append(context.getUserId())
//                    .toString();
//            String value = predictCache.get(uidKey);
//            LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule", "getUserDimension",
//                    LogUtils.parse1("key:{}-value:{}", uidKey, value));
//
//            if (StringUtils.isNotEmpty(value)) {
//                Map<String, Object> uidFeatureMap = JSON.parseObject(value, new TypeReference<Map<String, Object>>(){});
//                // 特征ID转名称
//                BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = context.getBoosterAndFeatureConfigModel();
//                if (null != boosterAndFeatureConfigModel) {
//                    Map<String, String> featureId2NameMap = boosterAndFeatureConfigModel.getFeatureId2NameMap();
//                    Map<String, Object> rawFeatureNameMap = featureName2IdTransfer(uidFeatureMap, featureId2NameMap, context.getModelName());
//                    LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule", "getUserDimension-rawFeatureNameMap",
//                            JSON.toJSONString(rawFeatureNameMap));
//
//                    Map<String, Object> uidRawFeatureMap = context.getUidRawFeatureMap();
//                    uidRawFeatureMap.putAll(rawFeatureNameMap);
//                }
//            }
//        } catch (Exception e) {
//            log.warn("GetDimensionDataPhase-getUserDimension-Exception", e);
//        } finally {
//            TimeMonitorUtils.finish("getUserDimension", context.getCostTimeMap());
//        }
    }


    /**
     * 用户维度原始特征查询
     *
     * @param context
     * @param keySeparator
     */
    private void getUserDimensionV2(PredictContext context, String keySeparator, String keyPrefix, String dimensionIdentifier) {
//        TimeMonitorUtils.start();
//        try {
//            String uidKey = "";
//            // 特征版本号+分隔符+维度关键字+分隔符+uid
//            if (StringUtils.isEmpty(keyPrefix)) {
//                uidKey = new StringBuilder()
//                        .append(context.getVersion())
//                        .append(keySeparator)
//                        .append(dimensionIdentifier)
//                        .append(keySeparator)
//                        .append(context.getUserId())
//                        .toString();
//            } else {
//                // 前缀+分隔符+维度关键字+分隔符+uid
//                uidKey = new StringBuilder()
//                        .append(keyPrefix)
//                        .append(keySeparator)
//                        .append(dimensionIdentifier)
//                        .append(keySeparator)
//                        .append(context.getUserId())
//                        .toString();
//            }
//            String value = predictCache.get(uidKey);
//            LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule", "getUserDimension",
//                    LogUtils.parse1("key:{}-value:{}", uidKey, value));
//
//            if (StringUtils.isNotEmpty(value)) {
//                Map<String, Object> uidFeatureMap = JSON.parseObject(value, new TypeReference<Map<String, Object>>() {
//                });
//                // 特征ID转名称
//                BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = context.getBoosterAndFeatureConfigModel();
//                if (null != boosterAndFeatureConfigModel) {
//                    Map<String, String> featureId2NameMap = boosterAndFeatureConfigModel.getFeatureId2NameMap();
//                    Map<String, Object> rawFeatureNameMap = featureName2IdTransfer(uidFeatureMap, featureId2NameMap, context.getModelName());
//                    LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule", "getUserDimension-rawFeatureNameMap",
//                            JSON.toJSONString(rawFeatureNameMap));
//
//                    Map<String, Object> uidRawFeatureMap = context.getUidRawFeatureMap();
//                    uidRawFeatureMap.putAll(rawFeatureNameMap);
//                }
//            }
//        } catch (Exception e) {
//            log.warn("GetDimensionDataPhase-getUserDimension-Exception", e);
//        } finally {
//            TimeMonitorUtils.finish("getUserDimension", context.getCostTimeMap());
//        }
    }


    /**
     * 获取prd维度特征
     *
     * @param context
     * @param keySeparator
     */
    private void getPrdDimension(PredictContext context, String keySeparator, String dimensionIdentifier) {
//        TimeMonitorUtils.start();
//        try {
//            // redis key组装： 特征版本号+分隔符+维度关键字+分隔符+pid
//            ModelPredictRequest request = context.getRequest();
//            List<ProductRequest> pidModelList = request.getProductIds();
//            Map<String, String> prdKeyAndPidMap = Maps.newHashMap();
//            List<String> repeatPidList = Lists.newArrayList();
//            if (CollectionUtils.isNotEmpty(pidModelList)) {
//                String commonkey = new StringBuilder()
//                        .append(context.getVersion())
//                        .append(keySeparator)
//                        .append(dimensionIdentifier)
//                        .append(keySeparator)
//                        .toString();
//                for (ProductRequest pidModel : pidModelList) {
//                    String key = new StringBuilder()
//                            .append(commonkey)
//                            .append(pidModel.getProductId().toLowerCase())
//                            .toString();
//                    if (! prdKeyAndPidMap.containsKey(key)) {
//                        prdKeyAndPidMap.put(key, pidModel.getProductId());
//                    } else {
//                        repeatPidList.add(pidModel.getProductId());
//                    }
//
//                }
//            }
//            LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule", "getPrdDimension",
//                    LogUtils.parse1("repeatPidList:{}-prdKeyAndPidMap:{}", JSON.toJSONString(repeatPidList), JSON.toJSONString(prdKeyAndPidMap)));
//
//            // 获取prd维度特征：判断模型是否为缓存的场景
//            String modelName = context.getModelName();
//            if (cacheBuilder.isCache(modelName)) {
//                LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule-getPrdDimension-cache-modelName", modelName);
//                ICache cache = cacheBuilder.build(modelName);
//                cache.getPrdFeature(context, prdKeyAndPidMap);
//            } else {
//                baseProductDimension(context, prdKeyAndPidMap);
//            }
//        } catch (Exception e) {
//            log.warn("GetDimensionDataPhase-getPrdDimension-Exception", e);
//        } finally {
//            TimeMonitorUtils.finish("getPrdDimension", context.getCostTimeMap());
//        }
    }

    /**
     * 获取prd维度特征：不需要走缓存的场景
     * @param prdKeyAndPidMap
     * @param prdKeyAndPidMap
     * @return
     */
    private void baseProductDimension(PredictContext context,
                                      Map<String, String> prdKeyAndPidMap) {
//        try {
//            Set<String> keySet = prdKeyAndPidMap.keySet();
//            TimeMonitorUtils.start();
//            Map<String, String> redisValueMap = predictCache.multiGet(keySet);
//            TimeMonitorUtils.finish("baseProductDimension_mget", context.getCostTimeMap());
//            LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase", "baseProductDimension",
//                    "redisValueMap", JSON.toJSONString(redisValueMap));
//            if (MapUtils.isEmpty(redisValueMap)) {
//                return;
//            }
//
//            TimeMonitorUtils.start();
//            // redis数据转换为原始特征
//            Future<Map<String, Map<String, Object>>> future = forkJoinPool.submit(() -> {
//                Map<String, Map<String, Object>> prdRedisFeatureMap = redisValueMap.entrySet()
//                        .parallelStream()
//                        .map(entry -> {
//                            String pid = prdKeyAndPidMap.getOrDefault(entry.getKey(), "");
//                            Map<String, Object> prdRawMap = JSON.parseObject(entry.getValue(), Map.class);
//                            return new Pair<String, Map<String, Object>>(pid, prdRawMap);
//                        })
//                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
//
//                return prdRedisFeatureMap;
//            });
//            Map<String, Map<String, Object>> prdRedisFeatureMap = future.get();
//            LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase", "baseProductDimension",
//                    "prdRedisFeatureMap", JSON.toJSONString(prdRedisFeatureMap));
//            TimeMonitorUtils.finish("baseProductDimension", context.getCostTimeMap());
//
//            // 特征ID转名称
//            featureId2Name(context, prdRedisFeatureMap);
//        } catch (Exception e) {
//            log.warn("MlpGetDimensionFeatureModule-baseProductDimension-Exception", e);
//        }

    }

    /**
     * 其他维度的特征
     *
     * @param context
     * @param combineKeyDimension
     */
    private void getOtherDimension(PredictContext context, CombineKeyDimension combineKeyDimension) {
//        TimeMonitorUtils.start();
//        try {
//            String dimension = combineKeyDimension.getDimension();
//            if (StringUtils.isNotEmpty(dimension) && dimension.equals(BusinessConstant.USER)) {
//                String uidCombineKey = combineKeyDimension.getUidCombineKey();
//                String value = predictCache.get(uidCombineKey);
//                LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule", "getOtherDimension",
//                        LogUtils.parse1("uidCombineKey:{}-value{}", uidCombineKey, value));
//
//                Map<String, Object> userValueMap = JSON.parseObject(value, new TypeReference<Map<String, Object>>(){});
//                // 特征ID转名称
//                BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = context.getBoosterAndFeatureConfigModel();
//                if (null != boosterAndFeatureConfigModel) {
//                    Map<String, String> featureId2NameMap = boosterAndFeatureConfigModel.getFeatureId2NameMap();
//                    Map<String, Object> userRawFeatureNameMap = featureName2IdTransfer(userValueMap, featureId2NameMap, context.getModelName());
//                    LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule",
//                            "getOtherDimension-userRawFeatureNameMap", JSON.toJSONString(userRawFeatureNameMap));
//
//                    Map<String, Object> uidRawFeatureMap = context.getUidRawFeatureMap();
//                    uidRawFeatureMap.putAll(userRawFeatureNameMap);
//                }
//            } else if (StringUtils.isNotEmpty(dimension) && dimension.equals(BusinessConstant.PRODUCT)) {
//                Map<String, String> combineAndPidKeyMap = combineKeyDimension.getCombineAndPidKeyMap();
//                // key组装：特征版本号+分隔符+维度关键字+分隔符+uid
//                Set<String> combineKeySet = new HashSet<>(combineAndPidKeyMap.values());
//                LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule", "getOtherDimension",
//                        "combineRedisKeySet", JSON.toJSONString(combineKeySet));
//
//                // 查询数据
////                Map<String, String> redisValueMap = predictCache.multiGet(combineKeySet);
//                Map<String, String> redisValueMap =  featureCacheHelper.getCombineFeature(context, combineAndPidKeyMap);
//                LogUtils.logInfo(context.isLogSwitch(), "MlpGetDimensionFeatureModule", "getOtherDimension",
//                        "redisValueMap", JSON.toJSONString(redisValueMap));
//                if (MapUtils.isNotEmpty(redisValueMap)) {
//                    BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = context.getBoosterAndFeatureConfigModel();
//                    if (null != boosterAndFeatureConfigModel) {
//                        Map<String, String> featureId2NameMap = boosterAndFeatureConfigModel.getFeatureId2NameMap();
//
//                        Map<String, Map<String, Object>> prdRawFeatureMap = context.getPrdRawFeatureMap();
//                        if (MapUtils.isNotEmpty(prdRawFeatureMap)) {
//                            prdRawFeatureMap.forEach((pid, map) -> {
//                                String combineKey = combineAndPidKeyMap.get(pid);
//                                if (StringUtils.isNotEmpty(combineKey)) {
//                                    String value = redisValueMap.get(combineKey);
//                                    Map<String, Object> prdRawMap = JSON.parseObject(value, Map.class);
//                                    // 特征ID转名称
//                                    Map<String, Object> rawFeatureNameMap = featureName2IdTransfer(prdRawMap, featureId2NameMap, context.getModelName());
//                                    if (map != null) {
//                                        map.putAll(rawFeatureNameMap);
//                                    }
//                                    LogUtils.logDebug(context.isLogDebugSwitch(), "GetDimensionDataPhase",
//                                            combineKey + ":" + pid, JSON.toJSONString(rawFeatureNameMap));
//                                }
//                            });
//                        }
//                        LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase", "getOtherDimension",
//                                "prdRawFeatureMap", JSON.toJSONString(prdRawFeatureMap));
//                    }
//                }
//            }
//        } catch ( Exception e) {
//            log.warn("GetDimensionDataPhase-getOtherDimension-CacheParamException", e);
//        } finally {
//            TimeMonitorUtils.finish("getOtherDimension", context.getCostTimeMap());
//        }
    }

    /**
     * 获取依赖维度的数据
     * @param context
     * @param featureDependModel
     * @param keySeparator
     */
    private CombineKeyDimension getFeatureDependModelKey(PredictContext context,
                                                         FeatureDependModel featureDependModel,
                                                         String keySeparator,
                                                         String dimensionIdentifier) {
        Map<String, List<String>> dependDimensionMap = featureDependModel.getDependDimensionMap();
        if (MapUtils.isEmpty(dependDimensionMap)) {
            LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase-getFeatureDependModelKey-dependDimensionMap", "NULL");
            return null;
        }
        Set<String> dependDimensionSet = dependDimensionMap.keySet();
        if (CollectionUtils.isEmpty(dependDimensionSet)) {
            LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase-getFeatureDependModelKey-dependDimensionSet", "NULL");
            return null;
        }

        CombineKeyDimension combineKeyDimension = new CombineKeyDimension();
        TimeMonitorUtils.start();
        try {
            // 获取用户维度的依赖特征
            Map<String, Object> uidKeyFeatureMap = Maps.newHashMap();
            if (dependDimensionSet.contains(BusinessConstant.USER)) {
                Map<String, Object> uidRawFeatureMap = context.getUidRawFeatureMap();
                List<String> featureList = dependDimensionMap.get(BusinessConstant.USER);
                for (String feature : featureList) {
                    Object featureValue = uidRawFeatureMap.getOrDefault(feature, "");
                    uidKeyFeatureMap.put(feature, featureValue);
                }
            }
            LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase-getFeatureDependModelKey-uidKeyFeatureMap", JSON.toJSONString(uidKeyFeatureMap));

            List<String> keySequences = featureDependModel.getKeySequences();
            LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase-getFeatureDependModelKey-keySequences", JSON.toJSONString(keySequences));

            // 获取prd维度的依赖特征, 并进行key特征组合
            if (dependDimensionSet.contains(BusinessConstant.PRODUCT)) {
                Map<String, Map<String, Object>> prdRawFeatureMap = context.getPrdRawFeatureMap();
                Map<String, String> combineAndPidKeyMap = Maps.newHashMap();
                if (MapUtils.isNotEmpty(prdRawFeatureMap)) {
                    for (Map.Entry<String, Map<String, Object>> prdEntry : prdRawFeatureMap.entrySet()) {
                        Map<String, Object> rawFeatureMap = prdEntry.getValue();
                        if (MapUtils.isNotEmpty(rawFeatureMap) && CollectionUtils.isNotEmpty(keySequences)) {
                            // 遍历key组装序列
                            StringBuilder sbKey = new StringBuilder();
                            for (String featureName : keySequences) {
                                if (rawFeatureMap.containsKey(featureName)) {
                                    // 获取pid依赖特征
                                    Object prdFeatureValue = rawFeatureMap.get(featureName);
                                    sbKey.append(prdFeatureValue).append(keySeparator);
                                } else if (MapUtils.isNotEmpty(uidKeyFeatureMap) && uidKeyFeatureMap.containsKey(featureName)) {
                                    // 获取uid维度依赖特征
                                    Object uidFeatureValue = uidKeyFeatureMap.get(featureName);
                                    sbKey.append(uidFeatureValue).append(keySeparator);
                                }
                            }

                            // 去除最后一个分隔符， 因为分隔符长度正常情况下为1，为了防止分隔符长度大于1所以做了分隔符计算。
                            String key = sbKey.substring(0, (sbKey.length() - keySeparator.length())).toLowerCase();
                            String redisKey = context.getVersion() + keySeparator + dimensionIdentifier + keySeparator + key;
                            combineAndPidKeyMap.put(prdEntry.getKey(),redisKey);
                        }
                    }
                }

                combineKeyDimension.setCombineAndPidKeyMap(combineAndPidKeyMap);
                combineKeyDimension.setDimension(BusinessConstant.PRODUCT);
                LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase-getFeatureDependModelKey-prd-pidAndCombineKeyMap", JSON.toJSONString(combineAndPidKeyMap));
            } else if (MapUtils.isNotEmpty(uidKeyFeatureMap)) {
                // uid维度key特征组合
                StringBuilder sbKey = new StringBuilder();
                for (String featureName : keySequences) {
                    if (uidKeyFeatureMap.containsKey(featureName)) {
                        Object uidFeatureValue = uidKeyFeatureMap.get(featureName);
                        sbKey.append(uidFeatureValue).append(keySeparator);
                    }
                }

                // 去除最后一个分隔符， 因为分隔符长度正常情况下为1，为了防止分隔符长度大于1所以做了分隔符计算。
                String uidCombineKey = sbKey.substring(0, (sbKey.length() - keySeparator.length())).toLowerCase();
                String redisKey = context.getVersion() + keySeparator + dimensionIdentifier + keySeparator + uidCombineKey;
                combineKeyDimension.setUidCombineKey(redisKey);
                combineKeyDimension.setDimension(BusinessConstant.USER);
                LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase-getFeatureDependModelKey-uid-combineKeySet", redisKey);
            }
        } catch (Exception e) {
            log.warn("GetDimensionDataPhase-getFeatureDependModelKey-Exception", e);
        } finally {
            TimeMonitorUtils.finish("getFeatureDependModelKey", context.getCostTimeMap());
        }

        LogUtils.logInfo(context.isLogSwitch(), "GetDimensionDataPhase-getFeatureDependModelKey-combineKeyDimension", JSON.toJSONString(combineKeyDimension));
        return combineKeyDimension;
    }


    public static void main(String[] args) {
        Map<String,Map<String,String>> pidMap = new HashMap<>();
        pidMap.put("1",null);
        pidMap.forEach((pid,map) ->{
            if(map==null){
                map=new HashMap<>();
            }
            map.put("1","1");
        });
        System.out.println(pidMap);
    }
}
