package com.android.settings;

/**
 * 	<string name="car_type_code" >"Vehicle Type Code"</string>
	<string name="android_system_version" >"Android System Version"</string>
	<string name="radio_hardware_version" >"Vehicle Navi Hardware Version"</string>
	<string name="radio_software_version" >"Vehicle Navi SoftWare Version"</string>
	<string name="navi_map_version" >"Navi Map Version"</string>
	<string name="display_resolution" >"Display Resolution"</string>
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.android.settings.R;
import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;
import android.app.GpsCtrlManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SystemActivity extends Settings implements OnClickListener {
	
	private static final String MAP_SOFTWARE_VERSION = "/storage/extsd/autonavidata/data/overall/global.dat";
	
	private CenterTextPreference carTypePref;
	private CenterTextPreference osVerPref;
	private CenterTextPreference hardwarePref;
	private CenterTextPreference softwarePref;
	private CenterTextPreference mapPref;
	private CenterTextPreference resolutionPref;
	
	private String hardVer = "04";
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		addPreferencesFromResource(R.xml.system_prefs);
        
		setActionBarTitle(R.string.system_settings);
		
		ListView listView = getListView();
		//remove vertical scrollbar
		listView.setVerticalScrollBarEnabled(false);
		// add footer button
		View footerView = LayoutInflater.from(this).inflate(
				R.layout.footer_common_button, listView, false);
		Button btnFooter = (Button) footerView.findViewById(R.id.footer_button);
		btnFooter.setText(R.string.check_update);
		btnFooter.setOnClickListener(this);
		listView.addFooterView(footerView);
		// fill empty space
		TextView tv = new TextView(this);
		tv.setPadding(0, 60, 0, 60);
		listView.addFooterView(tv);
		
		listView.setFooterDividersEnabled(false);
		listView.setHeaderDividersEnabled(false);
		
		carTypePref = (CenterTextPreference) findPreference("car_type_code");
		//debug
		carTypePref.setText("11111");
		
		osVerPref = (CenterTextPreference) findPreference("android_system_version");
		osVerPref.setText(Build.VERSION.RELEASE);
		
		hardwarePref = (CenterTextPreference) findPreference("radio_hardware_version");
		hardwarePref.setText(hardVer);
		
		softwarePref = (CenterTextPreference) findPreference("radio_software_version");
		softwarePref.setText(Build.VERSION.INCREMENTAL);
		
		mapPref = (CenterTextPreference) findPreference("navi_map_version");
		mapPref.setText(getMapVersion());
		
		resolutionPref = (CenterTextPreference) findPreference("display_resolution");
		resolutionPref.setText(getScreenResolution());
		
		mMcuRemoteService = (GpsCtrlManager)getSystemService(Context.GPS_CTRL_SERVICE);
		if(mListener !=null){
			try {
				mMcuRemoteService.registerEventListener(mListener,mListener.hashCode());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		startmCheckEpromVer();
	}

	byte mCheckEpromItem;
	
	private Handler mhandler = new Handler();

	private void startmCheckEpromVer() {
		mCheckEpromItem = 0;
		readEprom(mCheckEpromItem);
		mCheckEpromItem++;

		mhandler.postDelayed(mCheckEpromVerRunnable, 250);
	}

	private Runnable mCheckEpromVerRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mCheckEpromItem <= 0x02 || mCheckEpromItem == 0x0D) {
				readEprom(mCheckEpromItem);
				mCheckEpromItem++;

				if (mCheckEpromItem == 0x03) {
					mCheckEpromItem = 0x0D;
				}

				if (mCheckEpromItem < 0x0E) {
					mhandler.postDelayed(mCheckEpromVerRunnable, 250);
				}
			}
		}
	};
	
	public void readEprom(int type) {
		Log.d("ffff", "readEprom,type:" + type);

		epromData.readEprom(type);
		byte[] bs = new byte[1];
		bs[0] = (byte) (type & 0xFF);
		sendMcuData(bs);
	}

	public boolean sendMcuData(byte[] bs) {
		// byte[] actions = new byte[1];
		String[] strs = new String[1];
		if (mMcuRemoteService != null) {
			try {
				int ret = mMcuRemoteService.sendComm(mListener.hashCode(),
						CommonStatic.CM_EEPROM_READ_WRITE, bs.length, bs, 0,
						strs);
				return (ret != 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private GpsCtrlManager mMcuRemoteService = null;
	private EpromData epromData = new EpromData();
	private IGpsCtrl mListener = new IGpsCtrl.Stub(){
		
		@Override
		public void onGpsCtrlChange(int arg0) throws RemoteException {
			Log.d("ffff", "czy onGpsCtrlChange action=0x" + Integer.toHexString(arg0));
			switch(arg0){
				case CommonStatic.CM_EEPROM_READ_WRITE:
				{
					if(mMcuRemoteService.getSeriesValueCount()!=16){
						return;
					}
					Bundle bMcuData = new Bundle();
					byte[] srcData = mMcuRemoteService.getSeriesNumber();
					byte[] desData = new byte[32];
					System.arraycopy(srcData, 0, desData, 0, srcData.length);
					
					bMcuData.putByteArray("desData", desData);
					
					Message mMcuMessage = Message.obtain();
					mMcuMessage.what = arg0;
					mMcuMessage.setData(bMcuData);
					
					mMcuDeviceEventHandler.sendMessage(mMcuMessage);
				}
				break;
			}
			return;
		}
	};

	Handler mMcuDeviceEventHandler = new Handler() {

		public void handleMessage(Message msg) {
			
		Log.d("ffff","msg.what: 0x"+Integer.toHexString(msg.what));

		Bundle hBundle = msg.getData();
		byte[] hDesData = hBundle.getByteArray("desData");
		
		switch(msg.what){
			case CommonStatic.CM_EEPROM_READ_WRITE: {
				int valueType = 0;
				valueType = hDesData[0];
				if (valueType < 0 || valueType > 0x10) {
					return;
				}

				// maybe the answer is the PRODUCT_SERIES_NUMBER,and also maybe
				// the other item,that hasn't writen
				// assume only one item is waiting for the answer

				switch (valueType) {
				case 0x00:// PRODUCT_SERIES_NUMBER

					// src,dest

					if (epromData.mbWaitAnswer[valueType]) {// if some item
															// hasn't been
															// writen,it's
															// default value is
															// 0,when return the
															// unwritten item,it
															// can't be set as
															// item index 0
						epromData.mbWaitAnswer[valueType] = false;
						System.arraycopy(hDesData, 0,
								epromData.mProduct_Series_Number, 0,
								epromData.mProduct_Series_Number.length);
					}

					break;
				case 0x01:// NC_Series_Number
					// src,dest
					if (epromData.mbWaitAnswer[valueType]) {
						epromData.mbWaitAnswer[valueType] = false;
						System.arraycopy(hDesData, 0,
								epromData.mNC_Series_Number, 0,
								epromData.mNC_Series_Number.length);
					}
					break;
				case 0x02:// Manufactur_Date
					// src,dest
					if (epromData.mbWaitAnswer[valueType]) {
						epromData.mbWaitAnswer[valueType] = false;
						System.arraycopy(hDesData, 0, epromData.mManufactur_Date,
								0, epromData.mManufactur_Date.length);

						//setMachineVersion();
					}
					break;

				case 0x0d:// hard version
					// src,dest
					if (epromData.mbWaitAnswer[valueType]) {
						epromData.mbWaitAnswer[valueType] = false;
						System.arraycopy(hDesData, 0, epromData.mHardVersion, 0,
								epromData.mHardVersion.length);

						setHardVersion();
					}
					break;

				case 0x10:// aging test
					// src,dest
					if (epromData.mbWaitAnswer[valueType]) {
						epromData.mbWaitAnswer[valueType] = false;
						System.arraycopy(hDesData, 0, epromData.mAging_Result, 0,
								epromData.mAging_Result.length);
					}
					//setEpromdataVersion();
					break;

				default:
					if (valueType >= 0x03 && valueType <= 0x0C) {
						if (epromData.mbWaitAnswer[valueType]) {
							epromData.mbWaitAnswer[valueType] = false;
							System.arraycopy(
									hDesData,
									0,
									epromData.mTest_Result_n[valueType - 0x03],
									0,
									epromData.mTest_Result_n[valueType - 0x03].length);
						}
					}
					break;
				}
			}
				break;

			default:
				Log.w("ffff","onDevEvent(),unprocess action:0x"+ Integer.toHexString(msg.what));
				break;
			}
			return;
		}
	};

	@Override
	public void onClick(View v) {
		Bundle datas = new Bundle();
		datas.putString("hardware_version", hardVer);
		Intent intent = onBuildStartFragmentIntent(SystemUpdateSelectFragment.class.getCanonicalName(), datas, 0, 0);
    	this.startActivityForResult(intent, -1);
	}
	
	protected void setHardVersion() {
		hardVer  = epromData.getHardVersion();
		hardwarePref.setText(hardVer );
	}

	public String getMapVersion() {
		try {
			return readLine(MAP_SOFTWARE_VERSION).substring(5,21);
		} catch (IOException e) {
			Log.e("ffff", "IO Exception when getting map software version", e);
			return "Unavailable";
		}
    }
	
	private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
	}
	
    private String getScreenResolution() {
    	DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    	int W = mDisplayMetrics.widthPixels;
    	int H = mDisplayMetrics.heightPixels;
    	Log.d("ffff", "Width = " + W);
    	Log.d("ffff", "Height = " + H);

    	return W + " * " + H;
    }
}
