package com.broada.carrier.monitor.impl.host.cli.directory;

import java.io.Serializable;

import com.broada.carrier.monitor.common.util.SerializeUtil;

public class CLIDirectoryParameter implements Serializable {
	private static final long serialVersionUID = -1050077364856020347L;
	private CLIDirectory[] directories;

	public CLIDirectoryParameter() {
		directories = new CLIDirectory[0];
	}

	public CLIDirectoryParameter(String str) {
		directories = SerializeUtil.decodeJson(str, CLIDirectory[].class);
		if (directories == null)
			directories = new CLIDirectory[0];
	}
	
	public String encode() {
		return SerializeUtil.encodeJson(directories);
	}

	public CLIDirectory[] getDirectories() {
		return directories;
	}

	public void setDirectories(CLIDirectory[] directories) {
		this.directories = directories;
	}

}
