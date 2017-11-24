package com.broada.carrier.monitor.impl.db.db2.lockedtable;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.db2.Db2LockManager;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.utils.JDBCUtil;
import com.broada.utils.StringUtil;

public class Db2LockedTableMonitor extends BaseMonitor {
	private static final int ITEMIDX_LENGTH = 4;
  private Db2LockManager db2LockManager = null;

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		DB2MonitorMethodOption option = new DB2MonitorMethodOption(context.getMethod());

		// 连接参数的获取
		String ip = context.getNode().getIp();
		long respTime = System.currentTimeMillis();
		Connection testRespCon = null;
		try {
      if (db2LockManager == null)
        db2LockManager = new Db2LockManager(ip, option);
      else
        db2LockManager.setOption(option);
			testRespCon = db2LockManager.getConnection();
		} catch (SQLException lde) {
			if (lde.getErrorCode() == -99999) {// 用户名或密码出错
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
			} else {// 当前先不判断其他的错误代码,看情况以后再增加
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
			}
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			return result;
		} catch (CLIException e) {
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("通过agent方式获取数据出错:" + e.getMessage());
			return result;
		} finally {
			JDBCUtil.close(testRespCon);
		}

		// 获取被锁定表的信息
		List lockTableInfoList = null;
		try {
			lockTableInfoList = db2LockManager.getLockedTablesList();
		} catch (CLIException e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("通过agent方式获取数据出错:" + e.getMessage());
			return result;
		} catch (Exception e) {
			result.setResultDesc("无法获取当前连接锁列表.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} finally {
		}
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);

		for (int i = 0, perfIndex = 0; i < lockTableInfoList.size(); perfIndex = perfIndex + ITEMIDX_LENGTH, i++) {
			Db2LockedTable info = (Db2LockedTable) lockTableInfoList.get(i);

			// 计算单个表的锁数量
			// if (tableLockMap.containsKey(info.getTableName())) {
			// Integer cnt = (Integer) tableLockMap.get(info.getTableName());
			// tableLockMap.put(info.getTableName(), new Integer(cnt.intValue() + 1));
			// } else {
			// tableLockMap.put(info.getTableName(), new Integer(1));
			// }

			// 创建实例
			MonitorResultRow row = new MonitorResultRow(info.getRowNumber(), info.getTableName());

			// 性能项监测
			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				// 性能值
				String value = (String) getInfoValue(info, j);
				if (!StringUtil.isNullOrBlank(value)) {
					row.setIndicator("DB2-LOCKEDTABLE-JDBC-" + (j + 1), value);
				} 
			}

			result.addRow(row);
		}
		
		return result;
	}

	/**
	 * 根据索引取得属性值
	 * 
	 * @param info
	 * @param colIndex
	 * @return
	 */
	private Object getInfoValue(Db2LockedTable info, int colIndex) {
		switch (colIndex) {
		case 0:
			return info.getTableSchema();
		case 1:
			return info.getTableSpaceName();
		case 2:
			return info.getLockMode();
		case 3:
			return info.getLockStatus();
		default:
			return null;
		}
	}

}