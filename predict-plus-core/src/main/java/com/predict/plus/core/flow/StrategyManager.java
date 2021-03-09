package com.predict.plus.core.flow;

import java.util.List;

import org.springframework.stereotype.Service;
 
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StrategyManager {
	  
	public void run(PredictContext context) {  
		List<PlatformPhase> executePhaseList = context.getExecutePhaseList();
		try {
			for (PlatformPhase phase : executePhaseList) {
				phase.execute(context);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
