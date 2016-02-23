package com.example.mobileknow.entity;

import java.util.List;

public class Foods extends Result {
	
	private List<Food> list;
	
	public List<Food> getList() {
		return list;
	}

	public void setList(List<Food> list) {
		this.list = list;
	}

	public static class Food{
		
		private String detailurl;
		private String icon;
		private String info;
		private String name;
		
		public String getIcon() {
			return icon;
		}
		public void setIcon(String icon) {
			this.icon = icon;
		}
		public String getInfo() {
			return info;
		}
		public void setInfo(String info) {
			this.info = info;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDetailurl() {
			return detailurl;
		}
		public void setDetailurl(String detailurl) {
			this.detailurl = detailurl;
		}
	}
	
	@Override
	public String toString() {
		return "code:" + code + ",text:" + text + "," + list.get(0).getName();
	}
}
