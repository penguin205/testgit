package com.example.mobileknow.ui;


import java.util.Random;

import com.example.mobileknow.R;
import com.example.mobileknow.entity.ChatMessage;
import com.example.mobileknow.speech.VoicePlayer;
import com.example.mobileknow.utils.DateUtil;
import com.example.mobileknow.utils.HttpUtils;
import com.example.mobileknow.utils.ImageCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
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
		TextView tvDate = (TextView) myView.findViewById(R.id.tv_item_outcoming_content);
		TextView tvDetail = (TextView) myView.findViewById(R.id.tv_item_outcoming_content);
		
		tvDate.setText(DateUtil.getCurrentTime());
		tvDetail.setText(result);
		
		mLayout.addView(myView);
		scrollToBottom();
	}
	
	public void upateSheView(ChatMessage result) {
		View view = InflateView.inflateSheView(mContext);
		TextView tvDate = (TextView) view.findViewById(R.id.tv_item_incoming_date);
		TextView tvContent = (TextView) view.findViewById(R.id.tv_item_incoming_content);
		TextView tvDetail = (TextView) view.findViewById(R.id.tv_item_incoming_detail);
		ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_item_incoming_icon);
		
		String dateStr = DateUtil.getSimpleDateFormat().format(result.getDate());
		tvDate.setText(dateStr);
		tvContent.setText(result.getContent());
		String name = PreferenceManager.getDefaultSharedPreferences(mContext).getString("voiceName", "xiaoyan");
		mPlayer.setVoiceName(name);
		mPlayer.play(result.getContent());
		
		if(result.getDetail() != null) {
			tvDetail.setVisibility(View.VISIBLE);
			tvDetail.setText(result.getDetail());
		} else {
			tvDetail.setVisibility(View.GONE);
		}
		if(result.getImageUrl() != null) {
			BitmapWorkerTask task = new BitmapWorkerTask(ivIcon);
			task.execute(result.getImageUrl());
		} else {
			ivIcon.setVisibility(View.GONE);
		}
		mLayout.addView(view);
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
	private ImageCache imageCache = new ImageCache();
	class BitmapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {

		private String imageUrl;
		private ImageView imageView;
		
		public BitmapWorkerTask(ImageView ivIcon) {
			imageView = ivIcon;
		}

		@Override
		protected BitmapDrawable doInBackground(String... params) {
			imageUrl = params[0];
			Bitmap bitmap = HttpUtils.downloadBitmap(imageUrl);
			BitmapDrawable drawable = null;
			if(bitmap != null) {
				drawable = new BitmapDrawable(mContext.getResources(), bitmap);
				imageCache.addBitmapToMemoryCache(imageUrl, drawable);
			}
			return drawable;
		}

		@Override
		protected void onPostExecute(BitmapDrawable drawable) {
			if(drawable != null) {
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageDrawable(drawable);
			}
		}
	}
	
}
