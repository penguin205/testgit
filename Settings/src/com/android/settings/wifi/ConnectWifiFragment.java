package com.android.settings.wifi;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import com.android.settings.R;
import com.android.settings.wifi.db.WifiDbOperator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectWifiFragment extends Fragment implements OnClickListener{
   
    private WifiManager mWifiManager;
	private AccessPoint mAccessPoint;
	private FrameLayout rootView;
	private View view1;
	private View view2;
	private View view3;
	private View group1Button;
	private View group2Button;
	private TextView tvSsid;
	private EditText etPassword;
	private CheckBox cbShowPassword;
	private TextView tvSecurityWay;
	private Button btnGroup1Connect;
	private Button btnGroup1Cancel;
	private Button btnGroup2Connect;
	private Button btnGroup2Forget;
	private Button btnGroup2Cancel;
	private Button view2Button;
	private TextView view3Text;
	private Button view3Button;
	private Handler mHandler;
	private int mSecurityType;
	private BroadcastReceiver mWifiReceiver; 
	private IntentFilter mFilter;
	protected boolean isError = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		 mHandler = new Handler();
		 
		 mFilter = new IntentFilter();
		 mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	}
	
	private void updateView(boolean isSuccess) {
		view1.setVisibility(View.GONE);
    	view2.setVisibility(View.GONE);
    	view3.setVisibility(View.VISIBLE);
    	if(isSuccess) {
    		view3Text.setText(R.string.prompt_connect_wifi_success);
    	} else {
    		view3Text.setText(R.string.prompt_connect_wifi_fail);
    	}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_connect_wifi, null);
		rootView = (FrameLayout) view.findViewById(R.id.rootLayout);
		view1 = view.findViewById(R.id.view1);
		view2 = view.findViewById(R.id.view2);
		view3 = view.findViewById(R.id.view3);
		view1.setVisibility(View.VISIBLE);
		view2.setVisibility(View.GONE);
		view3.setVisibility(View.GONE);
		tvSsid = (TextView) view.findViewById(R.id.view1_tv_ssid);
		etPassword = (EditText) view.findViewById(R.id.view1_et_password);
		cbShowPassword = (CheckBox) view.findViewById(R.id.view1_cb_show_password);
		tvSecurityWay = (TextView) view.findViewById(R.id.view1_tv_security_way);
		group1Button = view.findViewById(R.id.group_buttons1);
		btnGroup1Connect = (Button) view.findViewById(R.id.view1_btn_positive);	
		btnGroup1Cancel = (Button) view.findViewById(R.id.view1_btn_negative);
		
		group2Button = view.findViewById(R.id.group_buttons2);
		btnGroup2Connect = (Button) view.findViewById(R.id.view1_btn2_positive);	
		btnGroup2Forget = (Button) view.findViewById(R.id.view1_btn2_netural);	
		btnGroup2Cancel = (Button) view.findViewById(R.id.view1_btn2_negative);
		
		view2Button = (Button) view.findViewById(R.id.view2_button);
		view3Text = (TextView) view.findViewById(R.id.view3_text);
		view3Button = (Button) view.findViewById(R.id.view3_button);
		
		btnGroup1Connect.setOnClickListener(this);
		btnGroup1Cancel.setOnClickListener(this);
		btnGroup2Connect.setOnClickListener(this);
		btnGroup2Forget.setOnClickListener(this);
		btnGroup2Cancel.setOnClickListener(this);
		view2Button.setOnClickListener(this);
		view3Button.setOnClickListener(this);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		 setCustomActionBar();
	     setupWallpaper();
	     
		if(getArguments() != null) {
    		ScanResult result = getArguments().getParcelable("result");
    		WifiConfiguration config = getArguments().getParcelable("config");
    		if(result != null) {
    			mAccessPoint = new AccessPoint(getActivity(), result);
    		} else if(config != null) {
    			mAccessPoint = new AccessPoint(getActivity(), config);
    		}
    		if(mAccessPoint == null) {
    			Log.d("ffff", "ap is null, error...");
    			return;
    		}
    		
    		mSecurityType = (mAccessPoint == null) ? AccessPoint.SECURITY_NONE :
				mAccessPoint.security;
    		
    		tvSsid.setText(mAccessPoint.ssid);
    		
    		int level = mAccessPoint.getLevel();
    		Log.d("ffff", "level : " + level); 
    		
    		WifiDbOperator mDbOperator = new WifiDbOperator(getActivity());
    		
    		if(level == -1 && !mDbOperator.querySsidIsExist(mAccessPoint.ssid)) { //不在范围内且无信号不是因为禁用的网络，显示两个按钮（取消，不保存）
    			etPassword.setVisibility(View.GONE);
    			cbShowPassword.setVisibility(View.GONE);
    			String string = getActivity().getResources().getString(R.string.security_way_title);
    			String resultStr = String.format(string + "    %s", getResources().getStringArray(R.array.wifi_security)[mSecurityType]);
    			tvSecurityWay.setText(resultStr);
    			tvSecurityWay.setVisibility(View.VISIBLE);
    			btnGroup1Connect.setVisibility(View.GONE);
    			group1Button.setVisibility(View.GONE);
    			group2Button.setVisibility(View.VISIBLE);
    			btnGroup2Connect.setVisibility(View.GONE);
    		} else if(config == null && result != null) { // 未保存的网络，显示输入密码
    			etPassword.setVisibility(View.VISIBLE);
    			cbShowPassword.setVisibility(View.VISIBLE);
    			tvSecurityWay.setVisibility(View.GONE);
    			etPassword.setOnFocusChangeListener(mOnFocusChangeListener);
        		etPassword.addTextChangedListener(mTextWatcher);
        		cbShowPassword.setOnClickListener(new OnClickListener() {
        			
        			@Override
        			public void onClick(View v) {
        				etPassword.setInputType(InputType.TYPE_CLASS_TEXT | (((CheckBox) v).isChecked() 
        								? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        								: InputType.TYPE_TEXT_VARIATION_PASSWORD));
        			}
        		});
        		btnGroup1Connect.setEnabled(false);
        		group1Button.setVisibility(View.VISIBLE);
        		group2Button.setVisibility(View.GONE);
    		} else {                                     //已保存的网络，显示（取消，不保存，连接）
    			etPassword.setVisibility(View.GONE);
    			cbShowPassword.setVisibility(View.GONE);
    			String string = getActivity().getResources().getString(R.string.security_way_title);
    			String resultStr = String.format(string + "    %s", getResources().getStringArray(R.array.wifi_security)[mSecurityType]);
    			tvSecurityWay.setText(resultStr);
    			tvSecurityWay.setVisibility(View.VISIBLE);
    			btnGroup1Connect.setEnabled(true);
    			group1Button.setVisibility(View.GONE);
    			group2Button.setVisibility(View.VISIBLE);
    		}
    	}
	}
	
	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus) {
				etPassword.setTextColor(0xff46a9d2);
			} else {
				etPassword.setTextColor(0xffffffff);
			}
		}
		
	};
	
	private TextWatcher mTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			  mHandler.post(new Runnable() {
	                public void run() {
	                    enableButtonConnectIfAppropriate();
	                }
	            });
		}
	};
	
    void enableButtonConnectIfAppropriate() {

        boolean passwordInvalid = false;
        
        if (etPassword != null &&
            ((mSecurityType == AccessPoint.SECURITY_WEP && etPassword.length() == 0) ||
            (mSecurityType == AccessPoint.SECURITY_PSK && etPassword.length() < 8))) {
        	passwordInvalid = true;
        }

        btnGroup1Connect.setEnabled(!passwordInvalid);
    }
    
    private void setupWallpaper() {
    	FrameLayout fl = (FrameLayout) rootView.getParent();
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
		actionBarTitle.setText(R.string.wifi_title);
		actionBar.setCustomView(actionbarLayout);
	}

    WifiConfiguration getConfig() {
        WifiConfiguration config = mAccessPoint.getConfig();
        if(config != null) {
        	return config;
        }
        config = new WifiConfiguration();
        if (mAccessPoint.networkId == INVALID_NETWORK_ID) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mAccessPoint.ssid);
        } else {
            config.networkId = mAccessPoint.networkId;
        }

        switch (mSecurityType) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (etPassword.length() != 0) {
                    int length = etPassword.length();
                    String password = etPassword.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_PSK:  // wpa/wpa2
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (etPassword.length() != 0) {
                    String password = etPassword.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_EAP: // 802.1X EAP
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

                if (etPassword.isShown()) {
                    // For security reasons, a previous password is not displayed to user.
                    // Update only if it has been changed.
                    if (etPassword.length() > 0) {
                        config.enterpriseConfig.setPassword(etPassword.getText().toString());
                    }
                } else {
                    // clear password
                    config.enterpriseConfig.setPassword(etPassword.getText().toString());
                }
                break;
            default:
                return null;
        }

        config.proxySettings = ProxySettings.UNASSIGNED;
        config.ipAssignment = IpAssignment.DHCP;  

        return config;
    }
    
    private WifiManager.ActionListener mConnectListener = new WifiManager.ActionListener() {
        @Override
        public void onSuccess() {
        }
        @Override
        public void onFailure(int reason) {
            Activity activity = getActivity();
            if (activity != null) {
                Toast.makeText(activity,
                     R.string.wifi_failed_connect_message,
                     Toast.LENGTH_SHORT).show();
            }
        }
    };
    
    
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.view1_btn_positive:
		case R.id.view1_btn2_positive:
		     mWifiReceiver = new  BroadcastReceiver() {
		    	@Override
		    	public void onReceive(Context context, Intent intent) {

		    		if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否
		    			
		    			NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		    			
		    			if(info.getState().equals(NetworkInfo.State.CONNECTED)){
		    				
		    				WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		    				android.net.wifi.WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		    				
		    				String connectedSSID = wifiInfo.getSSID().replace("\"", "");
		    				if(connectedSSID.equals(mAccessPoint.ssid)) {
		        				mHandler.removeCallbacks(mConnectErrorRunnable);
		        				updateView(true);
		    				}
		    			}
		    		}
		    	}
		    };
			getActivity().registerReceiver(mWifiReceiver, mFilter);
			view1.setVisibility(View.GONE);
			view2.setVisibility(View.VISIBLE);
			view3.setVisibility(View.GONE);
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mWifiManager.connect(getConfig(), mConnectListener);
					isError = false;
				}
			});
			mHandler.postDelayed(mConnectErrorRunnable, 15000);
			break;
		case R.id.view1_btn_negative: // 回退
		case R.id.view1_btn2_negative:
		case R.id.view2_button:
		case R.id.view3_button:
			getActivity().onBackPressed();
			break;
			
		case R.id.view1_btn2_netural:
			 mWifiManager.forget(mAccessPoint.networkId, mForgetListener);
		     getActivity().onBackPressed();
			break;
		default:
			break;
		}
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
    
	private Runnable mConnectErrorRunnable = new Runnable() {
		
		@Override
		public void run() {
			updateView(false);
		}
	};

}
