package com.android.settings;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SystemUpdateSelectFragment extends SettingsPreferenceFragment implements OnClickListener {
	
	private final String KEY_TARGET_FILE_PATH = "key_target_file_path";
	private LinearLayout rootLinearLayout;
	
	private View mFirstPart;
	private TextView tvSystemVer;
	private TextView tvHardwareVer;
	private TextView tvKernelVer;
	private Button btnUpdateOnline;
	private Button btnUpdateOffline;
	private String hardwareVer = "04";
	private static final int UPDATE_MODE_OFFLINE = 0;
	private static final int UPDATE_MODE_ONLINE = 1;
	private int updateMode = UPDATE_MODE_OFFLINE;
	
	private View mSecondPart;
    private ListView mListView;
    private Handler mHandler = new Handler();
    private ProgressBar mPbSpinner;
	private TextView mTvSpinnerMessage;
	private BroadcastReceiver mStorageReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_system_update_select, null);
		rootLinearLayout = (LinearLayout) view.findViewById(R.id.root_linearLayout);
		mFirstPart = view.findViewById(R.id.first_part);
		mFirstPart.setVisibility(View.VISIBLE);
		tvSystemVer = (TextView) view.findViewById(R.id.tv_system_version_value);
		tvHardwareVer = (TextView) view.findViewById(R.id.tv_hardware_version_value);
		tvKernelVer = (TextView) view.findViewById(R.id.tv_kernel_version_value);
		btnUpdateOnline = (Button) view.findViewById(R.id.btn_update_online);
		btnUpdateOffline = (Button) view.findViewById(R.id.btn_update_offline);
		btnUpdateOnline.setOnClickListener(this);
		btnUpdateOffline.setOnClickListener(this);

		mSecondPart = view.findViewById(R.id.second_part);
		mSecondPart.setVisibility(View.GONE);
		mListView = (ListView) view.findViewById(R.id.update_file_listView);
		// add empty view
		View emptyView = inflater.inflate(R.layout.emptyview_system_update, null);
		mPbSpinner = (ProgressBar) emptyView.findViewById(R.id.progressbar);
		mTvSpinnerMessage = (TextView) emptyView.findViewById(R.id.message);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 
				ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		((ViewGroup)mListView.getParent()).addView(emptyView, params);
		mListView.setEmptyView(emptyView);
		// add footer view
		View footerView = inflater.inflate(R.layout.footer_common_button, mListView, false);
		Button btnFooter = (Button) footerView.findViewById(R.id.footer_button);
		btnFooter.setText(R.string.confirm_update);
		btnFooter.setOnClickListener(this);
		mListView.addFooterView(footerView);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setCustomActionBar();
	    setupListViewAndWallpaper();
	    
	    Bundle bundle = getArguments();
		if(bundle != null) {
    		hardwareVer  = bundle.getString("hardware_version");
    	}
		
		tvSystemVer.setText(Build.VERSION.INCREMENTAL);
		tvHardwareVer.setText(hardwareVer);
		tvKernelVer.setText(Build.VERSION.RELEASE);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mStorageReceiver != null) {
			getActivity().unregisterReceiver(mStorageReceiver);
			mStorageReceiver = null;
		}
	}
	
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
		actionBarTitle.setText(R.string.system_update);
		actionBar.setCustomView(actionbarLayout);
	}
	
    private void setupListViewAndWallpaper() {
    	mListView.setDivider(getActivity().getResources().getDrawable(R.drawable.line_bg));
    	mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		FrameLayout rootLayout = (FrameLayout) rootLinearLayout.getParent();
		rootLayout.setBackgroundResource(R.drawable.default_wallpaper);
  	}
    
	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_update_online:
			mFirstPart.setVisibility(View.GONE);
			mSecondPart.setVisibility(View.VISIBLE);
			mPbSpinner.setVisibility(View.GONE);
		    mTvSpinnerMessage.setText(R.string.prompt_checking_update);
		    
		    mHandler.post(mCheckNetUpdateFiles);
			
			updateMode = UPDATE_MODE_ONLINE;
			
			break;
		case R.id.btn_update_offline:
			mFirstPart.setVisibility(View.GONE);
			mSecondPart.setVisibility(View.VISIBLE);
		    mTvSpinnerMessage.setText(R.string.prompt_find_target_file_fail);
		    
		    mHandler.post(mScanLocalUpdateFiles);
		    
		    mStorageReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					mHandler.post(mScanLocalUpdateFiles);
				}
			};
			
		    // 注册U盘 SD卡热插拔事件监听器
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
			iFilter.addDataScheme("file");
			getActivity().registerReceiver(mStorageReceiver, iFilter);
		    
			updateMode = UPDATE_MODE_OFFLINE;
			break;
		case R.id.footer_button:
			String path = null;
			if(updateMode == UPDATE_MODE_OFFLINE) {
				HashMap<String, Object> map = (HashMap<String, Object>) mListView.getAdapter().getItem(
						mListView.getCheckedItemPosition());
				path = (String) map.get(KEY_TARGET_FILE_PATH);
			} else if(updateMode == UPDATE_MODE_ONLINE) {
				path = DOWN_URL;
				boolean bWifiConnect = Utils.isWifiConnected(getActivity());
				if(!bWifiConnect) {
					Toast.makeText(getActivity(), "下载更新将产生巨大流量，本机禁止在非WIFI情况下更新系统!", 1).show();
					return;
				}
			}
			Bundle datas = new Bundle();
			datas.putString("path", path);
			startFragment(this, ConfirmInstallFragment.class.getCanonicalName(), -1, datas);
			break;
		}
	}
	
	
	private Runnable mScanLocalUpdateFiles = new Runnable() {

		@Override
		public void run() {

			List<HashMap<String, Object>> updateFiles = getUpdateFiles();

			SimpleAdapter adapter = constructSimpleAdapter(getActivity(),
					R.layout.checkable_oneline_list_item, updateFiles);
			mListView.setAdapter(adapter);
			mListView.setItemChecked(0, true);
		}
	};
	
	// 测试 by XiaoYJ
	private final String DOWN_URL = "http://sw.bos.baidu.com/sw-search-sp/software/0a2a3945bde/jdk_8u91_windows_i586_8.0.910.15.exe";
	
	private Runnable mCheckNetUpdateFiles = new Runnable() {

		@Override
		public void run() {
			
			new AsyncTask<String, Void, Integer>() {

				@Override
				protected Integer doInBackground(String... params) {
					URL url = null;
					int fileSize = 0;
					try {
						url = new URL(params[0]);
						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection.setConnectTimeout(5000);
						connection.setRequestMethod("GET");
						fileSize = connection.getContentLength(); 
					} catch (Exception e) {
						e.printStackTrace();
					}
				
					return fileSize;
				}

				@Override
				protected void onPostExecute(Integer fileSize) {
					super.onPostExecute(fileSize);
					if(fileSize > 0 ) {
						List<HashMap<String, Object>> updateFiles  = new ArrayList<HashMap<String,Object>>();
						HashMap<String, Object> map = new HashMap<String, Object>();
						String[] strs = DOWN_URL.split("/");
						String fileName = strs[strs.length-1];
						fileName = fileName.substring(0, 32);
						map.put(KEY_TARGET_FILE_PATH, "可更新文件：    "  + fileName + "  大小：  " + Utils.convertToMB(fileSize) + "MB");
						updateFiles.add(map);
						SimpleAdapter adapter = constructSimpleAdapter(getActivity(),
								R.layout.checkable_oneline_list_item, updateFiles);
						mListView.setAdapter(adapter);
						mListView.setItemChecked(0, true);
					} else {
						mTvSpinnerMessage.setText(R.string.prompt_net_timeout);
					}
				}
			}.execute(DOWN_URL);
		}
		
	};
	
	
	private List<HashMap<String, Object>> getUpdateFiles() {
     	List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
    	HashMap<String, Object> map = null;
    	String[] rootList = {
				Environment.getExternalStorageDirectory().getPath(),
				"/mnt/media_rw/udisk", "/mnt/media_rw/extsd" };
    	
    	File extsd = new File(Environment.getExternalStorageDirectory().getPath());
    	int mSleepTimes = 40;
		while(mSleepTimes-- > 0) {
			Log.d("ffff", "waiting for sdcard ..");
			if (extsd.exists()) {
				break;
			}
		}
		
    	for(int i=0; i<rootList.length; i++) {
    		File rootDir = new File(rootList[i]);
    		if(rootDir.exists()) {
	    		File[] listFiles = rootDir.listFiles();
	    		for(File file : listFiles) {
	    			String fileName = file.getName();
	    			if(fileName.startsWith("sabresd_6dq-ota-") && fileName.endsWith(".zip")) {
	    				map = new HashMap<String, Object>();
	    				map.put(KEY_TARGET_FILE_PATH, file.getPath());
	    				Log.d("ffff", "find target file :" + file.getPath());
	    				list.add(map);
	    			}
	    		}
    		}
    	}
		return list;
	}
	
	private SimpleAdapter constructSimpleAdapter(Context context, int layoutId, List<HashMap<String, Object>> filesList) {
		String[] from = new String[] { KEY_TARGET_FILE_PATH };
		int[] to = new int[] { android.R.id.text1 };
		SimpleAdapter adapter = new SimpleAdapter(context,
					filesList, layoutId, from, to);
		return adapter;
	}
}
