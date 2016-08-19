/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;

/**
 * Check box preference with check box replaced by radio button.
 *
 * Functionally speaking, it's actually a CheckBoxPreference. We only modified
 * the widget to RadioButton to make it "look like" a RadioButtonPreference.
 *
 * In other words, there's no "RadioButtonPreferenceGroup" in this
 * implementation. When you check one RadioButtonPreference, if you want to
 * uncheck all the other preferences, you should do that by code yourself.
 */
public class TextPreference extends Preference {

	private CharSequence mText = "";
	private boolean mImageShow = true;

    public TextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.preference_widget_textview);
    }

    public TextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.preferenceStyle);
    }

    public TextPreference(Context context) {
        this(context, null);
    }

    public void setText(String text) {
    	  if (text != mText) {
              mText = text;
              notifyChanged();
          }
    }
    
    public CharSequence getText() {
    	return mText;
    }
    
    public void setMoreImageVisibility(boolean bShow) {
    	if(bShow != mImageShow) {
    		mImageShow = bShow;
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
        
        ImageView widgetImageView = (ImageView) view.findViewById(R.id.widget_frame_image);
        if(widgetImageView != null) {
        	if(mImageShow){
        		widgetImageView.setVisibility(View.VISIBLE);
        	} else {
        		widgetImageView.setVisibility(View.GONE);
        	}
        }
    }
}
