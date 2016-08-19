package com.android.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import android.content.Context;

public class Parameter {
	Context context;

	public Parameter(Context context) {
		this.context = context;
	}

	private InputStream is;/* 文件输入流，读取文件流 */
	private String Text_of_output = "";/* 字符串，从文件中读取到得字符串 */

	private OutputStream oss;/* 文件输出流，保存文件流 */

	public void setParameter(String fileName, long value) {
		oss = null;
		try {
			oss = context.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);
			PrintStream ps = new PrintStream(oss);
			ps.print(value);
		} catch (FileNotFoundException e) {

		} finally {
			try {
				oss.close();
			} catch (IOException e) {
			}
		}
	}
	
	public String getParameter(String fileName) {
		/* 初次使用时 */
		oss = null;
		try {
			oss = context.openFileOutput(fileName, Context.MODE_APPEND);
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		} finally {
			try {
				oss.close();
			} catch (IOException e) {
			}
		}
		is = null;
		try {
			/* 打开文件输入流 */
			is = context.openFileInput(fileName);
			/* 获取流中可读取的数据大小,初始化字节数组 */
			byte[] readBytes =  new byte[is.available()];
			/* 从文件输入流中读取内容到字节数组中，返回内容长度 */
			int length = is.read(readBytes);
			/* 把字节数组转换成字符串 */
			Text_of_output = new String(readBytes);

			return Text_of_output;
		} catch (FileNotFoundException e) {
			/* 文件未找到，异常 */
			return null;

		} catch (IOException e) {
			/* 文件读取错误，异常 */
			return null;
		} finally {
			try {
				/* 关闭文件输入流 */
				is.close();
			} catch (IOException e) {
				return null;
			}
		}
	}
}
