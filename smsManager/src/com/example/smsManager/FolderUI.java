package com.example.smsManager;

import java.util.HashMap;

import com.example.smsManager.utils.CommonQueryHandler;
import com.example.smsManager.utils.CommonQueryHandler.OnQueryNotifyCompleteListener;
import com.example.smsManager.utils.Utils;

import android.R.integer;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FolderUI extends ListActivity implements OnQueryNotifyCompleteListener, OnItemClickListener {
	
	private int[] imageIds;
	private String[] typeArrays;
	private HashMap<Integer,Integer> countMap;
	private FolderAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initView();
	}

	private void initView() {
		ListView mListview = getListView();
		
		imageIds = new int[] {
				R.drawable.a_f_inbox,
				R.drawable.a_f_outbox,
				R.drawable.a_f_sent,
				R.drawable.a_f_draft
		};
		typeArrays = new String[] {
				"收件箱",
				"发件箱",
				"已发送",
				"草稿箱"
		};
		countMap = new HashMap<Integer, Integer>();
		
		CommonQueryHandler queryHandler = new CommonQueryHandler(getContentResolver());
		queryHandler.setOnNotifyQueryListener(this);
		Uri uri;
		for(int i=0; i<4; i++) {
			countMap.put(i, 0);
			uri = Utils.getUriFromIndex(i);
			queryHandler.startQuery(i, null, uri, new String[]{"count(*)"}, null, null, null);
		}
	
		mAdapter = new FolderAdapter();
		mListview.setAdapter(mAdapter);
		mListview.setOnItemClickListener(this);
	}
	
	class FolderAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return imageIds.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if(convertView == null) {
				view = View.inflate(FolderUI.this, R.layout.folder_item, null);
			} else {
				view = convertView;
			}
			
			ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_folder_item_icon);
			TextView tvType = (TextView) view.findViewById(R.id.tv_folder_item_type);
			TextView tvCount = (TextView) view.findViewById(R.id.tv_folder_item_count);
			
			ivIcon.setBackgroundResource(imageIds[position]);
			tvType.setText(typeArrays[position]);
			tvCount.setText(String.valueOf(countMap.get(position)));
			
			return view;
		}
		
	}

	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		if(cursor != null && cursor.moveToFirst()) {
			int count = cursor.getInt(0);
			countMap.put(token, count);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent();
		intent.setClass(this, FolderDetailUI.class);
		intent.putExtra("index", position);
		startActivity(intent);
	}
}
