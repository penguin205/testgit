package com.cngps.carvideo;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 加载MovieInfo里的duration和thumbnail
 * @author Yongjun.Xiao
 *
 */
public class MovideInfoLoader {
	
	public static final int MESSAGE_POST_RESULT_DURATION = 1;
	public static final int MESSAGE_POST_RESULT_THUMNBNAIL = 2;
	private static final int TAG_KEY_PATH = R.id.durationloader_path;
	private Executor THREAD_POOL_EXECUTOR;
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            String path = null;
            switch (msg.what) {
			case MESSAGE_POST_RESULT_DURATION:
				TextView textView = result.textView;
				path = (String) textView.getTag(TAG_KEY_PATH);
				if (path.equals(result.path)) {
					textView.setText(MainActivity.formatDuration(result.duration));
				} else {
					Log.w("ffff","set textview duration,but path has changed, ignored!");
				}
				break;
				
			case MESSAGE_POST_RESULT_THUMNBNAIL:
				ImageView imageView = result.imageView;
				path = (String) imageView.getTag(TAG_KEY_PATH);
				if (path.equals(result.path)) {
					imageView.setImageBitmap(result.bitmap);
				} else {
					Log.w("ffff","set image bitmap,but url has changed, ignored!");
				}
				break;
			}
        };
    };
    
	public MovideInfoLoader(Context context) {
		THREAD_POOL_EXECUTOR = Executors.newCachedThreadPool();
	}
	
	public static MovideInfoLoader build(Context context) {
		return new MovideInfoLoader(context);
	}

	public void bindDuration(final String path, final TextView textView) {
		textView.setTag(TAG_KEY_PATH, path);
		
		Runnable loadVideoDurationTask = new Runnable() {

			@Override
			public void run() {
				// 异步加载播放时长
				final MediaPlayer player = new MediaPlayer();
				try {
					player.setDataSource(path);
					player.prepare();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				player.setOnPreparedListener(new OnPreparedListener() {

					@Override
					public void onPrepared(MediaPlayer mp) {
						int duration = player.getDuration();
						LoaderResult result = new LoaderResult(textView, path,
								duration);
						mMainHandler.obtainMessage(MESSAGE_POST_RESULT_DURATION, result)
								.sendToTarget();
					}
				});
			}
		};
		THREAD_POOL_EXECUTOR.execute(loadVideoDurationTask);
	}
	
	public void bindThumbnail(final String path, final ImageView imageView) {
		imageView.setTag(TAG_KEY_PATH, path);
		Runnable loadVideoThumbnailTask = new Runnable() {
			
			@Override
			public void run() {
				Bitmap bitmap = getVideoThumbnail(path, 
						260, 160, MediaStore.Images.Thumbnails.MINI_KIND);
				LoaderResult result = new LoaderResult(imageView, path,
						bitmap);
				mMainHandler.obtainMessage(MESSAGE_POST_RESULT_THUMNBNAIL, result)
						.sendToTarget();
			}
		};
		
		THREAD_POOL_EXECUTOR.execute(loadVideoThumbnailTask);
	}
	
	/**
	 * 获取视频的缩略图
	 */
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
			int kind) {
		Bitmap bitmap = null;
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	
    private static class LoaderResult {
        public TextView textView;
        public ImageView imageView;
		public String path;
        public int duration;
        public Bitmap bitmap;
        
        public LoaderResult(TextView textView, String path, int duration) {
			this.textView = textView;
			this.path = path;
			this.duration = duration;
		}
        
        public LoaderResult(ImageView imageView, String path, Bitmap bitmap) {
        	this.imageView = imageView;
        	this.path = path;
        	this.bitmap = bitmap;
        }
    }
}
