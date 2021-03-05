package com.predict.plus.facade.response;

public class PredictResult {
	
	private String pid; 
	private float score;
	
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
}
