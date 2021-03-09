package com.predict.plus.core.context;

import java.util.List;
import java.util.Map;

import com.predict.plus.core.flow.iface.PlatformPhase;
import com.predict.plus.facade.request.ModelPredictRequest;
import com.predict.plus.facade.response.PredictResult;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
 
@Data
public class PredictContext  extends BaseContext{ 
  
	    @ApiModelProperty("每个阶段执行是否成功状态")
	    private Map<String, Boolean> executeStateMap;

	    @ApiModelProperty("模型名称")
	    private String modelName;

	    @ApiModelProperty("模型版本")
	    private Integer version; 
	    
	    @ApiModelProperty("用户维度原始特征")
	    private Map<String, Object> uidRawFeatureMap;

	    @ApiModelProperty("prd维度原始特征")
	    private Map<String, Map<String, Object>> prdRawFeatureMap;

	    @ApiModelProperty("prd模型特征")
	    Map<String, float[]> prdModelFeatureMap;

	    @ApiModelProperty("没有获取到原始特征prd，不需要走模型")
	    private List<String> missPrdList;

	    @ApiModelProperty("分批预测，每个集合元素数量")
	    private Integer batchCount;

	    @ApiModelProperty("特征分批，每个集合元素数量")
	    private Integer featureBatchCount;

	    @ApiModelProperty("预测结果")
	    private List<PredictResult> predictResults;
	    
	    @ApiModelProperty("预测执行步骤配置")
	    private List<PlatformPhase> executePhaseList;
	    
	    @ApiModelProperty("请求入参")
	    private ModelPredictRequest request;
}
