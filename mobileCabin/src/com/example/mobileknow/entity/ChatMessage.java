package com.example.mobileknow.entity;

import java.util.Date;

public class ChatMessage {

	private Date date;
	private String content;
	private String detail;
	private String imageUrl;
	private Type type;

	public ChatMessage(Date date, String content, Type type) {
		this.date = date;
		this.content = content;
		this.type = type;
	}

	public ChatMessage() {
	}

	public enum Type {
		INCOMING, OUTCOMING
	};
	
	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
