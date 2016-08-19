/*
 * Copyright (C) 2007 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.android.settings.R;
import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class LanguageFragment extends ListFragment {

	private final String KEY_SUPPORT_LANGUAGES = "key_support_language";
	
	private String[] mSupportLanguages;
	private ListView mListView;
	private int mCurrentLocalIndex = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final ListView list = (ListView) view.findViewById(android.R.id.list);
        Utils.forcePrepareCustomPreferencesList(container, view, list, false);
        return view;
    }
    
	@Override
    public void onActivityCreated(Bundle savedInstanseState) {
        super.onActivityCreated(savedInstanseState);
    	
        setCustomActionBar();
    	setupListView();
    	getCurrentLocaleIndex();
    	getSupportLanguages();
    	
    	SimpleAdapter languageAdapter = constructLanguageAdapter(getActivity());
    	setListAdapter(languageAdapter);
    	mListView.setItemChecked(mCurrentLocalIndex, true);
    }
	
	 private void getSupportLanguages() {
		 mSupportLanguages = new String[] {
			 getActivity().getResources().getString(R.string.chinese_language),
			 getActivity().getResources().getString(R.string.english_language),
		 };
	}

	public SimpleAdapter constructLanguageAdapter(Context context) {
	        return constructLanguageAdapter(context,
	                R.layout.checkable_oneline_list_item);
	    }

    public SimpleAdapter constructLanguageAdapter(Context context, int layoutId) {
        final String[] from = new String[] {KEY_SUPPORT_LANGUAGES  };
        final int[] to = new int[] {android.R.id.text1};

        final List<HashMap<String, Object>> encryptionList = getSupportLanguageList();
        
        final SimpleAdapter adapter = new SimpleAdapter(context,
        		encryptionList,
                layoutId,
                from,
                to);

        return adapter;
    }

    private List<HashMap<String, Object>> getSupportLanguageList() {
    	List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
    	HashMap<String, Object> map = null;
    	for(int i=0; i<mSupportLanguages.length; i++) {
    		map = new HashMap<String, Object>();
    		map.put(KEY_SUPPORT_LANGUAGES, mSupportLanguages[i]);
    		list.add(map);
    	}
		return list;
	}

	private void getCurrentLocaleIndex() {
		Locale locale = getActivity().getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if(language.endsWith("zh")) {
			mCurrentLocalIndex  = 0;
		} else {
			mCurrentLocalIndex = 1;
		}
	}

	/**
     * set default wallpaper and list divider
     */
    private void setupListView() {
    	mListView = getListView();
        if(mListView !=null){
        	mListView.setDivider(getResources().getDrawable(R.drawable.line_bg));
        	mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            FrameLayout fl = (FrameLayout)mListView.getParent();
            fl.setBackgroundResource(R.drawable.default_wallpaper);
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
		TextView actionBarTitle = (TextView) actionbarLayout.findViewById(R.id.tv_actionbar_title);
		actionBarTitle.setText(R.string.language_settings);
		actionBar.setCustomView(actionbarLayout);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Locale locale = getActivity().getResources().getConfiguration().locale;
		if (position == 0) {
			locale = Locale.CHINA;
		} else {
			locale = Locale.ENGLISH;
		}
		LocalePicker.updateLocale(locale);
	}
	
}

