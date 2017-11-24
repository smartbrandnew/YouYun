package com.broada.carrier.monitor.impl.host.cli.directory.unix;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.host.cli.directory.CLIDirectory;
import com.broada.carrier.monitor.impl.host.cli.directory.CLIDirectoryParameter;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.component.utils.text.Unit;
import com.broada.utils.StringUtil;

/**
 * unix目录监测器
 * 
 * @author huangjb Jun 16, 2008 11:27:29 AM
 */
public class CLIDirectoryMonitor implements Monitor {
	private static final Log logger = LogFactory.getLog(CLIDirectoryMonitor.class);

	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		CLIDirectoryParameter p = new CLIDirectoryParameter(context.getTask().getParameter());
		// 根据目录获取所有文件记录
		Map<String, Integer> fileCntMap = new HashMap<String, Integer>();
		boolean state = true;
		StringBuffer msgSB = new StringBuffer();
		StringBuffer valSB = new StringBuffer();
		StringBuffer buff = new StringBuffer();
		Map<String, CLIFile> filesMap = CLIDirectoryExecutor.getFilesMap(context.getTask().getId(), context.getNode(), context.getMethod(), buff, p.getDirectories(),
				fileCntMap, true);
		if (!StringUtil.isNullOrBlank(buff.toString())) {
			msgSB.append(buff.toString());
			valSB.append(buff.toString().replaceAll(";\n", "."));
			state = false;
		}
		if (filesMap == null || filesMap.isEmpty()) {
			if (!state) {
				result.setMessage(msgSB.toString());
				if (buff.indexOf("解析目录采集结果失败") != -1) {
					result.setState(MonitorState.FAILED);
				}
				if (buff.indexOf("无法连接目标服务器") != -1) {
					result.setState(MonitorState.FAILED);
				}
			} else {
				result.setMessage("监测目录内所有数据文件各监测项都在正常范围.");
			}
			return result;
		}

		// 获取上次监测后的文件实例列表
		Set<String> ksFile = filesMap.keySet();
		MonitorResult lastResult = context.getTempData(MonitorResult.class);		
		Set<String> ksInst = new HashSet<String>();
		if (lastResult != null) {
			if (lastResult.getRows() != null) {
				for (MonitorResultRow row : lastResult.getRows())
					ksInst.add(row.getInstCode());			
			}
		} else 
			lastResult = new MonitorResult();

		// 新增文件
		Set<String> newFiles = this.removeAll(ksFile, ksInst);
		boolean addstate = appendNewFileMsg(newFiles, msgSB, valSB);

		// 删除文件
		Set<String> rmFiles = this.removeAll(ksInst, ksFile);
		boolean delstate = appendDelFileMsg(rmFiles, msgSB, valSB);

		state = addstate && delstate;

		Map<String, String> dirmap = new HashMap<String, String>();
		CLIDirectory[] dirs = p.getDirectories();
		for (int i = 0; i < dirs.length; i++) {
			String dir = dirs[i].getPath();
			dirmap.put(dir, dir);
		}

		int idxInst = 0;
		MonitorInstance[] instances = new MonitorInstance[ksFile.size()];
		long now = System.currentTimeMillis();
		for (Iterator<String> iter = ksFile.iterator(); iter.hasNext();) {
			CLIFile fileCond = (CLIFile) filesMap.get(iter.next());
			String fileName = fileCond.getName();
			double fileSize = fileCond.getSize().doubleValue();

			instances[idxInst] = new MonitorInstance(fileName, fileName);
			idxInst++;

			String dirPath = getDirByFileName(dirmap, fileName);
			if (dirPath == null) {
				continue;
			}

			double currSize = fileSize;
			result.addPerfResult(new PerfResult(fileName, "CLI-UNIX-DIRECTORY-1", currSize));

      FileSize perf = getFileSize(lastResult, fileName);						
			double currValue = 0;
			if (perf != null) {				
				double incSize = currSize - perf.getSize();
				if (incSize > 0) {
					long time = (now - perf.getTime()) / 1000;
					if (time > 0) 
						currValue = Unit.MB_s.to(Unit.KB_s, incSize / time);
				}			
			}

			result.addPerfResult(new PerfResult(fileName, "CLI-UNIX-DIRECTORY-2", currValue));
			if (fileCond.getCreateTime() != null)
				result.addPerfResult(new PerfResult(fileName, "file-modified", fileCond.getCreateTime()));
    }
    		
