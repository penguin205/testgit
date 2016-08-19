/*
 * Copyright (C) 2011 The Android Open Source Project
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
import java.util.List;

import android.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;

import com.android.settings.ChooseLockGeneric.ChooseLockGenericFragment;

/**
 * Stub class for showing sub-settings; we can't use the main Settings class
 * since for our app it is a special singleTask class.
 */
public class SubSettings extends Settings {
	
	private List<MyTouchListener> myTouchListeners = new ArrayList<MyTouchListener>();
	/*
	 * callback interface
	 * @author XiaoYJ
	 */
	public interface MyTouchListener {
		boolean onTouchEvent(MotionEvent event);
	}

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }
	
	public void registerMyTouchListener(MyTouchListener listener) {
		myTouchListeners.add(listener);
	}


	public void unRegisterMyTouchListener(MyTouchListener listener) {
		myTouchListeners.remove(listener);
	}

	/*
	 * dispatch 触摸事件给所有注册了MyTouchListener的接口
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		for (MyTouchListener listener : myTouchListeners) {
			listener.onTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}
	
    @Override
    protected boolean isValidFragment(String fragmentName) {
        Log.d("SubSettings", "Launching fragment " + fragmentName);
        return true;
    }
}
