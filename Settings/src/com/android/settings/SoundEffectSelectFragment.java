package com.android.settings;

import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.GpsCtrlManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class SoundEffectSelectFragment extends SettingsPreferenceFragment implements OnItemClickListener {
	
	private int[] mUnselectImageRes = {
			R.drawable.ic_unselected_classic, 
			R.drawable.ic_unselected_pop, 
			R.drawable.ic_unselected_standard,
			R.drawable.ic_unselected_jazz,
			R.drawable.ic_unselected_rock,
			R.drawable.ic_unselected_user };
	
	private int[] mSelectImageRes = {
			R.drawable.ic_selected_classic, 
			R.drawable.ic_selected_pop, 
			R.drawable.ic_selected_standard,
			R.drawable.ic_selected_jazz,
			R.drawable.ic_selected_rock,
			R.drawable.ic_selected_user };
	
	private int[] mEffectNameResIds = { 
			R.string.classic, R.string.pop, 
			R.string.flat, R.string.jazz,
			R.string.rock, R.string.custom };
	
	private static final int INDEX_CLASSIC = 0;
	private static final int INDEX_POP = 1;
	private static final int INDEX_STANDARD = 2;
	private static final int INDEX_JAZZ = 3;
	private static final int INDEX_ROCK = 4;
	private static final int INDEX_CUSTOM = 5;
	
	private GpsCtrlManager mService = null;

	private GridView mGridView;
	private SoundEffectAdapter mAdapter;
	private float mDensity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sound_effect_select, null);
		mGridView = (GridView) view.findViewById(R.id.sound_effect_select_gridview);
		
		// Get Px density
	    DisplayMetrics outMetrics = new DisplayMetrics();  
	    getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);  
	    mDensity = outMetrics.density;
	    // Log.d("ffff", "density:" + mDensity );
	    // 根据item的数目，动态设定gridview的宽度  (itemWidth = 240dp, space = 20dp)
	    ViewGroup.LayoutParams params = mGridView.getLayoutParams();  
	    int itemWidth = (int) (240 * mDensity);  
	    int spacingWidth = (int) (20*mDensity);  
	    
	    int listSize = mUnselectImageRes.length;
	    params.width = itemWidth*listSize/2+(listSize-1)/2*spacingWidth;  
	    mGridView.setStretchMode(GridView.NO_STRETCH);
	    mGridView.setNumColumns(listSize/2);  
	    mGridView.setHorizontalSpacing(spacingWidth); 
	    mGridView.setVerticalSpacing(spacingWidth);
	    mGridView.setColumnWidth(itemWidth);  
	    mGridView.setLayoutParams(params);  

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setCustomActionBar();
		setWallpaper();
	    
		mAdapter = new SoundEffectAdapter(getActivity());
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		
		mService = (GpsCtrlManager)getActivity().getSystemService(Context.GPS_CTRL_SERVICE);
		if(mListener !=null){
			try {
				mService.registerEventListener(mListener,mListener.hashCode());
				getCurEffect();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}  
	}
	
	@Override
	public void onDestroy() {
    	if (mService != null) {
            try {
                mService.unregisterEventListener(mListener.hashCode());
            } catch (RemoteException e) {
            	Log.e("ffff", e.getMessage());
            }
        }else{
        	Log.w("ffff", "onDestroy,mService == null");
        }
        super.onDestroy();
	}
	
	protected void getCurEffect() {
		byte[] actions = new byte[1];
		String[] strs = new String[1];
		
		if(null!=mService){
			try {
				mService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MGR_READ_CURR_EFFECT,0, actions, 0, strs);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
        	Log.d("ffff", "getInitData,mService == null");
        }
	}
	
	Handler mDeviceEventHandler = new Handler(){
		
		int curEffect = -1;
		
		public void handleMessage(Message msg) {
			
		Log.d("ffff","msg.what: 0x"+Integer.toHexString(msg.what));

		Bundle hBundle = msg.getData();
		byte[] hDesData = hBundle.getByteArray("desData");
		
		switch(msg.what){
		case CommonStatic.CM_SOUND_MGR_READ_CURR_EFFECT:
		case CommonStatic.CM_SOUND_MGR_SET_EFFECT:
			switch(0xff&hDesData[0]){
			case CommonStatic.EFFECT_TYPE_CUSTOM:
				curEffect = INDEX_CUSTOM;
				break;
			case CommonStatic.EFFECT_TYPE_GOOD:
				curEffect = INDEX_CLASSIC;
				break;
			case CommonStatic.EFFECT_TYPE_ROCK:
				curEffect = INDEX_ROCK;
				break;
			case CommonStatic.EFFECT_TYPE_JAZZ:
				curEffect = INDEX_JAZZ;
				break;
			case CommonStatic.EFFECT_TYPE_POP:
				curEffect = INDEX_POP;
				break;
			case CommonStatic.EFFECT_TYPE_FLAT:
				curEffect = INDEX_STANDARD;
				break;
			}
			mAdapter.setSelection(curEffect);
			mAdapter.notifyDataSetChanged();
			break;
			
			default:
				Log.e("ffff", "onDevEvent(),action error,action:0x"+Integer.toHexString(msg.what));
				break;
			}
		}
	};


	IGpsCtrl mListener = new IGpsCtrl.Stub(){
		@Override
		public void onGpsCtrlChange(int arg0) throws RemoteException {
			Log.d("ffff", "xyj onGpsCtrlChange action=0x" + Integer.toHexString(arg0));
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


    private void setWallpaper() {
		if(mGridView != null) {
			FrameLayout rootLayout = (FrameLayout) mGridView.getParent().getParent();
			rootLayout.setBackgroundResource(R.drawable.default_wallpaper);
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
		actionBarTitle.setText(R.string.equalizer);
		actionBar.setCustomView(actionbarLayout);
	}
	
	public class SoundEffectAdapter extends BaseAdapter {
		
		private Context mContext;
		private int mSelectedPosition = 0;

		public class ViewHolder {
			ImageView imageView;
			TextView textView;
		}
		
		public SoundEffectAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			return mUnselectImageRes.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		public void setSelection(int position) {
			this.mSelectedPosition = position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.sound_effect_select_item, null);
				holder.imageView = (ImageView) convertView.findViewById(R.id.sound_effect_item_image);
				holder.textView = (TextView) convertView.findViewById(R.id.sound_effect_item_text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.textView.setText(mEffectNameResIds[position]);
			
			if(mSelectedPosition == position) {
				holder.imageView.setImageResource(mSelectImageRes[position]);
				holder.imageView.setPadding(2, 2, 2, 2);
				holder.imageView.setBackgroundResource(R.drawable.gridview_selected_background);
			} else {
				holder.imageView.setImageResource(mUnselectImageRes[position]);
				holder.imageView.setPadding(0, 0, 0, 0);
				holder.imageView.setBackground(null);
			}
			
			return convertView;
//			ImageView imageView = new ImageView(mContext);
//			// imageSize = 240dp * 190dp
//			imageView.setLayoutParams(new AbsListView.LayoutParams((int)(240 * mDensity), (int)(190 * mDensity)));
//			imageView.setScaleType(ScaleType.CENTER_CROP); 
//			if(mSelectedPosition == position) {
//				imageView.setImageResource(mSelectImageRes[position]);
//			} else {
//				imageView.setImageResource(mUnselectImageRes[position]);
//			}
//			return imageView;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case INDEX_CLASSIC:
			setEffect(CommonStatic.EFFECT_TYPE_GOOD);
			break;
		case INDEX_POP:
			setEffect(CommonStatic.EFFECT_TYPE_POP);
			break;
		case INDEX_STANDARD:
			setEffect(CommonStatic.EFFECT_TYPE_FLAT);
			break;
		case INDEX_JAZZ:
			setEffect(CommonStatic.EFFECT_TYPE_JAZZ);
			break;
		case INDEX_ROCK:
			setEffect(CommonStatic.EFFECT_TYPE_ROCK);
			break;
		case INDEX_CUSTOM:
			startFragment(this, SoundEffectSettings.class.getCanonicalName(), -1, null);
//			Activity act = getActivity();
//			Intent intent = new Intent(com.android.settings.widget.SoundEffectSettings2.class.getName());//Intent.ACTION_MAIN);
//	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//	        act.startActivity(intent);
		default:
			break;
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
        	Log.d("ffff", "setEffect,mService == null");
        }
	}
	
	private void setEffect(int type){
		setEffect(type,0,0,0,0,0,0);
	}
}
