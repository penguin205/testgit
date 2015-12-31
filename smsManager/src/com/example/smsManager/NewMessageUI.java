package com.example.smsManager;

import com.example.smsManager.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.Toast;

public class NewMessageUI extends Activity implements OnClickListener, OnItemClickListener{
	
	private static final int NEW_MESSAGE_PICKUP_CONTACT = 10;
	private AutoCompleteTextView actvNumber;
	private EditText etContent;
	
	private final String[] projection = {
			"_id", 
			"data1", 
			"display_name"
	};
	private final int NUMBER_COLUMN_INDEX = 1;
	private final int NAME_COLUMN_INDEX = 2;
	private ContactAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_message);
		
		initView();
	}

	private void initView() {
		findViewById(R.id.btn_new_message_pickup_contact).setOnClickListener(this);
		findViewById(R.id.btn_new_message_send).setOnClickListener(this);
		
		actvNumber = (AutoCompleteTextView) findViewById(R.id.actv_new_message_number);
		etContent = (EditText) findViewById(R.id.et_new_message_content);
		
		mAdapter = new ContactAdapter(this, null);
		actvNumber.setAdapter(mAdapter);
		actvNumber.setOnItemClickListener(this);
		mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			
			@Override
			public Cursor runQuery(CharSequence constraint) {
				String selection = "data1 like ?";
				String selectionArgs[] = { constraint + "%"};
				Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, 
						projection, selection, selectionArgs, null);
				return cursor;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == NEW_MESSAGE_PICKUP_CONTACT && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			int id = Utils.getContactId(getContentResolver(), uri);
			if(id != -1) {
				String address = Utils.getContactAddress(getContentResolver(), id);
				if(!TextUtils.isEmpty(address)) {
					actvNumber.setText(address);
				}
			}
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_new_message_pickup_contact:
			Intent it = new Intent(Intent.ACTION_PICK);
			it.setData(Contacts.CONTENT_URI);
			startActivityForResult(it, NEW_MESSAGE_PICKUP_CONTACT);
			break;
		case R.id.btn_new_message_send:
			String number = actvNumber.getText().toString();
			String content = etContent.getText().toString();
			if(TextUtils.isEmpty(number)) {
				Toast.makeText(this, "号码不能为空", 0).show();
				break;
			}
			if(TextUtils.isEmpty(content)) {
				Toast.makeText(this, "请输入短信内容", 0).show();
				break;
			}
			Utils.sendMessage(this, number, content);
			finish();
			break;

		default:
			break;
		}
	}
	
	public class ContactAdapter extends CursorAdapter {

		@SuppressWarnings("deprecation")
		public ContactAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = View.inflate(context, R.layout.new_message_item, null);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tvName = (TextView) view.findViewById(R.id.tv_new_message_item_name);
			TextView tvNumber = (TextView) view.findViewById(R.id.tv_new_message_item_number);
			
			String contactNumber = cursor.getString(NUMBER_COLUMN_INDEX);
			String contactName = cursor.getString(NAME_COLUMN_INDEX);
			
			tvName.setText(contactName);
			tvNumber.setText(contactNumber);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = mAdapter.getCursor();
		cursor.moveToPosition(position);
		String number = cursor.getString(NUMBER_COLUMN_INDEX);
		actvNumber.setText(number);
		etContent.requestFocus();
	}
}
