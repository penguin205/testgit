package com.example.mobileknow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class WelcomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		View view = findViewById(R.id.welcome_layout);
		AlphaAnimation mAnimation = new AlphaAnimation(0.1f, 1.0f);
		mAnimation.setDuration(2000);
		view.startAnimation(mAnimation);
		
		mAnimation.setAnimationListener(mAnimationListener);
	}
	
	private AnimationListener mAnimationListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			Intent intent = new Intent(WelcomeActivity.this, PageActivity.class);
			startActivity(intent);
			WelcomeActivity.this.finish();
		}
	};
}
