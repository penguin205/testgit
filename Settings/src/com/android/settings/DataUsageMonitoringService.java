package com.android.settings;

import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
/**
 * Data usage monitor service , stared by BOOT_COMPLETED broadcast
 * @author Yongjun.Xiao
 *
 */
public class DataUsageMonitoringService extends Service {

	private DatabaseAdapter dbAdapter;
	private Handler handler = new Handler();
	private long mobileRx = 0, mobileTx = 0, totalRx = 0, totalTx = 0,
			wifiRx = 0, wifiTx = 0;
	private long old_mobileRx = 0, old_mobileTx = 0, old_wifiRx = 0,
			old_wifiTx = 0;
	private long mrxUnit = 0, mtxUnit = 0, wrxUnit = 0, wtxUnit = 0;
	private long mobileRx_all = 0, mobileTx_all = 0, wifiRx_all = 0,
			wifiTx_all = 0;
	
	static int count = 12;
	NetworkInfo nwi;

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		// 计算3G网络接收、发送数据总量
		old_mobileRx = TrafficStats.getMobileRxBytes();
		old_mobileTx = TrafficStats.getMobileTxBytes();

		// 获取全部网络接收、发送数据总量
		totalRx = TrafficStats.getTotalRxBytes();
		totalTx = TrafficStats.getTotalTxBytes();

		// 计算WiFi网络接收、发送数据总量
//		old_wifiRx = totalRx - old_mobileRx;
//		old_wifiTx = totalTx - old_mobileTx;

		handler.post(thread);

		super.onCreate();

	}

	Runnable thread = new Runnable() {

		public void run() {
			dbAdapter = new DatabaseAdapter(DataUsageMonitoringService.this);
			dbAdapter.open();

			// 截至启动机器
			// 获取移动网络接收、发送数据总量，单位为byte，以下同上
			mobileRx = TrafficStats.getMobileRxBytes();
			mobileTx = TrafficStats.getMobileTxBytes();

			// 获取全部网络接收、发送数据总量
			totalRx = TrafficStats.getTotalRxBytes();
			totalTx = TrafficStats.getTotalTxBytes();

			// 计算WiFi网络接收、发送数据总量
//			wifiRx = totalRx - mobileRx;
//			wifiTx = totalTx - mobileTx;

			if (mobileRx == -1 && mobileTx == -1) {
			} else {
				mrxUnit = (mobileRx - old_mobileRx); // 得到瞬时GPRS流量
				old_mobileRx = mobileRx;
				mtxUnit = (mobileTx - old_mobileTx); // 得到瞬时GPRS流量
				old_mobileTx = mobileTx;

				mrxUnit = (long) ((float) (Math.round(mrxUnit * 100.0)) / 100);
				mtxUnit = (long) ((float) (Math.round(mtxUnit * 100.0)) / 100);

			}
//			if (wifiRx == -1 && wifiTx == -1) {
//			} else {
//				wrxUnit = (wifiRx - old_wifiRx); // 得到瞬时wifi流量
//				old_wifiRx = wifiRx;
//				wtxUnit = (wifiTx - old_wifiTx); // 得到瞬时wifi流量
//				old_wifiTx = wifiTx;
//				wrxUnit = (long) ((float) (Math.round(wrxUnit * 100.0)) / 100);// 保留两位小数
//				wtxUnit = (long) ((float) (Math.round(wtxUnit * 100.0)) / 100);
//			}
			Date date = new Date();
			mobileRx_all += mrxUnit; // 求同一天的数据之和
			mobileTx_all += mtxUnit; // 求同一天的数据之和
//			wifiTx_all += wtxUnit; // 求同一天的数据之和
//			wifiRx_all += wrxUnit; // 求同一天的数据之和
			
			if (count == 12) {
				// 如果存在该天GPRS流量的记录则跟新本条记录
				if (mobileTx_all != 0 || mobileRx_all != 0) {
					Cursor checkMobile = dbAdapter.check(DatabaseAdapter.NET_TYPE_3G, date);
					if (checkMobile.moveToNext()) {
						long up = dbAdapter.getProFlowUp(DatabaseAdapter.NET_TYPE_3G, date);
						long dw = dbAdapter.getProFlowDw(DatabaseAdapter.NET_TYPE_3G, date);
						mobileTx_all += up;
						mobileRx_all += dw;
						dbAdapter.updateData(mobileTx_all, mobileRx_all, DatabaseAdapter.NET_TYPE_3G,
								date);
						Log.d("ffff", "update GPRS data usage Tx :"
										+ mobileTx_all + " Rx :"
										+ mobileRx_all);
						mobileTx_all = 0;
						mobileRx_all = 0;

					}
					if (!checkMobile.moveToNext()) {

						dbAdapter.insertData(mobileTx_all, mobileRx_all, DatabaseAdapter.NET_TYPE_3G,
								date);
						Log.d("ffff", "insert GPRS data usage Tx :"
										+ mobileTx_all + " Rx : "
										+ mobileRx_all);

					}

				}
//				if (wifiTx_all != 0 || wifiRx_all != 0) {
//					Cursor checkWifi = dbAdapter.check(DatabaseAdapter.NET_TYPE_WIFI, date);// 0为 wifi流量类型
//					long up = dbAdapter.getProFlowUp(DatabaseAdapter.NET_TYPE_WIFI, date);
//					long dw = dbAdapter.getProFlowDw(DatabaseAdapter.NET_TYPE_WIFI, date);
//					if (checkWifi.moveToNext()) {
//						wifiTx_all += up;
//						wifiRx_all += dw;
//						dbAdapter.updateData(wifiTx_all, wifiRx_all, 0, date);
//						System.out
//								.println("geng xin le WIFI liu liang sahngxing"
//										+ wifiTx_all + "xia xing" + wifiRx_all);
//						wifiTx_all = 0;
//						wifiRx_all = 0;
//
//					} else {
//
//						dbAdapter.insertData(wifiTx_all, wifiRx_all, 0, date);
//						System.out
//								.println("cun chu le WIFI liu liang sahngxing"
//										+ wifiTx_all + "xia xing" + wifiRx_all);
//
//					}
//				}
				count = 1;
			}
			count++;
			dbAdapter.close();
			handler.postDelayed(thread, 500);
		}

	};

	public int onStartCommand(Intent intent, int flags, int startId) {
		handler.post(thread);
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(thread);
		Log.v("ffff", "on destroy");
	}
}
