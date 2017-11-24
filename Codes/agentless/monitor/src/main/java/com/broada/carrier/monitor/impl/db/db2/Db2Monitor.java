package com.broada.carrier.monitor.impl.db.db2;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.utils.JDBCUtil;

/**
 * IBM DB2 监听器实现类
 * 
 * @author lixy (lixy@broada.com.cn)
 * Create By 2007-3-22 下午05:11:30
 */
public class Db2Monitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(Db2Monitor.class);
  public static final String DRIVER_CLASS = "com.ibm.db2.jcc.DB2Driver";

  private static final String ITEMCODE_DB_STATUS = "DB2-1"; //数据库状态

  private static final String ITEMCODE_DB_CONN_TIME = "DB2-2"; //数据库激活时间

  private static final String ITEMCODE_APPLS_CUR_CONS = "DB2-3";//当前连接数

  private static final String ITEMCODE_TOTAL_CONS = "DB2-4"; //连接总次数
  
  private static final String ITEMCODE_LAST_BACKUP = "DB2-5"; //最后备份时间

  /**
   * IBM DB2 监听主方法
   */
  @Override
  public Serializable collect(CollectContext context) {
    PerfResult dbStatus = new PerfResult(ITEMCODE_DB_STATUS);
    PerfResult dbConnTime = new PerfResult(ITEMCODE_DB_CONN_TIME);
    PerfResult applsCurCons = new PerfResult(ITEMCODE_APPLS_CUR_CONS);
    PerfResult totalCons = new PerfResult(ITEMCODE_TOTAL_CONS);
    PerfResult lastBackup = new PerfResult(ITEMCODE_LAST_BACKUP);
    //性能数据
    PerfResult[] perfs = { dbStatus, dbConnTime, applsCurCons, totalCons,lastBackup };

    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);   

    Db2Parameter p = Db2Parameter.decode(context.getParameter());
    DB2MonitorMethodOption option = new DB2MonitorMethodOption(context.getMethod());

    //连接参数的获取
    String ip = context.getNode().getIp();
    int port = option.getPort();
    String sid = option.getDb();
    String user = option.getUsername();
    String pass = option.getPassword();

    StringBuffer msg = new StringBuffer(); // 监测结果信息描述
    
    // 建立连接
    Connection conn = null;
    int errCode = 0; // 0表示正常
    // 开始监测
    Exception exception = null;
    long now=System.currentTimeMillis();
    boolean wonted = true;
    try {
      try {
        if (option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
          conn = new Db2Manager(ip, option).getConnection();
        } else {
          //连接测试
          int testRT = testConnect(ip, port);
          if (testRT>0) {
            wonted = true;
          } else {
            result.setState(MonitorConstant.MONITORSTATE_FAILING);
            result.setResultDesc("无法连接到" + port + "端口,可能监听器没有打开。");
            return result;
          }
          // 监测数据库的其他情况
          if (sid == null || sid.length() == 0) {
            sid = "orcl";
          }
          String url = "jdbc:db2://" + ip + ":" + port + "/" + sid;
          conn = JDBCUtil.createConnection(DRIVER_CLASS, url, user, pass);
        }
      } catch (ClassNotFoundException ex) {
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        msg.append("系统内部错误:" + DRIVER_CLASS + "没有发现。");
        result.setResultDesc(msg.toString());
        return result;
      } catch (SQLException ex) {
        errCode = ex.getErrorCode();
        exception = ex;
        JDBCUtil.close(conn);
      } catch (CLIException e) {
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        msg.append("通过agent方式获取数据出错:"+e.getMessage());
        result.setResultDesc(msg.toString());
        return result;
      }
      //连接时间计算
      long linkTime=System.currentTimeMillis()-now;
      if(linkTime<=0){
        linkTime=1;
      }

      // 根据错误号来判断错误类型
      // -4499数据库url错误
      // -99999 用户名或密码错误

      if (errCode == -4499) {
        msg.delete(0, msg.length());
        msg.append("数据库url错误,可能指定的端口不是DB2监听端口或数据库实例名错误。\n");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        result.setResultDesc(msg.toString());
        return result;
      } else if (errCode != 0 && errCode != -99999) {
        if (logger.isDebugEnabled()) {
          logger.debug("数据库可能没有装载或打开", exception);
        }
        msg.append("数据库可能没有装载或打开。\n");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        result.setResultDesc(msg.toString());
        return result;
      } else if (errCode == -99999){
      	result.setResponseTime((int)linkTime);
        msg.append("用户名/密码错误,登录失败。");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        result.setResultDesc(msg.toString());
        return result;
      } else if (exception != null){
      	if (logger.isDebugEnabled()) {
          logger.debug("创建连接失败.", exception);
        }
        msg.append(exception.getMessage() + "\n");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        result.setResultDesc(msg.toString());
        return result;
      } else if (conn == null && option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.JDBC4DB2MONITORMETHOD)) {
        msg.append("创建连接失败。");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        result.setResultDesc(msg.toString());
        return result;
      }

      // 登陆失败
      if (errCode != 0) {
        msg.append("用户登录失败。\n");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        result.setResultDesc(msg.toString());
        return result;
      }

      // 检查连接地址
      if (p.isChkConAddrs()) {
        StringBuffer sbDetail = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        String conAddrs = p.getConAddrs();
        if (!checkLogonAddrs(conn, conAddrs, sb, sbDetail, option, sid, context.getNode().getIp())) {
          wonted = false;
          if (sbDetail.indexOf("发生了错误") > 0) {
            msg.append(sbDetail + "\n");
            result.setResultDesc(msg.toString());
          } else {
            msg.append(sbDetail);
          }
        }
      }
      long replyTime = System.currentTimeMillis() - now;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
      Db2Manager om = new Db2Manager(context.getNode().getIp(), option);
      Map<String, Object> db2Base=null;
      try {
        db2Base = om.getDb2BaseInfo(conn);
        replyTime = System.currentTimeMillis() - now;
        if (replyTime <= 0)
          replyTime = 1L;
        result.setResponseTime(replyTime);
        for (int i = 0; i < perfs.length; i++) {
          String value = (db2Base.size() > i) ? String.valueOf(db2Base.get(Db2ParamConfiger.keys[i])).trim() : "null";
          if (!("null".equalsIgnoreCase(value) || value.trim().length() < 1)) {
            if (Db2ParamConfiger.isStringType[i]) {
              perfs[i].setStrValue(value);
            } else {
              perfs[i].setValue(Double.parseDouble(value));
            }
          } 
        }
      } catch (SQLException e) {
        logger.error("连接数获取失败.", e);
        wonted = false;
        msg.append(e.getMessage()+"\n");
      } catch (CLIException e) {
        logger.error("通过agent方式获取连接数失败.", e);
        wonted = false;
        msg.append(e.getMessage()+"\n");
      }

      if (wonted) {
        result.setState(MonitorConstant.MONITORSTATE_NICER);
      } else {
        result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
        result.setResultDesc(msg.toString());
      }
    } finally {
      JDBCUtil.close(conn);
    }
    result.setPerfResults(perfs);
    return result;
  }

  /**
   * 使用Socket测试连接指定端口,返回响应时间
   *
   * @param ip
   * @param port
   * @return 响应时间，单位毫秒，如果为小于或等于0表示无法链接，
   */
  private int testConnect(String ip, int port) {
    Socket socket = null;
    try {
      // 不能直接调用，应该先new，然后再连接
      socket = new Socket();
      SocketAddress sa = new InetSocketAddress(ip, port);
      long now=System.currentTimeMillis();
      socket.connect(sa, 15000);
      now=System.currentTimeMillis()-now;
      if(now<=0){
        return 1;
      }
      return (int)now;
    } catch (IOException ex) {
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException ex1) {
        }
      }
    }
    return -1;
  }

  /**
   * 检查连接地址
   *
   * @param conn
   * @param addrs
   * @param sb
   * @return
   */
  private boolean checkLogonAddrs(Connection conn, String addrs, StringBuffer sb, StringBuffer sbDetail, DB2MonitorMethodOption option, String sid, String nodeIp) {
    // 把地址转化成InetAddress
    String[] aa = addrs == null ? new String[0] : addrs.split(";");
    List al = Arrays.asList(aa);
    Set as = new HashSet();
    for (Iterator iter = al.iterator(); iter.hasNext();) {
      String item = (String) iter.next();
      if (item != null && !item.equals("")) {
        as.add(item.toLowerCase().trim());
      }
    }
    
    Statement st = null;
    ResultSet rs = null;
    boolean r = true;
    String sql= "SELECT distinct substr(appl.INBOUND_COMM_ADDRESS,1,posstr(appl.INBOUND_COMM_ADDRESS,' ')) ip,appl_info.auth_id FROM TABLE(SNAPSHOT_APPL_INFO('"+sid+"',-1)) as appl_info,TABLE(SNAPSHOT_APPL('"+sid+"', -1)) AS appl  where  appl.INBOUND_COMM_ADDRESS != '' and appl.INBOUND_COMM_ADDRESS not like '%LOCAL.DB2' and appl_info.agent_id = appl.agent_id";
    int excepConnectionCnt = 0;
    try {
      if (option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
        List back = new DB2AgentExecutor(nodeIp).execute(nodeIp, option, CLIConstant.DB2AGENT,
            new String[] { option.getUsername(), option.getDb(), sql });
        if (back != null && back.size() > 0) {
          for (int i = 0; i < back.size(); i++) {
            Map map = (Map) back.get(i);
            boolean result = false;
            String ip = map.get("IP").toString();
            String uname = map.get("AUTH_ID").toString();
            uname = (uname == null ? "" : uname.trim());
            ip = (ip == null ? "" : ip.trim().toLowerCase());
            // 用户名匹配并且主机名为本机,则认为连接为测试连接
            if (ip != null && !ip.equals("")) {
              result = as.contains(ip);
              if (!result) {
                result = as.contains(uname);
              }
            }
            if (!result) {
              if (excepConnectionCnt == 0)
                sbDetail.append("非法连接:\n");
              r = result;
              sbDetail.append(ip).append("(").append(uname).append(")");
              sbDetail.append(",\n");
              excepConnectionCnt++;
              // sbDetail.append("用户[").append(uname).append("]在非法主机[").append(ip).append("]登陆。\n");
            } else {
              sbDetail.append("用户[").append(uname).append("]在合法主机[").append(ip).append("]登陆。\n");
            }
          }
        }
      } else {
        st = conn.createStatement();
        rs = st.executeQuery(sql);
        while (rs.next()) {
          boolean result = false;
          String ip = rs.getString("IP");
          String uname = rs.getString("auth_id");
          uname = (uname == null ? "" : uname.trim());
          ip = (ip == null ? "" : ip.trim().toLowerCase());
          // 用户名匹配并且主机名为本机,则认为连接为测试连接
          if (ip != null && !ip.equals("")) {
            result = as.contains(ip);
            if (!result) {
              result = as.contains(uname);
            }
          }
          if (!result) {
            if (excepConnectionCnt == 0)
              sbDetail.append("非法连接:\n");
            r = result;
            sbDetail.append(ip).append("(").append(uname).append(")");
            sbDetail.append(",\n");
            excepConnectionCnt++;
            // sbDetail.append("用户[").append(uname).append("]在非法主机[").append(ip).append("]登陆。\n");
          } else {
            sbDetail.append("用户[").append(uname).append("]在合法主机[").append(ip).append("]登陆。\n");
          }
        }
      }
        return r;
    } catch (Exception ex) {
      logger.error("检测合法连接时发生错误:"+ex.getMessage(), ex);
      r = false;
      System.out.println("error =="+ex.getMessage());
      sbDetail.append("监测\"数据库非法连接\"过程中发生了错误。\n");
      return r;
    } finally {
      JDBCUtil.close(rs, st);
    }
  }
}
