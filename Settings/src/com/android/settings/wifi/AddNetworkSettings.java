package com.android.settings.wifi;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.net.LinkProperties;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.net.wifi.WifiEnterpriseConfig.Eap;
import android.net.wifi.WifiEnterpriseConfig.Phase2;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import com.android.settings.LeftTextPreference;
import com.android.settings.NetworkPasswordPreference;
import com.android.settings.NetworkSSIDPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.wifi.WifiEncryptionSettings.WifiEncryptionSelectionListener;

public class AddNetworkSettings extends SettingsPreferenceFragment implements OnClickListener, TextWatcher, WifiEncryptionSelectionListener{

	private NetworkSSIDPreference mSsidPref;
	private LeftTextPreference mSecurityPref;
	private NetworkPasswordPreference mPasswordPref;
	private Button mBtnJoin;
	private Button mBtnCancel;
	private WifiManager mWifiManager;
	private int mSecurityTypeIndex = WifiEncryptionSettings.WIFI_SECURITY_NONE;
	private String[] mSecurityType;
	
	private String mSavedSsid;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		addPreferencesFromResource(R.xml.add_network_prefs);
		
		mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		
		Activity activity = getActivity();
	    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); 
	       
	    mSecurityType = getResources().getStringArray(R.array.wifi_security);
	    
		mSsidPref = (NetworkSSIDPreference) findPreference("network_ssid");
		mSsidPref.addTextChangedListener(this);
		
		mSecurityPref = (LeftTextPreference) findPreference("network_security");
		mSecurityPref.setText(mSecurityType[mSecurityTypeIndex]);
		
		mPasswordPref = new NetworkPasswordPreference(getActivity());
		mPasswordPref.setTitle(R.string.network_password);
		mPasswordPref.setIcon(R.drawable.ic_network_password);
		mPasswordPref.addTextChangedListener(this);
		mPasswordPref.setPersistent(false);
		
		getPreferenceScreen().addPreference(mSsidPref);
		getPreferenceScreen().addPreference(mSecurityPref);
		if(mSecurityTypeIndex != WifiEncryptionSettings.WIFI_SECURITY_NONE) {
			getPreferenceScreen().addPreference(mPasswordPref);
		}
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setCustomActionBar();
		setupListView();
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(!TextUtils.isEmpty(mSavedSsid)) {
			Log.d("ffff", "restore ssid");
			mSsidPref.setText(mSavedSsid);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mSsidPref.removeTextChangeListener(this);
		mPasswordPref.removeTextChangeListener(this);
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
		actionBarTitle.setText(R.string.search_wifi);
		actionBar.setCustomView(actionbarLayout);
	}
	
    private void setupListView() {
		ListView listView = getListView();
		if(listView != null) {
			listView.setDivider(getActivity().getResources().getDrawable(R.drawable.line_bg));
			listView.setDescendantFocusability(ListView.FOCUS_BEFORE_DESCENDANTS);
			listView.setFooterDividersEnabled(false);
			listView.setHeaderDividersEnabled(false);
			// when Listview is scrolling we let lost child view's focus
			listView.setOnScrollListener(mScrollListener);
			LinearLayout rootLayout = (LinearLayout) listView.getParent();
			rootLayout.setBackgroundResource(R.drawable.default_wallpaper);
			listView.setAdapter(null);
			// Add by XiaoYJ. if don't add a view above SSID preference, then the SSID editext can't work normally
			// i don't know why, just do it.
			View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.blankness_fill_view, listView, false);
			headerView.setVisibility(View.GONE);
			listView.addHeaderView(headerView);
			
			View footerView = LayoutInflater.from(getActivity()).inflate(
					R.layout.footer_add_network, listView, false);
			
			mBtnJoin = (Button) footerView.findViewById(R.id.button_footer_join);
			mBtnCancel = (Button) footerView.findViewById(R.id.button_footer_cancel);
			mBtnJoin.setOnClickListener(this);
			mBtnCancel.setOnClickListener(this);
			listView.addFooterView(footerView);
		}
  	}
    
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

    WifiConfiguration getConfig() {

        WifiConfiguration config = new WifiConfiguration();

        config.SSID = AccessPoint.convertToQuotedString(
                mSsidPref.getSsid().toString());
        // If the user adds a network manually, assume that it is hidden.
        config.hiddenSSID = true;

        switch (mSecurityTypeIndex) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPasswordPref.getPassword().length() != 0) {
                    int length = mPasswordPref.getPassword().length();
                    String password = mPasswordPref.getPassword().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPasswordPref.getPassword().length() != 0) {
                    String password = mPasswordPref.getPassword().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                config.enterpriseConfig = new WifiEnterpriseConfig();
                int eapMethod = Eap.NONE;
                int phase2Method = Phase2.NONE;
                config.enterpriseConfig.setEapMethod(eapMethod);
                switch (eapMethod) {
                    case Eap.PEAP:
                        // PEAP supports limited phase2 values
                        // Map the index from the PHASE2_PEAP_ADAPTER to the one used
                        // by the API which has the full list of PEAP methods.
                        switch(phase2Method) {
                            case WifiConfigController.WIFI_PEAP_PHASE2_NONE:
                                config.enterpriseConfig.setPhase2Method(Phase2.NONE);
                                break;
                            case WifiConfigController.WIFI_PEAP_PHASE2_MSCHAPV2:
                                config.enterpriseConfig.setPhase2Method(Phase2.MSCHAPV2);
                                break;
                            case WifiConfigController.WIFI_PEAP_PHASE2_GTC:
                                config.enterpriseConfig.setPhase2Method(Phase2.GTC);
                                break;
                            default:
                                Log.e("ffff", "Unknown phase2 method" + phase2Method);
                                break;
                        }
                        break;
                    default:
                        // The default index from PHASE2_FULL_ADAPTER maps to the API
                        config.enterpriseConfig.setPhase2Method(phase2Method);
                        break;
                }
                config.enterpriseConfig.setCaCertificateAlias(""); //Set CA certificate alias.
                config.enterpriseConfig.setClientCertificateAlias(""); //Set Client certificate alias.
                config.enterpriseConfig.setIdentity("usename");
                config.enterpriseConfig.setAnonymousIdentity("usename");


                if (mSecurityTypeIndex != WifiEncryptionSettings.WIFI_SECURITY_NONE) {
                    // For security reasons, a previous password is not displayed to user.
                    // Update only if it has been changed.
                    if (mPasswordPref.getPassword().length() > 0) {
                        config.enterpriseConfig.setPassword(mPasswordPref.getPassword().toString());
                    }
                } else {
                    // clear password
                    config.enterpriseConfig.setPassword(mPasswordPref.getPassword().toString());
                }
                break;
            default:
                return null;
        }

        config.proxySettings = ProxySettings.UNASSIGNED;
        config.ipAssignment = IpAssignment.DHCP;

        return config;
    }
    
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.button_footer_join) {
			Log.d("ffff", "footer JOIN button clicked");
			new Handler().post(new Runnable() {
				
				@Override
				public void run() {
					mWifiManager.connect(getConfig(), mSaveListener);
				}
			});
			getActivity().onBackPressed();
		} else if(v.getId() == R.id.button_footer_cancel) {
			Log.d("ffff", "footer Cancel button clicked");
			getActivity().onBackPressed();
		}
	}

	private WifiManager.ActionListener mSaveListener = new WifiManager.ActionListener() {
        @Override
        public void onSuccess() {
        }
        @Override
        public void onFailure(int reason) {
            Activity activity = getActivity();
            if (activity != null) {
                Toast.makeText(activity,
                    R.string.wifi_failed_save_message,
                    Toast.LENGTH_SHORT).show();
            }
        }
    };

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
		if ((mSsidPref.getSsid() != null && mSsidPref.getSsid().length() == 0)
				|| ((mSecurityTypeIndex != WifiEncryptionSettings.WIFI_SECURITY_NONE)
							&& mPasswordPref.getPassword() != null && mPasswordPref.getPassword().length() < 8)) {
			Log.d("ffff", "set button disable");
			mBtnJoin.setEnabled(false);
			
		} else {
			Log.d("ffff", "set button enable");
			mBtnJoin.setEnabled(true);
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		View currentFocus = getActivity().getCurrentFocus();
		if (currentFocus != null) {
			currentFocus.clearFocus();
			InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(
					currentFocus.getWindowToken(), 0);
		}
		if(preference == mSecurityPref) {
			
			mSavedSsid = mSsidPref.getSsid();
			
			WifiEncryptionSettings.setWifiEncryptionSelectionListener(this);
			Bundle extras = new Bundle();
			extras.putInt("security_index", mSecurityTypeIndex);
			startFragment(this, WifiEncryptionSettings.class.getCanonicalName(), -1, extras);
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onWifiEncryptionSelected(int securityIndex) {
		if(securityIndex != mSecurityTypeIndex) {
			mSecurityTypeIndex = securityIndex;
			mSecurityPref.setText(mSecurityType[mSecurityTypeIndex]);
			showSecurityFields();
		}
	}
	
    protected void showSecurityFields() {
		if (mSecurityTypeIndex == WifiEncryptionSettings.WIFI_SECURITY_NONE) {
			getPreferenceScreen().removePreference(mPasswordPref);
		} else {
			getPreferenceScreen().addPreference(mPasswordPref);
		}
	}
}
