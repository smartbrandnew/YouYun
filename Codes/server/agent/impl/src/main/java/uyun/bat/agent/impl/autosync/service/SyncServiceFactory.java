package uyun.bat.agent.impl.autosync.service;

public class SyncServiceFactory {
	private static AutoSyncServer instance;

	/**
	 * 获取默认实例
	 * @return
	 */
	public static AutoSyncServer getDefault() {
		if (instance == null) {
			synchronized (SyncServiceFactory.class) {
				if (instance == null)
					instance = new SyncServerImpl();
			}
		}
		return instance;
	}
}
