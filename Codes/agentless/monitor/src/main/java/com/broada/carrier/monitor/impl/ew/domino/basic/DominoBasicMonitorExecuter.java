package com.broada.carrier.monitor.impl.ew.domino.basic;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.domino.DominoMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import lotus.domino.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Properties;

public class DominoBasicMonitorExecuter extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(DominoBasicMonitorExecuter.class);

	private static final String ITEMIDX_MEMUSED = "DOMINO_BASIC-1";

	private static final String ITEMIDX_DBUSEDPCT = "DOMINO_BASIC-2";

	private static final String ITEMIDX_MAILWAITSEND = "DOMINO_BASIC-3";

	private static final String ITEMIDX_MAILDEAD = "DOMINO_BASIC-4";
	private static String STATREP_SYSTEM = "1. Statistics Reports \\ System";
	private static String STATREP_MAIL = "1. Statistics Reports \\ Mail & Database";
	private static String STATREP_SYSTEM_5 = "1. Statistics Reports \\ System";
	private static String STATREP_MAIL_5 = "1. Statistics Reports \\ Mail & Database";
	private static String STATREP_SYSTEM_6 = "Statistics Reports \\ System";
	private static String STATREP_MAIL_6 = "Statistics Reports \\ Mail & Database";
	private static String STATREP_SYSTEM_7 = "Statistics Reports \\ System";
	private static String STATREP_MAIL_7 = "Statistics Reports \\ Mail & Database";

	static {
		try {
			Properties p = new Properties();
			p.load(DominoBasicMonitorExecuter.class.getResourceAsStream("srvmonitor_dominobasic.properties"));
			STATREP_SYSTEM_5 = p.getProperty("STATREP_SYSTEM_5");
			STATREP_MAIL_5 = p.getProperty("STATREP_MAIL_5");
			STATREP_SYSTEM_6 = p.getProperty("STATREP_SYSTEM_6");
			STATREP_MAIL_6 = p.getProperty("STATREP_MAIL_6");
			STATREP_SYSTEM_7 = p.getProperty("STATREP_SYSTEM_7", "Statistics Reports \\ System");
			STATREP_MAIL_7 = p.getProperty("STATREP_MAIL_7", "Statistics Reports \\ Mail & Database");
		} catch (Exception e) {
			System.err.println("错误加载文件Srvmonitor_dominobasic.properties,该配置文件将适用R5的标准视图名称");
			e.printStackTrace();
		}
	}

	String ip = "";
	String user = null;
	String passwd = null;
	Session sess = null;
	Database statrepDB = null;
	Database monDB = null;
	String monDBName = null;

	@Override public Serializable collect(CollectContext context) {

		PerfResult perfMemUsed = new PerfResult(ITEMIDX_MEMUSED, false);
		PerfResult perfDBUsedPct = new PerfResult(ITEMIDX_DBUSEDPCT, false);
		PerfResult perfMailWaitSend = new PerfResult(ITEMIDX_MAILWAITSEND, false);
		PerfResult perfMailDead = new PerfResult(ITEMIDX_MAILDEAD, false);
		StringBuilder msg = new StringBuilder();
		StringBuilder currVal = new StringBuilder();
		MonitorState state = MonitorConstant.MONITORSTATE_NICER;

		MonitorResult result = new MonitorResult();
		result.setResponseTime(0);
		ip = context.getNode().getIp();
		DominoMonitorMethodOption options = new DominoMonitorMethodOption(context.getMethod());

		String user1 = options.getUsername();
		if (user1 == null)
			user1 = "";
		String passwd1 = options.getPassword();
		if (passwd1 == null)
			passwd1 = "";
		int iorPort = options.getPort();
		int versionId = getVersionId(options);

		switch (versionId) {
		case 5:
			STATREP_SYSTEM = STATREP_SYSTEM_5;
			STATREP_MAIL = STATREP_MAIL_5;
			break;
		case 6:
			STATREP_SYSTEM = STATREP_SYSTEM_6;
			STATREP_MAIL = STATREP_MAIL_6;
			break;
		case 7:
		default:
			STATREP_SYSTEM = STATREP_SYSTEM_7;
			STATREP_MAIL = STATREP_MAIL_7;
		}
		long time = System.currentTimeMillis();
		try {
			chkSession(user1, passwd1, iorPort);
			long replyTime = System.currentTimeMillis() - time;
			if (replyTime <= 0) {
				replyTime = 1;
			}
			result.setResponseTime((int) replyTime);
		} catch (Exception ex) {
			logger.warn("Domino监测失败，错误消息：" + ex.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Domino监测失败，错误堆栈：", ex);
			msg.append("无法连接到Domino服务器,请检查是否正确的配置的IIOP连接！");
			currVal.append("无法连接到Domino服务器！");
			state = MonitorConstant.MONITORSTATE_FAILING;
			result.setResultDesc(msg.toString());
			result.setState(state);
			return result;
		}

		// statrep.nsf
		View v1 = null;
		View v2 = null;
		try {
			chkStaterepDB();
		} catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("无法访问到Domino的统计和事件数据库Statrep.nsf", ex);
			}
			msg.append("无法访问到Domino的统计和事件数据库Statrep.nsf！\n");
			currVal.append("无法访问到Domino的统计和事件数据库Statrep.nsf！\n");
			state = MonitorConstant.MONITORSTATE_FAILING;
		}
		try {
			if (statrepDB != null) {
				// mem
				v1 = statrepDB.getView(STATREP_SYSTEM);
				if (v1 == null) {
					msg.append("无法获取到staterep.nsf数据库的服务器统计视图").append(STATREP_SYSTEM).append(",有可能是版本不对.");
					currVal.append("无法获取到staterep.nsf数据库的服务器统计视图").append(STATREP_SYSTEM).append(".");
				} else {
					Document d1 = v1.getFirstDocument();
					if (d1 == null) {
						msg.append("无法获取服务器内存占用,staterep.nsf数据库的统计视图").append(STATREP_SYSTEM).append("中没有文档.\n");
						currVal.append("staterep.nsf数据库的统计视图").append(STATREP_SYSTEM).append("中没有文档.\n");
					} else {
						int memUsed = d1.getItemValueInteger("Mem.Allocated");
						if (memUsed > 0) {
							perfMemUsed.setValue(memUsed / 1024 / 1024);
						}
					}
				}
				// mail
				v2 = statrepDB.getView(STATREP_MAIL);
				if (v2 == null) {
					msg.append("无法获取到staterep.nsf数据库的邮件统计视图").append(STATREP_MAIL).append(",有可能是版本不对.");
					currVal.append("无法获取到staterep.nsf数据库的邮件统计视图").append(STATREP_MAIL).append(".");
				} else {
					Document d2 = v2.getFirstDocument();
					if (d2 == null) {
						msg.append("staterep.nsf数据库的统计视图").append(STATREP_MAIL).append("中没有文档.");
						currVal.append("staterep.nsf数据库的统计视图").append(STATREP_MAIL).append("中没有文档.");
					} else {
						int mailWaitSend = d2.getItemValueInteger("MAIL.WaitingRecipients");
						perfMailWaitSend.setValue(mailWaitSend);
						int mailDead = d2.getItemValueInteger("MAIL.Dead");
						perfMailDead.setValue(mailDead);
					}
				}
			}
		} catch (Exception ex) {
			msg.append("访问Domino的统计和事件数据库Statrep.nsf的统计信息出错!");
			currVal.append("访问Domino的统计和事件数据库Statrep.nsf的统计信息出错!");
			state = MonitorConstant.MONITORSTATE_FAILING;
		} finally {
			recycleView(v1);
			recycleView(v2);
		}

		//custom nsf
		try {
			DominoParam param = context.getParameterObject(DominoParam.class);
			chkMonDB(param.getDbName());
			if (monDB != null) {
				double pct = monDB.getPercentUsed();
				perfDBUsedPct.setValue(pct);
			}
			long replyTime = System.currentTimeMillis() - time;
			if (replyTime <= 0) {
				replyTime = 1;
			}
			result.setResponseTime((int) replyTime);
		} catch (Exception ex2) {
			state = MonitorConstant.MONITORSTATE_OVERSTEP;
		}
		result.setPerfResults(new PerfResult[] { perfMemUsed, perfDBUsedPct, perfMailWaitSend, perfMailDead });
		if (state == MonitorConstant.MONITORSTATE_NICER) {
			result.setResultDesc("监测一切正常");
		} else {
			result.setResultDesc(msg.toString());
		}
		result.setState(state);
		return result;
	}

	private void chkSession(String user1, String passwd1, int iorPort) throws Exception {
		if (!user1.equals(user) || !passwd1.equals(passwd)) {
			recycleAll();
			try {
				sess = NotesFactory.createSession(ip, user1, passwd1);
			} catch (Exception ex) {
				logger.debug("通过常规途径访问DOMINO失败,IP=" + ip + ",user=" + user1, ex);
				String ior = DominoUtil.getIOR(ip,
						iorPort);// "IOR:0101ea002900000049444c3a6c6f7475732f646f6d696e6f2f636f7262612f494f626a6563745365727665723a312e3000000000010000000000000070000000010101000d00000031302e3133362e3231322e310000acf6310000000438353235363531612d656336382d313036632d656565302d303037653264323233336235004c6f7475734e4f490100010000000100000001000000140000000101ea0001000105000000000001010000000000";
				sess = NotesFactory.createSessionWithIOR(ior, user1, new String(passwd1));
			}
			user = user1;
			passwd = passwd1;
		}
	}

	private void chkStaterepDB() throws Exception {
		// 检查是否要重新构造统计statrepdb
		if (sess != null && statrepDB == null) {
			statrepDB = sess.getDatabase(sess.getServerName(), "statrep.nsf");
		}
	}

	private void chkMonDB(String monDBName1) throws Exception {
		// 名称为"",清除monDB
		if ("".equals(monDBName1)) {
			try {
				monDB.recycle();
			} catch (Exception ex) {
			}
			monDB = null;
			return;
		}
		// 检查是否要重新构造用户指定的monDb
		if (sess != null && monDB == null) {
			monDB = sess.getDatabase(sess.getServerName(), monDBName1);
			monDBName = monDBName1;
		} else if (sess != null && monDB != null && !monDBName1.equals(monDBName)) {
			try {
				monDB.recycle();
			} catch (NotesException ex) {
			}
			monDB = null;
			monDB = sess.getDatabase(sess.getServerName(), monDBName1);
			monDBName = monDBName1;
		}
	}

	/**
	 * 回收所有的Domino对象
	 */
	protected void recycleAll() {
		if (statrepDB != null) {
			try {
				statrepDB.recycle();
			} catch (Exception ex1) {
			}
			statrepDB = null;
		}
		if (monDB != null) {
			try {
				monDB.recycle();
			} catch (Exception ex2) {
			}
			monDB = null;
		}
		if (sess != null) {
			try {
				sess.recycle();
			} catch (Exception ex3) {
			}
			sess = null;
		}
	}

	private void recycleView(View v) {
		if (v != null) {
			try {
				v.recycle();
			} catch (Exception ex) {
			}
		}

	}

	// 回收远程note的资源
	protected void finalize() throws Throwable {
		recycleAll();
		super.finalize();
	}

	/**
	 * 得到版本号
	 *
	 * @return
	 */
	public int getVersionId(DominoMonitorMethodOption option) {
		String ver = getVersion(option);
		int verId = 5;
		if (ver.equals("R5"))
			verId = 5;
		else if (ver.equals("R6"))
			verId = 6;
		else if (ver.equals("R7"))
			verId = 7;
		return verId;
	}

	public String getVersion(DominoMonitorMethodOption option) {
		String version = option.getVersion();
		return version == null ? "R5" : version;
	}
}
