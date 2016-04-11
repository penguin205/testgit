package com.example.kakalauncher2.fragment;

import com.example.kakalauncher2.R;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class PrivateMessageLauncherFragment extends LauncherBaseFragment {

	private ImageView ivLikeVideo;
	private ImageView ivThinkReward;
	private ImageView ivWatchMovie;
	private ImageView ivThisWeek;
	private boolean started = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_private_message_launcher, null);
		ivLikeVideo=(ImageView) rootView.findViewById(R.id.iv_private_message_like_video);
		ivThinkReward=(ImageView) rootView.findViewById(R.id.iv_private_message_think_reward);
		ivWatchMovie=(ImageView) rootView.findViewById(R.id.iv_private_message_watch_movie);
		ivThisWeek=(ImageView) rootView.findViewById(R.id.private_message_this_week);
		return rootView;
	}
	
	@Override
	public void startAnimation() {
		started =true;
		ivLikeVideo.setVisibility(View.INVISIBLE);
		ivThinkReward.setVisibility(View.INVISIBLE);
		ivWatchMovie.setVisibility(View.INVISIBLE);
		ivThisWeek.setVisibility(View.INVISIBLE);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(started) {
					likeVideoAnimation();
				}
			}
		}, 500);
	}

	protected void likeVideoAnimation() {
		ivLikeVideo.setVisibility(View.VISIBLE);
		
		Animation likeVideoAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.private_message_launcher);
		ivLikeVideo.startAnimation(likeVideoAnimation);
		likeVideoAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if(started) {
					thinkRewardAnimation();
				}
			}
		});
	}

	protected void thinkRewardAnimation() {
		ivThinkReward.setVisibility(View.VISIBLE);
		
		Animation thinkRewardAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.private_message_launcher);
		ivThinkReward.startAnimation(thinkRewardAnimation);
		thinkRewardAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if(started) {
					watchMovieAnimation();
				}
			}
		});
	}
	
	protected void watchMovieAnimation() {
		ivWatchMovie.setVisibility(View.VISIBLE);
		
		Animation watchMovieAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.private_message_launcher);
		ivWatchMovie.startAnimation(watchMovieAnimation);
		watchMovieAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if(started) {
					thisWeekAnimation();
				}
			}
		});
	}

	protected void thisWeekAnimation() {
		ivThisWeek.setVisibility(View.VISIBLE);
		
		Animation thisWeekAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.private_message_launcher);
		ivThisWeek.startAnimation(thisWeekAnimation);
	}


	@Override
	public void stopAnimation() {
		started = false;
		ivLikeVideo.clearAnimation();
		ivThinkReward.clearAnimation();
		ivWatchMovie.clearAnimation();
		ivThisWeek.clearAnimation();
	}

}
