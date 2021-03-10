package com.predict.plus.core.flow.phase;

import org.springframework.stereotype.Service;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
/**
 * 2. 获取特征平台相关配置
 */
@Service
public class FeaturePlatformPhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------FeaturePlatformPhase----------------");

	}

}
