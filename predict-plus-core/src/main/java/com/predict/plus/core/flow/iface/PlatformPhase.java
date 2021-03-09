package com.predict.plus.core.flow.iface;

import com.predict.plus.core.context.PredictContext;

public interface PlatformPhase {
	 void execute(PredictContext context);
}



//// 0. 在线上下文特征初始化封装
//executePhaseList.add(onlineContextFeaturePhase);
//// 1. 数据同步状态
//executePhaseList.add(featureReadyStatusPhase);
//// 2. 获取特征平台相关配置
//executePhaseList.add(featurePlatformPhase);
//// 3. redis查询各维度原始特征数据
//executePhaseList.add(getDimensionDataPhase);
//// 4. 原始特征组装
//executePhaseList.add(rawFeatureCombinePhase);
//// 5. 原始特征转模型特征
//executePhaseList.add(rawFeature2ModelFeaturePhase);
//// 6. 预测
//executePhaseList.add(predictPhase);
//// 7. 排序
//executePhaseList.add(sortPhase);
//// 8. 没有获取到特征的pid，拼接在预测pid列表后面
//executePhaseList.add(missPidAppendPhase);
//// 9. 埋点
//executePhaseList.add(buriedPointPhase);