package com.android.settings;


import android.content.Context;
import android.preference.Preference;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;

public class NetworkPasswordPreference extends Preference {

	private String mText = "";
	private TextWatcher mTextWatcher;
	private EditText mEditText;
	private boolean isAdd;
	
	public NetworkPasswordPreference(Context context) {
		this(context, null);
	}
	
	public NetworkPasswordPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.network_password_preference_layout);
	}

	public void setText(String text) {
		if (text != mText) {
			mText = text;
			notifyChanged();
		}
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
	
	public String getPassword() {
		if(mEditText != null) {
			return mEditText.getText().toString();
		}
		return mText;
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
		
		
		CheckBox cb = (CheckBox) view.findViewById(R.id.show_password);
		cb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				et.setInputType(InputType.TYPE_CLASS_TEXT | (((CheckBox) v).isChecked() 
								? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
								: InputType.TYPE_TEXT_VARIATION_PASSWORD));
			}
		});
		
	}
}