		if (!state) {
			result.setMessage(msgSB.toString());
		} 		
		context.setTempData(result);
		return result;
	}

	private FileSize getFileSize(MonitorResult lastResult, String fileName) {
		MonitorResultRow row = lastResult.getRow(fileName);
		if (row == null)
			return null;
		Number value = (Number) row.getIndicator("CLI-UNIX-DIRECTORY-1");
		if (value == null)
			return null;
		
		return new FileSize(value.doubleValue(), lastResult.getTime().getTime());		
	}

	private static class FileSize {
		private double size;
		private long time;

		public FileSize(double size, long time) {
			super();
			this.size = size;
			this.time = time;
		}

		public double getSize() {
			return size;
		}

		public long getTime() {
			return time;
		}

	}

	private boolean appendNewFileMsg(Set<String> newFiles, StringBuffer msgSB, StringBuffer valSB) {
		if (newFiles != null && !newFiles.isEmpty()) {
			msgSB.append("新增文件");
			valSB.append("存在新增文件.");
			int idx = 0;
			for (Iterator<String> iter = newFiles.iterator(); iter.hasNext();) {
				if (idx > 0) {
					msgSB.append(",");
				}
				msgSB.append(iter.next());
				idx++;
			}
			msgSB.append(";\n");
			return false;
		}
		return true;
	}

	private boolean appendDelFileMsg(Set<String> rmFiles, StringBuffer msgSB, StringBuffer valSB) {
		if (rmFiles != null && !rmFiles.isEmpty()) {
			msgSB.append("文件");
			valSB.append("某些文件被删除.");
			int idx = 0;
			for (Iterator<String> iter = rmFiles.iterator(); iter.hasNext();) {
				if (idx > 0) {
					msgSB.append(",");
				}
				msgSB.append(iter.next());
				idx++;
			}
			msgSB.append("被删除;\n");
			return false;
		}
		return true;
	}

	private String getDirByFileName(Map<String, String> dirmap, String fileName) {
		String key = fileName;
		Set<String> keySet = dirmap.keySet();
		do {
			key = key.substring(0, key.lastIndexOf("/"));
			if (keySet.contains(key)) {
				return key;
			}
		} while (key.indexOf("/") != -1);
		return null;
	}

	private Set<String> removeAll(Set<String> srcSet, Set<String> aimSet) {
		if (isNullOrEmpty(srcSet) || isNullOrEmpty(aimSet)) {
			return srcSet;
		}
		if (aimSet.containsAll(srcSet)) {
			return null;
		}
		Set<String> set = new HashSet<String>();
		for (Iterator<String> iter = srcSet.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			if (!aimSet.contains(key)) {
				set.add(key);
			}
		}
		return set;
	}

	private boolean isNullOrEmpty(Set<String> set) {
		return set == null || set.isEmpty();
	}
	
	@Override
	public Serializable collect(CollectContext context) throws CollectException {		
		StringBuffer buff = new StringBuffer();
		HashMap<String, Integer> fileCnt = new LinkedHashMap<String, Integer>();		
		try {
			CollectParam param = context.getParameterObject(CollectParam.class);
			CLIDirectory[] cliDirectories = param.getDirs();
			Map<String, CLIFile> result = CLIDirectoryExecutor.getFilesMap("-1", context.getNode(), context.getMethod(), buff,
					cliDirectories, fileCnt, param.isFile(), 0);
			if (param.isFile())
				return (Serializable) result;
			else
				return fileCnt;
		} catch (Exception e) {
			logger.error("系统错误", e);
			throw new CollectException(e);
		}		
	}
}
