package com.broada.carrier.monitor.impl.host.cli.file;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class CLIFileExecutor {
	public static CLIFileMonitorCondition[] getFileConditions(String taskId, MonitorNode node, MonitorMethod method, CLIFileMonitorCondition[] oldList)
			throws CLIException {
		for (int index = 0; oldList != null && index < oldList.length; index++) {			
			getFileConditions(taskId, node, method, oldList[index]);
		}
		return oldList;
	}

	private static void getFileConditions(String taskId, MonitorNode node, MonitorMethod method,
			CLIFileMonitorCondition condition) throws CLIException {
		Map fileMap = CLIFileExecutor.getFileConditionMap(taskId, node, method,
				new String[] { condition.getField() });
		CLIFileMonitorCondition c = null;
		for (Object obj : fileMap.values()) {
			CLIFileMonitorCondition con = (CLIFileMonitorCondition) obj;
			if (con.getField().equalsIgnoreCase(condition.getField())) {
				c = con;
				break;
			}
		}
		if (c == null) {
			condition.setExists(false);
			condition.setSize(0);
			condition.setUser("Unknown");
		} else {
			try {
				BeanUtils.copyProperties(condition, c);
			} catch (Exception e) {
				throw new CLIResultParseException("文件属性设置失败。", e);
			}
		}
	}

	public static List getFileConditions(String taskId, MonitorNode node, MonitorMethod method, String[] files)
			throws CLIException {

		List fileList = new ArrayList();
		Map fileMap = getFileConditionMap(taskId, node, method, files);

		Iterator iter = fileMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			fileList.add(fileMap.get(key));
		}
		return fileList;
	}

	public static Map<String, CLIFileMonitorCondition> getFileConditionMap(String taskId, MonitorNode node, MonitorMethod method, String[] files)
			throws CLIException {
		return getFileConditionMap(taskId, node, method, files, 1);
	}
	
	/**
	 * 切割空格行
	 * @param text
	 * @return
	 */
	private static String[] splitWord(String text) {
		return text.split("\\s+");
	}
	
	/**
	 * 转换浮点数精度
	 * @param value
	 * @param scale
	 * @return
	 */
	private static double round(double value, int scale) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
		double d = bd.doubleValue();
		bd = null;
		return d;
	}		
	
	/**
	 * <pre>
	 * 将一行cli ls命令的输出，解析为一个CLIFileMonitorCondition对象，支持以下不同系统的格式：
	 * 1. aix
	 *    1. drwxrwxrwt  18 bin      bin           20480 Apr 20 11:44 tmp
	 *    2. drwxrwxr-x   2 root     system          256 Jul 08 2010  tftpboot
	 * 2. hpux
	 *    1. drwxrwxrwx   4 root       sys             96 Nov 19 21:34 openssh
	 *    2. dr-xr-xr-x  44 bin        bin           8192 Dec  9  2007 opt
	 * 3. linux
	 *    1. dr-xr-xr-x  70 root root     0 Feb 20 15:53 proc
	 *    2. drwxr-xr-x   2 root root  4096 2004-08-13  opt
	 *    3. lrwxrwxrwx  1 root root      4 2010-12-23  egrep -> grep
	 *    4. drwxr-xr-x  2 root  root    4096 12-16 10:14 blkid
	 * 
	 * 总结格式如下：
	 * 1. MMM dd HH:mm
	 * 2. MM-dd HH:mm
	 * 3. MMM dd yyyy
	 * 4. yyyy-MM-dd HH:mm
	 * 5. yyyy-MM-dd
	 * 
	 * 备注：
	 * 针对linux来说，是可以使用ls -l --time-style="long-iso"命令，以输出以下行的：
	 * -rw-r--r--  1 root    root       0 2015-01-04 10:24 file name
	 * 但此方法在aix上确认无法使用
	 *  
	 * @param line
	 * @return
	 */
	public static CLIFileMonitorCondition parseFileline(String line) {
		return parseFileline(line, new Date());
	}
		
	static CLIFileMonitorCondition parseFileline(String line, Date now) {		
		String[] fields = splitWord(line);
		if (fields.length < 7)
			return null;
		
		Date modifyTime = null;		
		int timeStartIndex = 5;
		int filepathIndex = 0;
		StringBuilder sb = new StringBuilder();
		for (ModifiedFormat format : ModifiedFormat.FORMATS) {
			if (fields.length > timeStartIndex + format.getFieldCount()) {
				for (int i = 0; i < format.getFieldCount(); i++) {
					if (sb.length() > 0)
						sb.append(" ");
					sb.append(fields[timeStartIndex + i]);					
				}
				modifyTime = format.format(sb.toString(), now);
				if (modifyTime != null) {
					filepathIndex = timeStartIndex + format.getFieldCount();
					break;
				}
				sb.setLength(0);
			}
		}
		
		if (modifyTime == null)
			throw new IllegalArgumentException("无法识别的文件修改时间格式：" + line);
					
		String filepath = fields[filepathIndex];
		for (int i = filepathIndex + 1; i < fields.length; i++)
			filepath += " " + fields[i];
		
		if (!filepath.startsWith("/")) {		// 路径
			int pos = filepath.indexOf("/");
			if (pos > -1)
				filepath = filepath.substring(pos);
		}
		
		if (filepath.indexOf("->") != -1) {	// 链接
			filepath = filepath.substring(0, filepath.indexOf("->")).trim();
		}
		
		if (filepath.indexOf(':') >= 0) {		// 盘符
			filepath = filepath.substring(0, 1).toLowerCase() + filepath.substring(1);				
		}
							
		return new CLIFileMonitorCondition(filepath, fields[3], fields[2], Integer.parseInt(fields[1]), round(Double.parseDouble(fields[4])/(1024*1024), 2), modifyTime);		
	}	

	public static List getFileConditions(String taskId, MonitorNode node, MonitorMethod method, List oldList,
			int tryTimes) throws CLIException {
		for (int index = 0; index < oldList.size(); index++) {
			CLIFileMonitorCondition condition = (CLIFileMonitorCondition) oldList.get(index);
			getFileConditions(taskId, node, method, condition, tryTimes);
		}
		return oldList;
	}

	private static void getFileConditions(String taskId, MonitorNode node, MonitorMethod method,
			CLIFileMonitorCondition condition, int tryTimes) throws CLIException {
		Map fileMap = CLIFileExecutor.getFileConditionMap(taskId, node, method,
				new String[] { condition.getField() }, tryTimes);
		String filepath = condition.getField();
		// 由于解析的时候把文件路径（包括文件名都转化为了小写）
		CLIFileMonitorCondition c = (CLIFileMonitorCondition) fileMap.get(filepath);
		if (c == null && filepath != null) 
			c = (CLIFileMonitorCondition) fileMap.get(filepath.toLowerCase());
		if (c == null) {
			condition.setExists(false);
			condition.setSize(0);
			condition.setUser("Unknown");
		} else {
			try {
				BeanUtils.copyProperties(condition, c);
			} catch (Exception e) {
				throw new CLIResultParseException("文件属性设置失败。", e);
			}
		}
	}

	public static List getFileConditions(String taskId, MonitorNode node, MonitorMethod method, String[] files,
			int tryTimes) throws CLIException {

		List fileList = new ArrayList();
		Map fileMap = getFileConditionMap(taskId, node, method, files, tryTimes);

		Iterator iter = fileMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			fileList.add(fileMap.get(key));
		}
		return fileList;
	}

	public static Map<String, CLIFileMonitorCondition> getFileConditionMap(String taskId, MonitorNode node, MonitorMethod method, String[] files,
			int tryTimes) throws CLIException {		
		CLIResult result = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_FILELIST, tryTimes, files);

		List<?> fileListProperties = result.getListTableResult();
		Map<String, CLIFileMonitorCondition> fileMap = new LinkedHashMap<String, CLIFileMonitorCondition>();
		for (int index = 0; index < fileListProperties.size(); index++) {			
			Object row = fileListProperties.get(index);
			CLIFileMonitorCondition cond;
			if (row instanceof Properties)
				cond = new CLIFileMonitorCondition((Properties)row);
			else if (row instanceof CLIFileMonitorCondition)
				cond = (CLIFileMonitorCondition)row;
			else			
				throw new IllegalArgumentException("CLI命令解析必须返回Properties或CLIFileMonitorCondition对象");								
			fileMap.put(cond.getFilepath(), cond);
		}

		return fileMap;		
	}
}
