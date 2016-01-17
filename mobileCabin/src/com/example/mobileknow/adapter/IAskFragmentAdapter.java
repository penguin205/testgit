package com.example.mobileknow.adapter;

import java.util.List;

import com.example.mobileknow.fragment.IAskAnswerFragment;
import com.example.mobileknow.fragment.IAskPersonCenterFragment;
import com.example.mobileknow.fragment.IAskQueryFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class IAskFragmentAdapter extends FragmentPagerAdapter {

	private List<Fragment> mList;
	
	public IAskFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	public IAskFragmentAdapter(FragmentManager fm, List<Fragment> list) {
		super(fm);
		mList = list;
	}

	@Override
	public Fragment getItem(int position) {
		return mList.get(position);
	}

	@Override
	public int getCount() {
		return mList.size();
	}
}
