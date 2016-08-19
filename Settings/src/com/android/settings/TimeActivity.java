/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.android.settings.widget.time.JudgeDate;
import com.android.settings.widget.time.ScreenInfo;
import com.android.settings.widget.time.WheelMain;

public class TimeActivity extends Settings implements OnSharedPreferenceChangeListener {

    private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";

    private final String dateFormatString = "yyyy 年 MM 月 dd 日   HH:mm";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    
    // Used for showing the current date format, which looks like "12/31/2010", "2010/12/13", etc.
    // The date value is dummy (independent of actual date).
    private Calendar mDummyDate;

    private static final String KEY_AUTO_TIME = "auto_time";

    // have we been launched from the setup wizard?
    protected static final String EXTRA_IS_FIRST_RUN = "firstRun";

    private CheckBoxPreference mAutoTimePref;
    private Preference mTime24Pref;
    private TextPreference mTimeZonePref;
    private TextPreference mDatePref;
    
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.date_time_prefs);
        
        setActionBarTitle(R.string.time_settings);
        
        initUI();
    }
    
	
    @SuppressWarnings("deprecation")
	private void initUI() {
        boolean autoTimeEnabled = getAutoState(android.provider.Settings.Global.AUTO_TIME);

        Intent intent = this.getIntent();
        boolean isFirstRun = intent.getBooleanExtra(EXTRA_IS_FIRST_RUN, false);

        mDummyDate = Calendar.getInstance();

        mAutoTimePref = (CheckBoxPreference) findPreference(KEY_AUTO_TIME);
        mAutoTimePref.setChecked(autoTimeEnabled);
       
        mTime24Pref = findPreference("24 hour");
        
        mTimeZonePref = (TextPreference) findPreference("timezone");
        mTimeZonePref.setMoreImageVisibility(true);
        
        mDatePref = (TextPreference) findPreference("date");
        mDatePref.setMoreImageVisibility(false);
        
        if (isFirstRun) {
            getPreferenceScreen().removePreference(mTime24Pref);
           
        }
        String [] dateFormats = getResources().getStringArray(R.array.date_format_values);
        String [] formattedDates = new String[dateFormats.length];
        String currentFormat = getDateFormat();
        // Initialize if DATE_FORMAT is not set in the system settings
        // This can happen after a factory reset (or data wipe)
        if (currentFormat == null) {
            currentFormat = "";
        }

        // Prevents duplicated values on date format selector.
        mDummyDate.set(mDummyDate.get(Calendar.YEAR), mDummyDate.DECEMBER, 31, 13, 0, 0);

        for (int i = 0; i < formattedDates.length; i++) {
            String formatted =
                    DateFormat.getDateFormatForSetting(this, dateFormats[i])
                    .format(mDummyDate.getTime());

            if (dateFormats[i].length() == 0) {
                formattedDates[i] = getResources().
                    getString(R.string.normal_date_format, formatted);
            } else {
                formattedDates[i] = formatted;
            }
        }
        
        if(autoTimeEnabled) {
        	getPreferenceScreen().removePreference(mDatePref);
        } 
        footerView = LayoutInflater.from(this).inflate(R.layout.blankness_fill_view, getListView(), false);
        getListView().addFooterView(footerView);
        footerView.setVisibility(View.INVISIBLE);
        getListView().setFooterDividersEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        ((CheckBoxPreference)mTime24Pref).setChecked(is24Hour());

        // Register for time ticks and other reasons for time change
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        this.registerReceiver(mIntentReceiver, filter, null, null);

        updateTimeAndDateDisplay();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(mIntentReceiver);
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void updateTimeAndDateDisplay() {
        //java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(this);
        final Calendar now = Calendar.getInstance();
        mDummyDate.setTimeZone(now.getTimeZone());
        // We use December 31st because it's unambiguous when demonstrating the date format.
        // We use 13:00 so we can demonstrate the 12/24 hour options.
        mDummyDate.set(now.get(Calendar.YEAR), 11, 31, 13, 0, 0);
        mDatePref.setText(dateFormat.format(new Date()));
        mTimeZonePref.setText(getTimeZoneText(now.getTimeZone()));
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(KEY_AUTO_TIME)) {
            boolean autoEnabled = preferences.getBoolean(key, true);
            android.provider.Settings.Global.putInt(getContentResolver(), 
            		android.provider.Settings.Global.AUTO_TIME,
                    autoEnabled ? 1 : 0);
           // mDatePref.setEnabled(!autoEnabled);
            if(autoEnabled) {
            	getPreferenceScreen().removePreference(mDatePref);
            } else {
            	getPreferenceScreen().addPreference(mDatePref);
            }
        }
    }


    static void configureDatePicker(DatePicker datePicker) {
        // The system clock can't represent dates outside this range.
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(1970, Calendar.JANUARY, 1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(2037, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mDatePref) {
        	// show time set window
        	showTimepickerPopWindow();
        	
        } else if (preference == mTime24Pref) {
            set24Hour(((CheckBoxPreference)mTime24Pref).isChecked());
            updateTimeAndDateDisplay();
            timeUpdated();
        } else if(preference == mTimeZonePref) {
        	Intent intent = onBuildStartFragmentIntent(ZonePicker.class.getCanonicalName(), null, 0, 0);
        	this.startActivityForResult(intent, -1);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

	private void showTimepickerPopWindow() {
		
//		View parent = ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
		View view = LayoutInflater.from(this).inflate(R.layout.footer_time_picker, null);
		
		ScreenInfo screenInfo = new ScreenInfo(this);
		final WheelMain wheelMain = new WheelMain(view, true);
		wheelMain.screenheight = screenInfo.getHeight();
		
		Calendar calendar = Calendar.getInstance();
		String time = mDatePref.getText().toString();
		Log.d("ffff", "time : " + time);
		if(JudgeDate.isDate(time, dateFormatString)){
			try {
				calendar.setTime(dateFormat.parse(time));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		
		wheelMain.initDateTimePicker(year, month, day, hour, min);
		
		//设置popupWindow的宽高即为屏幕的宽和高
		final PopupWindow popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		//设置显示和隐藏的动画
		popWindow.setAnimationStyle(R.style.AnimBottom);
		popWindow.setFocusable(true);
		popWindow.setOutsideTouchable(false);// 设置允许在外点击消失
		ColorDrawable dw = new ColorDrawable(0x30000000);
		popWindow.setBackgroundDrawable(dw);
		
		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.timepikcer_confirm:
					String timeStr = wheelMain.getTime();
					Log.d("ffff", "timeString : " + timeStr);
					Calendar calendar = Calendar.getInstance();
					if(JudgeDate.isDate(timeStr, "yyyy-MM-dd HH:mm")){
						try {
							Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(timeStr);
							calendar.setTime(newDate);
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					int year = calendar.get(Calendar.YEAR);
					int month = calendar.get(Calendar.MONTH);
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					int hour = calendar.get(Calendar.HOUR_OF_DAY);
					int min = calendar.get(Calendar.MINUTE);
					setDate(TimeActivity.this, year, month, day);
					setTime(TimeActivity.this, hour, min);
		            mDatePref.setText(timeStr);
					break;
				case R.id.timepikcer_cancel:
					
					break;
				}
				popWindow.dismiss();
			}
		};
		view.findViewById(R.id.timepikcer_confirm).setOnClickListener(listener);
		view.findViewById(R.id.timepikcer_cancel).setOnClickListener(listener);
//		popWindow.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, -20);
		popWindow.showAsDropDown(footerView, 0, 0);
	}

    
    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        updateTimeAndDateDisplay();
    }

    private void timeUpdated() {
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        this.sendBroadcast(timeChanged);
    }

    /*  Get & Set values from the system settings  */

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(this);
    }

    private void set24Hour(boolean is24Hour) {
    	android.provider.Settings.System.putString(getContentResolver(),
    			android.provider.Settings.System.TIME_12_24,
                is24Hour? HOURS_24 : HOURS_12);
    }

    private String getDateFormat() {
        return android.provider.Settings.System.getString(getContentResolver(),
        		android.provider.Settings.System.DATE_FORMAT);
    }

    private boolean getAutoState(String name) {
        try {
            return android.provider.Settings.Global.getInt(getContentResolver(), name) > 0;
        } catch (SettingNotFoundException snfe) {
            return false;
        }
    }

    /* package */ static void setDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /* package */ static void setTime(Context context, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    private static String getTimeZoneText(TimeZone tz) {
        SimpleDateFormat sdf = new SimpleDateFormat("zzzz");
        sdf.setTimeZone(tz);
        return sdf.format(new Date());
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
              updateTimeAndDateDisplay();
        }
    };
	private View footerView;

}
