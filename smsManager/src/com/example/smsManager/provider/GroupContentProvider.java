package com.example.smsManager.provider;

import org.apache.http.client.utils.URIUtils;

import com.example.smsManager.db.GroupOpenHelper;
import com.example.smsManager.utils.Sms;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Switch;

public class GroupContentProvider extends ContentProvider {
	
	private static final String AUTHORITY = "com.example.smsManager.provider.GroupContentProvider";
	private static final String TABLE_GROUPS = "groups";
	private static final String TABLE_THREAD_GROUP = "thread_group";
	
	private static final int GROUPS_QUERY_ALL = 0;
	private static final int THREAD_GROUP_QUERY_ALL = 1;
	private static final int GROUPS_INSERT = 2;
	private static final int THREAD_GROUP_INSERT = 3;
	private static final int GROUPS_SINGLE_DELETE = 4;
	private static final int GROUPS_ALL_DELETE = 5;
	private static final int GROUPS_UPDATE = 6;
	
	private static UriMatcher uriMatcher;
	private GroupOpenHelper mOpenHelper;
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		uriMatcher.addURI(AUTHORITY, "groups", GROUPS_QUERY_ALL);
		uriMatcher.addURI(AUTHORITY, "thread_group", THREAD_GROUP_QUERY_ALL);
		uriMatcher.addURI(AUTHORITY, "groups/insert", GROUPS_INSERT);
		uriMatcher.addURI(AUTHORITY, "thread_group/insert", THREAD_GROUP_INSERT);
		uriMatcher.addURI(AUTHORITY, "groups/delete/#", GROUPS_SINGLE_DELETE);
		uriMatcher.addURI(AUTHORITY, "groups/all_delete", GROUPS_ALL_DELETE);
		uriMatcher.addURI(AUTHORITY, "groups/update", GROUPS_UPDATE);
	}
	
	@Override
	public boolean onCreate() {
		mOpenHelper = GroupOpenHelper.getInstance(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case GROUPS_QUERY_ALL:
			if(db.isOpen()) {
				Cursor cursor = db.query(TABLE_GROUPS, projection, selection, selectionArgs, null, null, sortOrder);
				cursor.setNotificationUri(getContext().getContentResolver(), Sms.GROUPS_QUERY_URI);
				return cursor;
			}
			break;
		case THREAD_GROUP_QUERY_ALL:
			if(db.isOpen()) {
				Cursor cursor = db.query(TABLE_THREAD_GROUP, projection, selection, selectionArgs, null, null, sortOrder);
				return cursor;
			}
			break;
		
		default:
			throw new IllegalArgumentException("unknown uri £º " + uri);
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case GROUPS_INSERT:
			if(db.isOpen()) {
				long rowId = db.insert(TABLE_GROUPS, null, values);
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_URI, null);
				return ContentUris.withAppendedId(uri, rowId);
			}
			break;
		case THREAD_GROUP_INSERT:
			if(db.isOpen()) {
				long rowId = db.insert(TABLE_THREAD_GROUP, null, values);
				return ContentUris.withAppendedId(uri, rowId);
			}
			break;

		default:
			throw new IllegalArgumentException("unknown uri £º " + uri);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case GROUPS_SINGLE_DELETE:
			if(db.isOpen()) {
				long group_id = ContentUris.parseId(uri);
				String where = "_id = " + group_id;
				int count = db.delete(TABLE_GROUPS, where, null);
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_URI, null);
				
				String whereClause = "group_id = " + group_id;
				db.delete(TABLE_THREAD_GROUP, whereClause, null);
				return count;
			}
			break;
		case GROUPS_ALL_DELETE:
			if (db.isOpen()) {
				int count1 = db.delete(TABLE_GROUPS, "1", null);
				int count2 = db.delete(TABLE_THREAD_GROUP, "1", null);
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_URI, null);
				return (count1 + count2);
			}
			break;

		default:
			throw new IllegalArgumentException("unknown uri £º " + uri);
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case GROUPS_UPDATE:
			if(db.isOpen()) {
				int count = db.update(TABLE_GROUPS, values, selection, selectionArgs);
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_URI, null);
				return count;
			}
			break;

		default:
			throw new IllegalArgumentException("unknown uri £º " + uri);
		}
		return 0;
	}

}
