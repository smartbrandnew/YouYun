package com.broada.carrier.monitor.impl.db.db2.tablespace;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.db2.Db2TableSpaceManager;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.utils.JDBCUtil;

public class Db2TableSpaceMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(Db2TableSpaceMonitor.class);
	public final static String[] CONDITION_FIELDS = new String[] { "totalPages", "usedRate" };

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		StringBuffer msg = new StringBuffer(); // 监测结果信息描述
		StringBuffer currVal = new StringBuffer();// 当前情况，用于发送Trap
		DB2MonitorMethodOption option = new DB2MonitorMethodOption(context.getMethod());

		// 连接参数的获取
		String ip = context.getNode().getIp();
		Db2TableSpaceManager manager = new Db2TableSpaceManager(ip, option);
		Connection testCon = null;
    long replyTime = System.currentTimeMillis();
		try {
			testCon = manager.getConnection();
		} catch (SQLException e1) {
			msg.append(e1.getMessage() + "\n");
			currVal.append(e1.getMessage());
			result.setResultDesc(msg.toString());
			if (e1.getErrorCode() == -99999) {// 表示用户名密码错误
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
			} else {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
			}
			return result;
		} catch (CLIException e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("通过agent方式获取数据出错:" + e.getMessage());
			logger.error("通过agent方式获取数据出错", e);
			return result;
		} finally {
			JDBCUtil.close(testCon);
		}

		List tsList = Collections.EMPTY_LIST;
		try {
			tsList = manager.getTableSpaceList();
		} catch (Exception e) {
			logger.error("表空间信息获取失败", e);
		}
    replyTime = System.currentTimeMillis() - replyTime;
    if (replyTime <= 0)
      replyTime = 1L;
    result.setResponseTime(replyTime);

		for (int i = 0, size = tsList.size(); i < size; i++) {
			Db2TableSpace ts = (Db2TableSpace) tsList.get(i);
			MonitorResultRow row = new MonitorResultRow(ts.getName(), ts.getName());
			
			row.setIndicator("DB2-TABLESPACE-1", ts.getType());
			row.setIndicator("DB2-TABLESPACE-2", ts.getPageSize());
			row.setIndicator("DB2-TABLESPACE-3", ts.getTotalPages());
			row.setIndicator("DB2-TABLESPACE-4", ts.getUsedPages());
			row.setIndicator("DB2-TABLESPACE-5", ts.getFreePages());
			row.setIndicator("DB2-TABLESPACE-6", ts.getFreeRate());
			row.setIndicator("DB2-TABLESPACE-7", ts.getUsedRate());
			row.setIndicator("DB2-TABLESPACE-8", ts.getPrefetchSize());
			row.setIndicator("DB2-TABLESPACE-9", ts.getExtentSize());
			row.setIndicator("DB2-TABLESPACE-10", ts.getTablespaceState());			
			
			result.addRow(row);
		}
		return result;
	}
}
