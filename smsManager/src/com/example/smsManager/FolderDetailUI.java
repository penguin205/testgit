package com.example.smsManager;

import java.util.HashMap;

import com.example.smsManager.ConversationUI.ConversationViewHolder;
import com.example.smsManager.utils.CommonQueryHandler;
import com.example.smsManager.utils.CommonQueryHandler.OnQueryNotifyCompleteListener;
import com.example.smsManager.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class FolderDetailUI extends Activity implements
		OnQueryNotifyCompleteListener, OnItemClickListener, OnClickListener {
	private ListView mListview;
	private FolderDetailAdapter mAdapter;

	private String[] projection = {
			"_id",
			"address",
			"date",
			"body"
	};
	private final int ADDRESS_COLUMN_INDEX = 1;
	private final int DATE_COLUMN_INDEX = 2;
	private final int BODY_COLUMN_INDEX = 3;
	private int index;
	
	private HashMap<Integer,String> dateMap;
	private HashMap<Integer,Integer> realPostionMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_detail);
		
		Intent intent = getIntent();
		index = intent.getIntExtra("index", -1);
		
		initTittle();
		initView();
		prepareData();
	}

	private void initTittle() {
		switch (index) {
		case 0:
			setTitle("收件箱");
			break;
		case 1:
			setTitle("发件箱");
			break;
		case 2:
			setTitle("已发送");
			break;
		case 3:
			setTitle("草稿箱");
			break;
		default:
			break;
		}
	}

	private void prepareData() {
		Uri uri = Utils.getUriFromIndex(index);
		CommonQueryHandler queryHandler = new CommonQueryHandler(getContentResolver());
		queryHandler.startQuery(0, mAdapter, uri, projection, null, null, "date desc");
		queryHandler.setOnNotifyQueryListener(this);
	}

	private void initView() {
		dateMap = new HashMap<Integer, String>();
		realPostionMap = new HashMap<Integer, Integer>();
		
		findViewById(R.id.btn_folder_detail_new_message).setOnClickListener(this);
		
		mListview = (ListView) findViewById(R.id.lv_folder_detail_sms);
		mAdapter = new FolderDetailAdapter(this, null);
		mListview.setAdapter(mAdapter);	
		mListview.setOnItemClickListener(this);
	}
	
	class FolderDetailAdapter extends CursorAdapter {
		private FolderDetailViewHolder mHolder;

		public FolderDetailAdapter(Context context, Cursor c) {
			super(context, c);
		}
		
		@Override
		public int getCount() {
			return dateMap.size()+realPostionMap.size();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(dateMap.containsKey(position)) {
				String dateStr = dateMap.get(position);
				TextView tv = new TextView(FolderDetailUI.this);
				tv.setText(dateStr);
				tv.setGravity(Gravity.CENTER_HORIZONTAL);
				tv.setBackgroundResource(android.R.color.darker_gray);
				tv.setTextColor(Color.WHITE);
				tv.setTextSize(20);
				return tv;
			}
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(realPostionMap.get(position));
	        View v;
	        if (convertView == null || convertView instanceof TextView) {
	            v = newView(FolderDetailUI.this, cursor, parent);
	        } else {
	            v = convertView;
	        }
	        bindView(v, FolderDetailUI.this, cursor);
	        return v;
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			mHolder = new FolderDetailViewHolder();
			View view = View.inflate(context, R.layout.folder_detail_item, null);
			mHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_folder_detail_item_icon);
			mHolder.tvName = (TextView) view.findViewById(R.id.tv_folder_detail_item_name);
			mHolder.tvBody = (TextView) view.findViewById(R.id.tv_folder_detail_item_body);
			mHolder.tvDate = (TextView) view.findViewById(R.id.tv_folder_detail_item_date);
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (FolderDetailViewHolder) view.getTag();
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);

			mHolder.tvBody.setText(body);
			
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				mHolder.tvName.setText(address);
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			}else {
				mHolder.tvName.setText(contactName);
				Bitmap bmIcon = Utils.getContactIcon(getContentResolver(), address);
				if(bmIcon == null) {
					mHolder.ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
				} else {
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(bmIcon));
				}
			}
			
			String strDate = null;
			if(DateUtils.isToday(date)){
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
				strDate = DateFormat.getDateFormat(context).format(date);
			}
			mHolder.tvDate.setText(strDate);
		}
		
	}
	public static class FolderDetailViewHolder {
		ImageView ivIcon;
		TextView tvName;
		TextView tvBody;
		TextView tvDate;
	}
	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		
	}

	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
		String dateStr;
		int listViewIndex = 0;

		if(cursor != null && cursor.getCount() > 0) {
			while(cursor.moveToNext()) {
				dateStr = DateFormat.getDateFormat(this).format(cursor.getLong(DATE_COLUMN_INDEX));
				if(!dateMap.containsValue(dateStr)){
					dateMap.put(listViewIndex, dateStr);
					listViewIndex++;
				} 
				realPostionMap.put(listViewIndex, cursor.getPosition());
				listViewIndex++;
			}
		}
		cursor.moveToPosition(-1);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(realPostionMap.containsKey(position)) {
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(realPostionMap.get(position));
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			
			Intent intent = new Intent(this, SmsDetailUI.class);
			intent.putExtra("index", index);
			intent.putExtra("address", address);
			intent.putExtra("date", date);
			intent.putExtra("body", body);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btn_folder_detail_new_message) {
			startActivity(new Intent(this, NewMessageUI.class));
		}
	}
}
