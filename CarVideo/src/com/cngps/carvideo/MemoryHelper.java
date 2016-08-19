package com.cngps.carvideo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 利用数据库记录视频断点信息
 * @author XiaoYJ
 */
public class MemoryHelper extends SQLiteOpenHelper{

	private static final String SQL_NAME = "memory.db";
	private static final int DOWNLOAD_VERSION = 1;
	public static MemoryHelper instance;
	
	private MemoryHelper(Context context) {
		super(context, SQL_NAME, null, DOWNLOAD_VERSION);
	}
	
	public static MemoryHelper getInstance(Context context) {
		if (instance == null) {
			synchronized (MemoryHelper.class) {
				if(instance == null) {
					instance = new MemoryHelper(context);
				}
			}
		}
		return instance;
	}
	
	 /**
     * 在memory.db数据库下创建一个memory_info表存储下载信息
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table memory_info(_id integer PRIMARY KEY AUTOINCREMENT, seek_pos integer, "
                + "duration integer, display_name char, path char)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists memory_info");
		onCreate(db);
    }

}
