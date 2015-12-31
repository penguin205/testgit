package com.example.smsManager;

import com.example.smsManager.utils.CommonQueryHandler;
import com.example.smsManager.utils.CommonQueryHandler.OnQueryNotifyCompleteListener;
import com.example.smsManager.utils.Sms;
import com.example.smsManager.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationDetailUI extends Activity implements OnClickListener{
	private int threadId;
	private String address;
	private ListView mListView;
	private ConversationDetailAdapter mAdapter;
	
	private String[] projection = { "_id", "date", "body", "type" };
	private static final int DATE_COLUMN_INDEX = 1;
	private static final int BODY_COLUMN_INDEX = 2;
	private static final int TYPE_COLUMN_INDEX = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.conversation_detail);
		
		initTitle();
		
		initView();
		
		prepareData();
	}

	private void prepareData() {
		CommonQueryHandler queryHandler = new CommonQueryHandler(getContentResolver());
		queryHandler.setOnNotifyQueryListener(mQueryListener);
		String selection = "thread_id = ?";
		String selectionArgs[] = {String.valueOf(threadId)};
		queryHandler.startQuery(0, mAdapter, Sms.SMS_URI, projection , selection, selectionArgs, "date");
	}
	
	private OnQueryNotifyCompleteListener mQueryListener = new OnQueryNotifyCompleteListener() {
		
		@Override
		public void onPreNotify(int token, Object cookie, Cursor cursor) {
		}

		@Override
		public void onPostNotify(int token, Object cookie, Cursor cursor) {
			mListView.setSelection(mListView.getCount());
		}
	};
	private void initView() {
		findViewById(R.id.btn_conversation_detail_back).setOnClickListener(this);
		findViewById(R.id.btn_conversation_detail_send).setOnClickListener(this);
		
		mListView = (ListView) findViewById(R.id.lv_conversation_detail);
		mAdapter = new ConversationDetailAdapter(this,null);
		mListView.setAdapter(mAdapter);
	}

	private void initTitle() {
		Bundle data = getIntent().getExtras();
		threadId = data.getInt("thread_id");
		address = data.getString("address");	
		
		String contactName = Utils.getContactName(getContentResolver(), address);
		TextView tvName = (TextView) findViewById(R.id.tv_conversation_detail_name);
		if(TextUtils.isEmpty(contactName)){
			tvName.setText(address);
		} else {
			tvName.setText(contactName);
		}
	}
	
	public class ConversationDetailAdapter extends CursorAdapter {

		private ConversationDetailViewHolder mHolder;

		@SuppressWarnings("deprecation")
		public ConversationDetailAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}
		
		@Override
		protected void onContentChanged() {
			super.onContentChanged();
			mListView.setSelection(mListView.getCount());
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			mHolder = new ConversationDetailViewHolder();
			View view = View.inflate(context, R.layout.conversation_detail_item, null);
			mHolder.receiveView = view.findViewById(R.id.tl_conversation_detail_item_receive);
			mHolder.tvReceiveBody = (TextView) view.findViewById(R.id.tv_conversation_detail_item_receive_body);
			mHolder.tvReceiveDate = (TextView) view.findViewById(R.id.tv_conversation_detail_item_receive_date);
			
			mHolder.sendView = view.findViewById(R.id.tl_conversation_detail_item_send);
			mHolder.tvSendBody = (TextView) view.findViewById(R.id.tv_conversation_detail_item_send_body);
			mHolder.tvSendDate = (TextView) view.findViewById(R.id.tv_conversation_detail_item_send_date);
			
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (ConversationDetailViewHolder) view.getTag();
			
			String body = cursor.getString(BODY_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			int type = cursor.getInt(TYPE_COLUMN_INDEX);
			
			String dateStr = null;
			if(DateUtils.isToday(date)) {
				dateStr = DateFormat.getTimeFormat(context).format(date);
			} else {
				dateStr = DateFormat.getDateFormat(context).format(date);
			}
			
			if(type == Sms.SMS_RECEIVE) {
				mHolder.receiveView.setVisibility(View.VISIBLE);
				mHolder.sendView.setVisibility(View.GONE);
				mHolder.tvReceiveBody.setText(body);
				mHolder.tvReceiveDate.setText(dateStr);
			} else if(type == Sms.SMS_SEND) {
				mHolder.receiveView.setVisibility(View.GONE);
				mHolder.sendView.setVisibility(View.VISIBLE);
				mHolder.tvSendBody.setText(body);
				mHolder.tvSendDate.setText(dateStr);
			}
		}
		
	}
	
	static class ConversationDetailViewHolder {
		View receiveView;
		TextView tvReceiveBody;
		TextView tvReceiveDate;
		
		View sendView;
		TextView tvSendBody;
		TextView tvSendDate;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_conversation_detail_back:
			finish();
			break;
		case R.id.btn_conversation_detail_send:
			EditText etContent = (EditText) findViewById(R.id.et_conversation_detail_msg);
			String content = etContent.getText().toString();
			if(TextUtils.isEmpty(content)) {
				Toast.makeText(this, "«Î ‰»Î∂Ã–≈ƒ⁄»›£°", 0).show();
				break;
			} else {
				Utils.sendMessage(this, address, content);
			}
			etContent.setText("");
			break;
		default:
			break;
		}
	}
}
