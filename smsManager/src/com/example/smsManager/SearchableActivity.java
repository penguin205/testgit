package com.example.smsManager;

import com.example.smsManager.utils.CommonQueryHandler;
import com.example.smsManager.utils.Sms;
import com.example.smsManager.utils.Utils;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchableActivity extends ListActivity implements OnItemClickListener {
	
	private String[] projection = {
			"_id",
			"address",
			"date",
			"body",
			"type"
	};
	private final int ADDRESS_COLUMN_INDEX = 1;
	private final int DATE_COLUMN_INDEX = 2;
	private final int BODY_COLUMN_INDEX = 3;
	private final int TYPE_COLUMN_INDEX = 4;
	
	private ListView mListView;
	private SearchableAdapter mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("ËÑË÷½á¹û");
		Intent intent= getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())){
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}
	}

	private void doMySearch(String query) {
		mListView = getListView();
		mAdapter = new SearchableAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		
		String selection = "body like \"%" + query + "%\"";
		CommonQueryHandler queryHandler = new CommonQueryHandler(getContentResolver());
		queryHandler.startQuery(0, mAdapter, Sms.SMS_URI, projection, selection, null, "date desc");
	}
	
	
	class SearchableAdapter extends CursorAdapter {

		private SearchableAdapterViewHolder mHolder;

		public SearchableAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			mHolder = new SearchableAdapterViewHolder();
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
			mHolder = (SearchableAdapterViewHolder) view.getTag();
			
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
	public static class SearchableAdapterViewHolder {
		ImageView ivIcon;
		TextView tvName;
		TextView tvBody;
		TextView tvDate;
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		int index = -1;
		int type = cursor.getInt(TYPE_COLUMN_INDEX);
		if(type == Sms.SMS_SEND) {
			index = 2;
		} else if(type == Sms.SMS_RECEIVE) {
			index = 0;
		} else if(type == Sms.SMS_DUSTBIN) {
			index = 3;
		}
		String address = cursor.getString(ADDRESS_COLUMN_INDEX);
		long date = cursor.getLong(DATE_COLUMN_INDEX);
		String body = cursor.getString(BODY_COLUMN_INDEX);
		
		Intent intent = new Intent(this, SmsDetailUI.class);
		intent.putExtra("index", index);
		intent.putExtra("address", address);
		intent.putExtra("date", date);
		intent.putExtra("body", body);
		startActivity(intent);		
	}
}
