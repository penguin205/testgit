package com.example.mobileknow.entity;

public class IAnswer {
	public static final int JOKE_ANSWER_TYPE = 0;
	public static final int POEM_ANSWER_TYPE = 1;
	public static final int MM_ANSWER_TYPE = 2;
	
	private int answerType;
	private String joke;;
	private String poem;
	private int mmResId;
	
	public int getAnserType() {
		return answerType;
	}
	public void setAnswerType(int type) {
		this.answerType = type;
	}
	public String getJoke() {
		return joke;
	}
	public void setJoke(String joke) {
		this.joke = joke;
	}
	public String getPoem() {
		return poem;
	}
	public void setPoem(String poem) {
		this.poem = poem;
	}
	public int getMmResId() {
		return mmResId;
	}
	public void setMmResId(int mmResId) {
		this.mmResId = mmResId;
	}

}
