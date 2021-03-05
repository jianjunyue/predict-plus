package com.predict.plus.facade.request;

import java.util.List;
import java.util.Map;
 
  
public class ModelPredictRequest extends BaseRequest{ 

	private List<ProductRequest> productIds;
    
    //环境上下文特征 包括用户和接口层面的特征
    private Map<String, Object> contextAttributes;
 

    public List<ProductRequest> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<ProductRequest> productIds) {
		this.productIds = productIds;
	}

	public Map<String, Object> getContextAttributes() {
		return contextAttributes;
	}

	public void setContextAttributes(Map<String, Object> contextAttributes) {
		this.contextAttributes = contextAttributes;
	}

}
