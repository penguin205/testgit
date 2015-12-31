package com.example.smsManager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class GroupOpenHelper extends SQLiteOpenHelper {

	public static GroupOpenHelper instance;
	
	public static GroupOpenHelper getInstance(Context context) {
		if (instance == null) {
			synchronized (GroupOpenHelper.class) {
				if(instance == null) {
					instance = new GroupOpenHelper(context, "smartsms.db", null, 1);
				}
			}
		}
		return instance;
	}
	
	private GroupOpenHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table groups(_id integer primary key, group_name varchar(30));";
		db.execSQL(sql);
		sql = "create table thread_group(_id integer primary key, group_id integer, thread_id integer);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
