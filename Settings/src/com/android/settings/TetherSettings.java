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

import com.android.settings.EncryptionSettings.EncryptionSelectionListener;
import com.android.settings.R;
import com.android.settings.wifi.WifiApEnabler;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/*
 * Displays preferences for Tethering.
 */
public class TetherSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, EncryptionSelectionListener, TextWatcher {
    private static final String TAG = "TetherSettings";

    private static final String ENABLE_WIFI_AP = "enable_wifi_ap";
    
//    private WebView mView;

    private WifiApEnabler mWifiApEnabler;
    private CheckBoxPreference mEnableWifiAp;
    private LeftTextPreference mEncryptionWifiAp;
	private NetworkSSIDPreference mWifiApSSID;
	private NetworkPasswordPreference mWifiApPassword;

    private String[] mWifiRegexs;

    private String[] mSecurityType;

    private WifiManager mWifiManager;
    private WifiConfiguration mWifiConfig;

    private static final int INVALID             = -1;
    private static final int WIFI_TETHERING      = 0;
    private int mTetherChoice = INVALID;
    private static final int PROVISION_REQUEST = 0;
    
    /* Stores the package name and the class name of the provisioning app */
    private String[] mProvisionApp;
    private int mSecurityTypeIndex;
	private View mFooterView;
	private Button mBtnConfirm;
	private boolean bShowFooterView;
	private String mSavedSsid;

	
    @Override
    public void onCreate(Bundle icicle) {
    	super.onCreate(icicle);
        addPreferencesFromResource(R.xml.tether_prefs2);

        Activity activity = getActivity();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); 
         
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mSecurityType = getResources().getStringArray(R.array.wifi_ap_security);
        
		// 网络热点开关
		mEnableWifiAp = (CheckBoxPreference) findPreference(ENABLE_WIFI_AP);
       
        // 加密方式
        mEncryptionWifiAp = new LeftTextPreference(activity);
        mEncryptionWifiAp.setIcon(R.drawable.ic_wifi_ap_switch);
        mEncryptionWifiAp.setTitle(R.string.encryption_wifi_ap_way_title);
        mEncryptionWifiAp.setPersistent(false);
        
        // SSID
        mWifiApSSID = new NetworkSSIDPreference(activity);
        mWifiApSSID.setIcon(R.drawable.ic_settings_ssid);
        mWifiApSSID.setTitle(R.string.wifi_tether_configure_ssid);
        mWifiApSSID.addTextChangedListener(this);
        mWifiApSSID.setPersistent(false);
        
        // SSID密码
        mWifiApPassword = new NetworkPasswordPreference(activity);
        mWifiApPassword.setIcon(R.drawable.ic_settings_ssid_security);
        mWifiApPassword.setTitle(R.string.wifi_tether_configure_security);
        mWifiApPassword.addTextChangedListener(this);
        mWifiApPassword.setPersistent(false);
        
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiRegexs = cm.getTetherableWifiRegexs();
        
        mWifiApEnabler = new WifiApEnabler(activity, mEnableWifiAp);

        boolean wifiAvailable = mWifiRegexs.length != 0;
        
        if (mWifiManager.isWifiApEnabled() && wifiAvailable && !Utils.isMonkeyRunning()) {
            initWifiTethering();
            bShowFooterView = true;
        } else {
        	bShowFooterView = false;
        }

