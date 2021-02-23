package com.predict.plus.facade.request;

import java.util.List;
import java.util.Map;

import com.predict.plus.facade.model.ProductIdModel;
 

public class ModelPredictRequest extends BaseContextRequest{
 

    public List<ProductIdModel> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<ProductIdModel> productIds) {
		this.productIds = productIds;
	}

	public Map<String, Object> getContextAttributes() {
		return contextAttributes;
	}

	public void setContextAttributes(Map<String, Object> contextAttributes) {
		this.contextAttributes = contextAttributes;
	}

	private List<ProductIdModel> productIds;
    
    //环境上下文特征 包括用户和接口层面的特征
    private Map<String, Object> contextAttributes;
}
