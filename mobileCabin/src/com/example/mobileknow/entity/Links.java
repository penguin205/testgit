package com.example.mobileknow.entity;

public class Links extends Result{
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "code:" + code + ",text:" + text + ",url:" + url;
	}
}