        mProvisionApp = getResources().getStringArray(
                com.android.internal.R.array.config_mobile_hotspot_provision_app);

        
        // 注册broadcast监听器
        IntentFilter filter = new IntentFilter();
        filter.addAction("WIFI_AP_STATE_CHANGED");
        activity.registerReceiver(mReceiver, filter);
    }

	private void initWifiTethering() {
        getPreferenceScreen().removePreference(mEncryptionWifiAp);
        getPreferenceScreen().removePreference(mWifiApSSID);
        getPreferenceScreen().removePreference(mWifiApPassword);
		
    	getPreferenceScreen().addPreference(mEncryptionWifiAp);
		getPreferenceScreen().addPreference(mWifiApSSID);
		
		mWifiConfig = mWifiManager.getWifiApConfiguration();
        if (mWifiConfig == null) {
            mEncryptionWifiAp.setText(mSecurityType[EncryptionSettings.OPEN_INDEX]);
            mWifiApSSID.setText("AndroidAP"); //default name
        } else {
        	mSecurityTypeIndex = EncryptionSettings.getSecurityTypeIndex(mWifiConfig);
            mEncryptionWifiAp.setText(mSecurityType[mSecurityTypeIndex]); 
            mWifiApSSID.setText(mWifiConfig.SSID); //设置SSID
            
            if (mSecurityTypeIndex == EncryptionSettings.WPA_INDEX ||
            		mSecurityTypeIndex == EncryptionSettings.WPA2_INDEX) {
            	  getPreferenceScreen().addPreference(mWifiApPassword);
                  mWifiApPassword.setText(mWifiConfig.preSharedKey);//设置Password
            }
        }
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setCustomActionBar();
	    setupListView();
		super.onActivityCreated(savedInstanceState);
		mFooterView.setVisibility(bShowFooterView ? View.VISIBLE : View.GONE);
	}

	
    private void setupListView() {
		ListView listView = getListView();
		if(listView != null) {
			listView.setDivider(getActivity().getResources().getDrawable(R.drawable.line_bg));
			listView.setDescendantFocusability(ListView.FOCUS_BEFORE_DESCENDANTS);
			listView.setFooterDividersEnabled(false);
			// when Listview is scrolling we let lost child view's focus
			listView.setOnScrollListener(mScrollListener);
			LinearLayout rootLayout = (LinearLayout) listView.getParent();
			rootLayout.setBackgroundResource(R.drawable.default_wallpaper);
			
			mFooterView = LayoutInflater.from(getActivity()).inflate(
					R.layout.footer_common_button, listView, false);
			mFooterView.setVisibility(View.GONE); // default is hide
			mBtnConfirm = (Button) mFooterView.findViewById(R.id.footer_button);
			mBtnConfirm.setText(R.string.confirm);
			mBtnConfirm.setOnClickListener(mSaveNewConfigListener);
			
			listView.addFooterView(mFooterView);
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
		actionBarTitle.setText(R.string.net_hotspot_title);
		actionBar.setCustomView(actionbarLayout);
	}
	

	private OnClickListener mSaveNewConfigListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mWifiConfig = getConfig();
            if (mWifiConfig != null) {
                /**
                 * if soft AP is stopped, bring up
                 * else restart with new config
                 * TODO: update config on a running access point when framework support is added
                 */
                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                    mWifiManager.setWifiApEnabled(null, false);
                    mWifiManager.setWifiApEnabled(mWifiConfig, true);
                } else {
                    mWifiManager.setWifiApConfiguration(mWifiConfig);
                }
            }
		}
	};
	
    public WifiConfiguration getConfig() {

        WifiConfiguration config = new WifiConfiguration();

        /**
         * TODO: SSID in WifiConfiguration for soft ap
         * is being stored as a raw string without quotes.
         * This is not the case on the client side. We need to
         * make things consistent and clean it up
         */
        config.SSID = mWifiApSSID.getSsid();

        switch (mSecurityTypeIndex) {
            case EncryptionSettings.OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case EncryptionSettings.WPA_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mWifiApPassword.getPassword().length() != 0) {
                    String password = mWifiApPassword.getPassword();
                    config.preSharedKey = password;
                }
                return config;

            case EncryptionSettings.WPA2_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mWifiApPassword.getPassword().length() != 0) {
                    String password = mWifiApPassword.getPassword();
                    config.preSharedKey = password;
                }
                return config;
        }
        return null;
    }
    
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if("WIFI_AP_STATE_CHANGED".equals(action)) {
				
				if(mWifiManager.isWifiApEnabled()) {
					Log.d("ffff", "execute show...");
					initWifiTethering();
					bShowFooterView = true;
				} else {
					Log.d("ffff", "execute hide...");
					getPreferenceScreen().removePreference(mEncryptionWifiAp);
		            getPreferenceScreen().removePreference(mWifiApSSID);
		            getPreferenceScreen().removePreference(mWifiApPassword);
		            bShowFooterView = false;
				}
				mFooterView.setVisibility(bShowFooterView ? View.VISIBLE : View.GONE);
			}
		}
	};

	private OnScrollListener mScrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (SCROLL_STATE_TOUCH_SCROLL == scrollState) { 
				View currentFocus = getActivity().getCurrentFocus(); 
				if (currentFocus != null) {
					currentFocus.clearFocus();
				}
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};

	
    protected void showSecurityFields() {
		if (mSecurityTypeIndex == EncryptionSettings.OPEN_INDEX) {
			getPreferenceScreen().removePreference(mWifiApPassword);
		} else {
			getPreferenceScreen().addPreference(mWifiApPassword);
		}
	}

    @Override
    public void onStart() {
        super.onStart();
        if (mWifiApEnabler != null) {
            mEnableWifiAp.setOnPreferenceChangeListener(this);
            mWifiApEnabler.resume();
        }
        
        if(!TextUtils.isEmpty(mSavedSsid)) {
			mWifiApSSID.setText(mSavedSsid);
		}
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mWifiApEnabler != null) {
            mEnableWifiAp.setOnPreferenceChangeListener(null);
            mWifiApEnabler.pause();
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
		if(mReceiver != null) {
			getActivity().unregisterReceiver(mReceiver);
	     	mReceiver = null;
	    }
		EncryptionSettings.setEncryptionSelectionListener(null);
	    mWifiApSSID.removeTextChangeListener(this);
        mWifiApPassword.removeTextChangeListener(this);
    }
    
    // 点击 mEnableWifiAp 触发事件 
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean enable = (Boolean) value;
        
        if (enable) { 
            startProvisioningIfNecessary(WIFI_TETHERING);
        } else {
            mWifiApEnabler.setSoftapEnabled(false);
        }
        return false;
    }

    
    boolean isProvisioningNeeded() {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            return false;
        }
        return mProvisionApp.length == 2;
    }

    private void startProvisioningIfNecessary(int choice) {
        mTetherChoice = choice;
        if (isProvisioningNeeded()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(mProvisionApp[0], mProvisionApp[1]);
            startActivityForResult(intent, PROVISION_REQUEST);
        } else {
            startTethering();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == PROVISION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                startTethering();
            } else {
                mTetherChoice = INVALID;
            }
        }
    }

    private void startTethering() {
        switch (mTetherChoice) {
            case WIFI_TETHERING:
                mWifiApEnabler.setSoftapEnabled(true);
                break;
            default:
                //should not happen
                break;
        }
    }

    @Override
	public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
    	/*
		 * solve below exception, when remove child view before, we first remove its focus 
		 * java.lang.IllegalArgumentException: parameter must be a descendant of this view
		 *  Add by XiaoYJ
		 */
    	View currentFocus = getActivity().getCurrentFocus();
		if (currentFocus != null) {
			currentFocus.clearFocus();
			InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(
					currentFocus.getWindowToken(), 0);
		}
		
		if(preference == mEncryptionWifiAp) {
			
			mSavedSsid = mWifiApSSID.getSsid();
			
			EncryptionSettings.setEncryptionSelectionListener(this);
			Bundle extras = new Bundle();
			extras.putInt("security_index", mSecurityTypeIndex);
			startFragment(this, EncryptionSettings.class.getCanonicalName(), -1, extras);
		}

		return super.onPreferenceTreeClick(screen, preference);
	}

    
    
    @Override
    public int getHelpResource() {
        return R.string.help_url_tether;
    }

    /**
     * Checks whether this screen will have anything to show on this device. This is called by
     * the shortcut picker for Settings shortcuts (home screen widget).
     * @param context a context object for getting a system service.
     * @return whether Tether & portable hotspot should be shown in the shortcuts picker.
     */
    public static boolean showInShortcuts(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;
        return !isSecondaryUser && cm.isTetheringSupported();
    }

	@Override
	public void onEncryptionSelected(int securityIndex) {
		Log.d("ffff", "onEncryptionSelected : " + securityIndex);
		if(securityIndex != mSecurityTypeIndex) {
			mSecurityTypeIndex = securityIndex;
			mEncryptionWifiAp.setText(mSecurityType[mSecurityTypeIndex]);
			showSecurityFields();
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		Log.d("ffff", "text change...");
		validate();
	}
	
	public void validate() {
		if ((mWifiApSSID.getSsid() != null && mWifiApSSID.getSsid().length() == 0)
				|| (((mSecurityTypeIndex == EncryptionSettings.WPA_INDEX) || (mSecurityTypeIndex == EncryptionSettings.WPA2_INDEX)) && mWifiApPassword
						.getPassword().length() < 8)) {
			Log.d("ffff", "set button disable");
			mBtnConfirm.setEnabled(false);
		} else {
			Log.d("ffff", "set button enable");
			mBtnConfirm.setEnabled(true);
		}
	}
}
