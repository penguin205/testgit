package com.android.settings;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.settings.util.CustomProgressDialog;
import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;

import android.app.ActionBar;
import android.app.GpsCtrlManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class DefaultPrimarySettings extends SettingsPreferenceFragment implements OnClickListener{
	
	private static final String TAG = "DefaultPrimarySettings";
	private FrameLayout rootLayout;
	private View viewFirst;
	private Button btnPositive;
	private Button btnNegative;
	
	private View viewSecond;
	private View viewThird;
	private Button btnRestoreFinished;
	
	private GpsCtrlManager mService = null;
	private boolean mEraseSdCard = false;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		Context context = this.getActivity().getApplicationContext();
		mService = (GpsCtrlManager)context.getSystemService(Context.GPS_CTRL_SERVICE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_restore, null);
		
		rootLayout = (FrameLayout) view.findViewById(R.id.restore_rootlayout);
		
		viewFirst = view.findViewById(R.id.ll_restore_1st);
		TextView tvTitle = (TextView) view.findViewById(R.id.tv_restore_title);
		tvTitle.setText(R.string.prompt_confirm_restore_primary_factory);
		TextView tvSubTitle = (TextView) view.findViewById(R.id.tv_restore_subTitle);
		tvSubTitle.setText(R.string.prompt_confirm_all_apps_and_copys);
		btnPositive = (Button) view.findViewById(R.id.btn_restore_positive);
		btnPositive.setOnClickListener(this);
		btnNegative = (Button) view.findViewById(R.id.btn_restore_negative);
		btnNegative.setOnClickListener(this);
		
		viewSecond = view.findViewById(R.id.ll_restore_2nd);
		TextView tvRestoreOngoing = (TextView) view.findViewById(R.id.tv_restore_ongoing);
		tvRestoreOngoing.setText(R.string.prompt_ongoing_restore_primay_factory);
		
		viewThird = view.findViewById(R.id.ll_restore_3rd);
		TextView tvRestoreFinished = (TextView) view.findViewById(R.id.tv_restore_finished);
		tvRestoreFinished.setText(R.string.prompt_finish_restore_primay_factory);
		btnRestoreFinished = (Button) view.findViewById(R.id.btn_restore_finished);
		btnRestoreFinished.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		  setCustomActionBar();
		  setWallpaper();
	}
	
	 private void setWallpaper() {
			if(rootLayout != null) {
				FrameLayout fl = (FrameLayout) rootLayout.getParent();
				fl.setBackgroundResource(R.drawable.default_wallpaper);
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
			actionBarTitle.setText(R.string.restore_default_settings);
			actionBar.setCustomView(actionbarLayout);
		}
		
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.btn_restore_negative) {
			getActivity().onBackPressed();
			
		} else if(id == R.id.btn_restore_positive) {
			viewFirst.setVisibility(View.GONE);
			viewSecond.setVisibility(View.VISIBLE);
			// Restore process ...... need add
            if (Utils.isMonkeyRunning()) {
                return;
            }
            viewSecond.post(mRecoverPrimaryFactoryRunnable);
          
        } else if(id == R.id.btn_restore_finished) {
			if (mEraseSdCard ) {
			    Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
			    intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
			    getActivity().startService(intent);
			} else {
			    getActivity().sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
			}
        }
	}
	
	private Runnable mRecoverPrimaryFactoryRunnable = new Runnable() {

		@Override
		public void run() {
			recoverDefault();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			viewSecond.setVisibility(View.GONE);
			viewThird.setVisibility(View.VISIBLE);
		}
		
	};

	IGpsCtrl mListener = new IGpsCtrl.Stub() {
		@Override
		public void onGpsCtrlChange(int arg0) throws RemoteException {
		}
	};
	
	public void recoverDefault(){
 		
 		//SoundEffectSettings
 		byte[] rCustomV = {12, 12, 12, 0, 0, 0};
		setEffect(CommonStatic.EFFECT_TYPE_CUSTOM,
				rCustomV[0],
				rCustomV[1],
				rCustomV[2],
				rCustomV[3],
				rCustomV[4],
				rCustomV[5]);
		
		//EQ
		setEqVoice(0);
		//Volume
		setVolumeToMcu(10);
		//
		setArmVoice();
		//SoundSpakerSettings
		byte[] actions = new byte[4];
		String[] strs = new String[1];
		
		if(null!=mService){
			try {
                actions[0] = 0;
                actions[1] = 0;
                actions[2] = 0;
                actions[3] = 0;
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_WRITE_SPEEKER_SET,4, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.w(TAG, "getInitData,mService == null");
        }
		
	}
 	
	private void setVolumeToMcu(int vol) {
		byte[] actions = new byte[1];
		String[] strs = new String[1];
		
		if(vol > CommonStatic.Max_Volume) {
			vol = CommonStatic.Max_Volume;
		}else if(vol < 0) {
			vol = 0;
		}
		
		if(null!=mService){
			try {
				actions[0] = (byte) vol;
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_SET_VOLUME,1, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setArmVoice(){
		byte[] actions = new byte[1];
		String[] strs = new String[1];	
		if(null!=mService){
			try {
				actions[0] = CommonStatic.AUDIO_CHANEL_TYPE_ARM;
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_TAGOL_SOURCE,1, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
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
				Log.e(TAG,"setEffect try sendComm");
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_SET_EFFECT,7, actions, 0, strs);
			} catch (RemoteException e) {
				Log.e(TAG,e.toString());
			}
		}else{
        	Log.e(TAG, "setEffect,mService == null");
        }
	}
 	
 	
 	private void setEqVoice(int state){
		byte[] actions = new byte[1];
		String[] strs = new String[1];
		
		if(null!=mService){
			try {
				actions[0] = (byte)(state%2);
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_SET_SPEEKER_EQ,1, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.w(TAG, "getInitData,mService == null");
        }
	}
	
}
