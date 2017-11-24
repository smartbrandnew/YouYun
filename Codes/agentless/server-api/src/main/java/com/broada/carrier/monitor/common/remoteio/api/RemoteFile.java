package com.broada.carrier.monitor.common.remoteio.api;

import java.io.File;
import java.io.Serializable;

import com.broada.carrier.monitor.common.util.WorkPathUtil;

/**
 * 远程文件实体类
 * @author Jiangjw
 */
public class RemoteFile implements Serializable{
	private static final long serialVersionUID = 1L;
	private String file;
	private long size;
	private long lastModified;
	
	public RemoteFile() {		
	}

	public RemoteFile(File file) {
		this.file = WorkPathUtil.getRelativePath(file);
		this.size = file.length();
		this.lastModified = file.lastModified();
	}

	public RemoteFile(RemoteFile copy) {
		this.file = copy.file;
		this.size = copy.size;
		this.lastModified = copy.lastModified;
	}

	/**
	 * 获取文件路径
	 * @return
	 */
	public String getFile() {
		return file;
	}

	/**
	 * 获取文件大小
	 * @return
	 */
	public long getSize() {
		return size;
	}

	public long getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return String.format("%s[%s %d]", getClass().getSimpleName(), file, size);
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	/**
	 * 判断文件大小与修改时间是否相同
	 * @param size
	 * @param lastModified
	 * @return
	 */
	public boolean equals(long size, long lastModified) {
		return this.size == size && this.lastModified == lastModified;
	}
}
