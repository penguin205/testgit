package com.example.smsManager.provider;


import com.example.smsManager.utils.Sms;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class SearchableProvider extends SearchRecentSuggestionsProvider {

	private final String AUTHORITY = "com.example.smsManager.provider.SearchableProvider";
	public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
	
	private final String[] sms_projection = {
			"_id",
			"address",
			"body"
	};
	
	private final String[] columnNames = {
			BaseColumns._ID,
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_QUERY
	};
	
	public SearchableProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String where = "body like '%" + selectionArgs[0] + "%'";
		Cursor cursor = getContext().getContentResolver().query(Sms.SMS_URI, sms_projection, where, null, null);
		
		if(cursor != null && cursor.getCount() > 0) {
			
			/*
			 * �����matrix�α�����, ��ָ������ 
			 * _ID, 
			 * SUGGEST_COLUMN_TEXT_1(��ʾ������: ����), 
			 * SUGGEST_COLUMN_TEXT_2(��ʾ������: ����)
			 */
			MatrixCursor mc = new MatrixCursor(columnNames);
			
			String[] columnValues;
			while(cursor.moveToNext()) {
				columnValues = new String[columnNames.length];
				
				// �ѵ�ǰcursor�е�һ���������õ�columnValues. ��Ӹ�matrixCursor
				columnValues[0] = cursor.getString(0);
				columnValues[1] = cursor.getString(1);
				columnValues[2] = cursor.getString(2);
				// ���õ��û����itemʱ, ���ݹ�ȥ�Ĳ���Ϊcursor�е�2��: ���ŵ�����body
				columnValues[3] = cursor.getString(2);
				
				mc.addRow(columnValues);
			}
			return mc;
		}
		return null;
	}


}
