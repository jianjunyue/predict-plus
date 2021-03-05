package com.predict.plus.common.init;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.predict.plus.common.config.ApolloConfig;
import com.predict.plus.common.constant.BusinessConstant;
import com.predict.plus.common.context.PredictContext;
import com.predict.plus.common.iface.PlatformPhase;
import com.predict.plus.common.model.Module;
import com.predict.plus.facade.request.ModelPredictRequest;

public class PredictContextInit {

	@Autowired
	private PredictPhaseConfigBuilder predictPhaseConfigBuilder;

	public PredictContext initContext(ModelPredictRequest request) {

		PredictContext context = new PredictContext();

		context.setModelName(request.getModelName());

		context.setRequestId(request.getRequestId());

		context.setRankId(request.getRankId());

		context.setUserId(request.getUid());

		// 获取预测批量大小
		Integer batchCount = ApolloConfig.getParameterWithInteger(BusinessConstant.MODEL_PREDICT_BATCH_COUNT);
		context.setBatchCount(batchCount);

		// 特征分批处理大小
		Integer featureBatchCount = ApolloConfig.getParameterWithInteger(BusinessConstant.MODEL_FEATURE_BATCH_COUNT);
		context.setFeatureBatchCount(featureBatchCount);

		// 日志开关
		boolean logContentSwitch = ApolloConfig.switchStatus(BusinessConstant.LOG_MODEL_PREDICT_SWITCH);
		context.setLogSwitch(logContentSwitch);

		// debug日志开关
		boolean logDebugSwitch = ApolloConfig.switchStatus(BusinessConstant.LOG_DEBUG_PLATFORM_SWITCH);
		context.setLogDebugSwitch(logDebugSwitch);

		// 预测执行步骤加载
		List<PlatformPhase> executePhaseList=predictPhaseConfigBuilder.getModuleStrategy(getModule());
		context.setExecutePhaseList(executePhaseList);

		return context;
	}

	private Module getModule() {
		return Module.valueOf("platfrom_phase");
	}

	

}
