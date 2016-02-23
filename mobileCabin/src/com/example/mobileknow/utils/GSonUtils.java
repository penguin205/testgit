package com.example.mobileknow.utils;

import com.google.gson.Gson;

public class GSonUtils {

	public static <T> T jsonToBean(String jsonResult,Class<T> clz){
		Gson gson = new Gson();
		T t = gson.fromJson(jsonResult, clz);
		 return t;
	}

}
