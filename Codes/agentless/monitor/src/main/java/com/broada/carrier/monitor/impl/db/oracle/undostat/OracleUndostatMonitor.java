package com.broada.carrier.monitor.impl.db.oracle.undostat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * Oracle 撤销空间监测状态监测
 * 
 * @author ruanjj (ruanjj@broada.com.cn)
 * Create By 2008-10-10 16:24:03
 */
public class OracleUndostatMonitor implements Monitor{
	private static final int DEFAULT_INTERVAL = 600;
	private static final Logger logger = LoggerFactory.getLogger(OracleUndostatMonitor.class);
  private static final String UNDOSTAT_ONSPACEERRCNT_DATA = "ORACLE-UNDOSTAT-1";
  
  private static final String UNDOSTAT_SSOLDERRCNT_DATA = "ORACLE-UNDOSTAT-2";
  
  @Override
	public MonitorResult monitor(MonitorContext context) {
  	Date lastRunTime = context.getRecord() == null ? null : context.getRecord().getTime();
  	int interval = context.getPolicy() == null ? DEFAULT_INTERVAL : context.getPolicy().getInterval();
  	return collect(new CollectContext(context), lastRunTime, interval);
	}
  
  @Override
	public Serializable collect(CollectContext context) {
		return collect(new CollectContext(context), new Date(System.currentTimeMillis() - DEFAULT_INTERVAL * 1000l),
				DEFAULT_INTERVAL);
  }

	private MonitorResult collect(CollectContext context, Date lastRunTime, int interval) {
    MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
    OracleMethod option = new OracleMethod(context.getMethod());
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    int []values = new int[]{0,0};
    try {
      long replyTime = System.currentTimeMillis();
      om.initConnection();
      values = om.getUndoStat(getTime(lastRunTime, interval));
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (Exception e) {
      logger.debug("查询数据库失败:" +e.getMessage(), e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(e.getMessage());
      return result;
    } finally {
      om.close();
    }
    result.addPerfResult(new PerfResult(UNDOSTAT_ONSPACEERRCNT_DATA, values[0]));
    result.addPerfResult(new PerfResult(UNDOSTAT_SSOLDERRCNT_DATA, values[1]));
    return result;
  }
  
//根据最后一次监测时间点减去监测周期，获得一个时间点字符串
  public String getTime(Date lastRunTime,int interval){
    String dateString = "";
    GregorianCalendar ca = new GregorianCalendar();
    GregorianCalendar ca1 = new GregorianCalendar();
    ca.setTime(lastRunTime);
    long seconds = ca.getTimeInMillis()/1000-interval;
    ca1.setTimeInMillis(seconds*1000);
    Date date = ca1.getTime();
    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateString = sdFormat.format(date);
    return dateString;
  }
}
