package com.android.settings.wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.settings.R;
import com.android.settings.Utils;

import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class WifiEncryptionSettings extends ListFragment{
	
    public static interface WifiEncryptionSelectionListener {
        // You can add any argument if you really need it...
        public void onWifiEncryptionSelected(int securityIndex);
    }
    
    public static final int WIFI_SECURITY_NONE = 0;
    public static final int WIFI_SECURITY_WEP = 1;
    public static final int WIFI_SECURITY_PSK_GENERIC = 2;
    public static final int WIFI_SECURITY_EAP = 3;
    
    private final String KEY_ENCRYPTION_WAY = "key_encryption_way";
    
    private ListView mListView;
	private String[] mSecurityType;
	private static WifiEncryptionSelectionListener mListener;
	
	private int mSecurityTypeIndex = WIFI_SECURITY_NONE;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final ListView list = (ListView) view.findViewById(android.R.id.list);
        Utils.forcePrepareCustomPreferencesList(container, view, list, false);
        return view;
    }
    
	@Override
    public void onActivityCreated(Bundle savedInstanseState) {
        super.onActivityCreated(savedInstanseState);
    	
        setCustomActionBar();
    	setupListView();

    	mSecurityType = getResources().getStringArray(R.array.wifi_security);
    	
    	if(getArguments() != null) {
    		mSecurityTypeIndex = getArguments().getInt("security_index");
    		Log.d("ffff", "getArguments() : " + mSecurityTypeIndex);
    	}
    	
    	SimpleAdapter encryptionAdapter = constructEncryptionWayAdapter(getActivity());
    	setListAdapter(encryptionAdapter);
    	mListView.setItemChecked(mSecurityTypeIndex, true);
    }
    
//    public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
//        if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
//            return WPA_INDEX;
//        } else if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
//            return WPA2_INDEX;
//        }
//        return OPEN_INDEX;
//    }
    
    public SimpleAdapter constructEncryptionWayAdapter(Context context) {
        return constructEncryptionWayAdapter(context,
                R.layout.checkable_oneline_list_item);
    }

    public SimpleAdapter constructEncryptionWayAdapter(Context context, int layoutId) {
        final String[] from = new String[] {KEY_ENCRYPTION_WAY };
        final int[] to = new int[] {android.R.id.text1};

        final List<HashMap<String, Object>> encryptionList = getEncryptionList();
        
        final SimpleAdapter adapter = new SimpleAdapter(context,
        		encryptionList,
                layoutId,
                from,
                to);

        return adapter;
    }
    
    private List<HashMap<String, Object>> getEncryptionList() {
    	List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
    	HashMap<String, Object> map = null;
    	for(int i=0; i<mSecurityType.length; i++) {
    		map = new HashMap<String, Object>();
    		map.put(KEY_ENCRYPTION_WAY, mSecurityType[i]);
    		list.add(map);
    	}
		return list;
	}

	/**
     * set default wallpaper and list divider
     */
    private void setupListView() {
    	mListView = getListView();
        if(mListView !=null){
        	mListView.setDivider(getResources().getDrawable(R.drawable.line_bg));
        	mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            FrameLayout fl = (FrameLayout)mListView.getParent();
            fl.setBackgroundResource(R.drawable.default_wallpaper);
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
		actionBarTitle.setText(R.string.security_way_title);
		actionBar.setCustomView(actionbarLayout);
	}
	
    public static void setWifiEncryptionSelectionListener(WifiEncryptionSelectionListener listener) {
        mListener = listener;
    }
    
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mSecurityTypeIndex  = position;
		if(mListener != null) {
			mListener.onWifiEncryptionSelected(mSecurityTypeIndex);
		}
	}
}
