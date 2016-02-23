package com.example.mobileknow.utils;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;

@SuppressLint("NewApi")
public class ImageCache {

	private LruCache<String, BitmapDrawable> mMemoryCache;

	public ImageCache() {
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
			@Override
			protected int sizeOf(String key, BitmapDrawable drawable) {
				return drawable.getBitmap().getByteCount();
			}

		};
	}
	public BitmapDrawable getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}

	public void addBitmapToMemoryCache(String key, BitmapDrawable drawable) {
		mMemoryCache.put(key, drawable);
	}
}
