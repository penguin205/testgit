package com.android.settings.wifi.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class WifiDbOperator {
	private WifiDbHelper mDbHelper;
	
	public WifiDbOperator(Context context) {
		mDbHelper = WifiDbHelper.getInstance(context);
	}
	
	/**
	 * insert assigned ssid to db
	 * @param ssid 
	 * @return true success, false fail
	 */
	public boolean insertToDb(String ssid) {
		boolean flag = false;
		SQLiteDatabase db = null;
		try {
			db = mDbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(WifiDbHelper.COLUMN_NAME, ssid);
			long id = db.insert(WifiDbHelper.TABLE_NAME, null, cv);		
			flag = (id != -1 ? true : false);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(db != null) {
				db.close();
			}
		}
		return flag;
	}
	
	/**
	 * query all ssid that be banned of auto connection
	 * @return 
	 */
	public String[] queryAllSsid() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		String[] aps = null;
		try {
			db = mDbHelper.getReadableDatabase();
			cursor = db.query(WifiDbHelper.TABLE_NAME, new String[]{ WifiDbHelper.COLUMN_NAME }, 
					null, null, null, null, null);
			if(cursor != null && cursor.getCount() > 0) {
				aps = getStringArrayByCursor(cursor);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(db != null) {
				db.close();
			}
		   if(cursor != null) {
            	cursor.close();
            }
		}
		return aps;
	}
	
	/**
	 * query assigned ssid that be banned of auto connection
	 * @param ssid
	 * @return 
	 */
	public boolean querySsidIsExist(String ssid) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		boolean flag = false;
		String selection = WifiDbHelper.COLUMN_NAME + "=?";
		String[] selectionArgs = new String[]{ ssid };
		try {
			db = mDbHelper.getReadableDatabase();
			cursor = db.query(WifiDbHelper.TABLE_NAME, new String[]{WifiDbHelper.COLUMN_NAME}, 
					selection, selectionArgs, null, null, null);
			if(cursor != null && cursor.moveToFirst()) {
				String db_ssid = cursor.getString(cursor.getColumnIndex(WifiDbHelper.COLUMN_NAME));
				if(db_ssid.equals(ssid)) {
					flag = true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(db != null) {
				db.close();
			}
		   if(cursor != null) {
            	cursor.close();
            }
		}
		return flag;
	}
	
	/**
	 * delete assigned ssid that be banned of auto connection in db
	 * @param ssid
	 * @return boolean : true delete success
	 */
	public boolean deleteFromDb(String ssid) {
		SQLiteDatabase db = null;
		boolean flag = false;
		String selection = WifiDbHelper.COLUMN_NAME + "=?";
		String[] selectionArgs = new String[]{ ssid };	
		try {
			db = mDbHelper.getWritableDatabase();
			int count = db.delete(WifiDbHelper.TABLE_NAME, selection, selectionArgs);
			flag = (count > 0 ? true : false);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(db != null) {
				db.close();
			}
		}
		return flag;
	}
	
	/**
	 * convert cursor to string arry
	 * @param cursor
	 * @return
	 */
	private String[] getStringArrayByCursor(Cursor cursor) {
		String[] aps = null;
		if(cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			aps = new String[cursor.getCount()];
			for(int i=0; i< cursor.getCount(); i++) {
				aps[i] = cursor.getString(0);
				//Log.d("ffff", i+ ":" + aps[i]);
				cursor.moveToNext();
			}
		}
		return aps;
	}
}
