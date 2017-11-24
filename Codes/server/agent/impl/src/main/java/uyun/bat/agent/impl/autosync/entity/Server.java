package uyun.bat.agent.impl.autosync.entity;

/**
 * 服务配置项
 */
public class Server {
	private int versionChangeInterval;

	public Server() {
		versionChangeInterval = 60;
	}

	/**
	 * 服务端文件变更检查时间，单位s
	 * @return
	 */
	public int getVersionChangeInterval() {
		return versionChangeInterval;
	}

	public void setVersionChangeInterval(int versionChangeInterval) {
		this.versionChangeInterval = versionChangeInterval;
	}

}
