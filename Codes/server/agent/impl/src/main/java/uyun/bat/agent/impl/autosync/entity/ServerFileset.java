package uyun.bat.agent.impl.autosync.entity;


import uyun.whale.common.util.io.FileUtil;

import java.io.File;

public class ServerFileset extends Fileset {
	private String client;

	public ServerFileset() {
		super();
	}
	
	public ServerFileset(String id, String server, String client, FileOperMode deleteModel, FileMatcher[] includes, FileMatcher[] excludes) {
		super(id, server, deleteModel, includes, excludes);
		this.client = client;
	}
	
	public String getServer() {
		return getDir();
	}

	public void setServer(String server) {
		setDir(server);
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		if (client == null)
			this.client = null;
		else
			this.client = FileUtil.toLinuxPath(client);
	}
	
	protected LocalFile createLocalFile(String relativePath , File file) {
		return new LocalFile(getId(), joinPath(client, relativePath), file);
	}
	
	private static String joinPath(String path1, String path2) {
		if (path1 == null || path1.length() == 0)
			return path2;
		boolean flag1 = path1.endsWith("/");
		boolean flag2 = path2.startsWith("/");
		if (flag1 && flag2)
			return path1 + path2.substring(1);
		else if (flag1 || flag2)
			return path1 + path2;
		else
			return path1 + "/" + path2;
	}

	@Override
	public String toString() {
		return String.format("%s[dir: %s client: %s includes: %s excludes: %s]", getClass().getSimpleName(), getDir(),
				getClient(), getIncludes(), getExcludes());
	}
}
