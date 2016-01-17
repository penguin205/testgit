package com.example.mobileknow;

import java.util.ArrayList;
import java.util.List;

import com.example.mobileknow.adapter.ViewPagerAdapter;
import com.example.mobileknow.utils.Constants;
import com.example.mobileknow.utils.DensityUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class PageActivity extends Activity implements OnClickListener {
	private ViewPager pager;
	private LocalActivityManager lam;
	private SlidingMenu mSlidingMenu;
	
	private Button moreBtn;
	private LinearLayout subMenu;
	private Animation topDownAnim;
	private Animation topUpAnim;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pager_layout);
		
		initView();
		
		lam = new LocalActivityManager(this, true);
		lam.dispatchCreate(savedInstanceState);//必须被调用
		
		initActivities();
		
		initSliderMenu();
		
		registerBroadCast();
		
		topDownAnim = AnimationUtils.loadAnimation(this, R.anim.top_down);
		topUpAnim = AnimationUtils.loadAnimation(this, R.anim.top_up);
		topDownAnim.setAnimationListener(topDownAnimListener);
		topUpAnim.setAnimationListener(topUpAnimListener);
	}
	
	private void registerBroadCast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.SLIDING_MENU_ACTION);
		filter.addAction(Constants.IASK_ACTIVITY_ACTION);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(mPageBroadcastReceiver, filter);
	}
	
	private BroadcastReceiver mPageBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(Constants.SLIDING_MENU_ACTION.equals(action)) {
				mSlidingMenu.toggle();
			} else if(Constants.IASK_ACTIVITY_ACTION.equals(action)) {
				pager.setCurrentItem(1);
			}
		}};
	
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mPageBroadcastReceiver);
	}

	private AnimationListener topDownAnimListener = new AnimationListener() {
		
		@Override
		public void onAnimationEnd(Animation animation) {
			animation.setFillAfter(true);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getApplicationContext(),70));
			params.setMargins(0, DensityUtil.dip2px(getApplicationContext(), 50), 0, 0);
			subMenu.clearAnimation();
			subMenu.setLayoutParams(params);
		}
		
		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	};
	
	private AnimationListener topUpAnimListener = new AnimationListener() {
		
		@Override
		public void onAnimationEnd(Animation animation) {
			animation.setFillAfter(true);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getApplicationContext(),70));
			params.setMargins(0, 0, 0, 0);
			subMenu.clearAnimation();
			subMenu.setLayoutParams(params);
			subMenu.setVisibility(View.GONE);
		}
		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	};

	private void initView() {
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setOnPageChangeListener(mOnPageChangeListener);
		
		moreBtn = (Button) findViewById(R.id.more_btn);
		moreBtn.setOnClickListener(this);
		
		subMenu = (LinearLayout) findViewById(R.id.sub_menu);
		LinearLayout settingLayout = (LinearLayout) findViewById(R.id.setting_layout);
		settingLayout.setOnClickListener(this);
		LinearLayout loginLayout = (LinearLayout) findViewById(R.id.login_layout);
		loginLayout.setOnClickListener(this);
		LinearLayout helpLayout = (LinearLayout) findViewById(R.id.help_layout);
		helpLayout.setOnClickListener(this);
		LinearLayout updateLayout = (LinearLayout) findViewById(R.id.update_layout);
		updateLayout.setOnClickListener(this);
		
	}

	private void initSliderMenu() {
		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setMode(SlidingMenu.LEFT);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow);
		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mSlidingMenu.setMenu(R.layout.navigation_layout);
		
		new NavigationHandler(this, mSlidingMenu);
	}

	@SuppressWarnings("deprecation")
	private void initActivities() {
		List<View> viewList = new ArrayList<View>();
		
		Intent intent = new Intent(this, MainActivity.class);
		View mainView = lam.startActivity("MainActivity", intent).getDecorView();
		viewList.add(mainView);
		
		Intent intent2 = new Intent(this, IAskActivity.class);
		View iaskView = lam.startActivity("IAskActvity", intent2).getDecorView();
		viewList.add(iaskView);
		
		ViewPagerAdapter mAdapter = new ViewPagerAdapter(viewList);
		pager.setAdapter(mAdapter);
	}
	
	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int position) {
			switch (position) {
			case 0:
				mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
				break;
			case 1:
				mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				break;
			default:
				mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				break;
			}
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {}
	};
	
	
	private boolean animState = true;
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.more_btn:
			if(animState ) {
				animState = false;
				subMenu.setVisibility(View.VISIBLE);
				subMenu.startAnimation(topDownAnim);
			} else {
				animState = true;
				subMenu.startAnimation(topUpAnim);
			}
			break;
		case R.id.setting_layout:
			Intent it = new Intent(this, SettingActivity.class);
			startActivity(it);
			animState = true;
			subMenu.startAnimation(topUpAnim);
			break;
		case R.id.login_layout:
			Toast.makeText(this, "你点击了login", 0).show();
			animState = true;
			subMenu.startAnimation(topUpAnim);
			break;
		case R.id.help_layout:
			Toast.makeText(this, "你点击了help", 0).show();
			animState = true;
			subMenu.startAnimation(topUpAnim);
			break;
		case R.id.update_layout:
			Toast.makeText(this, "你点击了update", 0).show();
			animState = true;
			subMenu.startAnimation(topUpAnim);
			break;

		default:
			break;
		}
		
		
	}
}
