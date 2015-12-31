package com.example.smsManager;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.support.v4.app.NavUtils;

@SuppressLint("ParserError")
public class MainActivity extends TabActivity implements OnClickListener{

    private TabHost mTabHost;
	private View mSliderView;
	
	private int baseWidth = 0;
	private int startX = 0;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        initTabHost();
    }

	private void initTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mSliderView = findViewById(R.id.slider_view);
		
		final View rlConversation = findViewById(R.id.rl_conversation);
		final View llConversation = findViewById(R.id.ll_conversation);
		llConversation.setOnClickListener(this);
		findViewById(R.id.ll_folder).setOnClickListener(this);
		findViewById(R.id.ll_group).setOnClickListener(this);
		
		llConversation.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				llConversation.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				LayoutParams lp = (LayoutParams) mSliderView.getLayoutParams();
				lp.width = llConversation.getWidth();
				lp.height = llConversation.getHeight();
				lp.leftMargin = llConversation.getLeft();
				mSliderView.setLayoutParams(lp);
				
				baseWidth = rlConversation.getWidth();
			}
		});
		
		addTabSpec("conversation", "会话", R.drawable.tab_conversation, new Intent(this, ConversationUI.class));
		addTabSpec("folder", "文件夹", R.drawable.tab_folder, new Intent(this, FolderUI.class));
		addTabSpec("group", "群组", R.drawable.tab_group, new Intent(this, GroupUI.class));
	}

	private void addTabSpec(String tag, String label, int icon, Intent intent) {
		TabSpec newTabSpec = mTabHost.newTabSpec(tag);
		newTabSpec.setIndicator(label, getResources().getDrawable(icon));
		newTabSpec.setContent(intent);
		mTabHost.addTab(newTabSpec);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_conversation:
			if(!"conversation".equals(mTabHost.getCurrentTabTag())){
				mTabHost.setCurrentTabByTag("conversation");
				startTranslateAnimation(startX, 0);
				startX = 0;
			}
			break;
		case R.id.ll_folder:
			if(!"folder".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("folder");
				startTranslateAnimation(startX, baseWidth);
				startX = baseWidth;
			}
			break;
		case R.id.ll_group:
			if(!"group".equals(mTabHost.getCurrentTabTag())){
				mTabHost.setCurrentTabByTag("group");
				startTranslateAnimation(startX, baseWidth * 2);
				startX = baseWidth * 2;
			}
			break;

		default:
			break;
		}
	}
	
	private void startTranslateAnimation(int fromXDelta, int toXDelta) {
		TranslateAnimation animation = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
		animation.setDuration(500);
		animation.setFillAfter(true);
		mSliderView.startAnimation(animation);
	}

}
