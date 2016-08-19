package com.cngps.carvideo;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener, OnScrollListener {

	//private Uri videoListUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String TF_PATH = "/mnt/media_rw/extsd";
	public static final String USB_PATH = "/mnt/media_rw/udisk";
	private String[] STORAGE_ARRAY = new String[] { SDCARD_PATH, TF_PATH, USB_PATH };
	private ArrayList<MovieInfo> playList = new ArrayList<MovieInfo>();
	private final static int SCAN_NONE = 0;
	private final static int SCAN_OK = 1;
	private GridView mGridView;
	private float mDensity;
	private GridViewAdapter mAdapter;
	private BroadcastReceiver mMountReceiver;
	protected MediaPlayer mMediaPlayer;
	private ProgressBar mProgressBar;
	private TextView mMessage;
	private MemorySqlTool mSqlTool;
	private boolean mIsGridViewIdle = true;
	private MovideInfoLoader mLoader;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_OK:
				Log.d("fffff", "scan_ok...");
				initGridView();
				mAdapter.notifyDataSetChanged();
				break;
				
			case SCAN_NONE:
				Log.d("fffff", "scan_none...");
				mProgressBar.setVisibility(View.GONE);
				mMessage.setVisibility(View.VISIBLE);
				mAdapter.notifyDataSetChanged();
				break;
			}
		}

		private void initGridView() {
			//Log.d(tag, msg)
			ViewGroup.LayoutParams params = mGridView.getLayoutParams();
			int itemWidth = (int) (260 * mDensity);
			int spacingWidth = 0;
			int spacingHeight = 0;

			if(playList.size() > 0) {
				if(playList.size() == 1) {
					mGridView.setNumColumns(1);
					params.width = itemWidth;
				} else if(playList.size() == 2) {
					mGridView.setNumColumns(2);
					spacingWidth = (int) (50 * mDensity);
					params.width = itemWidth * 2 +  spacingWidth;
				} else {
					mGridView.setNumColumns(3);
					spacingWidth = (int) (50 * mDensity);
					spacingHeight = (int) (25 * mDensity);
					params.width = itemWidth * 3 + 2 * spacingWidth;
				}
			}
			mGridView.setStretchMode(GridView.NO_STRETCH);
			mGridView.setHorizontalSpacing(spacingWidth);
			mGridView.setVerticalSpacing(spacingHeight);
			mGridView.setColumnWidth(itemWidth);
			mGridView.setLayoutParams(params); 
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActionBar();
		setContentView(R.layout.activity_main);
		
		// read database, if has memory info then jump to playerActivity
		mSqlTool = new MemorySqlTool(this);
		MovieInfo info = mSqlTool.getInfo();
		if(info != null) {
			File targetFile = new File(info.path);
			if(!targetFile.exists()) {
				mSqlTool.delete();
			} else if(info.seekPos > 0) {
				Intent it = new Intent(this, PlayerActivity.class);
				it.putExtra("movie_info", info);
				startActivity(it);
			}
		}
		
		mLoader = MovideInfoLoader.build(this);
		
		// scan and load video files
		getVideoFiles();
		
	    DisplayMetrics outMetrics = new DisplayMetrics();  
	    getWindowManager().getDefaultDisplay().getMetrics(outMetrics);  
	    mDensity = outMetrics.density;
	    
	    // set adapter for gridview
		mGridView = (GridView) findViewById(R.id.gridview);
	    mAdapter = new GridViewAdapter(this, playList, mGridView);
		mGridView.setAdapter(mAdapter);
	    mGridView.setOnItemClickListener(this);
	    
		// add empty view
		View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_view,null);
		mProgressBar = (ProgressBar) emptyView.findViewById(R.id.progressbar);
		mMessage = (TextView) emptyView.findViewById(R.id.message);
		LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		((ViewGroup) mGridView.getParent()).addView(emptyView, emptyParams);
		mGridView.setEmptyView(emptyView);
		
		// register storage media mount/unmount broadcast receiver
		IntentFilter mountfilter = new IntentFilter();
		mountfilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		mountfilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		mountfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		mountfilter.addDataScheme("file");
		
		mMountReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					
					if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
						mProgressBar.setVisibility(View.VISIBLE);
						mMessage.setVisibility(View.GONE);
						return;
					}
					getVideoFiles();
				}
			};
		registerReceiver(mMountReceiver, mountfilter);
	}

	private void initActionBar() {
		// 返回箭头（默认不显示）
		getActionBar().setDisplayHomeAsUpEnabled(false);
		// 左侧图标点击事件使能
		getActionBar().setHomeButtonEnabled(true);
		// 使左上角图标(系统)是否显示
		getActionBar().setDisplayShowHomeEnabled(false);
		// 显示标题
		getActionBar().setDisplayShowTitleEnabled(false);
		// 显示自定义视图
		getActionBar().setDisplayShowCustomEnabled(true);
		View actionbarLayout = LayoutInflater.from(this).inflate(
				R.layout.actionbar_layout, null);
		TextView tvTitle = (TextView) actionbarLayout.findViewById(R.id.tv_actionbar_title);
		tvTitle.setText(R.string.app_name);
		getActionBar().setCustomView(actionbarLayout);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mProgressBar.setVisibility(View.VISIBLE);
		mMessage.setVisibility(View.GONE);
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mMountReceiver != null) {
			this.unregisterReceiver(mMountReceiver);
			mMountReceiver = null;
		}
	}
	
	/**
	 * 获取视频文件列表,并通知更新UI
	 */
