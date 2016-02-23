package com.example.mobileknow.ui;

import com.example.mobileknow.R;

import android.content.Context;
import android.view.View;

public class InflateView {

	public static View inflateMyView(Context context) {
		return View.inflate(context, R.layout.listview_item_outcoming, null);
	}

	public static View inflateSheView(Context context) {
		return View.inflate(context, R.layout.listview_item_incoming, null);
	}
}
