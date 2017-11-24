package com.broada.carrier.monitor.common.remoteio.api;


/**
 * 远程IO，便于客户端访问服务端的文件与数据
 * @author Jiangjw
 */
public interface RemoteIO {
	static final int READ_BLOCK_SIZE_MAX = 10 * 1024;
	
	/**
	 * 获取指定文件，如存在则返回其信息
	 * @param file
	 * @return
	 */
	RemoteFile get(String file);
	
	/**
	 * 罗列指定目录下的所有文件
	 * @param dir
	 * @return
	 */
	RemoteFile[] list(String dir);
	
	/**
	 * 用指定模式打开指定文件
	 * @param file
	 * @param mode
	 * @return 返回文件ID。如果打开失败会弹出标准异常，而不会在返回值中体现
	 */
	int open(String file, RemoteIOMode mode);

	/**
	 * 将数据写入到已打开的文件中
	 * @param fileId
	 * @param data
	 * @param offset
	 * @param length
	 */
	void write(int fileId, byte[] data, int offset, int length);

	/**
	 * 关闭指定文件
	 * @param fileId
	 */
	void close(int fileId);

	/**
	 * 从已打开的文件中读取指定长度数据
	 * @param fileId
	 * @param len
	 * @return
	 */
	byte[] read(int fileId, int len);
	
	/**
	 * 删除指定文件
	 * @param file
	 * @return 如果文件不存在返回false，如果删除失败弹出异常
	 */
	boolean delete(String file);
	
	/**
	 * 设置指定文件的修改时间
	 * @param file
	 * @param lastModified
	 * @return 修改成功返回true，如果文件不存在或已经在打开中无法修改返回false
	 */
	boolean setLastModified(String file, long lastModified);
}
