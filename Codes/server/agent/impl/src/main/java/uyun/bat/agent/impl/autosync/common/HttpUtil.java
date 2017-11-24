package uyun.bat.agent.impl.autosync.common;

import java.nio.charset.Charset;

/**
 * Http操作相关常量与操作
 * @author Jiangjw
 */
public class HttpUtil {
	/**
	 * Http通信默认字符集
	 */
	public static final Charset CHARSET = Charset.forName("UTF-8");
	/**
	 * Http通信CLIENT请求前缀
	 */
	public static final String PATH_CLIENT_PREFIX = "/client/";	
	
	/**
	 * 将一个url编码为适合用于url的字符串
	 * @param url
	 * @return
	 */
	public static String encodeUrl(String url) {
		byte[] data = url.getBytes(CHARSET);
		return Base64Util.encode(data);
	}
	
	/**
	 * 将一个 {@link #encodeUrl(String)} 返回的字符串解码
	 * @param url
	 * @return
	 */
	public static String decodeUrl(String url) {
		byte[] data = Base64Util.decode(url);
		return new String(data, CHARSET);
	}	
}
