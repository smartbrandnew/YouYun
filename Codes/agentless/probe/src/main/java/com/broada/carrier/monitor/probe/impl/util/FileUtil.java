package com.broada.carrier.monitor.probe.impl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {
	/**
	 * 获取文件内容
	 * @return
	 */
	public static String getFileContent(File file){
		FileReader fReader = null;
		BufferedReader bufferedReader = null;
		try {
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
			return content;
		} catch (Throwable e) {
			throw new RuntimeException("获取文件内容失败：" + file, e);
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
}