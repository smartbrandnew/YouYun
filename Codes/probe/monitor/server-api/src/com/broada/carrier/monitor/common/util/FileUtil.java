package com.broada.carrier.monitor.common.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.component.utils.error.ErrorUtil;

public class FileUtil {
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	/**
	 * 从一个指定的输入流中读取内容，以字符串返回
	 * @param is
	 * @param encoding
	 * @return
	 */
	public static String readString(InputStream is, String encoding) {
		byte[] data = read(is, null);
		try {
			return new String(data, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("不存在的字符集：" + encoding, e);
		}
	}
	
	/**
	 * 从一个指定的输入流中读取文件内容，以字节返回
	 * @param is
	 * @param filename 文件名，可以为null，主要用于错误消息反馈
	 * @return
	 */
	public static byte[] read(InputStream is, String filename) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] data = new byte[5 * 1024];
			while (true) {
				int len = is.read(data);
				if (len < 0)
					break;
				else if (len == 0)
					continue;
				os.write(data, 0, len);
			}			
			return os.toByteArray();
		} catch (Throwable e) {	
			throw ErrorUtil.createRuntimeException(createMessage("读取文件失败", filename), e);
		}
	}
	
	private static String createMessage(String message, File file) {
		return createMessage(message, file == null ? null : file.getPath());
	}
	
	private static String createMessage(String message, String filename) {
		if (filename != null)
			message += "：" + filename;
		return message;
	}

	/**
	 * 从一个指定的文件中读取文件内容，以字节返回
	 * @param file
	 * @return
	 */
	public static byte[] read(File file) {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			return read(is, file.getPath());
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException(createMessage("读取文件失败", file), e);
		} finally {
			close(file, is);
		}
	}
	
	/**
	 * 从一个指定的文件中读取文件内容，并使用JDK反序列化为对象返回
	 * @param file
	 * @return
	 */
	public static Object readObject(File file) {
		return SerializeUtil.decodeBytes(read(file));
	}
	
	private static void close(File file, Closeable handle) {
		if (handle == null)
			return;
		
		try {
			if (handle instanceof Closeable) 
				((Closeable) handle).close();			
		} catch (Throwable e) {
			ErrorUtil.warn(logger, createMessage("关闭文件失败", file), e);
		}
	}
	
	/**
	 * 将字节数组写入到指定的文件中
	 * @param file
	 * @param data
	 */
	public static void write(File file, byte[] data) {		
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(file);
			os.write(data);
			os.close();
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("写入文件失败：" + file, e);
		} finally {
			close(file, os);
		}
	}
	
	/**
	 * 将对象使用JDK序列化并写入到指定的文件中
	 * @param file
	 * @param obj
	 */
	public static void writeObject(File file, Object obj) {		
		write(file, SerializeUtil.encodeBytes(obj));		
	}

	/**
	 * 从一个文件中读取字符串
	 * @param file
	 * @param encoding
	 * @return
	 */
	public static String readString(File file, String encoding) {
		byte[] data = read(file);
		try {
			return new String(data, encoding);
		} catch (UnsupportedEncodingException e) {
			throw ErrorUtil.createIllegalArgumentException(encoding, e);
		}
	}
	
	/**
	 * 将一个字符串写入到一个文件中
	 * @param file
	 * @param text
	 * @param encoding
	 * @return 返回写入长度
	 */
	public static int writeString(File file, String text, String encoding) {
		byte[] data;
		try {
			data = text.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			throw ErrorUtil.createIllegalArgumentException(encoding, e);
		}		
		write(file, data);
		return data.length;
	}
}
