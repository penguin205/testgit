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

import com.android.settings.R;

import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ListView;

public class SoundSexFragment extends ListFragment {
	
	private final String KEY_SUPPORT_SOUND_SEX = "key_support_sound_sex";
	public static final String SHARE_PREFERENCE_KEY = "sound_sex_setting";
	
	private String[] mSupportSounds;
	private ListView mListView;
	private int mCurrentSoundSexIndex = -1;

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
    	getSupportSounds();
    	getCurrentSoundIndex();
    	
    	SimpleAdapter languageAdapter = constructLanguageAdapter(getActivity());
    	setListAdapter(languageAdapter);
    	mListView.setItemChecked(mCurrentSoundSexIndex, true);
    }
	
	 private void getSupportSounds() {
		 mSupportSounds = new String[] {
			 getActivity().getResources().getString(R.string.male_sex),
			 getActivity().getResources().getString(R.string.female_sex),
		 };
	}

	public SimpleAdapter constructLanguageAdapter(Context context) {
	        return constructLanguageAdapter(context,
	                R.layout.checkable_oneline_list_item);
	    }

    public SimpleAdapter constructLanguageAdapter(Context context, int layoutId) {
        final String[] from = new String[] {KEY_SUPPORT_SOUND_SEX  };
        final int[] to = new int[] {android.R.id.text1};

        final List<HashMap<String, Object>> encryptionList = getSupportSoundList();
        
        final SimpleAdapter adapter = new SimpleAdapter(context,
        		encryptionList,
                layoutId,
                from,
                to);

        return adapter;
    }

    private List<HashMap<String, Object>> getSupportSoundList() {
    	List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
    	HashMap<String, Object> map = null;
    	for(int i=0; i<mSupportSounds.length; i++) {
    		map = new HashMap<String, Object>();
    		map.put(KEY_SUPPORT_SOUND_SEX, mSupportSounds[i]);
    		list.add(map);
    	}
		return list;
	}

	private void getCurrentSoundIndex() {
		String currentSoundSexSettings = PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getString(SHARE_PREFERENCE_KEY, mSupportSounds[0]);
		for(int i=0; i<mSupportSounds.length; i++) {
			if(mSupportSounds[i].equals(currentSoundSexSettings)) {
				mCurrentSoundSexIndex = i;
			}
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
		actionBarTitle.setText(R.string.sound_sex_settings);
		actionBar.setCustomView(actionbarLayout);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Save to share PRF as TEMP handle way [Xiaoyj 2016/05/06]
		PreferenceManager.getDefaultSharedPreferences(getActivity())
		.edit()
		.putString(SHARE_PREFERENCE_KEY, mSupportSounds[position])
		.commit();
	}
	
}

