package com.example.mobileknow;


import com.example.mobileknow.recognizer.VoiceRecognizer;
import com.example.mobileknow.server.IAskAsyncTask;
import com.example.mobileknow.speech.VoicePlayer;
import com.example.mobileknow.speech.VoicePlayerImpl;
import com.example.mobileknow.ui.UpdateView;
import com.example.mobileknow.utils.BroadcastHelper;
import com.example.mobileknow.utils.Constants;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MainActivity extends Activity implements OnClickListener {

	private View netLayout;
	private ScrollView scrollView;
	private ConnectivityManager cm;
	private Button switch_text_voice_layout;
	private Button sendbtn_or_to_iask;
	private LinearLayout voice_edittext_switch_layout;
	private Button voice_start;
	private EditText text_start;
	private Button back_2_navigation_or_to_voice;
	private UpdateView updateView;
	private IAskAsyncTask iaskAsyncTask;
	private VoiceRecognizer recognizer;
	private boolean bVoiceMode = true;
	private VoicePlayer player; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		
		checkNet();
		
		registerBroadCast();
		
		initClass();
	}

	private void initClass() {
		player = new VoicePlayerImpl(this);
		updateView = new UpdateView(this, scrollView, player);
		iaskAsyncTask = new IAskAsyncTask(this, updateView);
		recognizer = new VoiceRecognizer(this);
	}

	private void registerBroadCast() {
		IntentFilter intentFilter = new IntentFilter();
		// 注册监听网络状态改变的广播
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(Constants.NAVIGATION_STRING_ACTION);
		intentFilter.addAction(Constants.VIEWPAGE_DESTROY_ACTION);
		intentFilter.addAction(Constants.VOICERECOGNIZER_STRING_ACTION);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(mainReceiver, intentFilter);
	}

	private void checkNet() {
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if(info != null && info.isAvailable() && info.isConnected()) {
			netLayout.setVisibility(View.GONE);
		} else {
			netLayout.setVisibility(View.VISIBLE);
		}
	}

	private void initView() {
		netLayout = findViewById(R.id.warning_layout);
		netLayout.setOnClickListener(this);
		
		back_2_navigation_or_to_voice = (Button) findViewById(R.id.back_2_navigation_or_to_voice);
		switch_text_voice_layout = (Button) findViewById(R.id.switch_text_voice_layout);
		sendbtn_or_to_iask = (Button) findViewById(R.id.sendbtn_or_to_iask);
		voice_edittext_switch_layout = (LinearLayout) findViewById(R.id.voice_edittext_switch_layout);
		voice_start = (Button) findViewById(R.id.voice_start);
		text_start = (EditText) findViewById(R.id.text_start);
		
		back_2_navigation_or_to_voice.setOnClickListener(this);
		switch_text_voice_layout.setOnClickListener(this);
		sendbtn_or_to_iask.setOnClickListener(this);
		voice_start.setOnClickListener(this);
		
		scrollView = (ScrollView) findViewById(R.id.scolllayout);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.warning_layout:
			Intent it = new Intent(android.provider.Settings.ACTION_SETTINGS);
	        startActivity(it);
			break;
		case R.id.back_2_navigation_or_to_voice:
			if(bVoiceMode) {
				BroadcastHelper.sendBroadCast(getApplicationContext(), Constants.SLIDING_MENU_ACTION, null, null);
			} else {
				back_2_navigation_or_to_voice.setBackgroundResource(R.drawable.introduction_btn_bg);
				text_start.setVisibility(View.GONE);
				voice_start.setVisibility(View.VISIBLE);
				sendbtn_or_to_iask.setBackgroundResource(R.drawable.iask_bottom_btn_bg);
				switch_text_voice_layout.setVisibility(View.VISIBLE);
				bVoiceMode = true;
			}
			break;
		case R.id.switch_text_voice_layout:
			if(bVoiceMode) {
				back_2_navigation_or_to_voice.setBackgroundResource(R.drawable.voice_btn_bg);
				text_start.setVisibility(View.VISIBLE);
				voice_start.setVisibility(View.GONE);
				sendbtn_or_to_iask.setBackgroundResource(R.drawable.send_btn_bg2);
				switch_text_voice_layout.setVisibility(View.GONE);
				bVoiceMode = false;
			} else {
				back_2_navigation_or_to_voice.setBackgroundResource(R.drawable.introduction_btn_bg);
				text_start.setVisibility(View.GONE);
				voice_start.setVisibility(View.VISIBLE);
				sendbtn_or_to_iask.setBackgroundResource(R.drawable.iask_bottom_btn_bg);
				switch_text_voice_layout.setVisibility(View.VISIBLE);
				bVoiceMode = true;
			}
			break;
		case R.id.sendbtn_or_to_iask:
			if(bVoiceMode) { //跳转到爱问
				BroadcastHelper.sendBroadCast(getApplicationContext(), Constants.IASK_ACTIVITY_ACTION, null, null);
			} else {
				String text = text_start.getText().toString();
				if(text != null) {
					updateView.updateMyView(text);
					iaskAsyncTask.queryAnswer(text);
					text_start.setText("");
				}
			}
			break;
		case R.id.voice_start:
			recognizer.start();
			break;
		default:
			break;
		}

    }
	
	private BroadcastReceiver mainReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				NetworkInfo info = cm.getActiveNetworkInfo();
				if (info != null && info.isAvailable() && info.isConnected()) {
					//网络是可用的
					netLayout.setVisibility(View.GONE);
				} else {
					//当前网络不可用
					netLayout.setVisibility(View.VISIBLE);
				}
			} else if(action.equals(Constants.NAVIGATION_STRING_ACTION)) {
				String result = intent.getStringExtra(Constants.NAVIGATION_STRING_ACTION_KEY);
				updateView.updateMyView(result);
				iaskAsyncTask.queryAnswer(result);
			} else if(action.equals(Constants.VOICERECOGNIZER_STRING_ACTION)) {
				String result = intent.getStringExtra(Constants.VOICERECOGNIZER_STRING_ACTION_KEY);
				updateView.updateMyView(result);
				iaskAsyncTask.queryAnswer(result);
			} else if(action.equals(Constants.VIEWPAGE_DESTROY_ACTION)) {
				unregisterReceiver(this);
			}
		}
	};

	
}
