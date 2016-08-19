package com.android.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.TwoLineListItem;

public class CustomTwoLineListItem extends TwoLineListItem implements Checkable {

	public CustomTwoLineListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private boolean mChecked = false;

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			refreshDrawableState();
			for (int i = 0, len = getChildCount(); i < len; i++) {
				View child = getChildAt(i);
				if (child instanceof Checkable) {
					((Checkable) child).setChecked(checked);
				}
			}
		}
	}

}
