package com.example.mobileknow.entity;

import java.util.List;

public class News extends Result{
	
	private List<New> list;

	public List<New> getList() {
		return list;
	}

	public void setList(List<New> list) {
		this.list = list;
	}

	public static class New {
		private String article;
		private String source;
		private String icon;
		private String detailurl;
		
		public String getArticle() {
			return article;
		}
		public void setArticle(String article) {
			this.article = article;
		}
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getIcon() {
			return icon;
		}
		public void setIcon(String icon) {
			this.icon = icon;
		}
		public String getDetailUrl() {
			return detailurl;
		}
		public void setDetailUrl(String detailUrl) {
			this.detailurl = detailUrl;
		}
	}
	
	@Override
	public String toString() {
		return list.get(0).getArticle() + "," + list.get(0).getDetailUrl() + "," + list.get(0).getSource();
	}
}
