package com.android.settings.wifi;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.wifi.db.WifiDbOperator;


public class ConnectedWifiDetailFragment extends Fragment implements OnClickListener, OnItemClickListener{

	private final String KEY_TITLE = "key_tile";
	private final String KEY_STATE = "key_state";
	
    private WifiManager mWifiManager;
    private AccessPoint mAccessPoint;
	private ListView mListView;
	private CheckBox mSwitch;
	private Button mBtnIgore;
	private WifiDbOperator mDbOperator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
    	mDbOperator = new WifiDbOperator(getActivity());
	}
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_connected_wifi_detail, null);
		mListView = (ListView) view.findViewById(R.id.sound_volume_listView);
		
		View headerView = inflater.inflate(R.layout.header_connected_wifi_detail, mListView, false);
		mListView.setAdapter(null);
		mListView.addHeaderView(headerView);
		mSwitch = (CheckBox) headerView.findViewById(R.id.header_connected_wifi_checkbox);
		
		View footerView = inflater.inflate(R.layout.footer_common_button, mListView, false);
		mListView.addFooterView(footerView);
		mBtnIgore = (Button) footerView.findViewById(R.id.footer_button);
		mBtnIgore.setBackgroundResource(R.drawable.btn_bg_igore);
		mBtnIgore.setText(R.string.igore_net);
		mBtnIgore.setOnClickListener(this);
		return view;
    }
    
    
	public void onActivityCreated(Bundle savedInstanseState) {
        super.onActivityCreated(savedInstanseState);
    	
        setCustomActionBar();
    	setupListView();
    	 
    	Bundle bundle = getArguments();
    	if(bundle != null) {
    		WifiConfiguration config = bundle.getParcelable("config");
    		mAccessPoint = new AccessPoint(getActivity(), config);
    		mAccessPoint.update(mWifiManager.getConnectionInfo(), DetailedState.CONNECTED);
    		
    		SimpleAdapter WifiDetailAdapter = constructWifiDetailAdapter(getActivity());
        	mListView.setAdapter(WifiDetailAdapter);
        	mListView.setOnItemClickListener(this);
        	
        	boolean isBan = mDbOperator.querySsidIsExist(mAccessPoint.ssid);
    		mSwitch.setChecked(!isBan);
    	}
    	
    }
    
	private SimpleAdapter constructWifiDetailAdapter(Context context) {
		 return constructWifiDetailAdapter(context,
	                R.layout.connected_wifi_detail_list_item);
	}


    public SimpleAdapter constructWifiDetailAdapter(Context context, int layoutId) {
        final String[] from = new String[] {KEY_TITLE, KEY_STATE};
        final int[] to = new int[] {R.id.text1, R.id.text2};

        final List<HashMap<String, Object>> detailList = getWifiDetailList(getActivity());
        
        final SimpleAdapter adapter = new SimpleAdapter(context,
        		detailList,
                layoutId,
                from,
                to);

        return adapter;
    }
    
	private List<HashMap<String, Object>> getWifiDetailList(Context context) {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		DetailedState state = mAccessPoint.getState(); //状态信息
        if (state != null) {
        	map.put(KEY_TITLE, getResources().getString(R.string.wifi_status));
    		map.put(KEY_STATE, Summary.get(context, state));
        }
		list.add(map);
		
		map = new HashMap<String, Object>();
		int level = mAccessPoint.getLevel(); //信号强度
        if (level != -1) {
            String[] signal = getResources().getStringArray(R.array.wifi_signal);
            map.put(KEY_TITLE, getResources().getString(R.string.wifi_signal));
     		map.put(KEY_STATE, signal[level]);
         }
        list.add(map);
        
        map = new HashMap<String, Object>();
        WifiInfo info = mAccessPoint.getInfo(); //连接速度
        if (info != null && info.getLinkSpeed() != -1) {
            map.put(KEY_TITLE, getResources().getString(R.string.wifi_speed));
     		map.put(KEY_STATE, info.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
        }
        list.add(map);
        
        map = new HashMap<String, Object>(); //安全性
        map.put(KEY_TITLE, getResources().getString(R.string.wifi_security));
 		map.put(KEY_STATE, mAccessPoint.getSecurityString(false));
 		list.add(map);
 		 
 		map = new HashMap<String, Object>();
        //Display IP addresses
 		WifiConfiguration config = mAccessPoint.getConfig(); //IP地址
        for(InetAddress a : config.linkProperties.getAddresses()) {
            map.put(KEY_TITLE, getResources().getString(R.string.wifi_ip_address));
     		map.put(KEY_STATE, a.getHostAddress());
        }
 		list.add(map); 
 		
		return list;
	}


	/**
     * set default wallpaper and list divider
     */
    private void setupListView() {
    	if (mListView != null) {
			FrameLayout ll = (FrameLayout) mListView.getParent().getParent();
			ll.setBackgroundResource(R.drawable.default_wallpaper);
		}
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
		actionBarTitle.setText(R.string.wifi_title);
		actionBar.setCustomView(actionbarLayout);
	}


	@Override
	public void onClick(View v) {
        if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
            // Should not happen, but a monkey seems to triger it
            Log.e("ffff", "Failed to forget invalid network " + mAccessPoint.getConfig());
            return;
        }

        mWifiManager.forget(mAccessPoint.networkId, mForgetListener);
        getActivity().onBackPressed();
	}

	WifiManager.ActionListener mForgetListener = new WifiManager.ActionListener() {
        @Override
        public void onSuccess() {
        }
        @Override
        public void onFailure(int reason) {
            Activity activity = getActivity();
            if (activity != null) {
                Toast.makeText(activity,
                    R.string.wifi_failed_forget_message,
                    Toast.LENGTH_SHORT).show();
            }
        }
    };
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
    	if(position == 0) {
			if(!mSwitch.isChecked()) { //关闭状态
				mSwitch.setChecked(true);
				boolean result = mDbOperator.deleteFromDb(mAccessPoint.ssid);
				if(result) {
					Log.d("ffff", mAccessPoint.ssid + " delete success");
				} else {
					Log.d("ffff", mAccessPoint.ssid + " delete fail");
				}
			} else {
				mSwitch.setChecked(false);
				if(!mDbOperator.querySsidIsExist(mAccessPoint.ssid)) { //唯一性约束
					boolean result = mDbOperator.insertToDb(mAccessPoint.ssid);
					if(result) {
						Log.d("ffff", mAccessPoint.ssid + " insert success");
					} else {
						Log.d("ffff", mAccessPoint.ssid + " insert fail");
					}
				} else {
					Log.d("ffff", mAccessPoint.ssid + " has exsit, insert fail");
				}
			}
		}
	}
	

}
