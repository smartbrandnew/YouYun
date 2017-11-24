package com.broada.carrier.monitor.impl.db.db2.bp;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.db2.DB2ErrorUtil;
import com.broada.carrier.monitor.impl.db.db2.DB2ExtendManager;
import com.broada.carrier.monitor.impl.db.db2.DB2ExtendManagerImpl;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * DB2缓冲池监测器
 * @author 杨帆
 * 
 */
public class Db2BufferPoolMonitor extends BaseMonitor {

  public static final Log logger = LogFactory.getLog(Db2BufferPoolMonitor.class);

  private static final String ITEM_DATA_L_READ = "DB2-BUFFERPOOL-JDBC-1";

  private static final String ITEM_INDEX_L_READ = "DB2-BUFFERPOOL-JDBC-2";

  private static final String ITEM_DATA_P_READ = "DB2-BUFFERPOOL-JDBC-3";

  private static final String ITEM_INDEX_P_READ = "DB2-BUFFERPOOL-JDBC-4";

  private static final String ITEM_DATA_RATIO = "DB2-BUFFERPOOL-JDBC-5";

  private static final String ITEM_INDEX_RATIO = "DB2-BUFFERPOOL-JDBC-6";

  private static final String ITEM_DIRECT_READS = "DB2-BUFFERPOOL-JDBC-7";

  private static final String ITEM_DIRECT_WRITE = "DB2-BUFFERPOOL-JDBC-8";

  private static final String ITEM_CAT_CACHE_RATIO = "DB2-BUFFERPOOL-JDBC-9";

  private static final String ITEM_PKG_CACHE_RATIO = "DB2-BUFFERPOOL-JDBC-10";

  @Override
  public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
    DB2MonitorMethodOption option = new DB2MonitorMethodOption(context.getMethod());
    DB2ExtendManager manager = new DB2ExtendManagerImpl(context.getNode().getIp(), option);
    DbBufferPool pool = null;
    try {
      long replyTime = System.currentTimeMillis();
      pool = manager.getBufferPoolData();
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (Throwable e) {
    	return DB2ErrorUtil.process(e);
    }
        
    PerfResult[] perfs = new PerfResult[10];
    PerfResult perf_data_l_read = new PerfResult(ITEM_DATA_L_READ, pool.getDataLogicReads());
    perfs[0] = perf_data_l_read;
    PerfResult perf_data_p_read = new PerfResult(ITEM_DATA_P_READ, pool.getDataPhysicsReads());
    perfs[2] = perf_data_p_read;
    PerfResult perf_index_l_read = new PerfResult(ITEM_INDEX_L_READ, pool.getIndexLogicReads());
    perfs[1] = perf_index_l_read;
    PerfResult perf_index_p_read = new PerfResult(ITEM_INDEX_P_READ, pool.getIndexPhysicsReads());
    perfs[3] = perf_index_p_read;

    PerfResult perf_data_ratio = new PerfResult(ITEM_DATA_RATIO, pool.getDataRatio());
    perfs[4] = perf_data_ratio;
    PerfResult perf_index_ratio = new PerfResult(ITEM_INDEX_RATIO, pool.getIndexRatio());
    perfs[5] = perf_index_ratio;
    PerfResult perf_index_direct_reads = new PerfResult(ITEM_DIRECT_READS, pool.getDirectReads());
    perfs[6] = perf_index_direct_reads;
    PerfResult perf_index_direct_write = new PerfResult(ITEM_DIRECT_WRITE, pool.getDirectWrites());
    perfs[7] = perf_index_direct_write;
    PerfResult perf_index_cat_ratio = new PerfResult(ITEM_CAT_CACHE_RATIO, pool.getCatCacheRatio());
    perfs[8] = perf_index_cat_ratio;
    PerfResult perf_index_pkg_ratio = new PerfResult(ITEM_PKG_CACHE_RATIO, pool.getPkgCacheRatio());
    perfs[9] = perf_index_pkg_ratio;
    result.setPerfResults(perfs);
    return result;
  }
}