//	private void getVideoFiles() {
//		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
//			return;
//		}
//		playList.clear(); 
//		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				mMediaPlayer = new MediaPlayer();
//				
//				Cursor cursor = getContentResolver().query(videoListUri,
//						new String[] { "_display_name", "_data" }, null, null,null);
//				if (cursor != null && cursor.getCount() > 0) {
//					while(cursor.moveToNext()) {
//						String displayName = cursor.getString(cursor.getColumnIndex("_display_name"));
//						int index = displayName.indexOf('.');
//						if(index == -1) { continue; }
//						String suffix = displayName.substring(index);
//						if (suffix.equalsIgnoreCase(".mp4") || suffix.equalsIgnoreCase(".3gp")
//								|| suffix.equalsIgnoreCase(".avi") || suffix.equalsIgnoreCase(".mov")
//								|| suffix.equalsIgnoreCase(".f4v") || suffix.equalsIgnoreCase(".m4a")
//								|| suffix.equalsIgnoreCase(".flv")) {
//							MovieInfo info = new MovieInfo();
//							info.displayName = displayName;
//							info.path = cursor.getString(cursor
//									.getColumnIndex("_data"));
//							info.thumbnail = getVideoThumbnail(info.path, 
//									260, 160, MediaStore.Images.Thumbnails.MINI_KIND);
//							
//							try {
//								// 获取播放时长
//								mMediaPlayer.setDataSource(info.path);
//								mMediaPlayer.prepare();
//								int i = mMediaPlayer.getDuration();
//								info.duration = i;
//								info.seekPos = 0;
//								mMediaPlayer.reset();
//								
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//							// remove small duration video
//							if(info.duration > 2000) {
//								playList.add(info);
//							}
//						}
//					}
//					mHandler.sendEmptyMessage(SCAN_OK);
//				} else {
//					mHandler.sendEmptyMessage(SCAN_NONE);
//				}
//				
//				cursor.close();
//				
//			}
//		}).start();
//	}
	
	public void getVideoFiles() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		playList.clear();
	
		new Thread(new Runnable() {

			@Override
			public void run() {
				for(String path : STORAGE_ARRAY) {
					if(checkDevicePluged(path)) {
						getVideoFile(new File(path));
					}
				} 
				if (playList.size() > 0) {
					mHandler.sendEmptyMessage(SCAN_OK);
					//发送扫描完成广播
					Intent intent = new Intent();
					intent.setAction("com.cngps.carvideo.scan_finish");
					intent.putParcelableArrayListExtra("playlist", playList);
					intent.addCategory(Intent.CATEGORY_DEFAULT);
					sendBroadcast(intent);
				} else {
					mHandler.sendEmptyMessage(SCAN_NONE);
				}
			}
		}).start();
	}
	
	private boolean checkDevicePluged(String path) {
		StatFs fs = new StatFs(path);
		long bcnt = fs.getBlockCountLong();
		if (bcnt == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public void getVideoFile(File file) {
		
		file.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				String name = file.getName();
				int i = name.indexOf('.');
				if (i != -1) {
					String suffix = name.substring(i);
					if (suffix.equalsIgnoreCase(".mp4") || suffix.equalsIgnoreCase(".3gp")
							|| suffix.equalsIgnoreCase(".avi") || suffix.equalsIgnoreCase(".mov")
							|| suffix.equalsIgnoreCase(".f4v") || suffix.equalsIgnoreCase(".m4a")
							|| suffix.equalsIgnoreCase(".flv")) {
						MovieInfo info = new MovieInfo();
						info.displayName = name;
						info.path = file.getAbsolutePath();
						info.thumbnail = null;
						info.seekPos = 0;
						info.duration = 0;
						playList.add(info);
						return true;
					}
				} else if (file.isDirectory() && !file.getName().equalsIgnoreCase("autonavidata")) {
					getVideoFile(file);
				}
				return false;
			}
		});
	}
	
	/**
	 * 整形时间格式成hh:MM:ss字符串
	 */
	public static String formatDuration(int duration) {
		duration /= 1000;
		int minute = duration / 60;
		int hour = minute / 60;
		int second = duration % 60;
		minute %= 60;
		return String.format("%02d:%02d:%02d", hour,minute, second);
	}
	
	/**
	 * 自定义Adapter
	 *
	 */
	public class GridViewAdapter extends BaseAdapter {

		public class ViewHolder {
			ImageView ivThumb;
			TextView tvDuration;
			TextView tvTitle;
		}
		
		private Context mContext;
		private List<MovieInfo> mList;
		private int mCount = 0;
		private Drawable mDefaultBitmapDrawable;
		 
		public GridViewAdapter(Context context, List<MovieInfo> movieList, 
				GridView gridView) {
			this.mContext = context;
			this.mList = movieList;
			this.mDefaultBitmapDrawable = context.getResources().getDrawable(R.drawable.thumbnail_default);
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d("ffff", "<getView> position = " + position + " count = " + parent.getChildCount() );
			//解决getView重复调用
			if (position == 0) {
				mCount++;
			} else {
				mCount = 0;
			}
			if (mCount > 1) {
				Log.v("ffff", "<getView> drop !!!");
				return convertView;
			}
			
			ViewHolder holder = null;
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.gridview_item, null);
				holder.ivThumb = (ImageView) convertView.findViewById(R.id.iv_thumb);
				holder.tvDuration = (TextView) convertView.findViewById(R.id.tv_duration);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//解决getView重复调用
			if(parent.getChildCount() == position) {
				MovieInfo info = mList.get(position);
				holder.tvTitle.setText(info.displayName);
				
				TextView textView = holder.tvDuration;
				ImageView imageView = holder.ivThumb;
				String tag = (String) textView.getTag();
				if (!info.path.equals(tag)) {
					textView.setText(formatDuration(info.duration));
					holder.ivThumb.setImageDrawable(mDefaultBitmapDrawable);
				}
	            if (mIsGridViewIdle) {
	                textView.setTag(info.path);
	                imageView.setTag(info.path);
	                mLoader.bindDuration(info.path, textView);
	                mLoader.bindThumbnail(info.path, imageView);
	            }
			}
			
			return convertView;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent it = new Intent(this, PlayerActivity.class);
		it.putParcelableArrayListExtra("playlist", playList);
		it.putExtra("position", position);
		startActivity(it);
	}

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            mIsGridViewIdle = true;
            mAdapter.notifyDataSetChanged();
        } else {
            mIsGridViewIdle = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        // ignored
    }

}
