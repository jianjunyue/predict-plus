package com.predict.plus.core.init;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps; 
import com.predict.plus.common.model.Module;
import com.predict.plus.common.utils.ConfigResourceLoad;
import com.predict.plus.core.flow.iface.PlatformPhase; 
 
@Service
public class PredictPhaseConfigBuilder  implements ApplicationContextAware {
 
    private static Map<String, List<PlatformPhase>> moduleStrategys =Maps.newHashMap();
     
    private ApplicationContext applicationContext = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext; 
	}
	
    @PostConstruct
    private void init() throws Exception { 
    	loadDefaultStrategies();
    }
     
	public List<PlatformPhase> getModuleStrategy(Module module) {
		return moduleStrategys.getOrDefault(module.getModuleName(),Lists.newArrayList() ); 
	 
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> buildRankStrategyList(List<String> rankStrategyList, Class<T> clazz) {
		return rankStrategyList.stream().map(rs -> (T) applicationContext.getBean(rs))
				.filter(bean -> clazz.isAssignableFrom(bean.getClass())).collect(Collectors.toList());
	}
	
	 private void loadDefaultStrategies() 
	 { 
		 Map<String, List<String>>  strategyMap=  ConfigResourceLoad.readStrategyFile(Map.class, new HashMap<String, List<String>>(), "platfrom_strategy.json");
		 strategyMap.keySet().forEach(strategyName ->{
			 List<PlatformPhase> strategyList=  buildRankStrategyList( strategyMap.get(strategyName),PlatformPhase.class);
			 moduleStrategys.put(strategyName, strategyList);
		 }); 
	 }
	
}
