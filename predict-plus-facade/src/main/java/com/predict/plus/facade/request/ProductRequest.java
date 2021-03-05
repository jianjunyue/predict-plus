package com.predict.plus.facade.request;

import java.util.Map;

public class ProductRequest { 

	private String pid;
	// 产品productId维度附加特征
	private Map<String, Object> productIdAttributes;

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public Map<String, Object> getProductIdAttributes() {
		return productIdAttributes;
	}

	public void setProductIdAttributes(Map<String, Object> productIdAttributes) {
		this.productIdAttributes = productIdAttributes;
	}

}
