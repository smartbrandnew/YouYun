package com.broada.carrier.monitor.common.remoteio.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.common.remoteio.api.RemoteFile;
import com.broada.carrier.monitor.common.remoteio.api.RemoteIO;
import com.broada.carrier.monitor.common.remoteio.api.RemoteIOMode;
import com.broada.carrier.monitor.common.util.WorkPathUtil;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.ThreadUtil;

/**
 * 远程IO实现类
 */
public class RemoteIOImpl implements RemoteIO {
	private static final Log logger = LogFactory.getLog(RemoteIOImpl.class);
	private static final long CHECK_INTERVAL = 0;	
	private ConcurrentMap<Integer, OpenedFile> openedFiles = new ConcurrentHashMap<Integer, OpenedFile>();
	private Thread checkerThread;

	public RemoteIOImpl() {
		startup();
	}

	/**
	 * 启动远程IO服务
	 */
	public void startup() {
		if (isRunning())
			return;

		openedFiles.clear();
		checkerThread = ThreadUtil.createThread(new RemoteIOChecker());
	}

	/**
	 * 关闭远程IO服务
	 */
	public void shutdown() {
		if (checkerThread != null) {
			checkerThread.interrupt();
			checkerThread = null;
		}
	}

	public RemoteFile[] list(String dir) {		
		if (dir == null || dir.length() == 0)
			dir = ".";
		File dirFile = WorkPathUtil.getFile(dir);
		if (!dirFile.exists())
			return null;
		
		Collection<?> files = FileUtils.listFiles(dirFile, null, true);
		if (files == null || files.isEmpty())
			return null;
		
		List<RemoteFile> result = new ArrayList<RemoteFile>(files.size());
		for (Object obj : files) {
			File file = (File) obj;						
			result.add(new RemoteFile(file));
		}
		return result.toArray(new RemoteFile[result.size()]);
	}

	public int open(String filepath, RemoteIOMode mode) {
		File file = WorkPathUtil.getFile(filepath);
		OpenedFile openedFile = new OpenedFile(file, mode);
		openedFiles.put(openedFile.getId(), openedFile);
		if (logger.isDebugEnabled())
			logger.debug("文件打开：" + openedFile);
		return openedFile.getId();
	}

	public void write(int fileId, byte[] data, int offset, int length) {
		OpenedFile file = checkFile(fileId, RemoteIOMode.WRITE);
		
		try {
			file.getOutputStream().write(data, offset, length);
		} catch (IOException e) {
			throw processError("文件写入失败", file, e);
		}
	}

	public void close(int fileId) {
		OpenedFile file = openedFiles.remove(fileId);
		if (file == null)
			return;
				
		try {
			file.close();
			if (logger.isDebugEnabled())
				logger.debug("文件关闭成功：" + file);
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "文件关闭失败：" + file, e);
		}
	}

	public byte[] read(int fileId, int len) {		
		if (len > READ_BLOCK_SIZE_MAX)
			throw new IllegalArgumentException(String.format("一次读取的块大小太大[最大：%s 当前：%s]", READ_BLOCK_SIZE_MAX, len));
		
		OpenedFile file = checkFile(fileId, RemoteIOMode.READ);

		try {
			int avail = file.getInputStream().available();
			if (avail <= 0)
				return null;
			
			byte[] buf = new byte[Math.min(avail, len)];
			len = file.getInputStream().read(buf);
			if (len <= 0)
				return null;
			else if (len != buf.length) 
				return Arrays.copyOf(buf, len);
			else 
				return buf;
		} catch (Throwable e) {			
			throw processError("文件读取失败", file, e);
		}
	}

	private RuntimeException processError(String msg, OpenedFile file, Throwable error) {
		msg = ErrorUtil.createMessage(msg + "：" + file, error);
		logger.warn(msg);
		logger.debug("堆栈：", error);			
		return new RuntimeException(msg, error);
	}

	private OpenedFile checkFile(int fileId, RemoteIOMode mode) {
		OpenedFile file = openedFiles.get(fileId);
		if (file == null)
			throw new IllegalArgumentException("指定的文件ID不存在：" + fileId);
		if (file.getMode() != mode)
			throw new IllegalArgumentException("指定的文件无法完成对应的操作：" + mode);
		file.updateLastAccessTime();
		return file;
	}

	public boolean delete(String filepath) {
		File file = WorkPathUtil.getFile(filepath);
		if (!file.exists())
			return false;
		if (file.delete()) {
			if (logger.isDebugEnabled())
				logger.debug("文件删除：" + filepath);
			return true;
		} else
			throw new RuntimeException("文件正在使用中无法删除：" + filepath);
	}

	private class RemoteIOChecker implements Runnable {
		public void run() {
			while (isRunning()) {
				try {
					Thread.sleep(CHECK_INTERVAL);
				} catch (InterruptedException e) {
					break;
				}

				try {
					for (OpenedFile file : openedFiles.values()) {
						if (file.isOvertime()) {
							logger.warn("远程文件访问超时，将主动关闭：" + file);
							close(file.getId());
						}
					}
				} catch (Throwable e) {
					ErrorUtil.warn(logger, "检查文件关闭情况失败", e);
				}
			}
		}
	}

	private boolean isRunning() {
		return checkerThread != null;
	}

	@Override
	public RemoteFile get(String file) {
		return new RemoteFile(new File(file));
	}

	@Override
	public boolean setLastModified(String filepath, long lastModified) {
		File file = WorkPathUtil.getFile(filepath);
		if (file.exists())
			return file.setLastModified(lastModified);
		else
			return false;
	}
}
