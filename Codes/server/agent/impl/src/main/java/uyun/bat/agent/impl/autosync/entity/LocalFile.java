package uyun.bat.agent.impl.autosync.entity;

import uyun.bat.agent.impl.autosync.common.Md5Util;

import java.io.File;

public class LocalFile extends SyncFileV2 {
	private File file;

	public LocalFile(String fileset, String name, File file) {
		super(fileset, name, file.lastModified(), file.length(), null);
		this.file = file;
	}

	public File getFile() {
		return file;
	}
	
	@Override
	public String getMd5() {
		String value = super.getMd5();
		if (value == null) {
			value = Md5Util.digest(file);
			setMd5(value);
		}
		return value;
	}

	@Override
	public String toString() {
		return String.format("%s[name: %s size: %d lastMod: %s file: %s]", getClass().getSimpleName(), getName(), getSize(), getLastModified(), getFile());
	}
}
