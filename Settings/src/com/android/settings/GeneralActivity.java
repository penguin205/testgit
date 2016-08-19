package com.android.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.android.settings.R;
import com.android.settings.accounts.AuthenticatorHelper;
import com.android.settings.accounts.ManageAccountsSettings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class GeneralActivity extends Settings {


	private ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		invalidateHeaders();

		setActionBarTitle(R.string.general_settings);
	}

	@Override
	public void onResume() {
		super.onResume();
		mListView = getListView();
		mListView.setAdapter(new HeaderAdapter(this, getHeaders(), mAuthenticatorHelper));
	}

	@Override
	public void onBuildHeaders(List<Header> headers) {
		loadHeadersFromResource(R.xml.settings_headers_general, headers);
	}
	
	
	private class HeaderAdapter extends ArrayAdapter<Header> {
		static final int HEADER_TYPE_NORMAL = 0;
		static final int HEADER_TYPE_SWITCH = 1;
		private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;
		
		private AuthenticatorHelper mAuthHelper;
		private CarVideoEnabler mCarVideoEnabler;
		
		private class HeaderViewHolder {
			ImageView icon;
			TextView title;
			TextView summary;
			TextView text1;
			CheckBox checkbox;
		}

		private LayoutInflater mInflater;
		private boolean firstRun = true;

		private int getHeaderType(Header header) {
			if (header.fragment == null && header.intent == null){
				return HEADER_TYPE_SWITCH;
			} else {
				return HEADER_TYPE_NORMAL;
			}
		}

		@Override
		public int getItemViewType(int position) {
			Header header = getItem(position);
			return getHeaderType(header);
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false; // because of categories
		}

		@Override
		public boolean isEnabled(int position) {
			// return getItemViewType(position) != HEADER_TYPE_TIMEZHONE_PICKER;
			return true;
		}

		@Override
		public int getViewTypeCount() {
			return HEADER_TYPE_COUNT;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		public HeaderAdapter(Context context, List<Header> objects,
				AuthenticatorHelper authenticatorHelper) {
			super(context, 0, objects);

			mAuthHelper = authenticatorHelper;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// Temp Switches provided as placeholder until the adapter replaces
			// these with actual
			// Switches inflated from their layouts. Must be done before adapter
			// is set in super
            mCarVideoEnabler = new CarVideoEnabler(context, new CheckBox(context));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HeaderViewHolder holder;
			Header header = getItem(position);
			int headerType = getHeaderType(header);
			View view = null;

			if (convertView == null) {
				holder = new HeaderViewHolder();
				switch (headerType) {
				case HEADER_TYPE_NORMAL:
					view = mInflater.inflate(R.layout.preference_header_with_text_item, parent,
							false);
					holder.icon = (ImageView) view.findViewById(R.id.icon);
					holder.title = (TextView) view
							.findViewById(com.android.internal.R.id.title);
					holder.text1 = (TextView) view.findViewById(R.id.text1);
					holder.summary = (TextView) view
							.findViewById(com.android.internal.R.id.summary);
					break;

				case HEADER_TYPE_SWITCH:
					view = mInflater.inflate(R.layout.preference_header_switch_item, parent, false);
                    holder.icon = (ImageView) view.findViewById(R.id.icon);
                    holder.title = (TextView)
                            view.findViewById(com.android.internal.R.id.title);
                    holder.summary = (TextView)
                            view.findViewById(com.android.internal.R.id.summary);
                    holder.checkbox = (CheckBox) view.findViewById(R.id.switchWidget);
                   
                    break;
				}
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (HeaderViewHolder) view.getTag();
			}

			// All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_SWITCH:
                    // Would need a different treatment if the main menu had more switches
                    if (header.id == R.id.carvideo_settings && firstRun ) {
                    	firstRun = false;
                        mCarVideoEnabler.setSwitch(holder.checkbox);
                        holder.checkbox.setTag("switch");
                    } 
                    updateCommonHeaderView(header, holder);
                    break;

                case HEADER_TYPE_NORMAL:
                    updateCommonHeaderView(header, holder);
                    break;
            }

            return view;
        }

        private void updateCommonHeaderView(Header header, HeaderViewHolder holder) {
                if (header.extras != null
                        && header.extras.containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
                    String accType = header.extras.getString(
                            ManageAccountsSettings.KEY_ACCOUNT_TYPE);
                    Drawable icon = mAuthHelper.getDrawableForType(getContext(), accType);
                    setHeaderIcon(holder, icon);
                } else {
                    holder.icon.setImageResource(header.iconRes);
                }
                holder.title.setText(header.getTitle(getContext().getResources()));
               
                if(header.id == R.id.carvideo_settings) {
                	holder.summary.setVisibility(View.GONE);
                } 
                else {
                	CharSequence summary = header.getSummary(getContext().getResources());
                	if (!TextUtils.isEmpty(summary)) {
                		holder.text1.setVisibility(View.VISIBLE);
                		holder.summary.setVisibility(View.GONE);
                		if(header.id == R.id.language_settings) {
                			Locale locale = getContext().getResources().getConfiguration().locale;
                			String language = locale.getLanguage();
                			if(language.endsWith("zh")) {
                				holder.text1.setText(R.string.chinese_language);
                			} else {
                				holder.text1.setText(R.string.english_language);
                			}
                		} else if(header.id == R.id.sound_sex_settings) {
                			String soundSexSettings = PreferenceManager.getDefaultSharedPreferences(getContext())
                					.getString(SoundSexFragment.SHARE_PREFERENCE_KEY,
                					 getContext().getResources().getString(R.string.male_sex));
                			holder.text1.setText(soundSexSettings);
                		}
                	} else {
                		holder.text1.setVisibility(View.GONE);
                		holder.summary.setVisibility(View.GONE);
                	}
                }
            }

        private void setHeaderIcon(HeaderViewHolder holder, Drawable icon) {
            ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
            lp.width = getContext().getResources().getDimensionPixelSize(
                    R.dimen.header_icon_width);
            lp.height = lp.width;
            holder.icon.setLayoutParams(lp);
            holder.icon.setImageDrawable(icon);
        }

        public void resume() {
//            mWifiEnabler.resume();
//            mBluetoothEnabler.resume();
        }

        public void pause() {
//            mWifiEnabler.pause();
//            mBluetoothEnabler.pause();
        }
	}
	
    @Override
    public void onHeaderClick(Header header, int position) {
        if (header.id == R.id.carvideo_settings) {
    		CheckBox checkBox = (CheckBox) mListView.findViewWithTag("switch");
    		if(checkBox.isChecked()) { // forbid -> allow
    			Intent intent = new Intent(this, CarVideoPromptActivity.class);
            	this.startActivityForResult(intent, 1);
    		} else {
    			checkBox.setChecked(true); // allow -> forbid
    		}
 //        	boolean isEnabled = PreferenceManager.getDefaultSharedPreferences(this)
//        			.getBoolean("carvideo", true);
//        	ListView listview = getListView();
//        	LinearLayout firstView = (LinearLayout) listview.getChildAt(position-listview.getFirstVisiblePosition());
//        	for (int i = 0, len = firstView.getChildCount(); i < len; i++) {
//				View child = firstView.getChildAt(i);
//				if(child instanceof CheckBox) {
//					CheckBox cb = (CheckBox) child;
//					CarVideoEnabler carVideoEnabler = new CarVideoEnabler(this, new CheckBox(this));
//					carVideoEnabler.setSwitch(cb);
//					cb.setChecked(!isEnabled);
//				}
//			}
        }
        super.onHeaderClick(header, position);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode == 1) {
    		boolean isForbid = data.getBooleanExtra("result", true);
    		if(!isForbid) { // allow
        		CheckBox checkBox = (CheckBox) mListView.findViewWithTag("switch");
    			checkBox.setChecked(false);
    		}
    	}
    }
    
//	@Override
//	public boolean onPreferenceStartFragment(PreferenceFragment caller,
//			Preference pref) {
//		int titleRes = pref.getTitleRes();
//		if(pref.getFragment().equals(LanguageSettings.class.getName())) {
//			titleRes = R.string.language_settings;
//		} else if(pref.getFragment().equals(SoundSexSettings.class.getName())) {
//			titleRes = R.string.sound_sex_settings;
//		}
//		startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes, pref.getTitle(),
//               null, 0);
//		return true;
//	}

}
