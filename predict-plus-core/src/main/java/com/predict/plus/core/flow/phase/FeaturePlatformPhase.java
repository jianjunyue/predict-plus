package com.predict.plus.core.flow.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.predict.plus.algo.common.Feature;
import com.predict.plus.algo.common.FeatureConfig;
import com.predict.plus.algo.common.PreprocessFeature;
import com.predict.plus.algo.features.RawFeatureType;
import com.predict.plus.common.utils.ConfigResourceLoad;
import com.predict.plus.common.utils.TimeMonitorUtils;
import com.predict.plus.core.cache.DataCacheManagerHelper;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel; 

import lombok.extern.slf4j.Slf4j; 

/**
 * 2. 获取特征平台相关配置
 */
@Slf4j
@Service
public class FeaturePlatformPhase implements PlatformPhase {
 
    /**
     * 获取特征平台相关配置
     * @param context
     */
    @Override
    public void execute(PredictContext context) {
    	System.out.println("-----------FeaturePlatformPhase----------------");
        TimeMonitorUtils.start();
        try {
        
            // 获取特征平台信息
            BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = context.getBoosterAndFeatureConfigModel();
            FeatureConfig featureConfig = boosterAndFeatureConfigModel.getFeatureConfig();
            Map<String, String> featureId2NameMap = boosterAndFeatureConfigModel.getFeatureId2NameMap();
            if (null == featureConfig || MapUtils.isEmpty(featureId2NameMap)) {
                Integer version = boosterAndFeatureConfigModel.getVersion();
//                BizBaseResponse<FeatureConfigResponse> featureConfigResponseBiz = mlpAlgorithmPlatformServiceRemote.getFeatureByVersion(version);
//                if (null != featureConfigResponseBiz && featureConfigResponseBiz.isSuccess()) {
                if(true) {
//                    FeatureConfigResponse featureConfigResponse = featureConfigResponseBiz.getData();
//                    if (null != featureConfigResponse) {
                	  if(true) {
                        // 特征名和ID映射
                        Map<String, String> featureIdNameMap =null;// featureConfigResponse.getFeatureIdNameMap();
                        featureIdNameMap =   ConfigResourceLoad.readJsonFile(Map.class, new HashMap<String, String>(), "featureIdNameMap.json");
                        log.info("FeaturePlatformPhase-特征名和ID映射featureIdNameMap:{}", JSON.toJSONString(featureIdNameMap));
                        if (MapUtils.isNotEmpty(featureIdNameMap)) {
                            featureId2NameMap = featureIdNameMap.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
                            log.info("FeaturePlatformPhase-ID和特征名映射featureId2NameMap:{}", JSON.toJSONString(featureId2NameMap));
                            boosterAndFeatureConfigModel.setFeatureId2NameMap(featureId2NameMap);
                        }

                        String modelName = context.getModelName();
                        List<Feature> features = getFeatures();// featureConfigResponse.getFeatures();
                      
                        
                        Map<String, RawFeatureType> allType = getRawFeatureTypeMap();// featureConfigResponse.getFeatureTypeMap();
                     
                        
                        if (CollectionUtils.isNotEmpty(features) && MapUtils.isNotEmpty(allType)) {
                            // 为了节约内存，把注释替换成ID，置空会报错。
                            features = features.stream().map(feature -> {
                                feature.setName(Integer.toString(feature.getId()));
                                return feature;
                            }).collect(Collectors.toList());

                            boosterAndFeatureConfigModel.setFeatureConfig(new FeatureConfig(version, features, allType,new ArrayList<PreprocessFeature>()));
                            // 特征version和FeatureConfig映射
                            LinkedHashMap<Integer, BoosterAndFeatureConfigModel> versionBoosterMap = DataCacheManagerHelper.boosterAndFeatureConfigModelMap.getOrDefault(modelName, new LinkedHashMap<>());
                            versionBoosterMap.put(version, boosterAndFeatureConfigModel);
                            // 元素个数大于1再根据key：version倒排 https://blog.csdn.net/jackyrongvip/article/details/89370396
                            if (versionBoosterMap.size() > 1) {
                                versionBoosterMap = versionBoosterMap.entrySet()
                                        .stream()
                                        .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (e1, e2) -> e1, LinkedHashMap::new)
                                        );
                            }
                            DataCacheManagerHelper.boosterAndFeatureConfigModelMap.put(modelName, versionBoosterMap);
                            log.info("FeaturePlatformPhase-结束加载versionBoosterMap={}对应的模型配置文件", JSON.toJSONString(versionBoosterMap));
                        }
                    }
                } else {
                    log.error("FeaturePlatformPhase-服务中未加载boosterAndFeatureConfigModel={}对应的模型文件", JSON.toJSONString(boosterAndFeatureConfigModel));
                }
            }

            // 从缓存中根据version获取信息
            if (null != boosterAndFeatureConfigModel.getFeatureConfig() &&
                    (null != boosterAndFeatureConfigModel.getBooster() || null != boosterAndFeatureConfigModel.getLightGBMModel() || null != boosterAndFeatureConfigModel.getTensorFlowModel())
                    && null != boosterAndFeatureConfigModel.getFeatureId2NameMap()) {
//                context.getExecuteStateMap().put(featurePlatformPhase, true);
                log.info("FeaturePlatformPhase-boosterAndFeatureConfigModel: success");
            }
        } catch (Exception e) {
            log.error("FeaturePlatformPhase-getBoosterAndFeatureConfig-Exception", e);
        } finally {
            TimeMonitorUtils.finish("FeaturePlatformPhase", context.getCostTimeMap());
        }
    }
    
    private   List<Feature> getFeatures(){
     	List<Map<String,Object>> features =ConfigResourceLoad.getFeatures(); 
    	List<Feature> featureList =Lists.newArrayList();
    	for(Map<String,Object> map : features)
    	{
    		Feature fea=new Feature();
    		fea.setId((int)map.get("id"));  
    		fea.setDefaultRawMap(map.getOrDefault("defaultRawMap","").toString());
    		List<Double> db= (List<Double>)map.getOrDefault("defaultValues",new ArrayList<Double>());
    		db=new ArrayList<Double>();
    		fea.setDefaultValues(db);
    		fea.setName(map.getOrDefault("name","").toString());
    		fea.setOperator(map.getOrDefault("operator","").toString());
    		fea.setOutlierCheck(map.getOrDefault("outlierCheck","").toString());
    		fea.setRawList((List<String>)map.getOrDefault("rawList",Lists.newArrayList()));
    		fea.setReturnValueLoc((List<Integer>)map.getOrDefault("returnValueLoc",Lists.newArrayList()));
    		fea.setReturnValueNum((int)map.getOrDefault("returnValueNum",0));
    		
    		featureList.add(fea);
    	}
 
    	return featureList;
    }
    
    private Map<String, RawFeatureType>  getRawFeatureTypeMap()
    {
    	Map<String,String> mapTemp=ConfigResourceLoad.readJsonFile(Map.class, new HashMap<String,String>(), "allType.json");
    	Map<String, RawFeatureType> mapType=Maps.newHashMap();
    	
    	mapTemp.keySet().forEach(key->{
    		RawFeatureType type="Number".equals(mapTemp.get(key))?RawFeatureType.Number:RawFeatureType.Text	;
    		mapType.put(key, type);
    	});
   	  
    	return mapType;
    }

}
