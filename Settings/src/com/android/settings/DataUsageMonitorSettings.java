package com.android.settings;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DataUsageMonitorSettings extends SettingsPreferenceFragment {
	
	private Calendar currentCa;
	private DatabaseAdapter dbAdapter;

	private RelativeLayout rootRelativeLayout;
	private TextView remain3G;
	private TextView month3G;
	private TextView today3G;
	private TextView limit;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_data_usage_monitor, null);
		rootRelativeLayout = (RelativeLayout) view.findViewById(R.id.root_relativeLayout);
		remain3G = (TextView) view.findViewById(R.id.tv_remains_this_month);
		month3G = (TextView) view.findViewById(R.id.tv_consumes_this_month);
		today3G = (TextView) view.findViewById(R.id.tv_consumes_this_day);
		limit = (TextView) view.findViewById(R.id.tv_totals_this_month);  //本月限制3G流量
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setCustomActionBar();
		setWallpaper();
		
		dbAdapter = new DatabaseAdapter(getActivity());
		dbAdapter.open();

		handler.post(runnable);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		
		public void run() {
			this.update();
			handler.postDelayed(this, 1000 * 3);
		}

		void update() {
			currentCa = Calendar.getInstance();
			int year = currentCa.get(Calendar.YEAR);
			int month = currentCa.get(Calendar.MONTH) + 1;
			int day = currentCa.get(Calendar.DATE);

			String month3GTraffic;
			String day3GTraffic;
//			String dayWIFITraffic;
//			String monthWIFITraffic;

//			Date date = new Date();
			// 显示本月已用3G流量
			month3GTraffic = Utils.convertToMB(dbAdapter
					.calculateForMonth(year, month, DatabaseAdapter.NET_TYPE_3G));
			month3G.setText(month3GTraffic);

			// 本日已用3G流量;从数据库中获取
			day3GTraffic = Utils.convertToMB(dbAdapter
					.calculate(year, month, day, DatabaseAdapter.NET_TYPE_3G));
			today3G.setText(day3GTraffic);

//			// 本日已用WIFI流量;从数据库中获取
//			dayWIFITraffic = convertTraffic(dbAdapter
//					.calculate(year, month, day, DatabaseAdapter.NET_TYPE_WIFI));
//			todayWifi.setText(dayWIFITraffic);
//
//			// 本月已用WIFI流量；从数据库中获取
//			monthWIFITraffic = convertTraffic(dbAdapter
//					.calculateForMonth(year, month, DatabaseAdapter.NET_TYPE_WIFI));
//			monthWifi.setText(monthWIFITraffic);

			// 显示流量限额
			try {
				Parameter par = new Parameter(getActivity());
				String parLimit = par.getParameter("mLimit");
				if (parLimit.equals("")) {
					parLimit = "500";
					limit.setText("500"); // 500MB
				} else {
					limit.setText(parLimit); 
				}

				// 剩余3G流量
				double iLimit;
				if (parLimit.equals("")) {
					iLimit = 100.0;
				} else {
					iLimit = Double.valueOf(parLimit);
				}
				double remain = iLimit - Double.valueOf(month3GTraffic);
				remain3G.setText(String.valueOf(remain));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};
	
    private void setWallpaper() {
		FrameLayout rootLayout = (FrameLayout) rootRelativeLayout.getParent();
		rootLayout.setBackgroundResource(R.drawable.default_wallpaper_data_usage);
  	}
	
	private void setCustomActionBar() {
    	ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		View actionbarLayout = LayoutInflater.from(getActivity()).inflate(
				R.layout.actionbar_data_usage_layout, null);
		TextView actionBarTitle = (TextView) actionbarLayout.findViewById(R.id.tv_actionbar_title);
		actionBarTitle.setText(R.string.flow_monitor);
		ImageView actionBarEdit = (ImageView) actionbarLayout.findViewById(R.id.iv_actionbar_edit);
		actionBarEdit.setOnClickListener(mOnClickListener);
		actionBar.setCustomView(actionbarLayout);
	}
	
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startFragment(DataUsageMonitorSettings.this, DataUsageLimitSettings.class.getCanonicalName(), -1, null);
		}};
}
