package com.example.mobileknow.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.mobileknow.entity.ChatMessage;
import com.example.mobileknow.entity.ChatMessage.Type;
import com.example.mobileknow.entity.Foods;
import com.example.mobileknow.entity.Links;
import com.example.mobileknow.entity.News;
import com.example.mobileknow.entity.Result;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;


public class HttpUtils {
	private static final String URL = "http://www.tuling123.com/openapi/api";
	private static final String KEY = "1d85fb81f15ec97c940ba8bac9eeb825";
	private static final String USERID = "2403230984 ";
	private static String TAG = "HttpUtils";
	
	public static String doGet(String msg) {
		String result = null;
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		String urlStr = setParams(msg);
		try {
			java.net.URL urlNet = new java.net.URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) urlNet.openConnection();
			conn.setReadTimeout(3 * 1000);
			conn.setConnectTimeout(3 * 1000);
			conn.setRequestMethod("GET");
			
			is = conn.getInputStream();
			byte[] buf = new byte[128];
			int len = -1;
			baos = new ByteArrayOutputStream();
			while((len = is.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			result = new String(baos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(is != null) {
					is.close();
				} 
				if(baos != null) {
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static Bitmap downloadBitmap(String imageUrl) {
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(imageUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			bitmap = BitmapFactory.decodeStream(conn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(conn != null) {
				conn.disconnect();
			}
		}
		return bitmap;
	}
	
	private static String setParams(String msg) {
		String url = "";
		try {
			url = URL + "?key=" + KEY + "&info=" + 
					URLEncoder.encode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
		return url;
	}

	public static ChatMessage sendMessage(String msg) {
		String jsonResult = doGet(msg);
		ChatMessage fromMessage = new ChatMessage();
		fromMessage.setDate(new Date());
		fromMessage.setType(Type.INCOMING);
		if(jsonResult == null) {
			fromMessage.setContent("呜呜呜… 主人，网络异常！");
			return fromMessage;
		};
		
		Gson gson = new Gson();
		try {
			JSONObject object = new JSONObject(jsonResult);
			int code = object.getInt("code");
			switch (code) {
			case 100000:
				Result result = GSonUtils.jsonToBean(jsonResult, Result.class);
				fromMessage.setContent(result.getText());
				break;
			case 200000:
				Links links = GSonUtils.jsonToBean(jsonResult, Links.class);
				fromMessage.setContent(links.getText());
				fromMessage.setDetail(links.getUrl());
				break;
			case 308000:
				Foods foods =  GSonUtils.jsonToBean(jsonResult, Foods.class);
				int index = getRandom(foods.getList().size());
				fromMessage.setContent(foods.getText() + "\n\n" + foods.getList().get(index).getName());
				fromMessage.setDetail(foods.getList().get(index).getInfo() + " " + 
						foods.getList().get(index).getDetailurl());
				fromMessage.setImageUrl(foods.getList().get(index).getIcon());
				break;
			case 302000:
				News news = GSonUtils.jsonToBean(jsonResult, News.class);
				int index2 = getRandom(news.getList().size());
				fromMessage.setContent(news.getText());
				fromMessage.setDetail( news.getList().get(index2).getArticle() + " " + 
						news.getList().get(index2).getDetailUrl());
				break;
			default:
				break;
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		
		return fromMessage;
	}

	private static int getRandom(int size) {
		Random r = new Random();
		return r.nextInt(size);
	}
}
