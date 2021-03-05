package com.predict.plus.facade.request;

public class BaseRequest {

	private String uid;

	private String deviceId;

	private String requestId;

	private String rankId;

	private Boolean isNeedScore;

	private Integer scene;

	private String modelName;

//    private VersionEnum testAbVersion;

	private Boolean isNeedFeaResult = false;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getRankId() {
		return rankId;
	}

	public void setRankId(String rankId) {
		this.rankId = rankId;
	}

	public Boolean getIsNeedScore() {
		return isNeedScore;
	}

	public void setIsNeedScore(Boolean isNeedScore) {
		this.isNeedScore = isNeedScore;
	}

	public Integer getScene() {
		return scene;
	}

	public void setScene(Integer scene) {
		this.scene = scene;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public Boolean getIsNeedFeaResult() {
		return isNeedFeaResult;
	}

	public void setIsNeedFeaResult(Boolean isNeedFeaResult) {
		this.isNeedFeaResult = isNeedFeaResult;
	}

}
