/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import com.android.settings.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {
	
	public static final String LOG_TAG = "MainActivity";
	
    private static final String EXTRA_UI_OPTIONS = "settings:ui_options";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().hasExtra(EXTRA_UI_OPTIONS)) {
            getWindow().setUiOptions(getIntent().getIntExtra(EXTRA_UI_OPTIONS, 0));
        }

        super.onCreate(savedInstanceState);

        setTitle(R.string.settings_label); 
        setContentView(R.layout.layout_main);
        findViewById(R.id.general_setting).setOnClickListener(this);
        findViewById(R.id.net_setting).setOnClickListener(this);
        findViewById(R.id.system_setting).setOnClickListener(this);
        findViewById(R.id.sound_setting).setOnClickListener(this);
        findViewById(R.id.time_setting).setOnClickListener(this);
        findViewById(R.id.restore_setting).setOnClickListener(this);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.general_setting:
			startTabActivity(GeneralActivity.class);
			break;
		case R.id.net_setting:
			startTabActivity(NetworkActivity.class);
			break;
		case R.id.system_setting:
			startTabActivity(SystemActivity.class);
			break;
		case R.id.sound_setting:
			startTabActivity(SoundActivity.class);
			break;
		case R.id.time_setting:
			startTabActivity(TimeActivity.class);
			break;
		case R.id.restore_setting:
			startTabActivity(RestoreActivity.class);
			break;
		default:
			break;
		}
	}
	
    private void startTabActivity(Class clss) {
    	Intent it = new Intent(this, clss);
		startActivity(it);
	}

}
