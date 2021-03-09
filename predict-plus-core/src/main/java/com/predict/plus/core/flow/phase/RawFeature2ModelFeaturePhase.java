package com.predict.plus.core.flow.phase;

import org.springframework.stereotype.Service;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
/**
 * 在线上下文特征初始化封装
 */
@Service
public class RawFeature2ModelFeaturePhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------RawFeature2ModelFeaturePhase----------------");

	}

}
