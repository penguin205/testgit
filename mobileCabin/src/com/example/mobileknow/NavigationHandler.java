package com.example.mobileknow;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mobileknow.anim.ExpandAnimation;
import com.example.mobileknow.entity.ViewBean;
import com.example.mobileknow.utils.BroadcastHelper;
import com.example.mobileknow.utils.Constants;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class NavigationHandler {
	private Activity mActivity;
	private SlidingMenu mSlidingMenu;
	private List<ViewBean> viewList;
	private View lastViewTitle;
	private View lastViewLayout;
	private boolean bAnimState = false;

	
	public NavigationHandler(PageActivity activity, SlidingMenu slidingMenu) {
		mActivity = activity;
		mSlidingMenu = slidingMenu;
		
		initView();
	}

	private void initView() {
		viewList = new ArrayList<ViewBean>();
		//1.将所有的viewTitle和viewLayout封装成ViewBean，并且用list保存
		ViewBean bean = new ViewBean();
		View view = mActivity.findViewById(R.id.navigation_root_view);
		//初始化营业厅
		RelativeLayout businessHallTitle = (RelativeLayout) view.findViewById(R.id.find_business_hall_title);
		final View businessHallLayout = view.findViewById(R.id.find_business_hall_layout);
		bean.setViewTitle(businessHallTitle);
		bean.setViewLayout(businessHallLayout);
		viewList.add(bean);
		
		//初始化国际漫游
		RelativeLayout internationalRoamingTitle = (RelativeLayout) view.findViewById(R.id.international_roaming_title);
		final View internationalRoamingLayout = view.findViewById(R.id.international_roaming_layout);
		bean = new ViewBean();
		bean.setViewTitle(internationalRoamingTitle);
		bean.setViewLayout(internationalRoamingLayout);
		viewList.add(bean);
		
		//初始化查找手机
		RelativeLayout cellPhoneTitle = (RelativeLayout) view.findViewById(R.id.find_cellphone_title);
		final View cellPhoneLayout = view.findViewById(R.id.find_cellphone_layout);
		bean = new ViewBean();
		bean.setViewTitle(cellPhoneTitle);
		bean.setViewLayout(cellPhoneLayout);
		viewList.add(bean);
		
		//初始化找美女
		RelativeLayout findBeautyTitle = (RelativeLayout) view.findViewById(R.id.find_beauty_title);
		final View findBeautyLayout = view.findViewById(R.id.find_beauty_layout);
		bean = new ViewBean();
		bean.setViewTitle(findBeautyTitle);
		bean.setViewLayout(findBeautyLayout);
		viewList.add(bean);
		
		//初始化实时话费
		RelativeLayout telephoneChargeTitle = (RelativeLayout) view.findViewById(R.id.telephone_charge_title);
		final View telephoneChargeLayout = view.findViewById(R.id.telephone_charge_layout);
		bean = new ViewBean();
		bean.setViewTitle(telephoneChargeTitle);
		bean.setViewLayout(telephoneChargeLayout);
		viewList.add(bean);
		
		//初始化拨打客服电话
		RelativeLayout customerServicePhoneTitle = (RelativeLayout) view.findViewById(R.id.call_customer_service_phone_title);
		final View customerServicePhoneLayout = view.findViewById(R.id.call_customer_service_phone_layout);
		bean = new ViewBean();
		bean.setViewTitle(customerServicePhoneTitle);
		bean.setViewLayout(customerServicePhoneLayout);
		viewList.add(bean);
		
		//初始化唐诗
		RelativeLayout tangTitle = (RelativeLayout) view.findViewById(R.id.listen_tang_poetry_title);
		final View tangPoetryLayout = view.findViewById(R.id.tang_poetry_layout);
		bean = new ViewBean();
		bean.setViewTitle(tangTitle);
		bean.setViewLayout(tangPoetryLayout);
		viewList.add(bean);
		
		//初始化笑话
		RelativeLayout jokeTitle = (RelativeLayout) view.findViewById(R.id.say_joke_title);
		final View jokeLayout = view.findViewById(R.id.jokey_layout);
		bean = new ViewBean();
		bean.setViewTitle(jokeTitle);
		bean.setViewLayout(jokeLayout);
		viewList.add(bean);

		//初始化应用
		RelativeLayout appTitle = (RelativeLayout) view.findViewById(R.id.find_app_title);
		final View appLayout = view.findViewById(R.id.app_layout);
		bean = new ViewBean();
		bean.setViewTitle(appTitle);
		bean.setViewLayout(appLayout);
		viewList.add(bean);
		
		//初始化在线客服
		RelativeLayout onlineServiceTitle = (RelativeLayout) view.findViewById(R.id.online_servant_title);
		final View onlineServiceLayout = view.findViewById(R.id.online_servant_layout);
		bean = new ViewBean();
		bean.setViewTitle(onlineServiceTitle);
		bean.setViewLayout(onlineServiceLayout);
		viewList.add(bean);
		
		setAnimation(viewList);
	}

	private void setAnimation(List<ViewBean> viewList) {
		for(ViewBean bean : viewList) {
			final View viewTitle = bean.getViewTitle();
			final View viewLayout = bean.getViewLayout();
			viewTitle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					executeAnimation(viewTitle,viewLayout);
				}
			});
		}
		
		for(ViewBean bean : viewList) {
			LinearLayout viewLayout = (LinearLayout) bean.getViewLayout();
			int count = viewLayout.getChildCount();
			for(int i=0; i<count; i++) {
				viewLayout.getChildAt(i).setOnClickListener(viewLayoutOnClickListener);
			}
		}
		
	}
	private OnClickListener viewLayoutOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TextView tv = (TextView) v;
			String text = tv.getText().toString();
			String action = Constants.NAVIGATION_STRING_ACTION;
			String key = Constants.NAVIGATION_STRING_ACTION_KEY;
			BroadcastHelper.sendBroadCast(mActivity, action, key, text);
			mSlidingMenu.toggle();//关闭slidingmenu
		}
	};

	protected void executeAnimation(View viewTitle, View viewLayout) {
		ExpandAnimation anim = new ExpandAnimation(viewLayout, 500);
		
		RelativeLayout rlViewTitle = (RelativeLayout) viewTitle;
		ImageView ivHeader = (ImageView) rlViewTitle.getChildAt(0);
		LinearLayout llsublayout = (LinearLayout) rlViewTitle.getChildAt(1);
		TextView tvTitle = (TextView) llsublayout.getChildAt(0);
		ImageView ivArrow = (ImageView) rlViewTitle.getChildAt(2);
		
		boolean toggle = anim.toggle();
		if(!bAnimState ) {
			if (toggle) { // Layout不可见状态 变为可见状态
				viewTitle.setBackgroundResource(R.drawable.business_hall_selector_pressed);
				ivArrow.setImageResource(R.drawable.arrows_right_press);
				setPressed(ivHeader, tvTitle);
				
				if(lastViewTitle == null || lastViewLayout == null) {
					lastViewTitle = viewTitle;
					lastViewLayout = viewLayout;
				} else {
					executeAnimation(lastViewTitle, lastViewLayout);
					lastViewTitle = viewTitle;
					lastViewLayout = viewLayout;
				}
				
			} else {// Layout可见状态 变为不可见状态
				viewTitle.setBackgroundResource(R.drawable.business_hall_selector_normall);
				ivArrow.setImageResource(R.drawable.arrows_right_normal);
				setNormal(ivHeader, tvTitle);
				
				lastViewTitle = null;
				lastViewLayout = null;
			}
			viewLayout.startAnimation(anim);
			anim.setAnimationListener(mAnimationListener);
		}
	}

	private void setNormal(ImageView img, TextView tv) {
		String str = tv.getText().toString();
		if (!TextUtils.isEmpty(str)) {
			if (str.contains(mActivity.getString(R.string.business_hall))) {
				img.setImageResource(R.drawable.find_businesshall);
			} else if (str.contains(mActivity.getString(R.string.international_roaming))) {
				img.setImageResource(R.drawable.internation_roaming);
			} else if (str.contains(mActivity.getString(R.string.mobile_mobile))) {
				img.setImageResource(R.drawable.find_cellphone);
			} else if (str.contains(mActivity.getString(R.string.packge_name))) {
				img.setImageResource(R.drawable.mobile_package);
			} else if (str.contains(mActivity.getString(R.string.telephone_charge))) {
				img.setImageResource(R.drawable.query_fee);
			} else if (str.contains(mActivity.getString(R.string.service_phone_number))) {
				img.setImageResource(R.drawable.call_customer_service_telephone);
			} else if (str.contains(mActivity.getString(R.string.tang_poetry))) {
				img.setImageResource(R.drawable.tang_poetry);
			} else if (str.contains(mActivity.getString(R.string.joke))) {
				img.setImageResource(R.drawable.joke);
			} else if (str.contains(mActivity.getString(R.string.myapp))) {
				img.setImageResource(R.drawable.app_btn_normal);
			} else if (str.contains(mActivity.getString(R.string.mm))) {
				img.setImageResource(R.drawable.online_service_normal);
			}
		}
	}

	private void setPressed(ImageView img, TextView tv) {
		String str = tv.getText().toString();
		if (!TextUtils.isEmpty(str)) {
			if (str.contains(mActivity.getString(R.string.business_hall))) {//设置营业厅
				img.setImageResource(R.drawable.find_businesshall_press);
			} else if (str.contains(mActivity.getString(R.string.international_roaming))) {//国际漫游
				img.setImageResource(R.drawable.internation_roaming_press);
			} else if (str.contains(mActivity.getString(R.string.mobile_mobile))) {//手机
				img.setImageResource(R.drawable.find_cellphone_press);
			} else if (str.contains(mActivity.getString(R.string.packge_name))) {//套餐
				img.setImageResource(R.drawable.mobile_package_press);
			} else if (str.contains(mActivity.getString(R.string.telephone_charge))) {//查询余额
				img.setImageResource(R.drawable.query_fee_press);
			} else if (str.contains(mActivity.getString(R.string.service_phone_number))) {//客户电话
				img.setImageResource(R.drawable.call_customer_service_telephone_press);
			} else if (str.contains(mActivity.getString(R.string.tang_poetry))) {//背唐诗
				img.setImageResource(R.drawable.tang_poetry_press);
			} else if (str.contains(mActivity.getString(R.string.joke))) {//讲笑话
				img.setImageResource(R.drawable.joke_press);
			} else if (str.contains(mActivity.getString(R.string.myapp))) {//找应用
				img.setImageResource(R.drawable.app_btn_pressed);
			} else if (str.contains(mActivity.getString(R.string.mm))) {//找美女
				img.setImageResource(R.drawable.online_service_pressed);
			}
		}
	}
	
	private AnimationListener mAnimationListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			bAnimState = true;
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			bAnimState = false;
		}
	};

}
