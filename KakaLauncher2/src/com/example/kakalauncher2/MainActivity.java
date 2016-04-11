package com.example.kakalauncher2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
/**
 * 主界面
 * @author Yongjun.Xiao
 *
 */
public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setText(MainActivity.class.getSimpleName());
		tv.setTextSize(35);
		setContentView(tv);
	}
}
