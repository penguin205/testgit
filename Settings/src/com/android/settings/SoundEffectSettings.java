package com.android.settings;

import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;//add by czy for refactor;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.GpsCtrlManager;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class SoundEffectSettings extends Fragment implements VerticalSeekBar.OnSeekBarChangeListener {
	private static String TAG = "SoundEffectSettings";

	private VerticalSeekBar mSeekbar31Hz = null;
	private VerticalSeekBar mSeekbar62Hz = null;
	private VerticalSeekBar mSeekbar125Hz = null;
	private VerticalSeekBar mSeekbar250Hz = null;
	private VerticalSeekBar mSeekbar500Hz = null;
	private VerticalSeekBar mSeekbar1KHz = null;
	private VerticalSeekBar mSeekbar2KHz = null;

	private byte[] mCustomV = new byte[]{0,0,0,0,0,0,0};
	
	private GpsCtrlManager mService = null;
	
	Handler mDeviceEventHandler = new Handler(){

		public void handleMessage(Message msg) {
			
		Log.d(TAG,"msg.what: 0x"+Integer.toHexString(msg.what));

		Bundle hBundle = msg.getData();
		byte[] hDesData = hBundle.getByteArray("desData");
		
		switch(msg.what){
		case CommonStatic.CM_SOUND_MGR_READ_CURR_EFFECT:
		case CommonStatic.CM_SOUND_MGR_SET_EFFECT:
			switch(0xff&hDesData[0]){
			case CommonStatic.EFFECT_TYPE_CUSTOM:
				Log.d(TAG, "CASE EFFECT_TYPE_CUSTOM");
				mCustomV[0] = hDesData[1];
				mCustomV[1] = hDesData[2];
				mCustomV[2] = hDesData[3];
				mCustomV[3] = hDesData[4];
				mCustomV[4] = hDesData[5];
				mCustomV[5] = hDesData[6];
				mCustomV[6] = hDesData[7];
				break;
			}

			for(int i=0; i<mCustomV.length; i++) {
				Log.d(TAG, "mCustomV [" + i + "] :"+ (0xff&hDesData[i]));
			}
			mSeekbar31Hz.setProgress(0xff&hDesData[1]);
			mSeekbar62Hz.setProgress(0xff&hDesData[2]);
			mSeekbar125Hz.setProgress(0xff&hDesData[3]);
			mSeekbar250Hz.setProgress(0xff&hDesData[4]);
			mSeekbar500Hz.setProgress(0xff&hDesData[5]);
			mSeekbar1KHz.setProgress(0xff&hDesData[6]);
			mSeekbar2KHz.setProgress(0xff&hDesData[7]);
			break;
			
			default:
				Log.e(TAG, "onDevEvent(),action error,action:0x"+Integer.toHexString(msg.what));
				break;
			}
		}
	};


	IGpsCtrl mListener = new IGpsCtrl.Stub(){
		@Override
		public void onGpsCtrlChange(int arg0) throws RemoteException {
			Log.d(TAG, "czy onGpsCtrlChange action=0x" + Integer.toHexString(arg0));
			switch(arg0){
				case CommonStatic.CM_SOUND_MGR_READ_CURR_EFFECT:
				case CommonStatic.CM_SOUND_MGR_SET_EFFECT:
					Bundle bMcuData = new Bundle();
					byte[] srcData = mService.getSoundEffectData();
					byte[] desData = new byte[32];
					System.arraycopy(srcData, 0, desData, 0, srcData.length);
					
					bMcuData.putByteArray("desData", desData);
					
					Message mMcuMessage = Message.obtain();
					mMcuMessage.what = arg0;
					mMcuMessage.setData(bMcuData);
					mDeviceEventHandler.sendMessage(mMcuMessage);
				break;
				default:
					break;
			}
		}
	};

	private View rootRelativeLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sound_effect_settings, null);
		rootRelativeLayout = view.findViewById(R.id.root_relativeLayout);
		mSeekbar31Hz = (VerticalSeekBar)view.findViewById(R.id.seekBar_31_hz);
		mSeekbar31Hz.setOnSeekBarChangeListener(this);
		mSeekbar62Hz = (VerticalSeekBar)view.findViewById(R.id.seekBar_62_hz);
		mSeekbar62Hz.setOnSeekBarChangeListener(this);
		mSeekbar125Hz = (VerticalSeekBar)view.findViewById(R.id.seekBar_125_hz);
		mSeekbar125Hz.setOnSeekBarChangeListener(this);
		mSeekbar250Hz = (VerticalSeekBar)view.findViewById(R.id.seekBar_250_hz);
		mSeekbar250Hz.setOnSeekBarChangeListener(this);
		mSeekbar500Hz = (VerticalSeekBar)view.findViewById(R.id.seekBar_500_hz);
		mSeekbar500Hz.setOnSeekBarChangeListener(this);
		mSeekbar1KHz = (VerticalSeekBar)view.findViewById(R.id.seekBar_1k_hz);
		mSeekbar1KHz.setOnSeekBarChangeListener(this);
		mSeekbar2KHz = (VerticalSeekBar)view.findViewById(R.id.seekBar_2k_hz);
		mSeekbar2KHz.setOnSeekBarChangeListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setCustomActionBar();
		setWallpaper();
		
		mService = (GpsCtrlManager)getActivity().getSystemService(Context.GPS_CTRL_SERVICE);
		if(mListener !=null){
			try {
				mService.registerEventListener(mListener,mListener.hashCode());
				getInitData();
				getEqVoice();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}  
	}
	

    private void setWallpaper() {
		FrameLayout rootLayout = (FrameLayout) rootRelativeLayout.getParent();
		rootLayout.setBackgroundResource(R.drawable.default_wallpaper);
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
		actionBarTitle.setText(R.string.equalizer);
		actionBar.setCustomView(actionbarLayout);
	}
	
	private void getEqVoice(){
		byte[] actions = new byte[1];
		String[] strs = new String[1];
		
		if(null!=mService){
			try {
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_READ_SPEEKER_EQ,0, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.w(TAG, "getInitData,mService == null");
        }
	}

	protected void getInitData() {
		byte[] actions = new byte[1];
		String[] strs = new String[1];
		
		if(null!=mService){
			try {
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_READ_CURR_EFFECT,0, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.w(TAG, "getInitData,mService == null");
        }
	}

	private void setEffect(int type,int _31hzVol,int _62hzVol,int _125hzVol,int _250hzVol,int _500hzVol,int _1KhzVol, int _2KhzVol) {
		byte[] actions = new byte[8];
		String[] strs = new String[1];
		actions[0] = (byte)type;
		actions[1] = (byte)_31hzVol;
		actions[2] = (byte)_62hzVol;
		actions[3] = (byte)_125hzVol;
		actions[4] = (byte)_250hzVol;
		actions[5] = (byte)_500hzVol;
		actions[6] = (byte)_1KhzVol;
		actions[7] = (byte)_2KhzVol;
		
		if(null!=mService){
			try {
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_SET_EFFECT,8, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.w(TAG, "setEffect,mService == null");
        }
	}
	
    @Override
	public void onDestroy() {
    	if (mService != null) {
            try {
                mService.unregisterEventListener(mListener.hashCode());
            } catch (RemoteException e) {
            	Log.e(TAG, e.getMessage());
            }
        }else{
        	Log.w(TAG, "onDestroy,mService == null");
        }
    	
        super.onDestroy();
    }

	@Override
	public void onProgressChanged(VerticalSeekBar VerticalSeekBar,
			int progress, boolean fromUser) {
		Log.d(TAG, "onProgressChanged, " + ",fromUser :" + fromUser + ", P1:" + mSeekbar31Hz.getProgress() + 
				" , p2:" + mSeekbar62Hz.getProgress() +
				" , p3:" + mSeekbar125Hz.getProgress() +
				" , p4:" + mSeekbar250Hz.getProgress() +
				" , p5:" + mSeekbar500Hz.getProgress() +
				" , p6:" + mSeekbar1KHz.getProgress() +
				" , p7:" + mSeekbar2KHz.getProgress());
		
		int p1 = mSeekbar31Hz.getProgress();
		int p2 = mSeekbar62Hz.getProgress();
		int p3 = mSeekbar125Hz.getProgress();
		int p4 = mSeekbar250Hz.getProgress() ;
		int p5 = mSeekbar500Hz.getProgress() ;
		int p6 = mSeekbar1KHz.getProgress() ;
		int p7 = mSeekbar2KHz.getProgress() ;
		
		if(!fromUser){
			return ;
		}
		setEffect(CommonStatic.EFFECT_TYPE_CUSTOM,p1,p2,p3,p4,p5,p6,p7);
		
	}

	@Override
	public void onStartTrackingTouch(VerticalSeekBar VerticalSeekBar) {
	}

	@Override
	public void onStopTrackingTouch(VerticalSeekBar VerticalSeekBar) {
	}

}
