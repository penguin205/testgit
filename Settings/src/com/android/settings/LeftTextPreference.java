package com.android.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class LeftTextPreference extends Preference {

	public LeftTextPreference(Context context) {
		this(context, null);
	}

	private String mText = "";

	public LeftTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.left_text_preference_layout);
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
			widgetTextView.setText(mText);
		}
	}
}
