package com.android.settings;

import java.util.List;

import com.android.settings.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.TextView;

public class NetworkActivity extends Settings {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 invalidateHeaders();
		 
		 setActionBarTitle(R.string.network_settings);
	}


	@Override
	public void onBuildHeaders(List<Header> headers) {
		loadHeadersFromResource(R.xml.settings_headers_networks,
				headers);
	}
}
