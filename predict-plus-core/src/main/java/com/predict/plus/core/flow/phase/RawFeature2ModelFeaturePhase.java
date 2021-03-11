package com.predict.plus.core.flow.phase;

import org.springframework.stereotype.Service;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
/**
 * 5. 原始特征转模型特征
 */
@Service
public class RawFeature2ModelFeaturePhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------RawFeature2ModelFeaturePhase----------------");

	}

}
