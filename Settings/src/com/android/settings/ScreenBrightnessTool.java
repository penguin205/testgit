package com.android.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager;

public class ScreenBrightnessTool {

	public static final int ACTIVITY_BRIGHTNESS_AUTOMATIC = -1;

	public static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;

	public static final int SCREEN_BRIGHTNESS_MODE_MANUAL = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

	public static final int SCREEN_BRIGHTNESS_DEFAULT = 75;

	public static final int MAX_BRIGHTNESS = 255;

	public static final int MIN_BRIGHTNESS = 0;


	// 当前系统调节模式
	private boolean sysAutomaticMode;
	// 当前系统亮度值
	private int sysBrightness;

	private Context context;

	/**
	 * 构造函数
	 * @param context  
	 * @param sysBrightness    当前系统亮度值
	 * @param sysAutomaticMode 当前系统调节模式
	 */
	private ScreenBrightnessTool(Context context, int sysBrightness,
			boolean sysAutomaticMode) {
		this.context = context;
		this.sysBrightness = sysBrightness; 
		this.sysAutomaticMode = sysAutomaticMode;
	}

	public static ScreenBrightnessTool Builder(Context context) {
		int brightness;
		boolean automaticMode;
		try {
			// 获取当前系统亮度值
			brightness = Settings.System.getInt(context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
			// 获取当前系统调节模式
			automaticMode = Settings.System.getInt(
					context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE) == SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (SettingNotFoundException e) {
			return null;
		}

		return new ScreenBrightnessTool(context, brightness, automaticMode);
	}

	public boolean getSystemAutomaticMode() {
		return sysAutomaticMode;
	}

	public int getSystemBrightness() {
		return sysBrightness;
	}

	/**
	 * 设置系统调节模式
	 * @param mode
	 */
	public void setMode(int mode) {
		if (mode != SCREEN_BRIGHTNESS_MODE_AUTOMATIC
				&& mode != SCREEN_BRIGHTNESS_MODE_MANUAL)
			return;

		sysAutomaticMode = (mode == SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
	}

	/**
	 * 设置系统亮度
	 * @param brightness
	 */
	public void setBrightness(int brightness) {
//		int mid = mMaxBrighrness - mMinBrighrness;
//		int bri = (int) (mMinBrighrness + mid * ((float) brightness)
//				/ MAX_BRIGHTNESS);

		ContentResolver resolver = context.getContentResolver();
		Settings.System
				.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
	}
	
    public void saveBrightness(ContentResolver resolver, int brightness) {
        Uri uri = android.provider.Settings.System
                .getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(resolver, "screen_brightness",
                brightness);
        // resolver.registerContentObserver(uri, true, myContentObserver);
        resolver.notifyChange(uri, null);
    }
    
    public static void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }
    
    public static void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }
    
//	public static void brightnessPreview(Activity activity, float brightness) {
//		Window window = activity.getWindow();
//		WindowManager.LayoutParams lp = window.getAttributes();
//		lp.screenBrightness = brightness;
//		window.setAttributes(lp);
//	}
//
//	public static void brightnessPreviewFromPercent(Activity activity,
//			float percent) {
//		float brightness = percent + (1.0f - percent)
//				* (((float) mMinBrighrness) / mMaxBrighrness);
//		brightnessPreview(activity, brightness);
//	}

}