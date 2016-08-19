package com.cngps.carvideo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * 数据库操作工具类
 */
public class MemorySqlTool {
	private static final String TAG = MemorySqlTool.class.getSimpleName();
    private MemoryHelper dbHelper;

    public MemorySqlTool(Context context) {
        dbHelper = MemoryHelper.getInstance(context);
    }

    /**
     * 插入断点信息
     */
    public void insertInfo(MovieInfo info) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String sql = "insert into memory_info(seek_pos,duration,display_name,path) values (?,?,?,?)";
        Object[] bindArgs = { info.seekPos, info.duration, info.displayName, info.path };
        database.execSQL(sql, bindArgs);
        database.close();
    }

    /**
     * 得到断点信息
     */
    public MovieInfo getInfo() {
    	MovieInfo info = null;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String sql = "select seek_pos, duration, display_name, path from memory_info";
        Cursor cursor = database.rawQuery(sql,null);
        
        if(cursor != null && cursor.moveToFirst()) {
        	 info = new MovieInfo(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3));
        	 cursor.close();
        	 database.close();
        	 return info;
        }
       return info;
    }

    /**
     * 删除数据库中的数据
     */
    public void delete() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete("memory_info", null, null);
        database.close();
    }
    
    /**
     * 更新数据库中的下载信息
     */
    public void updataInfo(MovieInfo info) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        delete();
        insertInfo(info);
        database.close();
    }


}