package com.cngps.carvideo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class MovieInfo implements Parcelable {
	
    Bitmap thumbnail;
	int seekPos;
	int duration;
	String displayName;
	String path;
	
	public MovieInfo(int seekPos, int duration, String displayName, 
			String path) {
		this.seekPos = seekPos;
		this.duration = duration;
		this.displayName = displayName;
		this.path = path;
	}
	
	public MovieInfo(Bitmap thumbnail, int seekPos, int duration,
			String displayName, String path) {
		this.thumbnail = thumbnail;
		this.seekPos = seekPos;
		this.duration = duration;
		this.displayName = displayName;
		this.path = path;
	}

	public MovieInfo() {
	}

	//内容描述
	@Override
	public int describeContents() {
		return 0;
	}

	//序列化
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(seekPos);
		dest.writeInt(duration);
		dest.writeString(displayName);
		dest.writeString(path);
	}
	//反序列化
	public static final Parcelable.Creator<MovieInfo> CREATOR = new Parcelable.Creator<MovieInfo>() {

		//如何创建序列化对象
		@Override
		public MovieInfo createFromParcel(Parcel source) {
			return new MovieInfo(source);
		}
		//如何创建序列化对象数组
		@Override
		public MovieInfo[] newArray(int size) {
			return new MovieInfo[size];
		}
	};
	
	public MovieInfo(Parcel source) {
		seekPos = source.readInt();
		duration = source.readInt();
		displayName = source.readString();
		path = source.readString();
	}
}
