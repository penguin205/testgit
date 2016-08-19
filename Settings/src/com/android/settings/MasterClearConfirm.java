/*
 * Copyright (C) 2010 The Android Open Source Project
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

import com.android.settings.R;
import com.android.settings.util.CustomProgressDialog;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.android.internal.widget.LockPatternUtils;
import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;//add by czy for refactor;
import com.chinagps.service.IMcuMsgServer;
import android.app.GpsCtrlManager;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.RecoverySystem;
import java.io.IOException;
import android.content.Context;
import android.util.Log;


/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the confirmation screen.
 */
public class MasterClearConfirm extends Fragment {

    private View mContentView;
    private boolean mEraseSdCard;
    private Button mFinalButton;
    private Context context;
    
    //private ProgressDialog pd;
    private Toast toast;
    CustomProgressDialog progressDialog;
    
    //private ProgressBar mProgressBar;
    private String TAG = "MasterClear";
    private Activity mActivity;
    
    private GpsCtrlManager mService = null;
    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Checkin Service to reset the device to its factory-default
     * state (rebooting in the process).
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (Utils.isMonkeyRunning()) {
                return;
            }
		   //pd= ProgressDialog.show(context, "","");
           // toast = Toast.makeText(context,
           // 	     "", Toast.LENGTH_LONG);
           // 	   toast.setGravity(Gravity.CENTER, 0, 0);
           // 	   toast.show();
            recoverDefault();
            if (mEraseSdCard) {
                Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                getActivity().startService(intent);
            } else {
            	//creat progress Dialog show waite enter recovery
            	 progressDialog = CustomProgressDialog.createDialog(getActivity());
     			 progressDialog.setMessage(getString(R.string.master_clear));
     			 progressDialog.show(); 
            	
                getActivity().sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                // Intent handling is asynchronous -- assume it will happen soon.
				
            }
        }
    };

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        mFinalButton = (Button) mContentView.findViewById(R.id.execute_master_clear);
        mFinalButton.setOnClickListener(mFinalClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.master_clear_confirm, null);
        establishFinalConfirmationState();
        return mContentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getApplicationContext();
        
        mActivity=this.getActivity();
        
        Bundle args = getArguments();
        mEraseSdCard = args != null ? args.getBoolean(MasterClear.ERASE_EXTERNAL_EXTRA) : false;
        
		mService = (GpsCtrlManager)context.getSystemService(Context.GPS_CTRL_SERVICE);

    }
	
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
 	
	public void onDestroy() {
		super.onDestroy();
		if (mService != null) {
	            try {
	                mService.unregisterEventListener(mListener.hashCode());
	            } catch (RemoteException e) {
	            	Log.e(TAG, e.getMessage());
	            }
	        }else{
	        	Log.d(TAG, "onDestroy,mService == null");
	        }    
    }
}
