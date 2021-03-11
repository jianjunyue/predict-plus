package com.predict.plus.core.flow.phase;

import org.springframework.stereotype.Service;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
/**
 * 3. redis查询各维度原始特征数据
 */
@Service
public class GetDimensionDataPhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		System.out.println("-----------GetDimensionDataPhase----------------");

	}

}
