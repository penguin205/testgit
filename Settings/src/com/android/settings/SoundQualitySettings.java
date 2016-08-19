package com.android.settings;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SoundQualitySettings extends SettingsPreferenceFragment {

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		 addPreferencesFromResource(R.xml.sound_quality_settings);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        setCustomActionBar();
    	setupListView();
	}
	
    private void setupListView() {
		ListView listView = getListView();
		if(listView != null) {
			listView.setDivider(getActivity().getResources().getDrawable(R.drawable.line_bg));
			LinearLayout rootLayout = (LinearLayout) listView.getParent();
			rootLayout.setBackgroundResource(R.drawable.default_wallpaper);
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
		actionBarTitle.setText(R.string.sound_quality_settings);
		actionBar.setCustomView(actionbarLayout);
	}
}
