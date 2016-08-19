package com.android.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NumberSeekBar extends LinearLayout {

	private ImageView ivSub;
	private ImageView ivAdd;
	private TextView tvSubtitle;
	private SeekBar seekbar;
	private float rate;
	private OnNumberSeekBarChangeListener mListener;

	public NumberSeekBar(Context context) {
		this(context, null);
	}

	public NumberSeekBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NumberSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWidget(context);
	}

	public void setOnNumberSeekBarChangeListener(OnNumberSeekBarChangeListener listener) {
		this.mListener = listener;
	}
	
	private void initWidget(Context context) {
		LayoutInflater.from(context).inflate(R.layout.number_seekbar, this);

		ivSub = (ImageView) findViewById(R.id.item_iv_sub);
		ivAdd = (ImageView) findViewById(R.id.item_iv_add);
		tvSubtitle = (TextView) findViewById(R.id.item_tv_subtitle);
		seekbar = (SeekBar) findViewById(R.id.item_seekbar);

		ivAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				Log.d("ffff", "btn add on clicked");
				seekbar.setProgress(seekbar.getProgress() + 5);
				if(mListener != null) {
					mListener.callback(seekbar.getProgress() + 5);
				}
			}
		});

		ivSub.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				Log.d("ffff", "btn sub on clicked");
				seekbar.setProgress(seekbar.getProgress() - 5);
				if(mListener != null) {
					mListener.callback(seekbar.getProgress() - 5);
				}
			}
		});

		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				tvSubtitle.setVisibility(View.VISIBLE);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				if(mListener != null) {
					mListener.callback(progress);
				}
				// 让TextView跟着Thumb动态移动
				tvSubtitle.setVisibility(View.VISIBLE);
				rate = (float) progress / seekBar.getMax();
				seekBar.post(new Runnable() {

					@Override
					public void run() {
						tvSubtitle.setText((int) (rate * 100) + "%");

						tvSubtitle.setPadding(
								(int) ((tvSubtitle.getWidth() * rate + 10) - (tvSubtitle.getTextSize() * 3 * rate)), 
								tvSubtitle.getPaddingTop(), 
								tvSubtitle.getPaddingRight(), 
								tvSubtitle.getPaddingBottom());

						tvSubtitle.invalidate();
					}
				});

			}
		});
	}
	
	public void setAllEnabled(boolean b) {
		ivSub.setEnabled(b);
		ivAdd.setEnabled(b);
		seekbar.setEnabled(b);
	}

	public void setProgress(int progress) {
		seekbar.setProgress(progress);
	}
	
	public void setMax(int max) {
		seekbar.setMax(max);
	}
	
	public interface OnNumberSeekBarChangeListener {
		void callback(int progress);
	}
}
