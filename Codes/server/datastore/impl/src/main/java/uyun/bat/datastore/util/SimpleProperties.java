package uyun.bat.datastore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProperties {
	private static final Logger logger = LoggerFactory.getLogger(SimpleProperties.class);
	private static final String MARCO_START = "${";
	private static final String MARCO_END = "}";
	private Properties props;

	public SimpleProperties() {
		this.props = new Properties();
	}

	public SimpleProperties(InputStream is) {
		props = new Properties();
		try {
			props.load(is);
		} catch (IOException e) {
			logger.warn("fail to load config file，it will make some unexpected error or all default setting", e);
		}
	}

	public SimpleProperties(Properties props) {
		this.props = props;
	}

	public SimpleProperties(String configFile) {
		props = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(new File(configFile));
			if (configFile.endsWith(".xml"))
				props.loadFromXML(is);
			else
				props.load(is);
		} catch (IOException e) {
			logger.warn("fail to load config file，it will make some unexpected error or all default setting：" + configFile, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.warn(String.format("fail to close config：%s", configFile), e);
				}
			}
		}
	}

	/**
	 * 读取指定配置项，如不存在返回默认值
	 * 注意：如果值超过了int类型的大小，也会判断不存在
	 * @param name
	 * @param defValue
	 * @return
	 */
	public int get(String name, int defValue) {
		String value = get(name);
		if (value == null)
			return defValue;

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			warn(name, value, defValue, e);
			return defValue;
		}
	}

	/**
	 * 读取指定配置项，如不存在返回默认值
	 * @param name
	 * @param defValue
	 * @return
	 */
	public long get(String name, long defValue) {
		String value = get(name);
		if (value == null)
			return defValue;

		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			warn(name, value, defValue, e);
			return defValue;
		}
	}

	/**
	 * 读取指定配置项，如不存在返回默认值
	 * @param name
	 * @param defValue
	 * @return
	 */
	public double get(String name, double defValue) {
		String value = get(name);
		if (value == null)
			return defValue;

		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			warn(name, value, defValue, e);
			return defValue;
		}
	}

	/**
	 * 获取指定配置项，如下存在弹出异常
	 * @param name
	 * @return
	 */
	public String check(String name) {
		String value = get(name);
		if (value == null)
			throw new IllegalArgumentException("Lost some necessary config ：" + name);
		return value;
	}

	/**
	 * 获取指定配置项
	 * @param name
	 * @return
	 */
	public String get(String name) {
		String value = props.getProperty(name);
		if (value == null)
			return null;

		if (value.indexOf(MARCO_START) < 0)
			return value;

		Set<String> accessed = new HashSet<String>();
		return getByMarco(name, accessed);
	}

	private String getByMarco(String name, Set<String> accessed) {
		if (accessed.contains(name))
			throw new IllegalArgumentException("config global replace cyclic definition：" + name);
		accessed.add(name);

		try {
			String value = props.getProperty(name);
			if (value == null)
				return "";
			while (true) {
				int marcoStart = value.indexOf(MARCO_START);
				if (marcoStart < 0)
					break;
				int marcoEnd = value.indexOf(MARCO_END, marcoStart);
				if (marcoEnd < 0)
					break;
				String marco = value.substring(marcoStart + 2, marcoEnd);
				String marcoValue = getByMarco(marco, accessed);
				value = value.substring(0, marcoStart) + marcoValue + value.substring(marcoEnd + 1);
			}
			return value;
		} finally {
			accessed.remove(name);
		}
	}

	/**
	 * 获取指定配置项，如不存在返回默认值
	 * @param name
	 * @param defValue
	 * @return
	 */
	public String get(String name, String defValue) {
		String value = get(name);
		if (value == null)
			return defValue;
		return value;
	}

	/**
	 * 获取所有节点系统属性
	 * @return
	 */
	public Set<String> getNames() {
		return props.stringPropertyNames();
	}

	/**
	 * 获取指定配置项，如不存在返回默认值
	 * @param name
	 * @param defValue
	 * @return
	 */
	public boolean get(String name, boolean defValue) {
		String value = get(name);
		if (value == null || value.length() == 0)
			return defValue;
		else if (value.equalsIgnoreCase("true"))
			return true;
		else if (value.equalsIgnoreCase("false"))
			return false;
		else {
			warn(name, value, defValue, null);
			return defValue;
		}
	}

	private static void warn(String name, String value, Object defValue, Throwable error) {
		String message = String.format("config [%s=%s] has some format problem，use default setting[%s]", name, value, defValue);
		if (error == null)
			logger.warn(message);
		else
			logger.warn(message, error);
	}

	/**
	 * 设置一个属性
	 * @param name
	 * @param value
	 */
	public void set(String name, String value) {
		props.put(name, value);
	}

	/**
	 * 保存到一个指定的输出流
	 * @param os
	 * @throws IOException
	 */
	public void save(OutputStream os) throws IOException {
		props.store(os, null);
	}

	/**
	 * 保存到一个指定的文件
	 * @param filename
	 * @throws IOException
	 */
	public void save(String filename) throws IOException {
		FileOutputStream os = new FileOutputStream(filename);
		try {
			props.store(os, null);
		} finally {
			os.close();
		}
	}
}
