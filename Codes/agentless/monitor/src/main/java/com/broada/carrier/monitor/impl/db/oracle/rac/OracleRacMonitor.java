package com.broada.carrier.monitor.impl.db.oracle.rac;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.db.oracle.rac.racinfo.OracleRacInstance;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class OracleRacMonitor extends BaseMonitor{
	private Logger logger = LoggerFactory.getLogger(OracleRacMonitor.class);

	@SuppressWarnings("unchecked")
	@Override
	public Serializable collect(CollectContext context) {
	OracleMethod method = new OracleMethod(context.getMethod());
	CustomOracleManager manager = new CustomOracleManager(context.getNode().getIp(),method.getSid(), method.getPort(),
			 method.getUsername(), method.getPassword());
	 List<OracleRacInstance> values = null;
	 MonitorResult result = new MonitorResult();
	    try {
	        manager.initConnection();
	        values = manager.getMultiRacInstance();
			for (int i = 0; i < values.size(); i++) {
				OracleRacInstance oracleRac = values.get(i);
				MonitorResultRow row = new MonitorResultRow();
				row.setInstCode(context.getNode().getIp()+"-"+oracleRac.getInstanceName());
				row.setInstName(oracleRac.getInstanceName());
				String status = oracleRac.getStatus();
				int status_int = 0;   // 默认对应 nomount
				if(status.equalsIgnoreCase("mount"))
					status_int = 1;
				else if(status.equalsIgnoreCase("open"))
					status_int = 2;
				else if(status.equalsIgnoreCase("shutdown"))
					status_int = 3;
				row.setIndicator("ORACLE-RAC-1", status_int);
				row.setIndicator("ORACLE-RAC-2", oracleRac.getHostName());
				result.addRow(row);
				logger.info("实例名:" + oracleRac.getInstanceName() + "\t 状态: " + status_int);
			}
			return result;
	        
	      } catch (Exception e) {
	        logger.error("查询数据库失败:" + e.getMessage(), e);
	        result.setState(MonitorState.FAILED);
	        result.setMessage("查询数据库失败:" + e.getMessage());
	        result.setResultDesc("查询数据库失败:" + e.getMessage());
	        return result;
	      }
	}

}
