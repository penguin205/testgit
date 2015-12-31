package com.example.smsManager.utils;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.client.utils.URIUtils;

import android.R.string;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.util.Log;

public class Utils {

	private static String TAG = "Utils";

	public static void printCursor(Cursor cursor) {
		if(cursor != null && cursor.getCount() > 0) {
			String columnName;
			String columnValue;
			while(cursor.moveToNext()) {
				
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					columnName = cursor.getColumnName(i);
					columnValue = cursor.getString(i);
					Log.i(TAG, "µÚ" + cursor.getPosition() + "ÐÐ: " + columnName + " = " + columnValue);
				}
			}
			
		}
	}

	public static String getContactName(ContentResolver resolver, String address) {
		
		// content://com.android.contacts/phone_lookup/95556
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);
		
		Cursor cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			String contactName = cursor.getString(0);
			cursor.close();
			return contactName;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static Bitmap getContactIcon(ContentResolver resolver,
			String address) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);
		
		Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			Long id = cursor.getLong(0);
			cursor.close();
			
			uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
			InputStream is = Contacts.openContactPhotoInputStream(resolver, uri);
			return BitmapFactory.decodeStream(is);
		}
		return null;
	}

	public static void sendMessage(Context context, String address, String content) {
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> divideMsgList = smsManager.divideMessage(content);

		Intent intent = new Intent("com.example.smsManager.receiver.ReceiveSmsBroadCastReceiver");
		
		PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, 
				intent, PendingIntent.FLAG_ONE_SHOT);

		for(String message : divideMsgList) {
			smsManager.sendTextMessage(address, null, message, sentIntent, null);
		}
		
		writeMessage2Db(context, address, content);
	}

	private static void writeMessage2Db(Context context, String address,
			String content) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("address", address);
		contentValues.put("body", content);
		contentValues.put("type", Sms.SMS_SEND);
		context.getContentResolver().insert(Sms.SMS_URI, contentValues);
	}

	public static int getContactId(ContentResolver resolver, Uri uri) {
		Cursor cursor = resolver.query(uri, new String[]{"_id", "has_phone_number"},
				null, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			
			int hasPhoneNumber = cursor.getInt(1);
			if(hasPhoneNumber > 0) {
				int id = cursor.getInt(0);
				cursor.close();
				return id;
			}
		}
		return -1;
	}

	public static String getContactAddress(ContentResolver resolver, int id) {
		String []projection = {"data1"};
		String selection = "contact_id = ?";
		String []selectionArgs = {String.valueOf(id)};
		Cursor cursor = resolver.query(Phone.CONTENT_URI, projection, 
				selection, selectionArgs, null);
		if(cursor != null && cursor.moveToFirst()) {
			String address = cursor.getString(0);
			cursor.close();
			return address;
		}
		return null;
	}
	
	public static Uri getUriFromIndex(int i) {
		switch (i) {
		case 0:
			return Sms.SMS_INBOX;
		case 1:
			return Sms.SMS_OUTBOX;
		case 2:
			return Sms.SMS_SENT;
		case 3:
			return Sms.SMS_DRAFT;
		}
		return null;
	} 
}
