package com.broada.carrier.monitor.common.remoteio.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 远程IO便于使用的客户端
 * @author Jiangjw
 */
public class RemoteIOClient {
	private static final int READ_BLOCK_SIZE = 1024;
	private RemoteIO service;	
	
	public RemoteIOClient(RemoteIO service) {
		super();
		this.service = service;
	}
	
	/**
	 * 获取指定文件
	 * @param file
	 * @return
	 */
	public RemoteFile get(String file) {
		return service.get(file);
	}

	/**
	 * 罗列指定目录的所有文件
	 * @param dir
	 * @return
	 */
	public RemoteFile[] list(String dir) {
		return service.list(dir);
	}
	
	/**
	 * 将指定数据保存到服务器上指定文件中
	 * @param file
	 * @param content
	 */
	public void save(String file, byte[] data) {
		int fileId = service.open(file, RemoteIOMode.WRITE);
		try {
			service.write(fileId, data, 0, data.length);
		} finally {
			service.close(fileId);
		}
	}
	
	/**
	 * 将服务器指定文件的数据读取到指定输出流中
	 * @param file
	 * @param os
	 * @return 返回读取的长度
	 * @throws IOException 
	 */
	public int read(String file, OutputStream os) throws IOException {
		int len = 0;
		int fileId = service.open(file, RemoteIOMode.READ);
		try {			
			while (true) {
				byte[] data = service.read(fileId, READ_BLOCK_SIZE);
				if (data == null || data.length == 0)
					break;
				len += data.length;
				os.write(data);
				if (data.length < READ_BLOCK_SIZE)
					break;
			}			
		} finally {
			service.close(fileId);
		}
		return len;
	}
	
	/**
	 * 读取服务器指定文件，并按指定编码组织为字符串返回
	 * @param file
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public String read(String file, String charset) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		read(file, os);
		return os.toString(charset);
	}
	
	/**
	 * 将指定字符串使用指定编码保存到服务器指定文件
	 * @param file
	 * @param content
	 * @param charset
	 * @throws UnsupportedEncodingException
	 */
	public void save(String file, String content, String charset) throws UnsupportedEncodingException {
		save(file, content.getBytes(charset));
	}
	
	/**
	 * 删除指定文件
	 * @param file
	 */
	public void delete(String file) {
		service.delete(file);
	}

	/**
	 * 将指定的文件用指定的名称保存
	 * @param filepath
	 * @param file
	 * @throws IOException 
	 */
	public void save(String filepath, File file) throws IOException {
		int fileId = service.open(filepath, RemoteIOMode.WRITE);
		FileInputStream is = new FileInputStream(file);
		try {
			byte[] buf = new byte[READ_BLOCK_SIZE]; 
			while (true) {
				int len = is.read(buf);
				if (len < 0)
					break;
				if (len > 0)
					service.write(fileId, buf, 0, len);
			}			
		} finally {
			service.close(fileId);
			is.close();
		}
	}
	
	/**
	 * 设置文件修改时间
	 * @param filepath
	 * @param lastModified
	 * @return
	 */
	public boolean setLastModified(String filepath, long lastModified) {
		return service.setLastModified(filepath, lastModified);
	}
}
