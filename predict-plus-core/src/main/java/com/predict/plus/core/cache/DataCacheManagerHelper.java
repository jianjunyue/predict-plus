package com.predict.plus.core.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
 
public class DataCacheManagerHelper {


    /**
     * 模型本地管理服务
     * String Key: 模型名称
     * Integer Key: 特征version
     */
    public static Map<String, LinkedHashMap<Integer, BoosterAndFeatureConfigModel>> boosterAndFeatureConfigModelMap = new ConcurrentHashMap<>();

}
