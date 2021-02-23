package com.predict.plus.facade.model;

import java.util.Map;

public class ProductIdModel {
	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Map<String, Object> getProductIdAttributes() {
		return productIdAttributes;
	}

	public void setProductIdAttributes(Map<String, Object> productIdAttributes) {
		this.productIdAttributes = productIdAttributes;
	}

	private String productId;

	// 产品productId维度附加特征
	private Map<String, Object> productIdAttributes;
}
