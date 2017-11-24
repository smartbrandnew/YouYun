package com.broada.carrier.monitor.impl.db.sybase;

import com.broada.carrier.monitor.impl.db.sybase.segment.PerfItemMap;
import com.broada.carrier.monitor.impl.db.sybase.segment.SybaseSegment;
import com.broada.carrier.monitor.impl.db.sybase.session.SybaseSession;
import com.broada.carrier.monitor.impl.db.sybase.transaction.SybaseTransaction;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.utils.JDBCUtil;
import com.broada.utils.StringUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SybaseEnhanceManager extends SybaseManager {
//  private static final String SYBASE_DB_SEGMENT = "select T1.name, T1.totalSize, "
//      + "T2.seg_data, T2.seg_index, T2.seg_unused from  (SELECT S.name, ROUND(SUM(U.size * 2 / 1024.576), 2) totalSize "
//      + "FROM  master.dbo.sysdevices D, master.dbo.sysusages  U, syssegments S WHERE  U.vstart BETWEEN D.low AND D.high "
//      + "AND U.dbid = DB_ID('master') AND D.cntrltype =  0 AND (POWER(2, S.segment) &U.segmap) != 0 group by S.name) T1, "
//      + "(SELECT S.name,  SUM(ROUND(CONVERT(numeric(20, 9), DATA_PGS(I.id, doampg)) * (2048 / 1048576.0),  2)) seg_data, "
//      + "SUM(ROUND(CONVERT(numeric(20, 9), DATA_PGS(I.id, ioampg)) * (2048  / 1048576.0), 2)) seg_index, SUM(ROUND(CONVERT(numeric(20, 9),  "
//      + "((RESERVED_PGS(I.id, doampg) + RESERVED_PGS(I.id, ioampg)) - (DATA_PGS(I.id,  doampg) + DATA_PGS(I.id, ioampg)))) * (2048 / 1048576.0), 2)) "
//      + "seg_unused FROM  syssegments S, sysindexes I WHERE I.segment = S.segment group by S.name) T2  where T1.name = T2.name";
  
  private static final String SYBASE_DB_SEGMENT = 
    " SELECT distinct S1.name ," +
    "            ( SELECT ROUND(SUM(U.size * 2 / 1024.576), 2) " +
    "                   FROM  master.dbo.sysdevices D, master.dbo.sysusages  U, syssegments S " +
    "                   WHERE  U.vstart BETWEEN D.low AND D.high AND U.dbid = DB_ID('master') AND D.cntrltype =  0 AND (POWER(2, S.segment) &U.segmap) != 0 " +
    "                   and S.name=S1.name group by S.name" +
    "      )    totalsize ," +
    "      ( SELECT SUM(ROUND(CONVERT(numeric(20, 9), DATA_PGS(I.id, doampg)) * (2048 / 1048576.0),  2))  " +
    "               FROM  syssegments S, sysindexes I " +
    "               WHERE I.segment = S.segment and S.name = S1.name group by S.name " +
    "      ) seg_data," +
    "     ( SELECT SUM(ROUND(CONVERT(numeric(20, 9), DATA_PGS(I.id, ioampg)) * (2048  / 1048576.0), 2))" +
    "               FROM  syssegments S, sysindexes I " +
    "               WHERE I.segment = S.segment and S.name = S1.name group by S.name " +
    "     ) seg_index," +
    "     ( SELECT SUM(ROUND(CONVERT(numeric(20, 9),((RESERVED_PGS(I.id, doampg) + RESERVED_PGS(I.id, ioampg)) - (DATA_PGS(I.id,  doampg) + DATA_PGS(I.id, ioampg)))) * (2048 / 1048576.0), 2))" +
    "               FROM  syssegments S, sysindexes I " +
    "               WHERE I.segment = S.segment and S.name = S1.name group by S.name " +
    "     ) seg_unused" +
    " FROM  syssegments S1  " +
    " WHERE exists ( select * from  sysindexes I where I.segment = S1.segment) ";


//  private static final String SYBASE_DB_SESSION = " SELECT spid 'pid', status 'status', SUSER_NAME(suid) 'user', (CASE   clienthostname WHEN '' THEN "
//      + "hostname WHEN NULL THEN hostname ELSE   clienthostname END)+'('+ipaddr+')' 'host', CASE clientapplname WHEN '' THEN   program_name WHEN NULL THEN "
//      + "program_name ELSE clientapplname END 'program',memusage 'memusage', cpu 'cputime', physical_io 'io', DB_NAME(dbid) 'database',   cmd 'command' "
//      + "FROM master.dbo.sysprocesses ORDER by 2";
  
  private static final String SYBASE_DB_SESSION = 
    " SELECT spid 'pid', status 'status'," + 
    "        SUSER_NAME(suid) 'user', " +
    "        hostname 'host', "+
    "        program_name 'program', "+
    "        memusage 'memusage',  "+
    "        cpu 'cputime', "+
    "        physical_io 'io', "+ 
    "        DB_NAME(dbid) 'database', "+
    "        cmd 'command' "+
    " FROM master.dbo.sysprocesses where suid <> 0  ORDER by 2";

  /**
   * 系统事务交易总数
   */
  private static final String SYBASE_TANSACTION_TOTAL = "select COUNT(*) 'total' "
      + "FROM sysmonitors Where group_name = 'access' and field_name = 'xacts'";

  /**
   * 每秒事务数
   */
  private static final String SYBASE_TANSACTION_SUMPEC = "select (count(*)/120) 'sumpec' "
      + "FROM systransactions where starttime> DateAdd(second,-120,getdate()) ";

  /**
   * 失败的事务数
   */
  private static final String SYBASE_TANSACTION_ABORT = "select (count(*)/120) 'abort' "
      + "FROM systransactions where starttime> DateAdd(second,-120,getdate()) and state in (6, 9, 10)";

  /**
   * 监测的结果信息(用于监测结果详细描述).
   */
  private StringBuffer resultDesc = new StringBuffer();

  /**
   * 当前情况(用于发送告警信息).
   */
  private StringBuffer currentVal = new StringBuffer();

  public SybaseEnhanceManager(String ip, String sid, int port, String user, String passwd) throws Exception {
    super(ip, sid, port, user, passwd);
    initConnection();
  }

  public List getSegments() throws SQLException {
    Statement stDbs = null;
    ResultSet rsDbs = null;
    Connection conn = getConnection();

    if (conn == null)
      return null;

    List<SybaseSegment> retList = null;
    try {
      stDbs = conn.createStatement();
      rsDbs = stDbs.executeQuery(SYBASE_DB_SEGMENT);
      retList = new ArrayList<SybaseSegment>();
      while (rsDbs.next()) {
        SybaseSegment ss = new SybaseSegment();
        ss.setName(rsDbs.getString("name"));
        ss.setTotalSize(rsDbs.getDouble("totalSize"));
        ss.setSegData(rsDbs.getDouble("seg_data"));
        ss.setSegIndex(rsDbs.getDouble("seg_index"));
        ss.setSegUnused(rsDbs.getDouble("seg_unused"));
        retList.add(ss);
      }
    } finally {
      JDBCUtil.close(rsDbs, stDbs);
    }

    return retList;
  }

  public List getSessions() throws SQLException {
    Statement stDbs = null;
    ResultSet rsDbs = null;
    Connection conn = getConnection();

    if (conn == null)
      return null;

    List retList = null;
    try {
      stDbs = conn.createStatement();
      rsDbs = stDbs.executeQuery(SYBASE_DB_SESSION);
      retList = new ArrayList<SybaseSession>();
      while (rsDbs.next()) {
        SybaseSession ss = new SybaseSession();
        ss.setPid(StringUtil.toStringNullReplaced(rsDbs.getString("pid")));
        ss.setStatus(StringUtil.toStringNullReplaced(rsDbs.getString("status")));
        ss.setUser(StringUtil.toStringNullReplaced(rsDbs.getString("user")));
        ss.setHost(StringUtil.toStringNullReplaced(rsDbs.getString("host")));
        ss.setProgram(StringUtil.toStringNullReplaced(rsDbs.getString("program")));
        ss.setDatabase(StringUtil.toStringNullReplaced(rsDbs.getString("database")));
        ss.setCommand(StringUtil.toStringNullReplaced(rsDbs.getString("command")));
        ss.setMemUsage(rsDbs.getDouble("memusage"));
        ss.setCpuTime(rsDbs.getDouble("cputime"));
        ss.setIoNumber(rsDbs.getDouble("io"));

        retList.add(ss);
      }
    } finally {
      JDBCUtil.close(stDbs);
    }

    return retList;
  }

  public List getTransaction() throws SQLException {
    Statement stDbs = null;
    ResultSet rsDbs = null;
    ResultSet rsDbs1 = null;
    ResultSet rsDbs2 = null;
    Connection conn = getConnection();

    if (conn == null)
      return null;

    List retList = null;
    try {
      retList = new ArrayList<SybaseTransaction>();
      stDbs = conn.createStatement();
      rsDbs = stDbs.executeQuery(SYBASE_TANSACTION_TOTAL);
      rsDbs.next();
      SybaseTransaction st1 = new SybaseTransaction(SybaseTransaction.FIELDS[0]);
      st1.setTransactionNumPerSec(rsDbs.getDouble(SybaseTransaction.FIELDS[0]));
      st1.setTransactionType(SybaseTransaction.TRANSACTIONTYPES[0]);
      st1.setThresHold(SybaseTransaction.THRESHOLDS[0]);
      st1.setUnit(SybaseTransaction.UNITS[0]);

      retList.add(st1);
      try {
				stDbs = conn.createStatement();
				rsDbs1 = stDbs.executeQuery(SYBASE_TANSACTION_SUMPEC);
				rsDbs1.next();
				SybaseTransaction st2 = new SybaseTransaction(SybaseTransaction.FIELDS[1]);
				st2.setTransactionNumPerSec(rsDbs1.getDouble(SybaseTransaction.FIELDS[1]));
				st2.setTransactionType(SybaseTransaction.TRANSACTIONTYPES[1]);
				st2.setThresHold(SybaseTransaction.THRESHOLDS[1]);
				st2.setUnit(SybaseTransaction.UNITS[1]);

				retList.add(st2);

				stDbs = conn.createStatement();
				rsDbs2 = stDbs.executeQuery(SYBASE_TANSACTION_ABORT);
				rsDbs2.next();
				SybaseTransaction st3 = new SybaseTransaction(SybaseTransaction.FIELDS[2]);
				st3.setTransactionNumPerSec(rsDbs2.getDouble(SybaseTransaction.FIELDS[2]));
				st3.setTransactionType(SybaseTransaction.TRANSACTIONTYPES[2]);
				st3.setThresHold(SybaseTransaction.THRESHOLDS[2]);
				st3.setUnit(SybaseTransaction.UNITS[2]);
				retList.add(st3);
			} catch (SQLException sql) {
				if (sql.getMessage().indexOf("systransactions not found") == -1) {
					// 如果是sybase12 以下的版本，systransactions对象不存在，此时忽略掉
					throw sql;
				}
			}
    } finally {
      JDBCUtil.close(stDbs);
    }

    return retList;
  }

  /**
   * 装配性能数据.
   * @param nameForIndex
   * @param instanceKey
   * @return
   */
  public static PerfResult[] assemblePerf(List nameForIndex, String instanceKey) {
    List perfResultList = new ArrayList();
    for (Iterator it = nameForIndex.iterator(); it.hasNext();) {
      PerfItemMap perfItemMap = (PerfItemMap) it.next();
      PerfResult pr = new PerfResult(perfItemMap.getCode(), true);
      Object value = perfItemMap.getValue();

      if (value instanceof String) {
        pr.setStrValue((String) value);
      } else if (value instanceof Double) {
        pr.setValue(((Double) value).doubleValue());
      }

      pr.setInstanceKey(instanceKey);

      perfResultList.add(pr);
    }

    return (PerfResult[]) perfResultList.toArray(new PerfResult[0]);
  }

  public StringBuffer getCurrentVal() {
    return currentVal;
  }

  public StringBuffer getResultDesc() {
    return resultDesc;
  }

}
