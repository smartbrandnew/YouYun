package uyun.bat.agent.impl.autosync.common;

/**
 * 文件变更通知监听器接口
 * @author Jiangjw
 */
public interface FileChangedListener {
	/**
	 * 指定clientId的文件产生了变更
	 * @param clientId
	 */
	void onChanged(String clientId);
}
