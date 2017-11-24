package com.broada.carrier.monitor.client.impl.common;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.config.Config;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.component.utils.error.ErrorUtil;

public class TargetTypeIconLibrary {
	private static final Logger logger = LoggerFactory.getLogger(TargetTypeIconLibrary.class);
	private static TargetTypeIconLibrary instance;
	private Set<String> iconsNotExists = new HashSet<String>();
	private Map<String, Icon> icons = new HashMap<String, Icon>();

	/**
	 * 获取默认实例
	 * 
	 * @return
	 */
	public static TargetTypeIconLibrary getDefault() {
		if (instance == null) {
			synchronized (TargetTypeIconLibrary.class) {
				if (instance == null)
					instance = new TargetTypeIconLibrary();
			}
		}
		return instance;
	}

	private TargetTypeIconLibrary() {
	}

	public Icon getIcon(String typeId) {
		if (iconsNotExists.contains(typeId))
			return null;

		Icon icon = icons.get(typeId.toUpperCase());
		if (icon == null) {
			icon = loadIcon(typeId);
			if (icon == null) {
				iconsNotExists.add(typeId);
				return null;
			}
			icons.put(typeId, icon);
		}
		return icon;
	}

	private Icon loadIcon(String typeId) {
		MonitorTargetType type = ServerContext.getTargetTypeService().getTargetType(typeId);
		if (type == null)
			return null;

		String urlString = ServerContext.getServerUrl() + Config.getDefault().getTargetTypeImageUrl(type);
		try {
			URL url = new URL(urlString);
			HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
			httpUrl.connect();
			BufferedInputStream bis = new BufferedInputStream(httpUrl.getInputStream());
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			int size = 0;
			byte[] buf = new byte[8096];
			while ((size = bis.read(buf)) != -1)
				fos.write(buf, 0, size);			
			bis.close();
			httpUrl.disconnect();
			return new ImageIcon(fos.toByteArray());
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "加载图片失败：" + urlString, e);
			return null;
		}
	}
}
