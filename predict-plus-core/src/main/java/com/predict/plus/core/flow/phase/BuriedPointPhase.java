package com.predict.plus.core.flow.phase;

import org.springframework.stereotype.Service;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
/**
 * 9. 埋点
 */
@Service
public class BuriedPointPhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------BuriedPointPhase----------------");

	}

}
