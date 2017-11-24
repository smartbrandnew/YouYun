package com.broada.carrier.monitor.impl.db.st.process;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.database.BaseDAO;
import com.broada.utils.JDBCUtil;

/**
 * shentong进程资源消耗信息监测
 * 
 * @author Zhouqa Create By 2016年4月13日 上午11:00:06
 */
public class ShentongProcessMonitor extends BaseMonitor {
	@Override
	public Serializable collect(CollectContext context) {
		long replyTime = System.currentTimeMillis();
		ShentongMethod method = new ShentongMethod(context.getMethod());
		List<VProcess> resultProcess = new InitDataDao(context.getNode().getIp(), method.getPort(), method.getSid(),
				method.getUsername(), method.getPassword()).fetchProcesses(null);
		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0)
			replyTime = 1L;
		MonitorResult result = new MonitorResult();
		int size = resultProcess.size();
		for (int i = 0; i < size; i++) {
			VProcess process = resultProcess.get(i);
			long allocMem = process.getAllocMem();
			long usedMem = process.getUsedMem();
			MonitorResultRow row = new MonitorResultRow(process.getName(), process.getDesc());
			row.setIndicator("SHENTONG-PROCESS-1", VProcess.convert2M(allocMem));
			row.setIndicator("SHENTONG-PROCESS-2", VProcess.convert2M(usedMem));
			row.setIndicator("SHENTONG-PROCESS-3", allocMem == 0 ? 0 : 1.0 * (allocMem - usedMem) / allocMem
					* 100.0);
			row.setIndicator("SHENTONG-PROCESS-4", VProcess.convert2M(allocMem - usedMem));
			result.addRow(row);
		}
		result.setResponseTime(replyTime);
		return result;
	}

	static class InitDataDao extends BaseDAO {
		String host;
		int port;
		String sid;
		String username;
		String password;

		public InitDataDao(String host, int port, String sid, String username, String password) {
			super();
			this.host = host;
			this.password = password;
			this.port = port;
			this.sid = sid;
			this.username = username;
		}

		List<VProcess> fetchProcesses(Collection<String> processnames) {
			ArrayList<String> indexedProcessNames = new ArrayList<String>();
			if (processnames != null)
				indexedProcessNames = new ArrayList<String>(processnames);
			List<VProcess> result = new ArrayList<VProcess>();
			Connection con = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				Map<String, VProcess> processMap = new LinkedHashMap<String, VProcess>();
				for (String key : VProcess.descMap.keySet()) {
					if (indexedProcessNames.size() == 0 || indexedProcessNames.contains(key))
						processMap.put(key, VProcess.create(key));
				}
				String url = "jdbc:oscar://" + host + ":" + port + "/" + sid;
				con = JDBCUtil.createConnection("com.oscar.Driver", url, username, password);
				ps = con.prepareStatement("select P.* from v$process P ");
				rs = ps.executeQuery();
				while (rs.next()) {
					String name = rs.getString("PDESC");
					VProcess p = processMap.get(name);
					if (p == null)
						continue;
					p.setName(name);
					p.addAllocMem(rs.getLong("ALLOC_MEM"));
					p.addUsedMem(rs.getLong("USED_MEM"));
					p.addCount();
				}
				result.addAll(processMap.values());
			} catch (Throwable e) {
				throw ErrorUtil.createRuntimeException("获取shentong进程信息失败", e);
			} finally {
				JDBCUtil.close(rs, ps, con);
			}
			return result;
		}
	}

}
