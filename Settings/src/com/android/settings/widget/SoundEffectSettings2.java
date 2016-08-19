package com.android.settings.widget;

import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;//add by czy for refactor;
import com.chinagps.service.IMcuMsgServer;
import android.app.GpsCtrlManager;


import com.android.settings.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class SoundEffectSettings2 extends  Activity implements SeekBar.OnSeekBarChangeListener{
	private static String TAG = "SoundEffectSettings";

	private SeekBar mSeekbarLowVol = null;
	private SeekBar mSeekbarMidVol = null;
	private SeekBar mSeekbarHhtVol = null;
	
	private SeekBar mSeekbarLPH = null;
	private SeekBar mSeekbarMPH = null;
	private SeekBar mSeekbarHPH = null;

	private byte[] mCustomV = new byte[]{0,0,0,0,0,0};
	
	private TextView mTextLowVol = null;
	private TextView mTextMidVol = null;
	private TextView mmTextHhtVol = null;
	
	private TextView mTextLPH = null;
	private TextView mTextMPH = null;
	private TextView mTextHPH = null;
	private GpsCtrlManager mService = null;
	
	private ImageButton  	mBtnEffectPop = null;
	private ImageButton 	mBtnEffectJazz = null;
	private ImageButton 	mBtnEffectRock = null;
	private ImageButton 	mBtnEffectGood = null;
	private ImageButton 	mBtnEffectCustom = null;
	private ImageButton 	mBtnEffectFlat = null;
	
	
	Handler mDeviceEventHandler = new Handler(){

		public void handleMessage(Message msg) {
			
		Log.d(TAG,"msg.what: 0x"+Integer.toHexString(msg.what));

		Bundle hBundle = msg.getData();
		byte[] hDesData = hBundle.getByteArray("desData");
		
		switch(msg.what){
		case CommonStatic.CM_SOUND_MGR_READ_CURR_EFFECT:
		case CommonStatic.CM_SOUND_MGR_SET_EFFECT:
			mBtnEffectCustom.setSelected(false);
			mBtnEffectGood.setSelected(false);
			mBtnEffectRock.setSelected(false);
			mBtnEffectJazz.setSelected(false);
			mBtnEffectPop.setSelected(false);
			mBtnEffectFlat.setSelected(false);
			byte [] margsI = new byte[32];
			switch(0xff&hDesData[0]){
			case CommonStatic.EFFECT_TYPE_CUSTOM:
				mBtnEffectCustom.setSelected(true);
				mCustomV[0] = hDesData[1];
				mCustomV[1] = hDesData[2];
				mCustomV[2] = hDesData[3];
				mCustomV[3] = hDesData[4];
				mCustomV[4] = hDesData[5];
				mCustomV[5] = hDesData[6];
				break;
			case CommonStatic.EFFECT_TYPE_GOOD:
				mBtnEffectGood.setSelected(true);
				break;
			case CommonStatic.EFFECT_TYPE_ROCK:
				mBtnEffectRock.setSelected(true);
				break;
			case CommonStatic.EFFECT_TYPE_JAZZ:
				mBtnEffectJazz.setSelected(true);
				break;
			case CommonStatic.EFFECT_TYPE_POP:
				mBtnEffectPop.setSelected(true);
				break;
			case CommonStatic.EFFECT_TYPE_FLAT:
				mBtnEffectFlat.setSelected(true);
				break;
			}
			
			for(int i=0; i<mCustomV.length; i++) {
				Log.d(TAG, "mCustomV [" + i + "] :"+ (0xff&hDesData[i]));
			}
			mSeekbarLowVol.setProgress(0xff&hDesData[1]);
			mSeekbarMidVol.setProgress(0xff&hDesData[2]);
			mSeekbarHhtVol.setProgress(0xff&hDesData[3]);
			mSeekbarLPH.setProgress(0xff&hDesData[4]);
			mSeekbarMPH.setProgress(0xff&hDesData[5]);
			mSeekbarHPH.setProgress(0xff&hDesData[6]);
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

 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_effect_settings2);
		mSeekbarLowVol = (SeekBar)findViewById(R.id.seekBar_low_voice);
		mSeekbarLowVol.setOnSeekBarChangeListener(this);
		mSeekbarMidVol = (SeekBar)findViewById(R.id.seekBar_midle_voice);
		mSeekbarMidVol.setOnSeekBarChangeListener(this);
		mSeekbarHhtVol = (SeekBar)findViewById(R.id.seekBar_height_voice);
		mSeekbarHhtVol.setOnSeekBarChangeListener(this);
		
		mSeekbarLPH = (SeekBar)findViewById(R.id.seekBar_lph);
		mSeekbarLPH.setOnSeekBarChangeListener(this);
		mSeekbarMPH = (SeekBar)findViewById(R.id.seekBar_mph);
		mSeekbarMPH.setOnSeekBarChangeListener(this);
		mSeekbarHPH = (SeekBar)findViewById(R.id.seekBar_hph);
		mSeekbarHPH.setOnSeekBarChangeListener(this);
		
		mBtnEffectPop = (ImageButton)findViewById(R.id.btn_pop);
		mBtnEffectJazz = (ImageButton)findViewById(R.id.btn_jazz);
		mBtnEffectRock = (ImageButton)findViewById(R.id.btn_rock);
		mBtnEffectGood = (ImageButton)findViewById(R.id.btn_good);
		mBtnEffectCustom = (ImageButton)findViewById(R.id.btn_custom);
		mBtnEffectFlat = (ImageButton)findViewById(R.id.btn_flat);
		
		mBtnEffectCustom.setOnClickListener(mEffectTypeSetOnClickListener);
		mBtnEffectGood.setOnClickListener(mEffectTypeSetOnClickListener);
		mBtnEffectRock.setOnClickListener(mEffectTypeSetOnClickListener);
		mBtnEffectJazz.setOnClickListener(mEffectTypeSetOnClickListener);
		mBtnEffectPop.setOnClickListener(mEffectTypeSetOnClickListener);
		mBtnEffectFlat.setOnClickListener(mEffectTypeSetOnClickListener);

		mTextLowVol = (TextView)findViewById(R.id.text_low_voice);
		mTextMidVol = (TextView)findViewById(R.id.text_midle_voice);
		mmTextHhtVol = (TextView)findViewById(R.id.text_height_voice);
		
		mTextLPH = (TextView)findViewById(R.id.text_lph);
		mTextMPH = (TextView)findViewById(R.id.text_mph);
		mTextHPH = (TextView)findViewById(R.id.text_hph);
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
	
	
	private View.OnClickListener mEffectTypeSetOnClickListener = 
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mBtnEffectCustom.setSelected(false);
					mBtnEffectGood.setSelected(false);
					mBtnEffectRock.setSelected(false);
					mBtnEffectJazz.setSelected(false);
					mBtnEffectPop.setSelected(false);
					mBtnEffectFlat.setSelected(false);
					
					if(v==mBtnEffectPop){
						mBtnEffectPop.setSelected(true);
						setEffect(CommonStatic.EFFECT_TYPE_POP);
					}else if(v==mBtnEffectJazz){
						mBtnEffectJazz.setSelected(true);
						setEffect(CommonStatic.EFFECT_TYPE_JAZZ);
					}else if(v==mBtnEffectRock){
						mBtnEffectRock.setSelected(true);
						setEffect(CommonStatic.EFFECT_TYPE_ROCK);
					}else if(v==mBtnEffectGood){
						setEffect(CommonStatic.EFFECT_TYPE_GOOD);
						mBtnEffectGood.setSelected(true);
					}else if(v==mBtnEffectFlat){
						setEffect(CommonStatic.EFFECT_TYPE_FLAT);
						mBtnEffectFlat.setSelected(true);
					}else if(v==mBtnEffectCustom){
						mBtnEffectCustom.setSelected(true);
						/*
						setEffect(CommonStatic.EFFECT_TYPE_CUSTOM,
								mSeekbarLowVol.getProgress(),
								mSeekbarMidVol.getProgress(),
								mSeekbarHhtVol.getProgress(),
								mSeekbarLPH.getProgress(),
								mSeekbarMPH.getProgress(),
								mSeekbarHPH.getProgress()
								);
								*/
						setEffect(CommonStatic.EFFECT_TYPE_CUSTOM,
								mCustomV[0],
								mCustomV[1],
								mCustomV[2],
								mCustomV[3],
								mCustomV[4],
								mCustomV[5]);
					}
				}
			};

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
	

	private void setEffect(int type,int iLVol,int iMVol,int iHVol,int iLPH,int iMPH,int iHPH) {
		byte[] actions = new byte[7];
		String[] strs = new String[1];
		actions[0] = (byte)type;
		actions[1] = (byte)iLVol;
		actions[2] = (byte)iMVol;
		actions[3] = (byte)iHVol;
		actions[4] = (byte)iLPH;
		actions[5] = (byte)iMPH;
		actions[6] = (byte)iHPH;
		
		if(null!=mService){
			try {
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_SET_EFFECT,7, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.w(TAG, "setEffect,mService == null");
        }
	}
	
	private void setEffect(int type){
		setEffect(type,0,0,0,0,0,0);
	}


	@Override
	public void onProgressChanged(SeekBar v, int prg, boolean fromUser) {
		
		int p1 = mSeekbarLowVol.getProgress();
		int p2 = mSeekbarMidVol.getProgress();
		int p3 = mSeekbarHhtVol.getProgress();
		int p4 = mSeekbarLPH.getProgress() ;
		int p5 = mSeekbarMPH.getProgress() ;
		int p6 = mSeekbarHPH.getProgress() ;
		
		int sp1 = p1-12;
		int sp2 = p2-12;
		int sp3 = p3-12;
		
		mTextLowVol.setText(((sp1>0?"+":"")+ sp1)+"dB");
		mTextMidVol.setText(((sp2>0?"+":"")+sp2)+"dB");
		mmTextHhtVol.setText(((sp3>0?"+":"")+sp3)+"dB");
		mTextLPH.setText((60 +p4*20 )+"Hz");
		mTextMPH.setText((p5<3?(0.5+p5*0.5):"2.5") + "KHz");
		mTextHPH.setText((7.5+p6*2.5) + "KHz");
		
		if(!fromUser){
			return ;
		}
		
		Log.d(TAG, "onProgressChanged,: "+p2 + ","+p3 + ","+p4 + ","+p5 + ","+p6 + ","+v);
		setEffect(CommonStatic.EFFECT_TYPE_CUSTOM,p1,p2,p3,p4,p5,p6);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		
	}
	
    @Override
    protected void onStart() {
        super.onStart();
		
		mService = (GpsCtrlManager)getSystemService(Context.GPS_CTRL_SERVICE);
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
    @Override
    protected void onDestroy() {
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

}
