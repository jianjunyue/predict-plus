package com.predict.plus.core.flow.phase;

import org.springframework.stereotype.Service;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;

/**
 * 4. 原始特征组装
 */
@Service
public class RawFeatureCombinePhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------RawFeatureCombinePhase----------------");

	}

}
