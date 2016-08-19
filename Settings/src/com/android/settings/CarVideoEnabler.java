package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class CarVideoEnabler implements CompoundButton.OnCheckedChangeListener{
	
	private final String KEY_CARVIDEO_FORBID = "carvideo_forbid";
    
	private CheckBox mSwitch;
	private SharedPreferences mPreference;

	public CarVideoEnabler(Context context, CheckBox switch_) {
	     mSwitch = switch_;
	     mPreference = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void setSwitch(CheckBox switch_) {
		if (mSwitch == switch_) return;
		mSwitch.setOnCheckedChangeListener(null);
		mSwitch = switch_;
		mSwitch.setOnCheckedChangeListener(this);

		boolean isEnabled = mPreference.getBoolean(KEY_CARVIDEO_FORBID, true);
		mSwitch.setChecked(isEnabled);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		Log.d("ffff", "onCheckedChanged.....");
		mPreference.edit().putBoolean(KEY_CARVIDEO_FORBID, isChecked).commit();
	}
	
	
}
