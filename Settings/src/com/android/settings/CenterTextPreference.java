package com.android.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class CenterTextPreference extends Preference {

	public CenterTextPreference(Context context) {
		this(context, null);
	}

	private String mText = "";

	public CenterTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.center_text_preference_layout);
	}

	public void setText(String text) {
		if (text != mText) {
			mText = text;
			notifyChanged();
		}
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		TextView widgetTextView = (TextView) view.findViewById(R.id.widget_frame_text);
		if (widgetTextView != null) {
			if (widgetTextView != null) {
				widgetTextView.setText(mText);
			}
		}
	}
}
