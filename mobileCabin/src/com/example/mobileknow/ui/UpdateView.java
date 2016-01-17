package com.example.mobileknow.ui;


import java.util.Random;

import com.example.mobileknow.R;
import com.example.mobileknow.speech.VoicePlayer;
import com.example.mobileknow.utils.DateUtil;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class UpdateView {
	private Context mContext;
	private LinearLayout mLayout;
	private ScrollView mScrollView;
	private VoicePlayer mPlayer;

	public UpdateView(Context context, ScrollView scrollView, VoicePlayer voicePlayer) {
		mContext = context;
		mScrollView = scrollView;
		mLayout = (LinearLayout) mScrollView.findViewById(R.id.base_layout);
		mPlayer = voicePlayer;
	}

	public void updateMyView(String result) {
		View myView = InflateView.inflateMyView(mContext);
		TextView tvDate = (TextView) myView.findViewById(R.id.tv_my_say_item_date);
		TextView tvDetail = (TextView) myView.findViewById(R.id.tv_my_say_item_detail);
		
		tvDate.setText(DateUtil.getCurrentTime());
		tvDetail.setText(result);
		
		mLayout.addView(myView);
		scrollToBottom();
	}

	public void upateSheView(String result) {
		View sheView = InflateView.inflateSheView(mContext);
		TextView tvDate = (TextView) sheView.findViewById(R.id.tv_she_say_item_date);
		TextView tvDetail = (TextView) sheView.findViewById(R.id.tv_she_say_item_detail);
		
		tvDate.setText(DateUtil.getCurrentTime());
		tvDetail.setText(result);
		
		String name = PreferenceManager.getDefaultSharedPreferences(mContext).getString("voiceName", "xiaoyan");
		mPlayer.setVoiceName(name);
		mPlayer.play(result);
		sheView.setOnClickListener(sheOnclickListener);
		
		mLayout.addView(sheView);
		scrollToBottom();
	}

	private OnClickListener sheOnclickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TextView tvDetail = (TextView) v.findViewById(R.id.tv_she_say_item_detail);
			String result = tvDetail.getText().toString();
			String name = PreferenceManager.getDefaultSharedPreferences(mContext).getString("voiceName", "xiaoyan");
			mPlayer.setVoiceName(name);
			mPlayer.play(result);
		}
	};
	
	public void updateMmView(int resId) {
		View mmView = InflateView.inflateMmView(mContext);
		TextView tvMsg = (TextView) mmView.findViewById(R.id.tv_mm_say_item_msg);
		String[] msgArr = mContext.getResources().getStringArray(R.array.mm);
		Random random = new Random();
		int index = random.nextInt(msgArr.length);
		tvMsg.setText(msgArr[index]);
		String name = PreferenceManager.getDefaultSharedPreferences(mContext).getString("voiceName", "xiaoyan");
		mPlayer.setVoiceName(name);
		mPlayer.play(msgArr[index]);
		ImageView ivMM = (ImageView) mmView.findViewById(R.id.iv_mm_say_item_mm);
		ivMM.setImageResource(resId);
		
		mLayout.addView(mmView);
		scrollToBottom();
	}
	
	private void scrollToBottom() {
		mScrollView.post(new Runnable() {
			
			@Override
			public void run() {
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}
}
