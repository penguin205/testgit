package com.example.mobileknow.server;

import com.example.mobileknow.entity.ChatMessage;
import com.example.mobileknow.ui.UpdateView;
import com.example.mobileknow.utils.HttpUtils;

import android.content.Context;
import android.os.AsyncTask;

public class IAskAsyncTask {
	
	private Context context;
	private UpdateView updateView;
	
	
	public IAskAsyncTask(Context context, UpdateView updateView) {
		this.context = context;
		this.updateView = updateView;
	}

	public void queryAnswer(String result) {
		new QueryAsynTask().execute(result);
	}
	
	class QueryAsynTask extends AsyncTask<String, Void, ChatMessage> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected ChatMessage doInBackground(String... params) {
			String str = params[0];
			return HttpUtils.sendMessage(str);
		}

		@Override
		protected void onPostExecute(ChatMessage result) {
			super.onPostExecute(result);
			if(result != null) {
				updateView.upateSheView(result);
			}
		}
	}
}


 
