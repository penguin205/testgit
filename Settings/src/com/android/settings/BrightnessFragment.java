package com.android.settings;

import com.android.settings.NumberSeekBar.OnNumberSeekBarChangeListener;
import com.chinagps.service.CommonStatic;
import com.chinagps.service.IGpsCtrl;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.GpsCtrlManager;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class BrightnessFragment extends Fragment implements OnItemClickListener {

	private String[] DayAndNight;
	private static final int DAY_POSITON = 0;
	private static final int NIGHT_POSITON = 1;
	
	private ListView mListView;
	private ScreenBrightnessTool mBrightnessTool;
	private SharedPreferences mPreferences;
	private BrightnessAdapter mAdapter;
	private GpsCtrlManager mService = null;
	
	private boolean mbMainLightOpen;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_brightness, null);
		mListView = (ListView) view.findViewById(R.id.brightness_listView);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanseState) {
		super.onActivityCreated(savedInstanseState);

		setCustomActionBar();
		setupListView();

		Activity activity = getActivity();
		
		DayAndNight = new String[] {activity.getResources().getString(R.string.daytime),
				activity.getResources().getString(R.string.night)};
		
		mAdapter = new BrightnessAdapter(activity);
		mListView.setAdapter(mAdapter);
		mListView.setItemChecked(0, true);
		
		ScreenBrightnessTool.stopAutoBrightness(activity); // 停止自动调节亮度模式
		mBrightnessTool = ScreenBrightnessTool.Builder(activity);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
		
		mService = (GpsCtrlManager)activity.getSystemService(Context.GPS_CTRL_SERVICE);
		if(mListener !=null){
			try {
				mService.registerEventListener(mListener,mListener.hashCode());
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
		} else {
			Log.w("ffff", "onDestroy,mService == null");
		}
		setCurBrightness(mbMainLightOpen ? NIGHT_POSITON:DAY_POSITON); //恢复亮度
		super.onDestroy();
	}
    
	IGpsCtrl mListener = new IGpsCtrl.Stub() {
		@Override
		public void onGpsCtrlChange(int arg0) throws RemoteException {
			Log.d("ffff", "xyj onGpsCtrlChange action=0x" + Integer.toHexString(arg0));

			Bundle bMcuData = new Bundle();
			byte[] srcData = mService.getCommonData();
			byte[] desData = new byte[32];
			System.arraycopy(srcData, 0, desData, 0, srcData.length);

			bMcuData.putByteArray("desData", desData);

			Message mMcuMessage = Message.obtain();
			mMcuMessage.what = arg0;
			mMcuMessage.setData(bMcuData);

			mDeviceEventHandler.sendMessage(mMcuMessage);
		}
	};

	Handler mDeviceEventHandler = new Handler() {

		public void handleMessage(Message msg) {

			Log.d("ffff", "msg.what: 0x" + Integer.toHexString(msg.what));
			Bundle hBundle = msg.getData();
			byte[] hDesData = hBundle.getByteArray("desData");

			switch (msg.what) {
			case CommonStatic.CM_MAIN_LIGHT: {
				if ((0xff & hDesData[0]) == 1) {
					mbMainLightOpen = true; // MainLight has opened, it's night
					mListView.setItemChecked(NIGHT_POSITON, true);
					setCurBrightness(NIGHT_POSITON);
				} else {
					mbMainLightOpen = false; // MainLight has closed, it's day
					mListView.setItemChecked(DAY_POSITON, true);
					setCurBrightness(DAY_POSITON);
				}
			}
				break;
			}
		}
	};
	
	/**
	 * set default wallpaper and list divider
	 */
	private void setupListView() {
		if (mListView != null) {
			mListView.setDivider(getResources().getDrawable(R.drawable.line_bg));
			mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			mListView.setOnItemClickListener(this);
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
		actionBarTitle.setText(R.string.brightness_settings);
		actionBar.setCustomView(actionbarLayout);
	}

	public class BrightnessAdapter extends BaseAdapter {

		public class ViewHolder {
			TextView tvDay;
			NumberSeekBar seekbar;
			CheckBox checkbox;
		}

		private Context mContext;

		public BrightnessAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			return DayAndNight.length;
		}

		@Override
		public Object getItem(int position) {
			return DayAndNight[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.brightness_adjust_item, null);
				holder.tvDay = (TextView) convertView.findViewById(R.id.brightness_item_text_day);
				holder.seekbar = (NumberSeekBar) convertView.findViewById(R.id.brightness_item_number_seekbar);
				holder.seekbar.setMax(ScreenBrightnessTool.MAX_BRIGHTNESS);
				// 当前屏幕亮度值
				int systemBrightness = mBrightnessTool.getSystemBrightness();
				int progress = 0;
				if(position == DAY_POSITON) { // 白天
					progress = mPreferences.getInt("day_brightness", systemBrightness);
					holder.seekbar.setProgress(progress);
				} else { // 晚上
					progress = mPreferences.getInt("night_brightness", systemBrightness);
					holder.seekbar.setProgress(progress);
				}
				holder.seekbar.setAllEnabled(false);
				holder.seekbar.setOnNumberSeekBarChangeListener(
						new OnNumberSeekBarChangeListener() {

							@Override
							public void callback(int progress) {
								switch (position) {
								case DAY_POSITON:
									mBrightnessTool.setBrightness(progress);
									mBrightnessTool.saveBrightness(mContext.getContentResolver(), progress);
									mPreferences.edit().putInt("day_brightness", progress).commit();
									break;
								case NIGHT_POSITON:
									mBrightnessTool.setBrightness(progress);
									mBrightnessTool.saveBrightness(mContext.getContentResolver(), progress);
									mPreferences.edit().putInt("night_brightness", progress).commit();
									break;
								default:
									break;
								}
							}
						});

				holder.checkbox = (CheckBox) convertView.findViewById(R.id.brightness_item_cb);
				
				holder.checkbox.setTag(holder);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tvDay.setText(DayAndNight[position]);
			holder.checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
					ViewHolder viewHolder = (ViewHolder) buttonView.getTag();
					if (isChecked) {
						viewHolder.tvDay.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
					} else {
						viewHolder.tvDay.setTextColor(getResources().getColor(android.R.color.white));
					}
					viewHolder.seekbar.setAllEnabled(isChecked);
				};
			});

			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mAdapter.notifyDataSetChanged();
		setCurBrightness(position);
	}

	private void setCurBrightness(int position) {
		// 当前屏幕亮度值
		int systemBrightness = mBrightnessTool.getSystemBrightness();
		int brightness = 0;
		
		if(position == DAY_POSITON) {
			brightness = mPreferences.getInt("day_brightness", systemBrightness);
		} else if(position == NIGHT_POSITON) {
			brightness = mPreferences.getInt("night_brightness", systemBrightness);
		}
		mBrightnessTool.setBrightness(brightness);
	}
}
