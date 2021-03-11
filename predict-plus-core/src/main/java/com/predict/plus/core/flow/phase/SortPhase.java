package com.predict.plus.core.flow.phase;

import org.springframework.stereotype.Service;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;

/**
 * 7. 排序
 */
@Service
public class SortPhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------SortPhase----------------");

	}

}
