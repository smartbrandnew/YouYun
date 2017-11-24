package uyun.bat.web.api.config;

import java.util.Map;

public interface ConfigWebService {

	/**
	 * 返回配置文件里是否开启自愈功能
	 * 
	 * @return
	 */
	Map<String, Object> isOpenAutoRecover();
}
