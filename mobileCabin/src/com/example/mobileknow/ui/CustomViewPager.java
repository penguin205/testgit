package com.example.mobileknow.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
/**
 * 禁止左右滑动
 * @author Yongjun.Xiao
 *
 */
public class CustomViewPager extends ViewPager {
	
	private boolean setTouchModel = false;
	
	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomViewPager(Context context) {
		super(context);
	}
	

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (setTouchModel)
			return super.onInterceptTouchEvent(ev);
		else
			return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (setTouchModel)
			return super.onTouchEvent(ev);
		else
			return false;
	}

}
