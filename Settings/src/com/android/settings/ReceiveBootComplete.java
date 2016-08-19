package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceiveBootComplete extends BroadcastReceiver{
	public static boolean isBootCompleted = false;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ReceiveBootComplete", "is receiver boot completed message");
		isBootCompleted = true;
		// Start Data usage monitor service add by XiaoYJ
		Intent sayHelloIntent = new Intent(context, DataUsageMonitoringService.class);
		context.startService(sayHelloIntent);
	}

}
