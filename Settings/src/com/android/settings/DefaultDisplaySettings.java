package com.android.settings;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class DefaultDisplaySettings extends SettingsPreferenceFragment implements OnClickListener{
	
	private FrameLayout rootLayout;
	private View viewFirst;
	private Button btnPositive;
	private Button btnNegative;
	
	private View viewSecond;
	private View viewThird;
	private Button btnRestoreFinished;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_restore, null);
		
		rootLayout = (FrameLayout) view.findViewById(R.id.restore_rootlayout);
		
		viewFirst = view.findViewById(R.id.ll_restore_1st);
		TextView tvTitle = (TextView) view.findViewById(R.id.tv_restore_title);
		tvTitle.setText(R.string.prompt_confirm_restore_display);
		TextView tvSubTitle = (TextView) view.findViewById(R.id.tv_restore_subTitle);
		tvSubTitle.setVisibility(View.INVISIBLE);
		btnPositive = (Button) view.findViewById(R.id.btn_restore_positive);
		btnPositive.setOnClickListener(this);
		btnNegative = (Button) view.findViewById(R.id.btn_restore_negative);
		btnNegative.setOnClickListener(this);
		
		viewSecond = view.findViewById(R.id.ll_restore_2nd);
		TextView tvRestoreOngoing = (TextView) view.findViewById(R.id.tv_restore_ongoing);
		tvRestoreOngoing.setText(R.string.prompt_ongoing_restore_display);
		
		viewThird = view.findViewById(R.id.ll_restore_3rd);
		TextView tvRestoreFinished = (TextView) view.findViewById(R.id.tv_restore_finished);
		tvRestoreFinished.setText(R.string.prompt_finish_restore_display);
		btnRestoreFinished = (Button) view.findViewById(R.id.btn_restore_finished);
		btnRestoreFinished.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		  setCustomActionBar();
		  setWallpaper();
	}
	
	 private void setWallpaper() {
			if(rootLayout != null) {
				FrameLayout fl = (FrameLayout) rootLayout.getParent();
				fl.setBackgroundResource(R.drawable.default_wallpaper);
			}
	  	}
	    
		private void setCustomActionBar() {
			ActionBar actionBar = getActivity().getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayShowCustomEnabled(true);
			View actionbarLayout = LayoutInflater.from(getActivity()).inflate(
					R.layout.actionbar_common_layout, null);
			TextView actionBarTitle = (TextView) actionbarLayout.findViewById(R.id.tv_actionbar_title);
			actionBarTitle.setText(R.string.restore_default_settings);
			actionBar.setCustomView(actionbarLayout);
		}
		
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.btn_restore_negative || id == R.id.btn_restore_finished) {
			getActivity().onBackPressed();
			
		} else if(id == R.id.btn_restore_positive) {
			viewFirst.setVisibility(View.GONE);
			viewSecond.setVisibility(View.VISIBLE);
			// Restore process ...... need add
		}
	}

	
	
}
