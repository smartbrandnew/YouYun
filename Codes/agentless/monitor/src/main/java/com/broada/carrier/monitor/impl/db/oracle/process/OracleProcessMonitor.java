package com.broada.carrier.monitor.impl.db.oracle.process;

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
import com.broada.carrier.monitor.impl.db.oracle.util.OracleJDBCUtil;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleUrlUtil;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.database.BaseDAO;
import com.broada.utils.StringUtil;

/**
 * Oracle进程资源消耗信息监测
 * 
 * @author nile black
 * 
 */
public class OracleProcessMonitor extends BaseMonitor {
	@Override
	public Serializable collect(CollectContext context) {
    long replyTime = System.currentTimeMillis();
    OracleMethod method = new OracleMethod(context.getMethod());
		List<VProcess> resultProcess = new InitDataDao(context.getNode().getIp(), method.getPort(),
				method.getSid(), method.getServiceName(), method.getUsername(), method.getPassword()).fetchProcesses(null);
    replyTime = System.currentTimeMillis() - replyTime;
    if (replyTime <= 0)
      replyTime = 1L;
		MonitorResult result = new MonitorResult();
		int size = resultProcess.size();
		for (int i = 0; i < size; i++) {
			VProcess process = resultProcess.get(i);
			long pgaAllocMem = process.getPgaAllocMem();
			long pgaUsedMem = process.getPgaUsedMem();
			MonitorResultRow row = new MonitorResultRow(process.getName(), process.getDesc());
			row.setIndicator("ORACLE-PROCESS-1", VProcess.convert2M(pgaAllocMem));
			row.setIndicator("ORACLE-PROCESS-2", VProcess.convert2M(pgaUsedMem));
			row.setIndicator("ORACLE-PROCESS-3", pgaAllocMem == 0 ? 0 : 1.0 * (pgaAllocMem - pgaUsedMem) / pgaAllocMem * 100.0);
			row.setIndicator("ORACLE-PROCESS-4", VProcess.convert2M(pgaAllocMem - pgaUsedMem));
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
    String servicename;

    public InitDataDao(String host, int port, String sid, String servicename, String username, String password) {
      super();
      this.host = host;
      this.password = password;
      this.port = port;
      this.sid = sid;
      this.username = username;
      this.servicename = servicename;
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
        String url = null;
        if(!StringUtil.isNullOrBlank(servicename))
        	url = OracleUrlUtil.getUrl(host, port, servicename, true);
        else
        	url = OracleUrlUtil.getUrl(host, port, sid, false);
        con = OracleJDBCUtil.createConnection(url, username, password);
        ps = con.prepareStatement("select P.* from v$process P ");
        rs = ps.executeQuery();
        while (rs.next()) {
          String program = rs.getString("PROGRAM");
          String name = VProcess.parseName(program);
          VProcess p = processMap.get(name);
          if (p == null)
            continue;
          p.setName(name);
          p.addPgaAllocMem(rs.getLong("PGA_ALLOC_MEM"));
          p.addPgaUsedMem(rs.getLong("PGA_USED_MEM"));
          p.addPgaMaxMem(rs.getLong("PGA_MAX_MEM"));
          p.addCount();
        }
        result.addAll(processMap.values());
      } catch (Throwable e) {
      	throw ErrorUtil.createRuntimeException("获取oracle进程信息失败", e);
      } finally {
        OracleJDBCUtil.close(rs, ps, con);
      }
      return result;
    }
  }
	
	
}
