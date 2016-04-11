package com.example.kakalauncher2;

import java.util.ArrayList;
import java.util.List;

import com.example.kakalauncher2.adapter.BaseFragmentAdapter;
import com.example.kakalauncher2.fragment.LauncherBaseFragment;
import com.example.kakalauncher2.fragment.PrivateMessageLauncherFragment;
import com.example.kakalauncher2.fragment.RewardLauncherFragment;
import com.example.kakalauncher2.fragment.StereoscopicLauncherFragment;
import com.example.kakalauncher2.view.GuideViewPager;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class KakaLauncherActivity extends FragmentActivity {

	private ImageView tips[];
	private List<LauncherBaseFragment> list = new ArrayList<LauncherBaseFragment>();
	private GuideViewPager vPager;
	private BaseFragmentAdapter adapter;
	private int currentIndex;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kakalauncher);
		
		ViewGroup group = (ViewGroup) findViewById(R.id.viewpager);
		tips = new ImageView[3];
		for(int i=0; i<tips.length; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(10, 10));
			if(i == 0) {
				imageView.setBackgroundResource(R.drawable.page_indicator_focused);
			} else {
				imageView.setBackgroundResource(R.drawable.page_indicator_unfocused);
			}
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.leftMargin = 20;
			params.rightMargin = 20;
			
			tips[i] = imageView;
			group.addView(imageView, params);
			
		}
		
		vPager = (GuideViewPager) findViewById(R.id.viewpager_launcher);
		vPager.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.bg_kaka_launcher));
		
		
		RewardLauncherFragment rewardLauncherFragment = new RewardLauncherFragment();
		PrivateMessageLauncherFragment privateMessageLauncherFragment = new PrivateMessageLauncherFragment();
		StereoscopicLauncherFragment stereoscopicLauncherFragment = new StereoscopicLauncherFragment();
		list.add(rewardLauncherFragment);
		list.add(privateMessageLauncherFragment);
		list.add(stereoscopicLauncherFragment);
		
		adapter = new BaseFragmentAdapter(getSupportFragmentManager(), list);
		vPager.setAdapter(adapter);
		vPager.setCurrentItem(0);
		vPager.setOnPageChangeListener(mOnPageChangeListener);
		vPager.setOffscreenPageLimit(2);
	}
	
	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
		

		@Override
		public void onPageSelected(int index) {
			setImageBackground(index);
			//停止原来的动画
			list.get(currentIndex).stopAnimation();
			//开始新的动画
			LauncherBaseFragment fragment = list.get(index);
			fragment.startAnimation();
			
			currentIndex = index;
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	protected void setImageBackground(int index) {
		for(int i=0; i<tips.length; i++) {
			if(i == index) {
				tips[i].setBackgroundResource(R.drawable.page_indicator_focused);
			}else {
				tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
			}
		}
	}
}
