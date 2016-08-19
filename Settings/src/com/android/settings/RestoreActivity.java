package com.android.settings;

import java.util.List;

import com.android.settings.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class RestoreActivity extends Settings {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 
		invalidateHeaders();
		 
		setActionBarTitle(R.string.restore_settings);
	}

	@Override
	public void onBuildHeaders(List<Header> headers) {
		loadHeadersFromResource(R.xml.settings_headers_restore,
				headers);
	}
}
