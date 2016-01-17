package com.example.mobileknow.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class ViewPagerAdapter extends PagerAdapter {

	private List<View> mList;
	
	public ViewPagerAdapter(List<View> mList) {
		this.mList = mList;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
		ViewPager viewPager = (ViewPager) container;
		View view = mList.get(position);
		viewPager.removeView(view);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ViewPager viewPager = (ViewPager) container;
		View view = mList.get(position);
		viewPager.addView(view);
		return view;
	}

	
}
