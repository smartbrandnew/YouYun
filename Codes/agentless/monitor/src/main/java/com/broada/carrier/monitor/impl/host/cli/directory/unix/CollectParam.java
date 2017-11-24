package com.broada.carrier.monitor.impl.host.cli.directory.unix;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.host.cli.directory.CLIDirectory;

public class CollectParam implements Serializable {
	private static final long serialVersionUID = 1L;

	private CLIDirectory[] dirs;
	private boolean isFile;

	public CollectParam(CLIDirectory[] dirs, boolean isFile) {
		this.dirs = dirs;
		this.isFile = isFile;
	}

	public CollectParam() {
	}

	public CLIDirectory[] getDirs() {
		return dirs;
	}

	public void setDirs(CLIDirectory[] dirs) {
		this.dirs = dirs;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

}
