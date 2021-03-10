package com.predict.plus.core.flow.phase;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.predict.plus.common.utils.LogUtils;
import com.predict.plus.common.utils.TimeMonitorUtils;
import com.predict.plus.core.cache.DataCacheManagerHelper;
import com.predict.plus.core.common.DataReadyStatusCacheStrategy;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
import com.predict.plus.core.model.PlatformModelConfig;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel; 

import lombok.extern.slf4j.Slf4j; 
/**
 * 检查modelName各维度特征同步状态 
 */
@Service
@Slf4j
public class FeatureReadyStatusPhase implements PlatformPhase {
	
	@Autowired
    private DataReadyStatusCacheStrategy dataReadyStatusCacheStrategy;
    
	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------FeatureReadyStatusPhase----------------");
		String modelName= context.getRequest().getModelName();
		 LinkedHashMap<Integer, BoosterAndFeatureConfigModel> versionBoosterConfigMap=DataCacheManagerHelper.boosterAndFeatureConfigModelMap.get(modelName);
	        LogUtils.logInfo(context.isLogSwitch(), "FeatureReadyStatusPhase", LogUtils.parse1("modelName:{}-versionBoosterConfigMap:{}", modelName, JSON.toJSONString(versionBoosterConfigMap)));
	        if (MapUtils.isEmpty(versionBoosterConfigMap)) {
	            log.error("FeatureReadyStatusPhase-服务中未加载modelName={}对应的模型文件", modelName);
	            return;
	        }
	        
	        try {
	            PlatformModelConfig platformModelConfig = DataCacheManagerHelper.platformModelConfigMap.getOrDefault(modelName, null);
	            if (null == platformModelConfig) {
	                log.error("FeatureReadyStatusPhase-Apollo配置platformModelConfig IS NULL");
	                return;
	            }
	            TimeMonitorUtils.finish("FeatureReadyStatusPhase_0", context.getCostTimeMap());

	            TimeMonitorUtils.start();
	            List<String> dimensionIdentifierList = platformModelConfig.getDimensionIdentifierList();
	            String keySeparator = platformModelConfig.getKeySeparator();
	            String keyPrefix = platformModelConfig.getKeyPrefix();
	            LogUtils.logInfo(context.isLogSwitch(), "FeatureReadyStatusPhase-dimensionIdentifierList", JSON.toJSONString(dimensionIdentifierList));
	            if (CollectionUtils.isNotEmpty(dimensionIdentifierList)) {
	                /* Map根据version倒叙， 然后依次通过version查询redis数据同步状态 */
	                for (Map.Entry<Integer, BoosterAndFeatureConfigModel> entry : versionBoosterConfigMap.entrySet()) {
	                    Integer version = entry.getKey();
	                    /* 1. redis key组装 */
	                    Set<String> dsKeySet = new HashSet<>();
	                    for (String dimension : dimensionIdentifierList) {
//	                        String key = "ds"+ keySeparator + version + keySeparator + dimension;
	                        String key;
	                        if ("uid".equals(dimension) && StringUtils.isNotEmpty(keyPrefix)) {
	                            key = new StringBuilder()
	                                    .append("ds")
	                                    .append(keySeparator)
	                                    .append(keyPrefix)
	                                    .append(keySeparator)
	                                    .append(dimension)
	                                    .toString();
	                        } else {
	                            key = new StringBuilder()
	                                    .append("ds")
	                                    .append(keySeparator)
	                                    .append(version)
	                                    .append(keySeparator)
	                                    .append(dimension)
	                                    .toString();
	                        }
	                        dsKeySet.add(key);
	                    }
	                    LogUtils.logInfo(context.isLogSwitch(), "FeatureReadyStatusPhase-dsKeySet", JSON.toJSONString(dsKeySet));

	                    /*  2.查询redis数据同步状态 */
	                    long s1 = System.currentTimeMillis();
	                    Map<String, String> dsStatusMap = dataReadyStatusCacheStrategy.multiGet(dsKeySet);
	                    long s2 = System.currentTimeMillis();
	                    Map<String, Long> costTimeMap = context.getCostTimeMap();
	                    costTimeMap.put("dsStatusMap", s2 - s1);
	                    LogUtils.logInfo(context.isLogSwitch(), "FeatureReadyStatusPhase", LogUtils.parse1("模型名：{}-dsKeySet:{}-dsStatusMap:{}",
	                            modelName, JSON.toJSONString(dsKeySet), JSON.toJSONString(dsStatusMap)));

	                    /* 3. version数据同步完成，则返回该特征平台信息 */
	                    if (MapUtils.isNotEmpty(dsStatusMap)) {
	                        boolean isSuccess = false;
	                        for (String dimensionKey : dsKeySet) {
	                            if (dsStatusMap.containsKey(dimensionKey)) {
	                                isSuccess = true;
	                            } else {
	                                isSuccess = false;
	                                break;
	                            }
	                        }

	                        // 各维度数据同步完成后，则返回该特征平台信信息。
	                        if (isSuccess) {
//	                            context.getExecuteStateMap().put(featureReadyStatusPhase, true);
	                            context.setBoosterAndFeatureConfigModel(entry.getValue());
	                            LogUtils.logInfo(context.isLogSwitch(), "FeatureReadyStatusPhase-模型名", modelName);
	                            return;
	                        }
	                    }
	                }
	            } else {
	                log.error("FeatureReadyStatusPhase-模型名：{}-boosterAndFeatureConfigModel: NULL", modelName);
	            }
	        } catch (Exception e) {
	            log.error("FeatureReadyStatusPhase-模型名：{}-CacheParamException", modelName, e);
	        } finally {
	            TimeMonitorUtils.finish("FeatureReadyStatusPhase", context.getCostTimeMap());
	        }
	}

}
