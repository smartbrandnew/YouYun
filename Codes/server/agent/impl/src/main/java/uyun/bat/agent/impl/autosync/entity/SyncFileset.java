package uyun.bat.agent.impl.autosync.entity;


import uyun.whale.common.util.text.DateUtil;

import java.util.Date;

/**
 * 自动同步文件集合，描述一个SyncClient在指定时间的所有文件版本
 * @author Jiangjw
 */
public class SyncFileset {
	private String version;
	private SyncFile[] files;

	public SyncFileset() {
	}

	public SyncFileset(SyncFile[] files) {
		this(DateUtil.format(new Date(), "yyMMdd.HHmmss"), files);
	}

	public SyncFileset(String version, SyncFile[] files) {
		super();
		this.version = version;
		this.files = files;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setFiles(SyncFile[] files) {
		this.files = files;
	}

	public SyncFile[] getFiles() {
		return files;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("版本：").append(version).append("\n");
		sb.append("文件个数：").append(files.length).append("\n");
		for (int i = 0; i < files.length; i++)
			sb.append(i).append(". ").append(files[i]).append("\n");
		return sb.toString();
	}

	public SyncFile getFile(String filename) {
		for (SyncFile file : files) {
			if (file.getName().equals(filename))
				return file;
		}
		return null;
	}
}
