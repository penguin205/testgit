package com.example.smsManager.utils;


import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.CursorAdapter;

public class CommonQueryHandler extends AsyncQueryHandler {

	private String TAG = "CommonQueryHandler";
	private OnQueryNotifyCompleteListener mListener;

	public CommonQueryHandler(ContentResolver cr) {
		super(cr);
	}

	@Override
	protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if(mListener != null) {
			mListener.onPreNotify(token, cookie, cursor);
		}
		if(cookie != null) {
			notifyAdapter((CursorAdapter)cookie, cursor);
		}
		if(mListener != null) {
			mListener.onPostNotify(token, cookie, cursor);
		}
	}

	private void notifyAdapter(CursorAdapter adapter, Cursor cursor) {
		adapter.changeCursor(cursor);
	}
	
	public void setOnNotifyQueryListener(OnQueryNotifyCompleteListener listener) {
		this.mListener = listener;
	}
	
	public interface OnQueryNotifyCompleteListener {
		public void onPreNotify(int token, Object cookie, Cursor cursor);
		public void onPostNotify(int token, Object cookie, Cursor cursor);
	} 

}
