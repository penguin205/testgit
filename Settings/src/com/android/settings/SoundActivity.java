package com.android.settings;

import java.util.List;

import com.android.settings.R;

import android.os.Bundle;

public class SoundActivity extends Settings {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 invalidateHeaders();
		 
		 setActionBarTitle(R.string.sound_quality_settings);
	}


	@Override
	public void onBuildHeaders(List<Header> headers) {
		loadHeadersFromResource(R.xml.settings_headers_sounds,
				headers);
	}
}
