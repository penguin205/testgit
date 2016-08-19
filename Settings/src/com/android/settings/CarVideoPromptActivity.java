package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class CarVideoPromptActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_carvideo_forbid_prompt);
		Button btnPlay = (Button) findViewById(R.id.btn_carvideo_prompt_play);
		Button btnStop = (Button) findViewById(R.id.btn_carvideo_prompt_stop);
		btnPlay.setOnClickListener(this);
		btnStop.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btn_carvideo_prompt_play) {
			Intent it = getIntent();
			it.putExtra("result", false); //car video allow
			this.setResult(RESULT_OK, it);
			finish();
		} else if(v.getId() == R.id.btn_carvideo_prompt_stop) {
			Intent it = getIntent();
			it.putExtra("result", true); //car video forbid
			this.setResult(RESULT_OK, it);
			finish();
		}
	}
}
