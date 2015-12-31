package com.example.smsManager;

import com.example.smsManager.utils.CommonQueryHandler;
import com.example.smsManager.utils.Sms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "ShowToast" })
public class GroupUI extends ListActivity implements OnItemLongClickListener, OnItemClickListener {
	private ListView mListview;
	private GroupAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initView();
		prepareData();
	}

	private void prepareData() {
		CommonQueryHandler queryHandler = new CommonQueryHandler(getContentResolver());
		queryHandler.startQuery(0, mAdapter, Sms.GROUPS_QUERY_URI, null, null, null, null);
	}

	private void initView() {
		mListview = getListView();
		mAdapter = new GroupAdapter(this, null);
		mListview.setAdapter(mAdapter);
		
		mListview.setOnItemLongClickListener(this);
		mListview.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_group, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Cursor cursor = mAdapter.getCursor();
		if(cursor == null || cursor.getCount() == 0) {
			menu.findItem(R.id.menu_delete_all_group).setVisible(false);
		} else {
			menu.findItem(R.id.menu_delete_all_group).setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_create_single_group) {
			showCreateGroupDialog();
		} else if(item.getItemId() == R.id.menu_delete_all_group) {
			showDeleteAllGroupDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	private void showDeleteAllGroupDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("删除所有群组");
		builder.setMessage("删除所有群组清除所有联系人的分组信息, 确认继续?");
		builder.setNegativeButton("取消", null);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteAllGroup();
				dialog.dismiss();
			}
		});
		builder.show();
	}

	protected void deleteAllGroup() {
		int count = getContentResolver().delete(Sms.GROUPS_ALL_DELETE_URI, null, null);
		if(count > 0) {
			Toast.makeText(this, "删除成功!", 0).show();
		} else {
			Toast.makeText(this, "删除失败!", 0).show();
		}
	}

	private void showCreateGroupDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("新建群组");
		final AlertDialog dialog = builder.create();
		
		View view = View.inflate(this, R.layout.create_group, null);
		final EditText etGroupName = (EditText) view.findViewById(R.id.et_create_group_name);
		view.findViewById(R.id.btn_create_group_confirm).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String groupName = etGroupName.getText().toString();
				if (TextUtils.isEmpty(groupName)) {
					Toast.makeText(GroupUI.this, "群组名不能为空！", 0).show();
				} else {
					createGroup(groupName);
					dialog.dismiss();
				}
			}
		});
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
		
		LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.7);
		dialog.getWindow().setAttributes(lp);
	}

	protected void createGroup(String groupName) {
		ContentValues values = new ContentValues();
		values.put("group_name", groupName);
		Uri uri = getContentResolver().insert(Sms.GROUPS_INSERT_URI, values);
		if(ContentUris.parseId(uri) >= 0) {
			Toast.makeText(this, "创建群组成功!", 0).show();
		} else {
			Toast.makeText(this, "创建群组失败!", 0).show();
		}
	}
	

	
	class GroupAdapter extends CursorAdapter {

		public GroupAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.group_item, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tvGroupName = (TextView) view.findViewById(R.id.tv_group_item_group_name);
			String groupName = cursor.getString(cursor.getColumnIndex("group_name"));
			String group_id = cursor.getString(cursor.getColumnIndex("_id"));
			Log.d("GroupUI", "bindview id:" + group_id);
			if(!TextUtils.isEmpty(groupName)) {
				tvGroupName.setText(groupName);
			}
		}
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		long group_id = cursor.getLong(cursor.getColumnIndex("_id"));
	
		showOperationDialog(group_id);
		return true;
	}

	private void showOperationDialog(final long group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setItems(new String[]{"修改","删除"}, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					showModifyDialog(group_id);
					break;
				case 1:
					showDeleteDialog(group_id);
					break;
				}
				dialog.dismiss();
			}
			
		});
		builder.show();
	}

	protected void showDeleteDialog(final long group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("删除");
		builder.setMessage("删除群组将会删除群组中所包含的所有短信的关联, 确认继续?");
		builder.setNegativeButton("取消", null);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteGroup(group_id);
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	protected void updateGroup(long group_id, String groupName) {
		ContentValues values = new ContentValues();
		values.put("group_name", groupName);
		String where = "_id = " + String.valueOf(group_id);
		int count = getContentResolver().update(Sms.GROUPS_UPDATE_URI, values, where, null);
		if(count > 0) {
			Toast.makeText(this, "修组成功!", 0).show();
		} else {
			Toast.makeText(this, "修组失败!", 0).show();
		}
	}

	
	protected void deleteGroup(long group_id) {
		Uri uri = ContentUris.withAppendedId(Sms.GROUPS_SINGLE_DELETE_URI, group_id);
		int count = getContentResolver().delete(uri, null, null);
		if(count > 0) {
			Toast.makeText(this, "删除成功!", 0).show();
		} else {
			Toast.makeText(this, "删除失败!", 0).show();
		}
	}
	protected void showModifyDialog(final long group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("修改群组");
		final AlertDialog dialog = builder.create();
		
		View view = View.inflate(this, R.layout.create_group, null);
		final EditText etGroupName = (EditText) view.findViewById(R.id.et_create_group_name);
		Button btnModifyGroupName = (Button) view.findViewById(R.id.btn_create_group_confirm);
		btnModifyGroupName.setText("确认修改");
		btnModifyGroupName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String groupName = etGroupName.getText().toString();
				if (TextUtils.isEmpty(groupName)) {
					Toast.makeText(GroupUI.this, "群组名不能为空！", 0).show();
				} else {
					updateGroup(group_id, groupName);
					dialog.dismiss();
				}
			}
		});
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
		
		LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.7);
		dialog.getWindow().setAttributes(lp);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String group_id = cursor.getString(cursor.getColumnIndex("_id"));
		String group_name = cursor.getString(cursor.getColumnIndex("group_name"));
		
		String selection = "group_id = " + group_id;
		Cursor c = getContentResolver().query(Sms.THREAD_GROUP_QUERY_URI,
				new String[] { "thread_id" }, selection, null, null);
		if(c != null && c.getCount() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("(");
			while(c.moveToNext()) {
				sb.append(c.getString(0)+",");
			}
			c.close();
			String threadIDs = sb.substring(0, sb.lastIndexOf(",")) + ")";
			
			Intent intent = new Intent(this, ConversationUI.class);
			intent.putExtra("thread_ids", threadIDs);
			intent.putExtra("title", group_name);
			startActivity(intent);
		}
	}
}
