package com.predict.plus.facade.model;

public class PidScoreModel {
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

	private String pid;

	private float score;
}
