package com.android.settings;

import android.content.Context;
import android.preference.Preference;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class NetworkSSIDPreference extends Preference {

	private String mText = "";
	private EditText mEditText;
	private TextWatcher mTextWatcher;
	private boolean isAdd;
	
	
	public NetworkSSIDPreference(Context context) {
		this(context, null);
	}

	public NetworkSSIDPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.left_edit_preference_layout);
		notifyChanged();
	}

	public void setText(String text) {
		if (text != mText) {
			mText = text;
			notifyChanged();
		}
	}
	
	public String getSsid() {
		if(mEditText != null) {
			return mEditText.getText().toString();
		}
		return mText;
	}
	
	public void addTextChangedListener(TextWatcher textWatcher) {
		if(mTextWatcher != textWatcher) {
			mTextWatcher = textWatcher;
			isAdd = true;
			notifyChanged();
		}
	}
	
	public void removeTextChangeListener(TextWatcher textWatcher) {
		if(mTextWatcher != textWatcher) {
			mTextWatcher = textWatcher;
			isAdd = false;
			notifyChanged();
		}
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		final EditText et = (EditText) view.findViewById(R.id.widget_frame_edit);
		et.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					et.setTextColor(0xff46a9d2);
				} else {
					et.setTextColor(0xffffffff);
				}
			}
		});
		if (mTextWatcher != null) {
			if(isAdd ) {
				et.addTextChangedListener(mTextWatcher);
			} else {
				et.removeTextChangedListener(mTextWatcher);
			}
		} 

		et.setText(mText);

		mEditText = et;
	}
	
	public EditText getEditText() {
		return mEditText;
	}

}
