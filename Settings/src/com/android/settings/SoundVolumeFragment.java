package com.android.settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.NumberSeekBar.OnNumberSeekBarChangeListener;
import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.GpsCtrlManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SoundVolumeFragment extends Fragment implements OnItemClickListener {
	
	private static String SHARED_PREFERENCES_SYSTEM_SERVICE = "SharedPreferences_SystemService";
	
	private static final int INDEX_NAVI = 0;
	private static final int INDEX_MUSIC = 1;
	private static final int INDEX_CALL = 2;
	private static final int INDEX_ALARM = 3;
	
	private List<String> mSoundCategory;
	private ListView mListView;
	private CheckBox mSwitch;
	private SoundVolumeAdapter mAdapter;
	private GpsCtrlManager mRemoteService = null;

	private AudioManager mAudioManager;
//	private SharedPreferences mPreference;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sound_volume, null);
		mListView = (ListView) view.findViewById(R.id.sound_volume_listView);
		View headerView = inflater.inflate(R.layout.header_sound_volume_listview, mListView, false);
		mListView.setAdapter(null);
		mListView.addHeaderView(headerView);
		
		TextView tv = new TextView(getActivity());
		tv.setPadding(0, 110, 0, 120);
		mListView.addFooterView(tv);
		mListView.setFooterDividersEnabled(false);
		mListView.setHeaderDividersEnabled(false);
		
		mSwitch = (CheckBox) headerView.findViewById(R.id.header_sound_volume_checkbox);
		mSwitch.setChecked(loadMute());
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanseState) {
		super.onActivityCreated(savedInstanseState);

		setCustomActionBar();
		setupListView();
		
		bindMCUService(getActivity());
		
		initData();
		
		mAdapter = new SoundVolumeAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		
//		//通话音量
//		int max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL );
//		 int current = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
//		 Log.d("VIOCE_CALL", "max : " + max + " current : " + current);
//
//		//系统音量
//		max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_SYSTEM );
//		 current = mAudioManager.getStreamVolume( AudioManager.STREAM_SYSTEM );
//		 Log.d("SYSTEM", "max : " + max + " current : " + current);
//		 
//		//铃声音量
//		max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_RING );
//		 current = mAudioManager.getStreamVolume( AudioManager.STREAM_RING );
//		 Log.d("RING", "max : " + max + " current : " + current);
//		 
//		//音乐音量
//		max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
//		 current = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
//		 Log.d("MUSIC", "max : " + max + " current : " + current);
//		 
//		//提示声音音量
//		max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_ALARM );
//		 current = mAudioManager.getStreamVolume( AudioManager.STREAM_ALARM );
//		 Log.d("ALARM", "max : " + max + " current : " + current);
	}
	
	private void saveMute(boolean mute) {
		Context otherProcessContext = null;
		try {
			otherProcessContext = getActivity().createPackageContext("com.cngps.systemservice", Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		SharedPreferences sp = otherProcessContext.getSharedPreferences(SHARED_PREFERENCES_SYSTEM_SERVICE, 
			Context.MODE_WORLD_WRITEABLE + Context.MODE_MULTI_PROCESS);
		sp.edit().putBoolean("muted", mute).commit();
		
		try {
			Runtime.getRuntime().exec("sync");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean loadMute() {
		Context otherProcessContext = null;
		try {
			otherProcessContext = getActivity().createPackageContext("com.cngps.systemservice", Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		SharedPreferences sp = otherProcessContext.getSharedPreferences(SHARED_PREFERENCES_SYSTEM_SERVICE, 
			Context.MODE_WORLD_WRITEABLE + Context.MODE_MULTI_PROCESS);
		Log.d("ffff", "SharedPreferences : mute=" + sp.getBoolean("muted", false));
		return sp.getBoolean("muted", false);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unBindMcuService();
	}
	
	private void initData() {
		//"导航", "音乐", "通话音量" , "系统提示音"
		mSoundCategory = new ArrayList<String>();
		Resources res = getActivity().getResources();
		mSoundCategory.add(res.getString(R.string.navigation));
		mSoundCategory.add(res.getString(R.string.music));
		mSoundCategory.add(res.getString(R.string.voice_call));
		mSoundCategory.add(res.getString(R.string.system_alarm));
	}

	public void bindMCUService(Context context) {
		mRemoteService = (GpsCtrlManager)context.getSystemService(Context.GPS_CTRL_SERVICE);
		if(mListener !=null){
			try {
				mRemoteService.registerEventListener(mListener,mListener.hashCode());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}  
	}
	
	public void unBindMcuService() {
		if (mRemoteService != null) {
			try {
				mRemoteService.unregisterEventListener(mListener.hashCode());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mRemoteService = null;
		}

	}
	
	IGpsCtrl mListener = new IGpsCtrl.Stub(){
		@Override
		public void onGpsCtrlChange(int arg0) throws RemoteException {
			Log.d("ffff", "xyj onGpsCtrlChange action=0x" + Integer.toHexString(arg0));

			Bundle bMcuData = new Bundle();
			byte[] srcData = mRemoteService.getCommonData();
			byte[] desData = new byte[32];
			System.arraycopy(srcData, 0, desData, 0, srcData.length);

			
			bMcuData.putByteArray("desData", desData);
			bMcuData.putInt("desDataLen", mRemoteService.getComAvaiDataLen());
			bMcuData.putInt("desStrLen", mRemoteService.getComStrLen());
			
			Message mMcuMessage = Message.obtain();
			mMcuMessage.what = arg0;
			mMcuMessage.setData(bMcuData);
			
			mDeviceEventHandler.sendMessage(mMcuMessage);
		}
	};
	
	private Handler mDeviceEventHandler = new Handler() {

		private boolean mMuted;

		public void handleMessage(Message msg) {

			Log.d("ffff", "msg.what: 0x" + Integer.toHexString(msg.what));

			Bundle hBundle = msg.getData();
			byte[] hDesData = hBundle.getByteArray("desData");
			int hDesDataLen = hBundle.getInt("desDataLen");
			int hDesStrLen = hBundle.getInt("desStrLen");

			switch (msg.what) {
			case CommonStatic.CM_SOUND_MUTE_CTRL: {
				try {
					Log.d("ffff", "receive CM_SOUND_MUTE_CTRL msg...");
					mMuted = mRemoteService.getMuted();
					// mSwitch.setChecked(mMuted);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
				break;

			default:
				break;
			}
		};
	};
	
	private void setMute(boolean mute){
		byte[] actions = new byte[1];
		String[] strs = new String[1];
		
		if(null!= mRemoteService){
			actions[0] = mute?(byte) 1:0;
			try{
				mRemoteService.sendComm(mListener.hashCode(), CommonStatic.CM_SOUND_MUTE_CTRL,1, actions, 0, strs);
			}catch(RemoteException e){
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * set default wallpaper and list divider
	 */
	private void setupListView() {
		if (mListView != null) {
			FrameLayout ll = (FrameLayout) mListView.getParent().getParent();
			ll.setBackgroundResource(R.drawable.default_wallpaper);
		}
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
		TextView actionBarTitle = (TextView) actionbarLayout
				.findViewById(R.id.tv_actionbar_title);
		actionBarTitle.setText(R.string.sound_volume_settings);
		actionBar.setCustomView(actionbarLayout);
	}
	
	public class SoundVolumeAdapter extends BaseAdapter {
		
		public class ViewHolder {
			TextView tvTitle;
			NumberSeekBar seekbar;
		}
		private Context mContext;
		
		public SoundVolumeAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			return mSoundCategory.size();
		}

		@Override
		public Object getItem(int position) {
			return mSoundCategory.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.sound_volume_list_item, null);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.sound_volume_item_text);
				holder.seekbar = (NumberSeekBar) convertView.findViewById(R.id.sound_volume_item_seekbar);
				setDefaultSoundVolume(position, holder.seekbar);
				holder.seekbar.setOnNumberSeekBarChangeListener(new OnNumberSeekBarChangeListener() {
					
					@Override
					public void callback(int progress) {
						setSoundVolume(position, progress);
					}
				});
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvTitle.setText(mSoundCategory.get(position));
			return convertView;
		}

		private void setDefaultSoundVolume(final int position, NumberSeekBar seekbar) {
			int max, current;
			switch (position) {
			case INDEX_NAVI:
				
				break;
			case INDEX_MUSIC:
				//音乐音量
				max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC );
				current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC );
				seekbar.setMax(max);
				seekbar.setProgress(current);
				break;
			case INDEX_CALL:
				max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL );
				current = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
				seekbar.setMax(max);
				seekbar.setProgress(current);
				break;
			case INDEX_ALARM:
				max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_ALARM );
				current = mAudioManager.getStreamVolume( AudioManager.STREAM_ALARM );
				seekbar.setMax(max);
				seekbar.setProgress(current);
				break;

			default:
				break;
			}
		}
		
		private void setSoundVolume(int position, int progress) {
			switch (position) {
			case INDEX_NAVI:
				
				break;
			case INDEX_MUSIC:
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND); 
				break;
			case INDEX_CALL:
				mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, progress, AudioManager.FLAG_PLAY_SOUND); 
				break;
			case INDEX_ALARM:
				mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress, AudioManager.FLAG_PLAY_SOUND); 
				break;

			default:
				break;
			}
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position == 0) {
			if(!mSwitch.isChecked()) { //关闭状态
				mSoundCategory.clear();
				mSwitch.setChecked(true);
				setMute(true);
				saveMute(true);
			} else {
				initData();
				mSwitch.setChecked(false);
				setMute(false);
				saveMute(false);
			}
			mAdapter.notifyDataSetChanged();
		}
		
	}
}
