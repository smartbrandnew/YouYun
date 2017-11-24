package com.broada.carrier.monitor.common.swing;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.broada.carrier.monitor.common.util.WorkPathUtil;

/**
 * 图标缓存库
 * @author Jiangjw
 */
public class IconLibrary {
	private static IconLibrary instance;
	private Map<String, Icon> icons = new ConcurrentHashMap<String, Icon>();
	private Map<String, Image> images = new ConcurrentHashMap<String, Image>();

	/**
	 * 获取默认实例
	 * @return
	 */
	public static IconLibrary getDefault() {
		if (instance == null) {
			synchronized (IconLibrary.class) {
				if (instance == null)
					instance = new IconLibrary();
			}
		}
		return instance;
	}
	
	/**
	 * 获取指定文件路径的图标
	 * @param filename
	 * @return
	 */
	public Icon getIcon(String filename) {
		Icon icon = icons.get(filename);
		if (icon == null) {			
			icon = new ImageIcon(WorkPathUtil.getFile(filename).getAbsolutePath());
			icons.put(filename, icon);
		}
		return icon;
	}

	/**
	 * 获取指定文件路径的图像
	 * @param filename
	 * @return
	 */
	public Image getImage(String filename) {
		Image image = images.get(filename);
		if (image == null) {
			image = Toolkit.getDefaultToolkit().createImage(WorkPathUtil.getFile(filename).getAbsolutePath());
			images.put(filename, image);
		}
		return image;
	}
}
