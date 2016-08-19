
package com.android.settings;


import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;//add by czy for refactor;
import com.chinagps.service.IMcuMsgServer;
import android.app.GpsCtrlManager;


import com.android.settings.R;
import com.android.settings.SubSettings.MyTouchListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SoundSpeakerSettings extends SettingsPreferenceFragment {
	private static String TAG = "SoundSettings";

	private GpsCtrlManager mService = null;
	private static final int mOffset = 30;
	private static final int mPerCellSize = 23;
	
	private Button 	mBtnPhaseFronce = null;
	private Button 	mBtnPhaseBack = null;
	private Button 	mBtnPhaseLeft = null;
	private Button 	mBtnPhaseRight = null;
//	private Button 	mBtnDxOnOff = null;
//	private Button 	mBtnPhaseReset = null;
	
	private RelativeLayout rootRelativeLayout = null;
	private RelativeLayout relativeLayoutRefrash=null;
	
	private int mVoiceRase = 0;
	private int mEqVoice = 0;
    public Point cur = new Point(0,0);

	
	Handler mDeviceEventHandler = new Handler(){

		public void handleMessage(Message msg) {
			
			Log.d(TAG,"msg.what: 0x"+Integer.toHexString(msg.what));

			Bundle hBundle = msg.getData();
			byte[] hDesData = hBundle.getByteArray("desData");
			
			switch(msg.what){
				case CommonStatic.CM_SOUND_MGR_READ_SPEEKER_SET:
					int lf= (0xff&hDesData[0]),rf=(0xff&hDesData[1]),
					lb=(0xff&hDesData[2]),rb=(0xff&hDesData[3]);
	                SharedPreferences sp= getActivity().getSharedPreferences("Point", Context.MODE_PRIVATE); 
	                int x =sp.getInt("X", 0);
	                int y =sp.getInt("Y", 0);
	                Log.d(TAG,"======="+cur.getXPoint()+";"+cur.getYPoint());
					break;
				case CommonStatic.CM_SOUND_MGR_READ_SPEEKER_EQ:
					mEqVoice = hBundle.getInt("mEqVoiceStub");
				//	mBtnDxOnOff.setSelected(mEqVoice==0?false:true);
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

			Bundle bMcuData = new Bundle();
			byte[] srcData = mService.getSoundMgrReadSpeekerSet();
			byte[] desData = new byte[32];
			System.arraycopy(srcData, 0, desData, 0, srcData.length);
			
			bMcuData.putByteArray("desData", desData);
			bMcuData.putInt("mEqVoiceStub",mService.getEqVoice());

			Message mMcuMessage = Message.obtain();
			mMcuMessage.what = arg0;
			mMcuMessage.setData(bMcuData);
			
			mDeviceEventHandler.sendMessage(mMcuMessage);
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_speaker_settings2, null);
		rootRelativeLayout = (RelativeLayout)view.findViewById(R.id.root_relativeLayout);
		relativeLayoutRefrash =(RelativeLayout)view.findViewById(R.id.page_phase_icon);
		
		mBtnPhaseFronce = (Button)view.findViewById(R.id.btn_fron);
		mBtnPhaseBack = (Button)view.findViewById(R.id.btn_back);
		mBtnPhaseLeft = (Button)view.findViewById(R.id.btn_left);
		mBtnPhaseRight = (Button)view.findViewById(R.id.btn_right);
//		mBtnPhaseReset = (Button)view.findViewById(R.id.btn_s_reset);
		
		mBtnPhaseFronce.setOnClickListener(mPhaseButonOnClickListener);
		mBtnPhaseBack.setOnClickListener(mPhaseButonOnClickListener);
		mBtnPhaseLeft.setOnClickListener(mPhaseButonOnClickListener);
		mBtnPhaseRight.setOnClickListener(mPhaseButonOnClickListener);
//		mBtnPhaseReset.setOnClickListener(mPhaseButonOnClickListener);
		
//		mBtnDxOnOff = (Button)view.findViewById(R.id.btn_s_onoff);
//		mBtnDxOnOff.setOnClickListener(mBtnDxOnffListener);
		return view;
	}
 	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mVoiceRase = getResources().getInteger(R.integer.voice_rase);
		
		bmIcon = BitmapFactory.decodeResource(getResources(), R.drawable.tiaojie);
		
		//初始化画笔
		initPaint();
		
		SharedPreferences mySharedPreferences= getActivity().getSharedPreferences("Point", 
				Context.MODE_PRIVATE);
		int tempX = mySharedPreferences.getInt("X_point", 0);
		int tempY = mySharedPreferences.getInt("Y_point", 0);
		cur.setPoint(tempX, tempY);//the center of cycle 135+190=325;
		myDraw(cur.getXPoint()+190, cur.getYPoint()+190);
		
		setPhase();
		
		setActionBar();
		
		setWallpaper();
		
		((SubSettings)this.getActivity()).registerMyTouchListener(mTouchListener);
	}
	
	 /**
	  * receive SoundActivity's touch event callback
	  * @author XiaoYJ
	  */
	private int cx=0;
	private int cy=0;
	private MyTouchListener mTouchListener = new MyTouchListener() {
	 
	        @Override
	        public boolean onTouchEvent(MotionEvent event) {
	 
	        	switch (event.getAction()) {
	    		case MotionEvent.ACTION_DOWN:	
	    			cx = (int) event.getX();
	    			cy = (int) event.getY();
	    			Log.d(TAG, "posmyDraw(),cx:"+ cx +",cy:"+ cy );
	    			if(	cx>=322
	    				&&cx<=702
	    				&&cy>=135
	    				&&cy<=515){
	    				
	    				cur.setPoint((cx-512), (cy-325));//the center of cycle 135+190=325;		
	    				myDraw(cur.getXPoint()+190,cur.getYPoint()+190);
	    				setPhase();
	    			}
	    			break;
	    		case MotionEvent.ACTION_MOVE:
	    			// 移动
	    			cx = (int) event.getX();
	    			cy = (int) event.getY();
	    			Log.d(TAG, "posmyDraw(),cx:"+ cx +",cy:"+ cy );
	    			if(	cx>=322
	    					&&cx<=702
	    					&&cy>=135
	    					&&cy<=515){
	    				cur.setPoint((cx-512), (cy-325));//the center of cycle 135+190=325;
	    				Log.e(TAG ,"czyMOVE tempCurPointX="+cur.getXPoint());
	    				Log.e(TAG ,"czyMOVE tempCurPointY="+cur.getYPoint());
	    				myDraw(cur.getXPoint()+190,cur.getYPoint()+190);
	    				setPhase();
	    			}
	    			break;
	    		case MotionEvent.ACTION_UP:
	    			// 抬起
//	    			cx = (int) event.getX();
//	    			cy = (int) event.getY();
//	    			if(323<=cx&&cx<=690&&cy>=100&&cy<=500){
//	    				myDraw(cx,cy);
//	    				cur.setPoint((cx-512)*7/189, (cy-300)*7/189);
//	    				setPhase();
//	    			}
	    			break;
	    		}
	    		return true;  
	        }
	 
	};
	 

    private void setWallpaper() {
		FrameLayout rootLayout = (FrameLayout) rootRelativeLayout.getParent();
		rootLayout.setBackgroundResource(R.drawable.default_wallpaper);
  	}
    
	private void setActionBar() {
		Activity activity = getActivity();
		activity.getActionBar().setDisplayHomeAsUpEnabled(false);
		activity.getActionBar().setHomeButtonEnabled(true);
		activity.getActionBar().setDisplayShowHomeEnabled(false);
		activity.getActionBar().setDisplayShowTitleEnabled(false);
		activity.getActionBar().setDisplayShowCustomEnabled(true);
		View actionbarLayout = LayoutInflater.from(activity).inflate(
				R.layout.actionbar_common_layout, null);
		TextView title = (TextView) actionbarLayout.findViewById(R.id.tv_actionbar_title);
		title.setText(R.string.sound_effect_balance);
		activity.getActionBar().setCustomView(actionbarLayout);
	}
	
	private Paint paint=null; 
	private Bitmap bmp=null;
	private Bitmap bmIcon=null;
	private Canvas canvas = null;
 
	private void initPaint(){
		paint = new Paint();
		//设置消除锯齿
		paint.setAntiAlias(true);
		//设置画笔颜色
		paint.setColor(Color.BLACK);
		Log.d("srun","init" );
	}
  
  private int a=0;
  private int b=0;
  
  private void myDraw(int x,int y){
	  bmp = Bitmap.createBitmap(380, 380, Bitmap.Config.ARGB_8888); //设置位图的宽高,bitmap为透明  
//	  bmp = Bitmap.createBitmap(1024, 600, Bitmap.Config.ARGB_8888); //设置位图的宽高,bitmap为透明  
	  canvas = new Canvas(bmp);
      canvas.drawColor(Color.TRANSPARENT);//设置为透明，画布也是透明  
      //在画布上贴张小图  
       
      Log.d(TAG, "posmyDraw(),a:"+ a +",b:"+ b );
      
      //center of a circle
      if(x<30){
    	  x=30; 
      }
      if(y<30){
    	  y=30;
      }
      if(x>380-mOffset){
    	  x=380-mOffset;
      }
      if(y>380-mOffset){
    	  y=380-mOffset;
      }
      canvas.drawBitmap(bmIcon, x-30, y-30, paint);  
      Drawable drawable = new BitmapDrawable(bmp) ;  
//      relativeLayout.setBackgroundDrawable(drawable);
      relativeLayoutRefrash.setBackgroundDrawable(drawable);
  }
//	private View.OnClickListener mBtnDxOnffListener = new View.OnClickListener(){
//		@Override
//		public void onClick(View arg0) {
//			mEqVoice += 1;
//			mEqVoice %= 2;
//			mBtnDxOnOff.setSelected(mEqVoice==0?false:true);
//			setEqVoice(mEqVoice);
//		}
//	};

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
	
	private void getPhase(){
		byte[] actions = new byte[1];
		String[] strs = new String[1];
		
		if(null!=mService){
			try {
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_READ_SPEEKER_SET,0, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.w(TAG, "getInitData,mService == null");
        }
	}
	
	private void setPhase(){    
        int lf = 0,rf = 0,lb =0,rb =0;
		if(cur.getXStep() > 0){
		    lf = Math.abs(Math.round(cur.getXStep()));
		    lb = Math.abs(Math.round(cur.getXStep()));
		}else{
            rf = Math.abs(Math.round(cur.getXStep()));
            rb = Math.abs(Math.round(cur.getXStep()));
		}
		
        if(cur.getYStep() > 0){
            lf = lf > Math.abs(Math.round(cur.getYStep())) ? lf : Math.abs(Math.round(cur.getYStep()));
            rf = rf > Math.abs(Math.round(cur.getYStep())) ? rf : Math.abs(Math.round(cur.getYStep()));
        }else{
            lb = lb > Math.abs(Math.round(cur.getYStep())) ? lb : Math.abs(Math.round(cur.getYStep()));
            rb = rb > Math.abs(Math.round(cur.getYStep())) ? rb : Math.abs(Math.round(cur.getYStep())); 
        }
 
		Log.d(TAG, "setPhase,lf:"+ lf +",rf:"+ rf +",lb:"+lb+",rb:"+rb);
		
        SharedPreferences mySharedPreferences= getActivity().getSharedPreferences("Point", Context.MODE_PRIVATE); 
        SharedPreferences.Editor editor = mySharedPreferences.edit();         
        editor.putInt("X_point", cur.getXPoint());
        editor.putInt("Y_point", cur.getYPoint());
        editor.commit();
		
		byte[] actions = new byte[4];
		String[] strs = new String[1];
		
		if(null!=mService){
			try {
                actions[0] = (byte)(lf);
                actions[1] = (byte)(rf);
                actions[2] = (byte)(lb);
                actions[3] = (byte)(rb);
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_WRITE_SPEEKER_SET,4, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.w(TAG, "getInitData,mService == null");
        }
	}
	private View.OnClickListener mPhaseButonOnClickListener = 
		new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
				int tempCurStepX = 0;
				int tempCurStepY = 0;
				int tempCurPointX = cur.getXPoint();
				int tempCurPointY = cur.getYPoint();
				if(v==mBtnPhaseFronce){
					//
					tempCurStepY=(int) Math.ceil(cur.getYStep());
					tempCurStepY = (tempCurStepY-1);
					if(tempCurStepY<=-7){
						tempCurStepY=-7;
					}
					tempCurPointY = tempCurStepY*mPerCellSize;
				}else if(v==mBtnPhaseBack){
					//
					tempCurStepY=(int) Math.floor(cur.getYStep());
					tempCurStepY = (tempCurStepY+1);
					if(tempCurStepY>7){
						tempCurStepY=7;
					}
					tempCurPointY = tempCurStepY*mPerCellSize;
				}else if(v==mBtnPhaseLeft){
					//
					tempCurStepX=(int) Math.ceil(cur.getXStep());
					tempCurStepX=(tempCurStepX-1);
					if(tempCurStepX<=-7){
						tempCurStepX=-7;
					}
					tempCurPointX = tempCurStepX*mPerCellSize;
				}else if(v==mBtnPhaseRight){
					//
					tempCurStepX=(int) Math.floor(cur.getXStep());
					tempCurStepX=(tempCurStepX+1);
					if(tempCurStepX>7){
						tempCurStepX=7;
					}
					tempCurPointX = tempCurStepX*mPerCellSize;
				}
//				else if(v==mBtnPhaseReset){
//					//
//					tempCurPointX=0;
//					tempCurPointY=0;
//				}

				cur.setPoint(tempCurPointX,tempCurPointY);
				myDraw(cur.getXPoint()+190,cur.getYPoint()+190);//相对位置
				setPhase();
			}
		};
			
    @Override
	public void onStart() {
        super.onStart();
		mService = (GpsCtrlManager)getSystemService(Context.GPS_CTRL_SERVICE);
		if(mListener !=null){
			try {
				mService.registerEventListener(mListener,mListener.hashCode());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}   
		getPhase();
        getEqVoice();
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
        	Log.d(TAG, "onDestroy,mService == null");
        }
    	
    	if(mTouchListener != null) {
    		((SubSettings)this.getActivity()).registerMyTouchListener(mTouchListener);
    		mTouchListener = null;
    	}
        super.onDestroy();
    }
}
