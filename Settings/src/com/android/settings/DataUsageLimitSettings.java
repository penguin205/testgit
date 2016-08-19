package com.android.settings;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataUsageLimitSettings extends Fragment {

	private EditText etLimit;
	private TextView tvLimitUnit;
	private TextView tvConsume;
	private TextView tvDayRemain;
	private LinearLayout rootLinearLayout;
	private DatabaseAdapter dbAdapter;
	private Parameter par;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_data_usage_limit, null);
		rootLinearLayout = (LinearLayout) view
				.findViewById(R.id.root_linearLayout);
		etLimit = (EditText) view.findViewById(R.id.et_month_limit_num);
		tvLimitUnit = (TextView) view.findViewById(R.id.tv_month_limit_unit);
		tvConsume = (TextView) view.findViewById(R.id.tv_month_consume_num);
		tvDayRemain = (TextView) view.findViewById(R.id.tv_day_remain_num);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setCustomActionBar();
		setWallpaper();
		
		par = new Parameter(getActivity());
		etLimit.setText(par.getParameter("mLimit"));
		etLimit.addTextChangedListener(mTextWatcher);
		etLimit.setOnFocusChangeListener(mOnFocusChangeListener);
		
		dbAdapter = new DatabaseAdapter(getActivity());
		dbAdapter.open();

		Calendar currentCa = Calendar.getInstance();
		int year = currentCa.get(Calendar.YEAR);
		int month = currentCa.get(Calendar.MONTH) + 1;
		int day = currentCa.get(Calendar.DATE);

		String month3GTraffic = Utils.convertToMB(dbAdapter.calculateForMonth(
				year, month, DatabaseAdapter.NET_TYPE_3G));
		tvConsume.setText(month3GTraffic);
		
		updateDayRemain();
	}

	int mMaxLenth = 10;//设置允许输入的字符长度
	public static String stringFilter(String str)throws PatternSyntaxException{  
		String regEx = "[/\\:*?<>|\"\n\t]";
		Pattern p = Pattern.compile(regEx); 
		Matcher m = p.matcher(str); 
		return m.replaceAll("");
	}
	
	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus) {
				etLimit.setTextColor(0xff46a9d2);
				tvLimitUnit.setTextColor(0xff46a9d2);
			} else {
				etLimit.setTextColor(0xffffffff);
				tvLimitUnit.setTextColor(0xffffffff);
			}
		}
	};
	
	private TextWatcher mTextWatcher = new TextWatcher() {
		private int cou = 0;
		int selectionEnd = 0;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			cou = before + count;
			String editable = etLimit.getText().toString();
			String str = stringFilter(editable); // 过滤特殊字符
			if (!editable.equals(str)) {
				etLimit.setText(str);
			}
			etLimit.setSelection(etLimit.length());
			cou = etLimit.length();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if (cou > mMaxLenth) {
				selectionEnd = etLimit.getSelectionEnd();
				s.delete(mMaxLenth, selectionEnd);
			}
			
			String strValue = etLimit.getText().toString();
			if(TextUtils.isEmpty(strValue)) {
				par.setParameter("mLimit", 500);
			} else {
				par.setParameter("mLimit", Long.valueOf(strValue));
				updateDayRemain();
			}
		}
	};
	
	public static int calcRemainDaysOfCurrentMonth() {
		Calendar cal = Calendar.getInstance();
		int dateOfMonth = cal.getActualMaximum(Calendar.DATE);
		int dayIndex = cal.get(Calendar.DAY_OF_MONTH);
		return dateOfMonth - dayIndex;							
	}
	
	protected void updateDayRemain() {
		int remainDays = calcRemainDaysOfCurrentMonth();
		BigDecimal divide = new BigDecimal(remainDays);
		double consume = Double.valueOf(tvConsume.getText().toString());
		double limit = TextUtils.isEmpty(etLimit.getText().toString()) ? 
				500.0 : Double.valueOf(etLimit.getText().toString());
		BigDecimal temp = new BigDecimal(limit - consume);
		String result = temp.divide(divide, 2, 1).doubleValue() + ""; 
		tvDayRemain.setText(result);
	}
	

	private void setWallpaper() {
		FrameLayout fl = (FrameLayout) rootLinearLayout.getParent();
		fl.setBackgroundResource(R.drawable.default_wallpaper);
	}

	private void setCustomActionBar() {
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		View actionbarLayout = LayoutInflater.from(getActivity()).inflate(
				R.layout.actionbar_common_layout, null);
		TextView actionBarTitle = (TextView) actionbarLayout
				.findViewById(R.id.tv_actionbar_title);
		actionBarTitle.setText(R.string.flow_monitor);
		actionBar.setCustomView(actionbarLayout);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		dbAdapter.close();
		
		etLimit.removeTextChangedListener(mTextWatcher);
	}
}
