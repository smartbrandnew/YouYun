package com.broada.carrier.monitor.common.remoteio.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.broada.carrier.monitor.common.remoteio.api.RemoteIOMode;
import com.broada.component.utils.text.DateUtil;

/**
 * 打开文件实体类
 * @author Jiangjw
 */
class OpenedFile {
	/**
	 * 文件打开超时阈值，单位ms，默认为5m
	 */
	public static final long OVERTIME_MS = 5 * 60 * 1000;
	private static AtomicInteger nextId = new AtomicInteger(1);
	private int id;
	private File file;
	private RemoteIOMode mode;
	private FileInputStream is;
	private FileOutputStream os;
	private long openTime;
	private long lastAccessTime;

	public OpenedFile(File file, RemoteIOMode mode) {
		try {
			switch (mode) {
			case READ:
				is = new FileInputStream(file);
				break;
			case WRITE:
				if (file.getParentFile() != null)
					file.getParentFile().mkdirs();
				os = new FileOutputStream(file);
				break;
			default:
				throw new IllegalArgumentException();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("文件不存在：" + file, e);
		}
		this.id = nextId.getAndIncrement();
		this.file = file;
		this.mode = mode;
		this.openTime = this.lastAccessTime = System.currentTimeMillis();
	}

	/**
	 * 获取操作模式
	 */
	public RemoteIOMode getMode() {
		return mode;
	}

	/**
	 * 获取文件ID
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * 获取文件
	 * @return
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * 获取最后访问时间
	 * @return
	 */
	public long getLastAccessTime() {
		return lastAccessTime;
	}

	/**
	 * 获取文件打开时间
	 * @return
	 */
	public long getOpenTime() {
		return openTime;
	}

	/**
	 * <pre>
	 * 判断文件是否时间已经超时
	 * 即一个文件在客户端请求打开后，超过指定的时间没有操作
	 * @return
	 */
	public boolean isOvertime() {
		return System.currentTimeMillis() - lastAccessTime > OVERTIME_MS;
	}

	/**
	 * 更新最后访问时间
	 */
	public void updateLastAccessTime() {
		lastAccessTime = System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return String.format("%s[%d %s mode: %s open: %s last: %s]", getClass().getSimpleName(), id, file, mode,
				DateUtil.format(new Date(openTime)), DateUtil.format(new Date(lastAccessTime)));
	}

	/**
	 * 关闭文件
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (is != null) {
			is.close();
			is = null;
		}
		if (os != null) {
			os.close();
			os = null;
		}
	}

	/**
	 * 获取文件输入流
	 * @return
	 */
	public FileInputStream getInputStream() {
		return is;
	}
	
	/**
	 * 获取文件输出流
	 * @return
	 */
	public FileOutputStream getOutputStream() {
		return os;
	}
}