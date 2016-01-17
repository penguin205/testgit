package com.example.mobileknow.ui;

import com.example.mobileknow.R;

import android.content.Context;
import android.view.View;

public class InflateView {

	public static View inflateMyView(Context context) {
		return View.inflate(context, R.layout.list_my_say_item, null);
	}

	public static View inflateSheView(Context context) {
		return View.inflate(context, R.layout.list_she_say_item, null);
	}

	public static View inflateMmView(Context context) {
		return  View.inflate(context, R.layout.list_mm_say_item, null);
	}

}
