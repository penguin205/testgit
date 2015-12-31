package com.example.smsManager;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.example.smsManager.utils.CommonQueryHandler;
import com.example.smsManager.utils.Sms;
import com.example.smsManager.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationUI extends Activity implements OnClickListener, 
	OnItemClickListener, OnItemLongClickListener {
	
	private static final int CONVERSATION_MENU_SEARCH = 0;
	private static final int CONVERSATION_MENU_EDIT = 1;
	private static final int CONVERSATION_MENU_CANCEL_EDIT = 2;
	
	private ListView mListView;
	private ConversationAdapter mAdapter;
	private HashSet<Integer> mMultiSelectedSet;

	private String[] projection = {
			"sms.thread_id AS _id",
			"sms.body AS body",
			"groups.msg_count AS count",
			"sms.date AS date",
			"sms.address AS address"
	};
	
	private final int THREAD_ID_COLUMN_INDEX = 0;
	private final int BODY_COLUMN_INDEX = 1;
	private final int MSG_COUNT_COLUMN_INDEX = 2;
	private final int DATE_COLUMN_INDEX = 3;
	private final int ADDRESS_COLUMN_INDEX = 4;
	
	private int EDIT_SATE = -1;
	private int LIST_STATE = -2;
	private int currentState = LIST_STATE;
	
	private Button mBtnNewMsg;
	private Button mBtnDeleteMsg;
	private Button mBtnSelectAll;
	private Button mBtnCancelSelect;
	private String TAG = "ConversationUI";
	private ProgressDialog mProgressDialog;
	protected boolean isStop = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);
		
		initViews();
		prepareDate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONVERSATION_MENU_SEARCH, 0, "搜索");
		menu.add(0, CONVERSATION_MENU_EDIT, 1, "编辑");
		menu.add(0, CONVERSATION_MENU_CANCEL_EDIT, 2, "取消编辑");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(currentState == LIST_STATE) {
			menu.findItem(CONVERSATION_MENU_CANCEL_EDIT).setVisible(false);
			menu.findItem(CONVERSATION_MENU_EDIT).setVisible(true);
			menu.findItem(CONVERSATION_MENU_SEARCH).setVisible(true);
		} else {
			menu.findItem(CONVERSATION_MENU_EDIT).setVisible(false);
			menu.findItem(CONVERSATION_MENU_SEARCH).setVisible(false);
			menu.findItem(CONVERSATION_MENU_CANCEL_EDIT).setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONVERSATION_MENU_SEARCH:
			onSearchRequested();
			break;
		case CONVERSATION_MENU_EDIT:
			currentState = EDIT_SATE;
			refreshState();
			break;
		case CONVERSATION_MENU_CANCEL_EDIT:
			currentState = LIST_STATE;
			mMultiSelectedSet.clear();
			refreshState();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	private void refreshState() {
		if(currentState == EDIT_SATE) {
			mBtnNewMsg.setVisibility(View.GONE);
			mBtnSelectAll.setVisibility(View.VISIBLE);
			mBtnCancelSelect.setVisibility(View.VISIBLE);
			mBtnDeleteMsg.setVisibility(View.VISIBLE);
			
			if(mMultiSelectedSet.size() > 0) {
				mBtnCancelSelect.setEnabled(true);
				mBtnDeleteMsg.setEnabled(true);
			} else {
				mBtnCancelSelect.setEnabled(false);
				mBtnDeleteMsg.setEnabled(false);
			}
			mBtnSelectAll.setEnabled(mMultiSelectedSet.size() != mListView.getCount());
		} else {
			mBtnNewMsg.setVisibility(View.VISIBLE);
			mBtnSelectAll.setVisibility(View.GONE);
			mBtnCancelSelect.setVisibility(View.GONE);
			mBtnDeleteMsg.setVisibility(View.GONE);
		}
	}

	private void prepareDate() {
		String selection = null;
		Intent intent = getIntent();
		if(intent != null) {
			String threadIds = intent.getStringExtra("thread_ids");
			String title = intent.getStringExtra("title");
			if(!TextUtils.isEmpty(title)) {
				setTitle(title);
			}
			if(!TextUtils.isEmpty(threadIds)) {
				selection = "thread_id in" + threadIds;
			}
		}
		CommonQueryHandler mQueryHandler = new CommonQueryHandler(this.getContentResolver());
		mQueryHandler.startQuery(0, mAdapter, Sms.CONVERSATION_URI, projection, selection, null, "date desc");
	}

	private void initViews() {
		mMultiSelectedSet = new HashSet<Integer>();
		
		mListView = (ListView) findViewById(R.id.lv_conversation);
		mAdapter = new ConversationAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		
		mBtnNewMsg = (Button) findViewById(R.id.btn_conversation_new_message);
		mBtnDeleteMsg = (Button) findViewById(R.id.btn_conversation_delete_message);
		mBtnSelectAll = (Button) findViewById(R.id.btn_conversation_select_all);
		mBtnCancelSelect = (Button) findViewById(R.id.btn_conversation_cancel_select);
		
		mBtnNewMsg.setOnClickListener(this);
		mBtnDeleteMsg.setOnClickListener(this);
		mBtnSelectAll.setOnClickListener(this);
		mBtnCancelSelect.setOnClickListener(this);
	}
	
	@Override
	public void onBackPressed() {
		if(currentState == EDIT_SATE) {
			currentState = LIST_STATE;
			mMultiSelectedSet.clear();
			refreshState();
		} else {
			super.onBackPressed();
		}
	}
	public class ConversationAdapter extends CursorAdapter {
		
		private ConversationViewHolder mHolder;
		
		public ConversationAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			mHolder = new ConversationViewHolder();
			View view = View.inflate(context, R.layout.conversation_item, null);
			mHolder.cbSelect = (CheckBox) view.findViewById(R.id.cb_conversation_item_select);
			mHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_conversation_item_icon);
			mHolder.tvName = (TextView) view.findViewById(R.id.tv_conversation_item_name);
			mHolder.tvBody = (TextView) view.findViewById(R.id.tv_conversation_item_body);
			mHolder.tvDate = (TextView) view.findViewById(R.id.tv_conversation_item_date);
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (ConversationViewHolder) view.getTag();
			
			int threadID = cursor.getInt(THREAD_ID_COLUMN_INDEX);
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			int msgCount = cursor.getInt(MSG_COUNT_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);

			if(currentState == LIST_STATE) {
				mHolder.cbSelect.setVisibility(View.GONE);
			} else {
				mHolder.cbSelect.setVisibility(View.VISIBLE);
				mHolder.cbSelect.setChecked(mMultiSelectedSet.contains(threadID));
			}
			mHolder.tvBody.setText(body);
			
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				mHolder.tvName.setText(address+"("+ msgCount+")");
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			}else {
				mHolder.tvName.setText(contactName+"("+ msgCount+")");
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
	
	public static class ConversationViewHolder {
		CheckBox cbSelect;
		ImageView ivIcon;
		TextView tvName;
		TextView tvBody;
		TextView tvDate;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		int threadId = cursor.getInt(THREAD_ID_COLUMN_INDEX);
		String address = cursor.getString(ADDRESS_COLUMN_INDEX);
		
		if(currentState == EDIT_SATE) {
			CheckBox cb = (CheckBox) view.findViewById(R.id.cb_conversation_item_select);
			if(cb.isChecked()) {
				mMultiSelectedSet.remove(threadId);
			} else {
				mMultiSelectedSet.add(threadId);
			}
			cb.setChecked(!cb.isChecked());
			refreshState();
		} else {
			Intent it = new Intent(ConversationUI.this, ConversationDetailUI.class);
			Bundle data = new Bundle();
			data.putInt("thread_id", threadId);
			data.putString("address", address);
			it.putExtras(data);
			startActivity(it);
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_conversation_new_message:
			startActivity(new Intent(this, NewMessageUI.class));
			break;
		case R.id.btn_conversation_delete_message:
			showConfirmDeleteDialog();
			break;
		case R.id.btn_conversation_select_all:
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(-1);
			int id;
			while(cursor.moveToNext()) {
				id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
				mMultiSelectedSet.add(id);
			}
			mAdapter.notifyDataSetChanged();
			refreshState();
			
			break;
		case R.id.btn_conversation_cancel_select:
			mMultiSelectedSet.clear();
			mAdapter.notifyDataSetChanged();
			refreshState();
			break;

		default:
			break;
		}
	}

	private void showConfirmDeleteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("删除");
		builder.setMessage("确认删除选中的会话吗？");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showDeleteProgressDialog();
				new Thread(new DeleteRunnable()).start();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	protected void showDeleteProgressDialog() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMax(mMultiSelectedSet.size());
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isStop  = true;
			}
		});
		mProgressDialog.show();
		mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				currentState = LIST_STATE;
				refreshState();
			}
		});
	}
	//delete table where id=thread_id
	public class DeleteRunnable implements Runnable {

		@Override
		public void run() {
			Iterator<Integer> iterator = mMultiSelectedSet.iterator();
			int thread_id;
			String where;
			String [] selectionArgs;
			while(iterator.hasNext()) {
				if(isStop) {
					break;
				}
				thread_id = iterator.next();
				where = "thread_id = ?";
				selectionArgs = new String[] {String.valueOf(thread_id)}; 
				int row = getContentResolver().delete(Sms.SMS_URI, where, selectionArgs);
				Log.d(TAG, "row : "+row + ", setSize:" + mMultiSelectedSet.size());
				SystemClock.sleep(1000);
				mProgressDialog.incrementProgressBy(1);
			}
			mMultiSelectedSet.clear();
			mProgressDialog.dismiss();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String thread_id = cursor.getString(THREAD_ID_COLUMN_INDEX);
		String groupName = getGroupName(thread_id);
		if(TextUtils.isEmpty(groupName)) {
			showSelectGroupDialog(thread_id);
		} else {
			Toast.makeText(this, "该会话已经存放于\"" + groupName + "\"中", 0).show();
		}
		return true;
	}

	private String getGroupName(String thread_id) {
		String selection = "thread_id = " + thread_id;
		Cursor cursor = getContentResolver().query(Sms.THREAD_GROUP_QUERY_URI, new String[] {"group_id"}, 
				selection, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			String group_id = cursor.getString(0);
			cursor.close();
			
			if(!TextUtils.isEmpty(group_id)) {
				selection = "_id = " + group_id;
				cursor = getContentResolver().query(Sms.GROUPS_QUERY_URI, new String[] {"group_name"}, 
						selection, null, null);
				if(cursor != null && cursor.moveToFirst()) {
					String groupName = cursor.getString(cursor.getColumnIndex("group_name"));
					cursor.close();
					return groupName;
				}
			}
		}
		return null;
	}

	private void showSelectGroupDialog(final String thread_id) {
		
		Cursor cursor = getContentResolver().query(Sms.GROUPS_QUERY_URI, null, null, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			String[] groupNameArray = new String[cursor.getCount()];
			final String[] groupIdArray = new String[cursor.getCount()];
			while(cursor.moveToNext()) {
				groupIdArray[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("_id"));
				groupNameArray[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("group_name"));
			}
			cursor.close();
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("选择将要加入的群组");
			builder.setItems(groupNameArray, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					addGroup(thread_id, groupIdArray[which]);
					dialog.dismiss();
				}
			});
			builder.show();
		}
	}

	private void addGroup(final String thread_id, final String group_id) {
		ContentValues values = new ContentValues();
		values.put("thread_id", thread_id);
		values.put("group_id", group_id);
		Uri uri = getContentResolver().insert(Sms.THREAD_GROUP_INSERT, values);
		long id = ContentUris.parseId(uri);
		if(id != -1) {
			Toast.makeText(ConversationUI.this, "添加成功!", 0).show();
		} else {
			Toast.makeText(ConversationUI.this, "添加失败!", 0).show();
		}
	}
}
