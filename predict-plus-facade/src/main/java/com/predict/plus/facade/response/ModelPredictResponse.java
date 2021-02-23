package com.predict.plus.facade.response;

import java.util.List;
import java.util.Map;

import com.predict.plus.facade.model.PidScoreModel;
 

public class ModelPredictResponse {

    public List<PidScoreModel> getPidScoreModelList() {
		return pidScoreModelList;
	}

	public void setPidScoreModelList(List<PidScoreModel> pidScoreModelList) {
		this.pidScoreModelList = pidScoreModelList;
	}

	public Map<String, Map<String, Object>> getPrdRawFeatureMap() {
		return prdRawFeatureMap;
	}

	public void setPrdRawFeatureMap(Map<String, Map<String, Object>> prdRawFeatureMap) {
		this.prdRawFeatureMap = prdRawFeatureMap;
	}

	public Map<String, String> getPrdModelFeatureMap() {
		return prdModelFeatureMap;
	}

	public void setPrdModelFeatureMap(Map<String, String> prdModelFeatureMap) {
		this.prdModelFeatureMap = prdModelFeatureMap;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	//返回分数 
    private List<PidScoreModel> pidScoreModelList;

    //prd维度原始特征 
    private Map<String, Map<String, Object>> prdRawFeatureMap;

    // prd模型特征 
    Map<String, String> prdModelFeatureMap;

    //模型名称 
    private String modelName;
}
