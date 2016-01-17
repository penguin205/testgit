package com.example.mobileknow;

import java.util.ArrayList;
import java.util.List;

import com.example.mobileknow.adapter.IAskFragmentAdapter;
import com.example.mobileknow.fragment.IAskAnswerFragment;
import com.example.mobileknow.fragment.IAskPersonCenterFragment;
import com.example.mobileknow.fragment.IAskQueryFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class IAskActivity extends FragmentActivity implements OnClickListener {
	private ViewPager mViewPager;
	private List<Fragment> fragmentList;
	private TextView tvQuery;
	private TextView tvAnswer;
	private TextView tvPersonCenter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iask);
		
		initView();
	}

	private void initView() {
		
		mViewPager = (ViewPager) findViewById(R.id.viewpager_iask);
		
		tvQuery = (TextView) findViewById(R.id.tv_iask_query);
		tvAnswer = (TextView) findViewById(R.id.tv_iask_answer);
		tvPersonCenter = (TextView) findViewById(R.id.tv_iask_person_center);
		
		mViewPager.setCurrentItem(0);
		setQueryTabFocused();
		
		tvQuery.setOnClickListener(this);
		tvAnswer.setOnClickListener(this);
		tvPersonCenter.setOnClickListener(this);
		
		fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new IAskQueryFragment());
		fragmentList.add(new IAskAnswerFragment());
		fragmentList.add(new IAskPersonCenterFragment());
		
		IAskFragmentAdapter mAdapter = new IAskFragmentAdapter(getSupportFragmentManager(), fragmentList);
		mViewPager.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_iask_query:
			mViewPager.setCurrentItem(0);
			setQueryTabFocused();
			break;
		case R.id.tv_iask_answer:
			mViewPager.setCurrentItem(1);
			setAnswerTabFocused();
			break;
		case R.id.tv_iask_person_center:
			mViewPager.setCurrentItem(2);
			setPersonCenterTabFocused();
			break;
		}
	}

	protected void setPersonCenterTabFocused() {
		tvPersonCenter.setBackgroundResource(R.drawable.iask_bottom_tab_pressed);
		tvQuery.setBackgroundResource(R.drawable.iask_bottom_tab_bg);
		tvAnswer.setBackgroundResource(R.drawable.iask_bottom_tab_bg);
	}

	protected void setAnswerTabFocused() {
		tvAnswer.setBackgroundResource(R.drawable.iask_bottom_tab_pressed);
		tvQuery.setBackgroundResource(R.drawable.iask_bottom_tab_bg);
		tvPersonCenter.setBackgroundResource(R.drawable.iask_bottom_tab_bg);
	}

	protected void setQueryTabFocused() {
		tvQuery.setBackgroundResource(R.drawable.iask_bottom_tab_pressed);
		tvAnswer.setBackgroundResource(R.drawable.iask_bottom_tab_bg);
		tvPersonCenter.setBackgroundResource(R.drawable.iask_bottom_tab_bg);
	}

}
