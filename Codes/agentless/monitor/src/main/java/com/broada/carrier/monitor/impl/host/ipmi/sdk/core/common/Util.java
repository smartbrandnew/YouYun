package com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.util.OsInfoUtil;

public class Util {
	private static final Logger logger = LoggerFactory.getLogger(Util.class);
	private static String root = System.getProperty(Constants.WORK_DIR, null);
	private final static Runtime rt = Runtime.getRuntime();
	private static File file = new File(root);
	private static boolean isLoad = false;
	private static final int BUFFER_SIZE = 5 * 1024;
	private static String absolutePath;	

	/**
	 * 读取一个进程输出
	 * 
	 * @param process
	 * @param output
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static void readOutput(Process process, StringBuffer output, byte[] buffer) throws IOException,
			InterruptedException {
		boolean hadOutput;
		do {
			hadOutput = false;

			if (process.getErrorStream().available() > 0) {
				int len = process.getErrorStream().read(buffer);
				if (len > 0) {
					output.append(new String(buffer, 0, len));
					hadOutput = true;
				}
			}

			if (process.getInputStream().available() > 0) {
				int len = process.getInputStream().read(buffer);
				if (len > 0) {
					output.append(new String(buffer, 0, len));
					hadOutput = true;
				}
			}
		} while (hadOutput);
	}

	/**
	 * 判断一个进程是否终止
	 * 
	 * @param process
	 * @return
	 */
	private static boolean isProcessTerminated(Process process) {
		try {
			process.exitValue();
			return true;
		} catch (IllegalThreadStateException err) {
			return false;
		}
	}
	
	/**
	 * 执行shell命令，返回输出，默认超时时间60s
	 * 
	 * @param command	待执行ipmitool命令
	 * @return
	 * @throws IPMIException 
	 */
	public static String[] exec(String command) throws IPMIException {
		return exec(command, ConfigProperties.getPropertyInteger(Constants.IPMI_EXE_TIMEOUT, 60), null, false);//修改因为无法连接服务器而命令执行时间还是很长的问题
	}

	/**
	 * 执行shell命令，返回输出
	 * 
	 * @param command	待执行ipmitool命令
	 * @param timeout 超时时间，单位s
	 * @return
	 * @throws IPMIException 
	 */
	public static String[] exec(String command, int timeout) throws IPMIException {
		return exec(command, timeout, null, false);
	}

	private static String[] exec(String command, int timeout, String defaultPath, boolean isTest) throws IPMIException {
		if (logger.isDebugEnabled())
			logger.debug("IPMI命令：" + command);
		StringBuffer output = new StringBuffer(BUFFER_SIZE);
		byte[] data = new byte[BUFFER_SIZE];
		Process process = null;
		long startTime = System.currentTimeMillis();
		try {
			process = getProcess(command, defaultPath, isTest);
			do {
				readOutput(process, output, data);
				if (System.currentTimeMillis() - startTime > timeout * 1000) {
					throw new TimeoutException(String.format("命令执行超过最大限定时间【%s秒】，请检查连接或设置。", timeout));
				}				
				Thread.sleep(500);
			} while (!isProcessTerminated(process));
			readOutput(process, output, data);
		} catch(TimeoutException e){
			logger.warn(String.format("命令【%s】执行超过最大限定时间【%s秒】。错误：%s", command, timeout, e));
			throw new IPMIException(e.getMessage(), e);
		} catch (Throwable e) {
			logger.warn(String.format("当前命令 [%s]执行失败。错误：%s", command, e));
			logger.debug("堆栈：", e);
			throw new IPMIException(String.format("当前命令 [%s]执行失败。返回结果：\n%s", command, e));
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		String result = output.toString();
		if(result.startsWith("Error:"))
			throw new IPMIException(String.format("当前命令 [%s]执行失败。返回结果：\n%s", command, result));
		if (logger.isDebugEnabled())
			logger.debug("IPMI命令：" + command + " 结果：\n" + result);
		return result.split("\n");
	}

	/**
	 * 执行ipmitool命令得到Process
	 * 
	 * @param command 要执行命令
	 * @param defaultPath 默认ipmitool工具路径，可为null
	 * @param defaultPath 是否本地测试
	 * @return
	 */
	private static synchronized Process getProcess(String command, String defaultPath, boolean isTest) {
		try {
			loadEntityType(isTest);			
			Process p = rt.exec(getAbsolutePath(defaultPath) + command);
			return p;
		} catch (Exception e) {
			throw new RuntimeException(String.format("执行ipmitool 命令失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)),
					e);
		}
	}

	/**
	 * 加载ipmi实例配置
	 * @param isTest 是否测试代码
	 */
	private static void loadEntityType(boolean isTest) {
		if (!isTest) {
			if (!isLoad) {
				ConfigProperties.loadEntityType();
				isLoad = true;
			}
		}
	}

	/**
	 * 取得ipmitool路径
	 * 
	 * @param defaultValue 传入路径，可以为null，将自动去获取
	 * @return 绝对路径
	 */
	private static String getAbsolutePath(String defaultValue) {
		if (StringUtils.isNotBlank(defaultValue)) {
			return defaultValue;
		}
		
		if (absolutePath == null)
			if(OsInfoUtil.getOS().equalsIgnoreCase("windows"))
				absolutePath = file.getAbsolutePath() + "/conf/ipmitool/";
			else
				absolutePath = file.getAbsolutePath() + "/conf/ipmitool-linux/src/";
		return absolutePath;			
	}

	/**
	 * 判断对象是否为“空”： 1、当对象为null时，返回true 2、当对象为字符串类型时，根据字符串是否为空来判断
	 * 2、当对象为集合类型或者Map类型时，根据isEmpty()方法来判断 3、当对象为数组类型时，根据数组的长度是否为0来判断
	 * 
	 * @param object 待判断的对象
	 * @return 如果为“空”则返回true，否则返回false
	 */
	public static boolean isEmpty(Object object) {
		if (object == null) {
			return true;
		}

		// 字符串类型
		if (object instanceof String && "".equals(object)) {
			return true;
		}

		// 集合类型
		if (object instanceof Collection<?> && ((Collection<?>) object).isEmpty()) {
			return true;
		}

		// Map
		if (object instanceof Map<?, ?> && ((Map<?, ?>) object).isEmpty()) {
			return true;
		}

		// 数组
		if (object.getClass().isArray() && ((Object[]) object).length == 0) {
			return true;
		}
		return false;

	}

}
