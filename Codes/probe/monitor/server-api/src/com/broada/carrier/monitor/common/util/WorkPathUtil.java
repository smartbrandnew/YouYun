package com.broada.carrier.monitor.common.util;

import java.io.File;

import com.broada.component.utils.io.FileUtil;

/**
 * 提供几个关键的针对工作路径的操作方法
 * @author Jiangjw
 */
public class WorkPathUtil {
	private static String workPath;
	private static String rootPath;
	
	/**
	 * 获取工作路径下的指定文件，如果文件非工作路径，则会弹出异常
	 * @param filepath
	 * @return
	 */
	public static File getFile(String filepath) {
		File file = new File(filepath);
		String path = file.getPath();
		if(!path.startsWith(getRootPath())){
			path = getRootPath() + path;
		}
		filepath = FileUtil.toAbsolute(path);
		if (filepath.startsWith(getWorkPath()))
			return new File(filepath);
		else
			throw new IllegalArgumentException(String.format("不允许访问工作路径以外的文件或目录[工作路径：%s 访问路径：%s]", getWorkPath(), filepath));	
	}

	/**
	 * 获取工作路径，总是有/结尾
	 * @return
	 */
	public static String getWorkPath() {
		if (workPath == null) { 
			synchronized (WorkPathUtil.class) {
				if (workPath == null) {
					workPath = FileUtil.toAbsolute(new File(System.getProperty("user.dir")).getPath());
					if (!workPath.endsWith("/"))
						workPath += "/";				
				}
			}
		}
		return workPath;
	}
	
	/**
	 * 判断指定的文件是否为工作目录中的文件，并返回其相对路径
	 * @param file
	 * @return
	 */
	public static String getRelativePath(File file) {
		String filePath = file.getPath();
		if(!filePath.startsWith(getRootPath())){
			filePath = getRootPath() + filePath;
		}
		String path = FileUtil.toLinuxPath(getFile(filePath).getPath());
		return path.substring(WorkPathUtil.getWorkPath().length());
	}
	
	/**
	 * 获取根路径，总是有/结尾
	 * @return
	 */
	public static String getRootPath() {
		if (rootPath == null) { 
			synchronized (WorkPathUtil.class) {
				if (rootPath == null) {
					rootPath = new File(System.getProperty("user.dir")).getPath();
					if (!rootPath.endsWith(File.separator))
						rootPath += File.separator;				
				}
			}
		}
		return rootPath;
	}
}
