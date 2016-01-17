package com.example.mobileknow.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.mobileknow.R;
import com.example.mobileknow.entity.IAnswer;
import com.example.mobileknow.ui.UpdateView;

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
	
	class QueryAsynTask extends AsyncTask<String, Void, IAnswer> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected IAnswer doInBackground(String... params) {
			String str = params[0];
			IAnswer answer = getAnswer(str);
			return answer;
		}

		@Override
		protected void onPostExecute(IAnswer result) {
			super.onPostExecute(result);
			if(result != null) {
				int type = result.getAnserType();
				switch (type) {
				case IAnswer.JOKE_ANSWER_TYPE:
					updateView.upateSheView(result.getJoke());
					break;
				case IAnswer.POEM_ANSWER_TYPE:
					updateView.upateSheView(result.getPoem());
					break;
				case IAnswer.MM_ANSWER_TYPE:
					updateView.updateMmView(result.getMmResId());
					break;
					
				default:
					break;
				}
			}
		}
	}

	private IAnswer getAnswer(String str) {
		IAnswer answer = null;
		Random random = new Random();
		int index = -1;
		if(str.contains("笑话")) {
			String[] jokeArr = context.getResources().getStringArray(R.array.joke);
			random = new Random();
			index = random.nextInt(jokeArr.length);
			answer = new IAnswer();
			answer.setAnswerType(IAnswer.JOKE_ANSWER_TYPE);
			answer.setJoke(jokeArr[index]);
			return answer;
		} else if(str.contains("美女") || str.contains("妹子") || str.contains("妹纸")) {
			int mmResId = mmRandom();
			answer = new IAnswer();
			answer.setAnswerType(IAnswer.MM_ANSWER_TYPE);
			answer.setMmResId(mmResId);
			return answer;
		} else if(str.contains("唐诗") || str.contains("诗歌")) {
			String[] poemArr = context.getResources().getStringArray(R.array.tang_poetry);
			index = random.nextInt(poemArr.length);
			answer = new IAnswer();
			answer.setAnswerType(IAnswer.POEM_ANSWER_TYPE);
			answer.setPoem(poemArr[index]);
			return answer;
		}
		return null;
	}

	private int mmRandom() {
		List<Integer> mmArr = new ArrayList<Integer>();
		mmArr.add(R.drawable.mm0);
		mmArr.add(R.drawable.mm1);
		mmArr.add(R.drawable.mm2);
		mmArr.add(R.drawable.mm3);
		mmArr.add(R.drawable.mm4);
		mmArr.add(R.drawable.mm5);
		mmArr.add(R.drawable.mm6);
		mmArr.add(R.drawable.mm7);
		mmArr.add(R.drawable.mm8);
		mmArr.add(R.drawable.mm9);
		mmArr.add(R.drawable.mm10);
		mmArr.add(R.drawable.mm11);
		mmArr.add(R.drawable.mm12);
		mmArr.add(R.drawable.mm13);
		mmArr.add(R.drawable.mm14);
		mmArr.add(R.drawable.mm15);
		mmArr.add(R.drawable.mm16);
		mmArr.add(R.drawable.mm17);
		mmArr.add(R.drawable.mm18);
		mmArr.add(R.drawable.mm19);
		mmArr.add(R.drawable.mm20);
		mmArr.add(R.drawable.mm21);
		mmArr.add(R.drawable.mm22);
		mmArr.add(R.drawable.mm23);
		
		Random r = new Random();
		int index = r.nextInt(mmArr.size());
		return mmArr.get(index);
	}
}


 
