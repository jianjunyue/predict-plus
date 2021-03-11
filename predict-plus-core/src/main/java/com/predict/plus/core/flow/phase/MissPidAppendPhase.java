package com.predict.plus.core.flow.phase;

import org.springframework.stereotype.Service;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
/**
 * 8. 没有获取到特征的pid，拼接在预测pid列表后面
 */
@Service
public class MissPidAppendPhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------MissPidAppendPhase----------------");

	}

}
