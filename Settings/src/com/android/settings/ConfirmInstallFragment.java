package com.android.settings;

import java.io.File;

import com.android.settings.download.DownloadUtil;
import com.android.settings.download.DownloadUtil.OnDownloadListener;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmInstallFragment extends SettingsPreferenceFragment implements OnClickListener {
	
	private FrameLayout mRootLayout;
	private TextView mTvSystemVer;
	private TextView mTvUpdateTime;
	private TextView mTvFileSize;
	private View firstPart;
	private View secondPart;
	private View mViewProgressBar;
	private ProgressBar mProgressBar;
	private TextView mTvProgress;
	private TextView mTvTotal;
	private Button mBtnStartInstall;
	
	private int max;
	private DownloadUtil mDownloadUtil;
	private BroadcastReceiver mConnectionReceiver;
	protected boolean flag = false;
 	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_confirm_install, null);
		mRootLayout = (FrameLayout) view.findViewById(R.id.root_layout);
		firstPart = view.findViewById(R.id.first_part);
		mTvSystemVer = (TextView) view.findViewById(R.id.tv_system_version);
		mTvUpdateTime = (TextView) view.findViewById(R.id.tv_update_time);
		mTvFileSize = (TextView) view.findViewById(R.id.tv_file_size);
		mViewProgressBar = view.findViewById(R.id.progressBarView);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
		mTvProgress = (TextView) view.findViewById(R.id.textView_progress);
		mTvTotal = (TextView) view.findViewById(R.id.textView_total);
		mBtnStartInstall = (Button) view.findViewById(R.id.btn_start_install);
		mBtnStartInstall.setOnClickListener(this);
		
		secondPart = view.findViewById(R.id.second_part);
		view.findViewById(R.id.btn_install_positive).setOnClickListener(this);
		view.findViewById(R.id.btn_install_negative).setOnClickListener(this);
		firstPart.setVisibility(View.VISIBLE);
		secondPart.setVisibility(View.GONE);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setCustomActionBar();
		setWallpaper();
		
		Bundle bundle = getArguments();
		if(bundle != null) {
    		String pathStr  = bundle.getString("path");
    		if(pathStr.startsWith("/mnt") || pathStr.startsWith("/storage")) {
        		File targetfile = new File(pathStr);
        		if(!targetfile.exists()) {
        			Toast.makeText(getActivity(), R.string.toast_file_no_exist, 1).show();
        			getActivity().onBackPressed();
        			return;
        		}
        		Log.d("ffff", "filepath: " + pathStr);
        		String[] strs = pathStr.split("/");
        		String fileName = strs[strs.length-1];
        		String ver = fileName.substring(16, 24);
        		mTvSystemVer.setText(ver);
        		mTvUpdateTime.setText(ver);
        		
        		if(targetfile.isFile()) {
        			mTvFileSize.setText(Utils.convertToMB(targetfile.length()) + "MB");
        		}
        		
        		mViewProgressBar.setVisibility(View.GONE);
        		mBtnStartInstall.setVisibility(View.VISIBLE);
        		
        		
    		} else if(pathStr.startsWith("http://") || pathStr.startsWith("https://")) {
    			//jdk_8u91_windows_i586_8.0.910.15.exe
    			String[] strs = pathStr.split("/");
				String fileName = strs[strs.length-1];
				String ver = fileName.substring(0, 32);
				
				mTvSystemVer.setText(ver);
        		mTvUpdateTime.setText("20150910");
        		
        		String localPath = Environment.getExternalStorageDirectory()
        				.getAbsolutePath() + "/local";
        		
        		mDownloadUtil = new DownloadUtil(2, localPath, "NV1504_11_update.zip", pathStr, getActivity());
        		
        		File targetFile = new File(localPath + "/NV1504_11_update.zip");
        		if(flag && targetFile.exists() ) {
        			mViewProgressBar.setVisibility(View.GONE);
        			mBtnStartInstall.setVisibility(View.VISIBLE);
        			return;
        		} else {
        			mViewProgressBar.setVisibility(View.VISIBLE);
        			mBtnStartInstall.setVisibility(View.GONE);
        		}
        		
        		mDownloadUtil.start();
        		
        		mDownloadUtil.setOnDownloadListener(new OnDownloadListener() {

        			@Override
        			public void downloadStart(int fileSize, int downloadedSize) {
        				Log.d("ffff", "fileSize::" + fileSize);
        				max = fileSize;
        				mProgressBar.setMax(fileSize); //设置 progressBar最大值
        				mTvFileSize.setText(Utils.convertToMB(fileSize) + "MB");
        				mTvTotal.setText(Utils.convertToMB(fileSize) + "MB");
        				mTvProgress.setText((int) (downloadedSize * 100.0f / max) + "%");
        			}

        			@Override
        			public void downloadProgress(int downloadedSize) {
        				Log.d("ffff", "Compelete::" + downloadedSize + " percent::" + (int) (downloadedSize * 100.0f / max) + "%");
        				mProgressBar.setProgress(downloadedSize);
        				mTvProgress.setText((int) (downloadedSize * 100.0f / max) + "%");
        			}

        			@Override
        			public void downloadEnd() {
        				Log.d("ffff", "ENd");
        				mViewProgressBar.setVisibility(View.GONE);
                		mBtnStartInstall.setVisibility(View.VISIBLE);
                		flag  = true;
        			}
        		});
        		
				registerBroadcastReceiver();
    		}
    	}
	}

	// Monitor wifi state change
	private void registerBroadcastReceiver() {
		mConnectionReceiver  = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if(!Utils.isWifiConnected(context)) {
					 Log.d("ffff", "wifi unconnect");
					 mDownloadUtil.pause();
				} else {
					mDownloadUtil.start();
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		// 注册监听网络状态改变的广播
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(mConnectionReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mConnectionReceiver != null) {
			getActivity().unregisterReceiver(mConnectionReceiver);
			mConnectionReceiver = null;
		}
		//mDownloadUtil.pause();
		Log.d("ffff", "ConfirmInstall onDestroy");
	}
	
	/**
     * set default wallpaper 
     */
    private void setWallpaper() {
    	FrameLayout fl = (FrameLayout)mRootLayout.getParent();
    	fl.setBackgroundResource(R.drawable.default_wallpaper);
  	}
    
    /**
     * set customed action bar
     */
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
		actionBarTitle.setText(R.string.system_install_package);
		actionBar.setCustomView(actionbarLayout);
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btn_start_install:
			// Show prompt dialog
			firstPart.setVisibility(View.GONE);
			secondPart.setVisibility(View.VISIBLE);
			break;
		case R.id.btn_install_positive:
			// Seriously install update
			Intent it = new Intent();   
    		it.setComponent(new ComponentName("com.fsl.android.ota", "com.fsl.android.ota.OtaConfirmActivity"));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getActivity().startActivity(it);
			break;
		case R.id.btn_install_negative:
			getActivity().onBackPressed();
			break;

		default:
			break;
		}
	}

}
