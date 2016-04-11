package com.example.kakalauncher2.adapter;

import java.util.List;

import com.example.kakalauncher2.fragment.LauncherBaseFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class BaseFragmentAdapter extends FragmentStatePagerAdapter{
	
	private List<LauncherBaseFragment> list;
	
	public BaseFragmentAdapter(FragmentManager fm, List<LauncherBaseFragment> list) {
		super(fm);
		this.list = list;
	}

	@Override
	public Fragment getItem(int position) {
		return list.get(position);
	}

	@Override
	public int getCount() {
		return list.size();
	}


}
