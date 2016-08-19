package com.android.settings.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownloadHttpTool {
	/**
	 * 
	 * 利用Http协议进行多线程下载具体实现类
	 */

	private static final String TAG = DownloadHttpTool.class.getSimpleName();

	private int threadCount;//线程数量
	private String urlstr;//URL地址
	private Context mContext;
	private Handler mHandler;
	private List<DownloadInfo> downloadInfos;//保存下载信息的类

	private String localPath;//目录
	private String fileName;//文件名
	private int fileSize;
	private DownloadSqlTool sqlTool;//文件信息保存的数据库操作类

	private enum Download_State {
		Downloading, Pause, Ready;//利用枚举表示下载的三种状态
	}

	private Download_State state = Download_State.Ready;//当前下载状态

	private int globalCompelete = 0;//所有线程下载的总数

	public DownloadHttpTool(int threadCount, String urlString,
			String localPath, String fileName, Context context, Handler handler) {
		super();
		this.threadCount = threadCount;
		this.urlstr = urlString;
		this.localPath = localPath;
		this.mContext = context;
		this.mHandler = handler;
		this.fileName = fileName;
		sqlTool = new DownloadSqlTool(mContext);
	}

	//在开始下载之前需要调用ready方法进行配置
	public void ready() {
		Log.w(TAG, "ready");
		globalCompelete = 0;
		//查询数据表获得下载信息：List<DownloadInfo>
		downloadInfos = sqlTool.getInfos(urlstr);
		//无信息，第一次下载
		if (downloadInfos.size() == 0) {
			initFirst();
		} else {
			File file = new File(localPath + "/" + fileName);
			if (!file.exists()) { // 下载的文件已被删除，同时删除数据库记录
				sqlTool.delete(urlstr);
				initFirst();
			} else {
				fileSize = downloadInfos.get(downloadInfos.size() - 1)
						.getEndPos();
				for (DownloadInfo info : downloadInfos) {
					globalCompelete += info.getCompeleteSize();
				}
				Log.w(TAG, "globalCompelete:::" + globalCompelete);
			}
		}
	}

	public void start() {
		Log.w(TAG, "start");
		if (downloadInfos != null) {
			if (state == Download_State.Downloading) {
				return;
			}
			state = Download_State.Downloading;
			for (DownloadInfo info : downloadInfos) {
				Log.v(TAG, "startThread");
				new DownloadThread(info.getThreadId(), info.getStartPos(),
						info.getEndPos(), info.getCompeleteSize(),
						info.getUrl()).start();
			}
		}
	}

	public void pause() {
		state = Download_State.Pause;
		sqlTool.closeDb();
	}
	
	public void delete(){
		compelete();
		File file = new File(localPath + "/" + fileName);
		file.delete();
	}
	
	public void compelete() {
		state = Download_State.Ready;
		sqlTool.delete(urlstr);
		sqlTool.closeDb();
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getCompeleteSize() {
		return globalCompelete;
	}

	//第一次下载初始化
	private void initFirst() {
		Log.w(TAG, "initFirst");
		try {
			URL url = new URL(urlstr);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			fileSize = connection.getContentLength(); //得到要下载的文件大小
			Log.w(TAG, "fileSize::" + fileSize);
			//创建本地文件
			File fileParent = new File(localPath);
			if (!fileParent.exists()) {
				fileParent.mkdir();
			}
			File file = new File(fileParent, fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			// 本地访问文件
			RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			accessFile.setLength(fileSize);
			accessFile.close();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//根据下载总线程数来划分文件的下载区域
		int range = fileSize / threadCount;
		downloadInfos = new ArrayList<DownloadInfo>();
		//每一段的位置是i * range到(i + 1) * rang  - 1
		for (int i = 0; i < threadCount - 1; i++) {
			//threadId, startPos, endPos, compeleteSize,  url
			DownloadInfo info = new DownloadInfo(i, i * range, (i + 1) * range
					- 1, 0, urlstr);
			downloadInfos.add(info);
		}
		//最后一段的位置是到filesize - 1
		DownloadInfo info = new DownloadInfo(threadCount - 1, (threadCount - 1)
				* range, fileSize - 1, 0, urlstr);
		downloadInfos.add(info);
		// 下载线程的下载信息插入数据库
		sqlTool.insertInfos(downloadInfos);
	}

	//自定义下载线程
	private class DownloadThread extends Thread {

		private int threadId;
		private int startPos;
		private int endPos;
		private int compeleteSize;
		private String urlstr;
		private int totalThreadSize;

		public DownloadThread(int threadId, int startPos, int endPos,
				int compeleteSize, String urlstr) {
			this.threadId = threadId;
			this.startPos = startPos;
			this.endPos = endPos;
			totalThreadSize = endPos - startPos + 1;
			this.urlstr = urlstr;
			this.compeleteSize = compeleteSize;
		}

		@Override
		public void run() {
			HttpURLConnection connection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream is = null;
			try {
				randomAccessFile = new RandomAccessFile(localPath + "/"
						+ fileName, "rwd");
				//定位的文件的指定位置
				randomAccessFile.seek(startPos + compeleteSize);
				URL url = new URL(urlstr);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				//通过Http协议的Range字段实现下载文件的分段
				connection.setRequestProperty("Range", "bytes="
						+ (startPos + compeleteSize) + "-" + endPos);
				is = connection.getInputStream();
				byte[] buffer = new byte[1024];
				int length = -1;
				while ((length = is.read(buffer)) != -1) {
					randomAccessFile.write(buffer, 0, length);
					compeleteSize += length;
					Message message = Message.obtain();
					message.what = threadId;
					message.obj = urlstr;
					message.arg1 = length;
					mHandler.sendMessage(message); //更新界面下载进度
					sqlTool.updataInfos(threadId, compeleteSize, urlstr); //更新数据库下载线程下载信息
					Log.w(TAG, "Threadid::" + threadId + "    compelete::"
							+ compeleteSize + "    total::" + totalThreadSize);
					if (compeleteSize >= totalThreadSize) {
						break;
					}
					if (state != Download_State.Downloading) {//state不为downloading状态，跳出下载
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (is != null) {
						is.close();
					}
					randomAccessFile.close();
					connection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}