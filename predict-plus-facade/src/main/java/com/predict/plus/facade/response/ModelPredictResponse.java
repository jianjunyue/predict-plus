package com.predict.plus.facade.response;

import java.util.List;
import java.util.Map;
  

public class ModelPredictResponse {

	//返回分数 
    private List<PredictScore> pidScoreModelList;

    //prd维度原始特征 
    private Map<String, Map<String, Object>> prdRawFeatureMap;

    // prd模型特征 
    Map<String, String> prdModelFeatureMap;

 
    

    public List<PredictScore> getPidScoreModelList() {
		return pidScoreModelList;
	}

	public void setPidScoreModelList(List<PredictScore> pidScoreModelList) {
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
 

}
