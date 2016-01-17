package com.example.mobileknow.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.example.mobileknow.R;
import com.example.mobileknow.adapter.IAskListViewAdapter;
import com.example.mobileknow.entity.IAsk;
import com.example.mobileknow.ui.XListView;
import com.example.mobileknow.ui.XListView.IXListViewListener;
import com.example.mobileknow.utils.DateUtil;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class IAskAnswerFragment extends Fragment implements IXListViewListener {

	private View rootView;
	private XListView mListView;
	private IAskListViewAdapter mAdapter;
	private SimpleDateFormat dateFormat;
	private List<IAsk> mList;
	private Handler handler = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.iask_answer_fragment, null);

		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mListView = (XListView) rootView.findViewById(R.id.listview_iask_answer);
		mListView.setPullLoadEnable(true);
		mListView.setPullRefreshEnable(true);
		mListView.setXListViewListener(this);
		
		dateFormat = DateUtil.getSimpleDateFormat();
		mList = initData();
		
		mAdapter = new IAskListViewAdapter(getActivity(), mList);
		mListView.setAdapter(mAdapter);
	}

	private List<IAsk> initData() {
		List<IAsk> list = new ArrayList<IAsk>();
		IAsk ask = null;
		try {
			for (int i = 0; i < 20; i++) {
				ask = new IAsk();
				ask.setTitle("今天放假！" + i);
				ask.setDetails("放假一起玩游戏去吗？" + i);
				ask.setScore(10 + i);
				ask.setAnswerCount(5 + i);
				String date = "2015-12-02 11:54:" + i;
				ask.setAskTime(dateFormat.parse(date));
				ask.setAvatarId(R.drawable.user_icon);
				list.add(ask);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public void onRefresh() {
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				IAsk ask = null;
				try {
					ask = new IAsk();
					ask.setTitle("下拉刷新加载的最新问题标题");
					ask.setDetails("下拉刷新加载详情");
					ask.setScore(20);
					ask.setAnswerCount(5);
					ask.setAvatarId(R.drawable.user_icon);
					
					Random random = new Random();
					int s = random.nextInt(60);
					String date = "2015-12-02 11:54:" + s;
					ask.setAskTime(dateFormat.parse(date));
					
					mList.add(ask);
					
					Collections.sort(mList, comparator);
					
					mListView.setRefreshTime(dateFormat.format(new Date()));
					mAdapter.notifyDataSetChanged();
					mListView.stopRefresh();
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}}, 2000);
	}

	private Comparator<IAsk> comparator = new Comparator<IAsk>() {

		@Override
		public int compare(IAsk i1, IAsk i2) {
			if (i1.getAskTime().getTime() < i2.getAskTime().getTime()) {
				return 1;
			} else if (i1.getAskTime().getTime() > i2.getAskTime().getTime()) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	
	@Override
	public void onLoadMore() {
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				IAsk ask = null;
				try {
					ask = new IAsk();
					ask.setTitle("更多问题标题");
					ask.setDetails("更多问题详情");
					ask.setScore(20);
					ask.setAnswerCount(5);
					ask.setAvatarId(R.drawable.user_icon);
					
					Random random = new Random();
					int s = random.nextInt(60);
					String date = "2015-12-02 10:00:" + s;
					ask.setAskTime(dateFormat.parse(date));
					
					mList.add(ask);
					
					Collections.sort(mList, comparator);
					
					mAdapter.notifyDataSetChanged();
					mListView.stopLoadMore();
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}}, 2000);
	}
}
