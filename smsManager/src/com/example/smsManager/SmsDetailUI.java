package com.example.smsManager;

import com.example.smsManager.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SmsDetailUI extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_detail);
		
		setTitle("短信详情");
		initView();
	}

	@SuppressWarnings("deprecation")
	private void initView() {
		Intent intent = getIntent();
		int index = intent.getIntExtra("index", -1);
		long date = intent.getLongExtra("date", -1);
		String body = intent.getStringExtra("body");
		String address = intent.getStringExtra("address");
		
		ImageView ivIcon = (ImageView) findViewById(R.id.iv_sms_detail_icon);
		TextView tvAddress = (TextView) findViewById(R.id.tv_sms_detail_address);
		TextView tvName = (TextView) findViewById(R.id.tv_sms_detail_name);
		TextView tvType = (TextView) findViewById(R.id.tv_sms_detail_type);
		TextView tvDate = (TextView) findViewById(R.id.tv_sms_detail_date);
		TextView tvBody = (TextView) findViewById(R.id.tv_sms_detail_body);
		
		tvAddress.setText(address);
		tvBody.setText(body);
		String contactName = Utils.getContactName(getContentResolver(), address);
		if(TextUtils.isEmpty(contactName)) {
			tvName.setText("");
			ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
		} else {
			tvName.setText(contactName);
			Bitmap contactIcon = Utils.getContactIcon(getContentResolver(), address);
			if(contactIcon != null) {
				ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
			} else {
				ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
			}
		}
		
		String dateStr;
		if(DateUtils.isToday(date)) {
			dateStr = DateFormat.getTimeFormat(this).format(date);
		} else {
			dateStr = DateFormat.getDateFormat(this).format(date);
		}
		tvDate.setText(dateStr);
		
		switch (index) {
		case 0:
			tvType.setText("接收于：");
			break;
		case 1:
			tvType.setText("正在发送：");
			break;
		case 2:
			tvType.setText("发送于：");
			break;
		case 3:
			tvType.setText("存档于：");
			break;
			
		default:
			break;
		}
		
	}
}
