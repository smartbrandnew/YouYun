package uyun.bat.common.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.whale.common.util.error.ErrorUtil;

import com.baidu.disconf.client.addons.properties.ReloadablePropertiesFactoryBean;
import com.baidu.disconf.client.config.DisClientConfig;

/**
 * 重写disconf的相关加载项，允许结合system与local配置
 */
public class PropertiesFactoryBean extends ReloadablePropertiesFactoryBean {
	private static final Logger logger = LoggerFactory.getLogger(PropertiesFactoryBean.class);

	public PropertiesFactoryBean() {
		setLocations(Arrays.asList("common.properties", "monitor.properties"));

	}

	@Override
	protected void loadProperties(Properties props) throws IOException {
		props.putAll(Config.getInstance().getLocalProperties());

		props.putAll(System.getProperties());

		// 加载远程配置项
		Properties remoteProperties = new Properties();
		try {
			// 启用disconf远程配置,才加载该部分配置
			if (DisClientConfig.getInstance().ENABLE_DISCONF)
				super.loadProperties(remoteProperties);
			Config.getInstance().getRemoteProperties().putAll(remoteProperties);
			props.putAll(remoteProperties);
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "cannot load remote config", e);
		}

		Config.getInstance().dumpConfigItem();
	}
}
