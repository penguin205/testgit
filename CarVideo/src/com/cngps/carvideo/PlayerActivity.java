package com.cngps.carvideo;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cngps.carvideo.MySoundControlView.OnVolumeChangedListener;
import com.cngps.carvideo.MyVideoView.MySizeChangeLinstener;

/**
 * 播放界面
 */
public class PlayerActivity extends Activity implements OnClickListener {

	public static List<MovieInfo> playList;
	private final static int PROGRESS_CHANGED = 0;
	private final static int HIDE_WIDGETS = 1;
	private final static int SCREEN_FULL = 0;
	private final static int SCREEN_DEFAULT = 1;
	private final static int TIME = 4000;
	private MemorySqlTool mSqlTool;
	private MovieInfo currentInfo;
	private int position = -1;

	private MyVideoView myVideoView;
	private View overLay;
	private SeekBar mSeekBar;
	private TextView durationTextView;
	private TextView playedTextView;
	private GestureDetector mGestureDetector;
	private AudioManager mAudioManager;
	private OnAudioFocusChangeListener mAudioFocusChangeListener;
	private int maxVolume = 0;
	private int currentVolume = 0;

	private ImageButton btnPlay, btnSound;
	private TextView tvTitle;
	private View controlView;
	private PopupWindow controlPopupWindow;
	private MySoundControlView soundView;
	private PopupWindow soundPopupWindow;
	private View extralView;
	private PopupWindow extralWindow;
	private int titleHeight;
	private int screenWidth, screenHeight, controlHeight;
	private boolean mPausedByTransientLossOfFocus = false;
	private boolean isControllerShow = true;
	private boolean isPaused = false;
	private boolean isFullScreen = false;
	private boolean isSilent = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		
		overLay = findViewById(R.id.fl_overlay);
		findViewById(R.id.iv_reload).setOnClickListener(this);
		
		// init controlView
		controlView = getLayoutInflater().inflate(R.layout.play_control_layout, null);
		controlPopupWindow = new PopupWindow(controlView);
		durationTextView = (TextView) controlView.findViewById(R.id.duration);
		playedTextView = (TextView) controlView.findViewById(R.id.has_played);
		
		// init soundView
		soundView = new MySoundControlView(this);
		soundView.setOnVolumeChangeListener(new OnVolumeChangedListener() {

			@Override
			public void setYourVolume(int index) {
				if(isSilent) {
					//解除静音
					btnSound.setImageResource(R.drawable.ic_jog_dial_sound_on);
					isSilent = false;
				}
				cancelDelayHide();
				updateVolume(index);
				hideWidgetsDelay();
			}
		});
		soundPopupWindow = new PopupWindow(soundView);
		
		// init extralView
		extralView = getLayoutInflater().inflate(R.layout.top_layout, null);
		extralWindow = new PopupWindow(extralView);
		tvTitle = (TextView) extralView.findViewById(R.id.title);

		// init myVideoView
		myVideoView = (MyVideoView) findViewById(R.id.vv);
		myVideoView.post(new Runnable() {
		
			@Override
			public void run() {
				viewInited();
			}
			// 得到状态栏高度
			private void viewInited() {
				Rect rect = new Rect();
				myVideoView.getWindowVisibleDisplayFrame(rect);
				titleHeight = rect.top; 
			}
		});
		
		myVideoView.setMySizeChangeLinstener(new MySizeChangeLinstener() {

			@Override
			public void doMyThings() {
				setVideoScale(SCREEN_DEFAULT);
			}
		});
		
		myVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer arg0) {

				setVideoScale(SCREEN_DEFAULT);
				isFullScreen = false;
				if (isControllerShow) {
					showWidgets();
				}

				int i = myVideoView.getDuration();
				mSeekBar.setMax(i);
				durationTextView.setText(MainActivity.formatDuration(i));

				myVideoView.start();
				btnPlay.setImageResource(R.drawable.pause);
				hideWidgetsDelay();
				myHandler.sendEmptyMessage(PROGRESS_CHANGED);
			}
		});

		myVideoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {
				// 当前Video播放完成,清除数据库,显示overlay和控制条
				mSqlTool.delete();
				myVideoView.setVisibility(View.INVISIBLE);
				overLay.setVisibility(View.VISIBLE);
				btnPlay.setImageResource(R.drawable.play);
				showWidgets();
				cancelDelayHide();
				//自动播放下一视频
				if(playList != null && position < playList.size()-1) {
					myHandler.postDelayed(mPlayNextRunnable, 3000);
				}
			}
		});

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

			@Override
			public void onAudioFocusChange(int focusChange) {
				if(focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        			//Log.d("ffff", "AUDIOFOCUS_LOSS : lost focus so pause");
                    if(myVideoView.isPlaying()) {
                        mPausedByTransientLossOfFocus = false;
                    }
                	if(null != myVideoView){
                		myVideoView.pause();
                		btnPlay.setImageResource(R.drawable.play);
                	}
        		}else if(focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        			//Log.d("ffff", "AUDIOFOCUS_GAIN : gain focus again, so play");
                    if(!myVideoView.isPlaying() && mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                    	if(null != myVideoView){
                    		myVideoView.start();
                    		btnPlay.setImageResource(R.drawable.pause);
        					hideWidgetsDelay();
                    	} 
                    } 
        		}
			}
		};
		
		// init btnSound and btnPlay
		btnSound = (ImageButton) controlView.findViewById(R.id.btnSound);
		btnSound.setAlpha(findAlphaFromSound());
		btnPlay = (ImageButton) controlView.findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelDelayHide();
				if (isPaused) {
					myVideoView.start();
					btnPlay.setImageResource(R.drawable.pause);
					hideWidgetsDelay();
				} else {
					myVideoView.pause();
					btnPlay.setImageResource(R.drawable.play);
				}
				isPaused = !isPaused;
			}
		});

		btnSound.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cancelDelayHide();
				if (soundPopupWindow.isShowing()) {
					soundPopupWindow.dismiss();
				} else {
					soundPopupWindow.showAtLocation(myVideoView, Gravity.NO_GRAVITY, 0, 0);
					soundPopupWindow.update(960, 360, MySoundControlView.MY_WIDTH,
							MySoundControlView.MY_HEIGHT);
				}
				hideWidgetsDelay();
			}
		});

		btnSound.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				if (isSilent) {
					btnSound.setImageResource(R.drawable.ic_jog_dial_sound_on);
				} else {
					btnSound.setImageResource(R.drawable.ic_jog_dial_sound_off);
				}
				isSilent = !isSilent;
				updateVolume(currentVolume);
				cancelDelayHide();
				hideWidgetsDelay();
				return true;
			}

		});
		
		// init seekbar
		mSeekBar = (SeekBar) controlView.findViewById(R.id.seekbar);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekbar, int progress,
					boolean fromUser) {

				if (fromUser)
					myVideoView.seekTo(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				myHandler.removeMessages(HIDE_WIDGETS);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				myHandler.sendEmptyMessageDelayed(HIDE_WIDGETS, TIME);
			}
		});

		getScreenSize();

		mGestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (isFullScreen) {
					setVideoScale(SCREEN_DEFAULT);
				} else {
					setVideoScale(SCREEN_FULL);
				}
				isFullScreen = !isFullScreen;
				
				if (!isControllerShow) {
					showWidgets();
					hideWidgetsDelay();
				} else {
					cancelDelayHide();
					hideWidgets();
				}
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (isPaused) {
					myVideoView.start();
					btnPlay.setImageResource(R.drawable.pause);
					cancelDelayHide();
					hideWidgetsDelay();
				} else {
					myVideoView.pause();
					btnPlay.setImageResource(R.drawable.play);
					cancelDelayHide();
					showWidgets();
				}
				isPaused = !isPaused;
			}
		});

		IntentFilter mountfilter = new IntentFilter();
		mountfilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		mountfilter.addDataScheme("file");
		registerReceiver(mMountReceiver, mountfilter);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.cngps.carvideo.scan_finish");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(mScanResultReceiver, filter);
		
		mSqlTool = new MemorySqlTool(PlayerActivity.this);
	}

	private BroadcastReceiver mMountReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String rootPath = intent.getData().getPath();
			if(myVideoView.getVideoURI().toString().startsWith(rootPath)) {
				myVideoView.stopPlayback();
				Toast.makeText(getApplicationContext(), "视频不存在!", Toast.LENGTH_SHORT).show();
				if(currentInfo != null) {
		        	mSqlTool.updataInfo(new MovieInfo(myVideoView.getCurrentPosition(), myVideoView.getDuration(), 
		        			currentInfo.displayName, currentInfo.path));
		        }
				finish();
			}
		}
	};
	
	private BroadcastReceiver mScanResultReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			playList = intent.getParcelableArrayListExtra("playlist");
			for (int i = 0; i < playList.size(); i++) {
				MovieInfo movieInfo = playList.get(i);
				if(movieInfo.path.equals(currentInfo.path)) {
					position = i;
					break;
				}
			}
		}
	};
	
	Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case PROGRESS_CHANGED:
				int i = myVideoView.getCurrentPosition();
				mSeekBar.setProgress(i);
				playedTextView.setText(MainActivity.formatDuration(i));
				sendEmptyMessage(PROGRESS_CHANGED); //循环更新进度
				break;

			case HIDE_WIDGETS:
				setVideoScale(SCREEN_FULL);
				isFullScreen = true;
				hideWidgets();
				break;
			}
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = mGestureDetector.onTouchEvent(event);

		if (!result) {
			result = super.onTouchEvent(event);
		}
		return result;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		getScreenSize();
		if (isControllerShow) {
			cancelDelayHide();
			hideWidgets();
			showWidgets();
			hideWidgetsDelay();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		initBooleans();
		
		//按Home键回到主页再进入情况
		if(currentInfo != null) {
			File file = new File(currentInfo.path);
			if(!file.exists()) {
				Toast.makeText(this, "原视频文件已不存在!", Toast.LENGTH_LONG).show();
				//延时1秒后回来MainActivity
				myHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						Intent it = new Intent(PlayerActivity.this, MainActivity.class);
						startActivity(it);
						finish();
					}
				}, 1000);
				
			} else {
				MovieInfo memoryInfo = mSqlTool.getInfo();
				if(memoryInfo != null && memoryInfo.path.equals(currentInfo.path)) {
					currentInfo.seekPos = memoryInfo.seekPos;
				} else {
					currentInfo.seekPos = 0;
				}
				play(currentInfo);
			}
		} else {
			// 接收MainActivity传递过来的参数
			MovieInfo info = null;
			playList = getIntent().getParcelableArrayListExtra("playlist");
			position = getIntent().getIntExtra("position", 0);
			if(playList != null && playList.size() > 0) {
				info = playList.get(position);
			} else {
				info = (MovieInfo) getIntent().getParcelableExtra("movie_info");
			}
			
			if(info != null) {
				//更新seekPos
				MovieInfo memoryInfo = mSqlTool.getInfo();
				if(memoryInfo != null && memoryInfo.path.equals(info.path)) {
					info.seekPos = memoryInfo.seekPos;
				} else {
					info.seekPos = 0;
				}
				
				play(info);
				
				currentInfo = info;
			} else {
				// 接收由外界传递过来的参数
				String uriPath = getIntent().getDataString();
				if (uriPath != null) {
					if (myVideoView.getVideoHeight() == 0) {
						uriPath = Uri.decode(uriPath); //解决中文乱码
				        //Log.d("ffff", "uri path : " + uriPath);
						String[] strs = uriPath.split("/");
						MovieInfo trigInfo = new MovieInfo(0, myVideoView.getDuration(), 
								strs[strs.length-1], uriPath.replace("file:///", "/"));
						
						play(trigInfo);
						
						currentInfo = trigInfo;
					}
				} 
			}
		}
		
		if(currentInfo == null) {
			btnPlay.setImageResource(R.drawable.play);
		}
		
		// 延时隐藏所有的popupWindow
		if (myVideoView.getVideoHeight() != 0) {
			btnPlay.setImageResource(R.drawable.pause);
			hideWidgetsDelay();
		}
	}

	private void initBooleans() {
		isControllerShow = true;
		isPaused = false;
		isFullScreen = false;
		isSilent = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		myVideoView.pause();
		btnPlay.setImageResource(R.drawable.play);
		
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        // 记忆
        if(currentInfo != null && myVideoView.getCurrentPosition()+20 < myVideoView.getDuration()) {
        	mSqlTool.updataInfo(new MovieInfo(myVideoView.getCurrentPosition(), myVideoView.getDuration(), 
        			currentInfo.displayName, currentInfo.path));
        } else {
        	mSqlTool.delete();
        }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		myVideoView.stopPlayback();
		if (controlPopupWindow.isShowing()) {
			controlPopupWindow.dismiss();
			extralWindow.dismiss();
		}
		if (soundPopupWindow.isShowing()) {
			soundPopupWindow.dismiss();
		}

		myHandler.removeMessages(PROGRESS_CHANGED);
		myHandler.removeMessages(HIDE_WIDGETS);

		if(mMountReceiver != null) {
			this.unregisterReceiver(mMountReceiver);
			mMountReceiver = null;
		}
		if(mScanResultReceiver != null) {
			this.unregisterReceiver(mScanResultReceiver);
			mScanResultReceiver = null;
		}
		
		currentInfo = null;
	}
 
	/**
	 * 得到屏幕宽高并计算controlView高
	 */
	private void getScreenSize() {
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		// 初始化controlView高度
		controlHeight = screenHeight / 7; 
	}

	/**
	 * 隐藏controlPopupWindow,extralWindow,soundPopupWindow
	 */
	private void hideWidgets() {
		if (controlPopupWindow.isShowing()) {
			controlPopupWindow.dismiss();
			extralWindow.dismiss();
			isControllerShow = false;
		}
		if (soundPopupWindow.isShowing()) {
			soundPopupWindow.dismiss();
		}
	}
	
	/**
	 * 显示controlPopupWindow,extralWindow
	 */
	private void showWidgets() {
		controlPopupWindow.showAtLocation(myVideoView, Gravity.BOTTOM, 0, 0);
		controlPopupWindow.update(0, 0, screenWidth, controlHeight);
		extralWindow.showAtLocation(myVideoView, Gravity.TOP, 0, 0);
		if (isFullScreen) {
			extralWindow.update(0, 0, screenWidth, 66);
		} else {
			extralWindow.update(0, titleHeight, screenWidth, 66);
		}
		isControllerShow = true;
	}
	
	/**
	 * 延迟TIME毫秒显示controlPopupWindow,extralWindow
	 */
	private void hideWidgetsDelay() {
		myHandler.sendEmptyMessageDelayed(HIDE_WIDGETS, TIME);
	}
	
	/**
	 * 取消显示controlPopupWindow,extralWindow
	 */
	private void cancelDelayHide() {
		myHandler.removeMessages(HIDE_WIDGETS);
	}

	/**
	 * 设置屏幕大小 
	 */
	private void setVideoScale(int flag) {
		switch (flag) {
		case SCREEN_FULL:
			myVideoView.setVideoScale(screenWidth, screenHeight);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			break;
		case SCREEN_DEFAULT:
			int videoWidth = myVideoView.getVideoWidth();
			int videoHeight = myVideoView.getVideoHeight();
			int mWidth = screenWidth;
			int mHeight = screenHeight - 25;

			if (videoWidth > 0 && videoHeight > 0) {
				if (videoWidth * mHeight > mWidth * videoHeight)
					mHeight = mWidth * videoHeight / videoWidth;
				else if (videoWidth * mHeight < mWidth * videoHeight)
					mWidth = mHeight * videoWidth / videoHeight;
			}
			myVideoView.setVideoScale(mWidth, mHeight);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			break;
		default:
			break;
		}
	}
	/**
	 * 根据当前音量大小设置btnSound的alpha值
	 */
	private float findAlphaFromSound() {
		if (mAudioManager != null)
			return currentVolume * (0.8f - 0.3f) / maxVolume + 0.3f;
		else
			return 0.8f;
	}
	
	/**
	 * 更新音量大小
	 */
	private void updateVolume(int index) {
		if(index > 10) {
			index = 10;
		}  else if (index < 0) {
			index = 0;
		}
		//index值最大为10， 设成声音里要转换一下
		index = index * 15 / 10;
		
		if (mAudioManager != null) {
			if (isSilent)
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			else
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

			currentVolume = index;
			btnSound.setAlpha(findAlphaFromSound());
		}
	}
	
	/**
	 * 播放下一首runnable
	 */
	private Runnable mPlayNextRunnable = new Runnable() {
		
		@Override
		public void run() {
			currentInfo = playList.get(++position);
			play();
		}
	};
	
	@Override
	public void onClick(View v) {
		myHandler.removeCallbacks(mPlayNextRunnable);
		play();
	}

	private void play() {
		myVideoView.setVisibility(View.VISIBLE);
		overLay.setVisibility(View.INVISIBLE);
		if(playList != null) {
			play(playList.get(position));
		}
	}
	
	/**
	 * 播放特定的MovieInfo
	 */
	private void play(MovieInfo info) {
		myVideoView.setVideoPath(info.path, info.seekPos);
		btnPlay.setImageResource(R.drawable.pause);
		tvTitle.setText(info.displayName);
		//请求音源
		mAudioManager.requestAudioFocus(mAudioFocusChangeListener, 1, /*CommonStatic.EXTRA_SOUND_SOURCE_APP_KEY_Vedio*/
				AudioManager.AUDIOFOCUS_GAIN);
		//显示控件Window
		myHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						showWidgets();
					}
				}, 800);
	}
}