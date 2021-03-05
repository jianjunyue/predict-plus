package com.predict.plus.core.flow.phase;

import com.predict.plus.common.context.PredictContext;
import com.predict.plus.common.iface.PlatformPhase;

/**
 * 在线上下文特征初始化封装
 */
public class SortPhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------SortPhase----------------");

	}

}
