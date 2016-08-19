package com.cngps.carvideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint("DrawAllocation")
public class MySoundControlView extends View {

	private Context mContext;
	private Bitmap bmFocused, bmNormal;
	private int bitmapWidth, bitmapHeight;
	private int index;
	private OnVolumeChangedListener mOnVolumeChangedListener;

	private final static int HEIGHT = 15;
	public final static int MY_HEIGHT = 165;
	public final static int MY_WIDTH = 44;

	public interface OnVolumeChangedListener {
		public void setYourVolume(int index);
	}

	public void setOnVolumeChangeListener(OnVolumeChangedListener l) {
		mOnVolumeChangedListener = l;
	}
	
	public MySoundControlView(Context context) {
		this(context, null);
	}
	
	public MySoundControlView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MySoundControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}


	private void init() {
		bmFocused = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.volume_highlight);
		bmNormal = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.volume_normal);
		bitmapWidth = bmFocused.getWidth();
		bitmapHeight = bmFocused.getHeight();
		// setIndex(5);
		AudioManager am = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		setIndex(am.getStreamVolume(AudioManager.STREAM_MUSIC));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int y = (int) event.getY();
		int n = y * 10 / MY_HEIGHT;
		setIndex(10 - n);
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		int reverseIndex = 10 - index;
		for (int i = 0; i != reverseIndex; ++i) {
			canvas.drawBitmap(bmNormal, new Rect(0, 0, bitmapWidth, bitmapHeight),
					new Rect(0, i * HEIGHT, bitmapWidth, i * HEIGHT
							+ bitmapHeight), null);
		}
		for (int i = reverseIndex; i != 10; ++i) {
			canvas.drawBitmap(bmFocused, new Rect(0, 0, bitmapWidth, bitmapHeight),
					new Rect(0, i * HEIGHT, bitmapWidth, i * HEIGHT
							+ bitmapHeight), null);
		}

		super.onDraw(canvas);
	}

	private void setIndex(int n) {
		if (n > 10) {
			n = 10;
		} else if (n < 0) {
			n = 0;
		}
		if (index != n) {
			index = n;
			if (mOnVolumeChangedListener != null) {
				mOnVolumeChangedListener.setYourVolume(n);
			}
		}
		invalidate();
	}

}
