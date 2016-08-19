package com.android.settings.wifi.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class WifiDbHelper extends SQLiteOpenHelper{
	
	public static WifiDbHelper instance;
	public static final String DB_NAME = "wifi_ban.db";
	public static final String TABLE_NAME = "wifi_ban_table";
	public static final String COLUMN_NAME = "ap_name";
	
	public static WifiDbHelper getInstance(Context context) {
		if (instance == null) {
			synchronized (WifiDbHelper.class) {
				if(instance == null) {
					instance = new WifiDbHelper(context, DB_NAME, null, 1);
				}
			}
		}
		return instance;
	}
	
	private WifiDbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE_NAME
				+ "(_id integer primary key, " + COLUMN_NAME
				+ " varchar(30));";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_NAME);
		onCreate(db);
	}
}
