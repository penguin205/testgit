package com.example.mobileknow.utils;

import android.content.Context;
import android.content.Intent;

public class BroadcastHelper {

	public static void sendBroadCast(Context ctx, String action, String key, String value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(key, value);
		ctx.sendBroadcast(intent);
	}
	
	public static void sendBroadCast(Context ctx, String action, String key, int value) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(key, value);
		ctx.sendBroadcast(intent);
	}
}
