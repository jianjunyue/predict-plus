package com.predict.plus.core.flow.phase;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Service;

import com.predict.plus.core.cache.DataCacheManagerHelper;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
/**
 * 检查modelName各维度特征同步状态 
 */
@Service
public class FeatureReadyStatusPhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------FeatureReadyStatusPhase----------------");
		
		 LinkedHashMap<Integer, BoosterAndFeatureConfigModel> list=DataCacheManagerHelper.boosterAndFeatureConfigModelMap.get("");

	}

}
