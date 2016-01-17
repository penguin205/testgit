package com.example.mobileknow.adapter;

import java.text.SimpleDateFormat;
import java.util.List;

import com.example.mobileknow.R;
import com.example.mobileknow.entity.IAsk;
import com.example.mobileknow.utils.DateUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IAskListViewAdapter extends BaseAdapter {
	private Context context;
	private List<IAsk> list;
	
	public IAskListViewAdapter(Context context, List<IAsk> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		IAskViewHolder holder = null;
		if(convertView == null) {
			convertView = View.inflate(context, R.layout.iask_listview_item, null);
			
			holder = new IAskViewHolder();
			holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_listview_item_icon);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_listview_item_title);
			holder.tvMessage = (TextView) convertView.findViewById(R.id.tv_listview_item_message);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tv_listview_item_date);
			holder.tvAnswerCnt = (TextView) convertView.findViewById(R.id.tv_listview_item_answer_count);
			holder.tvRewardScore = (TextView) convertView.findViewById(R.id.tv_listview_item_reward_score);
			convertView.setTag(holder);
		} else {
			holder = (IAskViewHolder) convertView.getTag();
		}
		holder.ivIcon.setBackgroundResource(list.get(position).getAvatarId());
		holder.tvTitle.setText(list.get(position).getTitle());
		holder.tvMessage.setText(list.get(position).getDetails());
		SimpleDateFormat dateFormat = DateUtil.getSimpleDateFormat();
		String dateStr = dateFormat.format(list.get(position).getAskTime());
		holder.tvDate.setText(dateStr);
		holder.tvAnswerCnt.setText(String.valueOf(list.get(position).getAnswerCount()));
		holder.tvRewardScore.setText(String.valueOf(list.get(position).getScore()));
		return convertView;
	}
	
	public static class IAskViewHolder {
		ImageView ivIcon;
		TextView  tvTitle;
		TextView tvMessage;
		TextView tvDate;
		TextView tvAnswerCnt;
		TextView tvRewardScore;
	}

}
