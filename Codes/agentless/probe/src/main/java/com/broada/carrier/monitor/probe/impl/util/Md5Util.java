package com.broada.carrier.monitor.probe.impl.util;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;

import com.broada.carrier.monitor.probe.impl.sync.entity.FileCharSet;


public class Md5Util {
	private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	//修改使用reader进行读取
	public static String digest(File file) {
		FileReader fReader = null;
		BufferedReader bufferedReader = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			fReader = new FileReader(file);
			bufferedReader = new BufferedReader(fReader);
			StringBuilder sb = new StringBuilder();
			String line = bufferedReader.readLine();
			while (line!=null) {
				sb.append(line);
				sb.append("\n");
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			fReader.close();
			String content = sb.substring(0, sb.lastIndexOf("\n"));
			byte[] bytes = content.getBytes(FileCharSet.DEFAULT_CHARSET);
			md.update(bytes);
			byte[] b = md.digest();
			return byteToHexString(b);
		} catch (Throwable e) {
			throw new RuntimeException("对文件进行MD5操作失败：" + file, e);
		} finally {
			try {
				if (bufferedReader != null)
					bufferedReader.close();
				if (fReader != null)
					fReader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static String byteToHexString(byte[] tmp) {
		String s;
		char str[] = new char[16 * 2];
		int k = 0;
		for (int i = 0; i < 16; i++) {
			byte byte0 = tmp[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		s = new String(str);
		return s;
	}


	public static String digest(String content) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = content.getBytes(FileCharSet.DEFAULT_CHARSET);
			md.update(bytes);
			byte[] b = md.digest();
			return byteToHexString(b);
		} catch (Throwable e) {
			throw new RuntimeException("字符串转换md5异常: ", e);
		}
	}
	
}
