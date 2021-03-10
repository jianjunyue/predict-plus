package com.predict.plus.core.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.predict.plus.core.model.ExecutorPriority;
import com.predict.plus.core.model.PlatformModelConfig;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel; 
 
public class DataCacheManagerHelper {


    /**
     * 模型本地管理服务
     * String Key: 模型名称
     * Integer Key: 特征version
     */
    public static Map<String, LinkedHashMap<Integer, BoosterAndFeatureConfigModel>> boosterAndFeatureConfigModelMap = new ConcurrentHashMap<>();
    /**
     * 最新版：模型对应的特征维度map
     */
    public static Map<String, PlatformModelConfig> platformModelConfigMap = new ConcurrentHashMap<>();
    
    /**
     * 执行优先级
     */
    public static Map<String, ExecutorPriority> executorPriorityMap = new ConcurrentHashMap<>();
     

}
