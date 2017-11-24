package com.broada.carrier.monitor.impl.stdsvc.pop3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * POP3 服务监听器实现类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class POP3Monitor implements Monitor {
	private static final String ITEMKDX_REPLYTIME = "POP3-1";// 响应时间

	private static final Log logger = LogFactory.getLog(POP3Monitor.class);

	public POP3Monitor() {
	}

	/**
	 * 实现SMTP监测
	 *
	 * 暂时使用Socket进行端口的连接测试
	 * 然后根据SMTP协议，读取返回的第一行，看是否状态是220
	 *
	 * 返回结果包括监测参数，端口和结果信息等
	 *
	 * @param srv
	 * @return
	 */
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		PerfResult reqReplyTime = new PerfResult(ITEMKDX_REPLYTIME, false);

		String param = context.getTask().getParameter();
		String ip = context.getNode().getIp();
		POP3Parameter p = new POP3Parameter(param);
		int port = p.getPort();
		int timeout = p.getTimeout();

		result.setState(MonitorConstant.MONITORSTATE_FAILING);

		POP3Client pop3 = new POP3Client(context.getTask());
		long time = System.currentTimeMillis();
		double replyTime = 0;//响应时间
		try {
			pop3.connect(ip, port, timeout);
			long repTime = (System.currentTimeMillis() - time) / 1000;
			replyTime = (double) repTime;
			result.setResponseTime(repTime);
		} catch (IOException ex) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("不能连接到目标POP3服务.");
			pop3.close();
			return result;
		}

		StringBuffer msg = new StringBuffer(); //监测结果信息描述
		StringBuffer currVal = new StringBuffer(); //当前情况描述字符串
		//    msg.append("成功连接到服务器.\n");
		//    currVal.append("服务运行.");
		boolean wonted = true;
		if (p.isChkMailCount() || p.isChkBoxSize() || p.isChkReplyTime()) {
			reqReplyTime.setValue(replyTime);
			if (p.isChkReplyTime()) {
				if (replyTime <= p.getReplyTime()) {
					wonted = wonted && true;
					msg.append("响应时间" + replyTime + "秒.\n");
				} else {
					wonted = wonted && false;
					msg.append("响应时间" + replyTime + "秒 > " + p.getReplyTime() + "秒.\n");
				}
			}
			if (p.getUser() != null && p.getUser().length() != 0 && !"".equals(p.getUser())) {
				try {
					pop3.login(p.getUser(), p.getPassword());
					pop3.doList();
				} catch (IOException ex1) {
					wonted = false;
					msg.append("用户\"" + p.getUser() + "\"登录失败.\n");
					currVal.append("用户登录失败.");
				}
				if (p.isChkMailCount()) {
					wonted = wonted && (pop3.getMailCount() < p.getMailCount());
					msg.append("邮箱中邮件数=" + pop3.getMailCount() + ".\n");
					currVal.append("邮件数=" + pop3.getMailCount() + ".");
				}
				if (p.isChkBoxSize()) {
					wonted = wonted && (pop3.getBoxSize() < p.getBoxSize());
					msg.append("当前邮箱使用量=" + pop3.getBoxSize() + "Kb(千字节).\n");
					currVal.append("使用量=" + pop3.getBoxSize() + "Kb.");
				}
			}
		}
		time = System.currentTimeMillis() - time;
		if (wonted) {
			result.setState(MonitorConstant.MONITORSTATE_NICER);
			result.setResultDesc(msg.toString());
		} else {
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			result.setResultDesc(msg.toString());
		}

		pop3.close();
		result.addPerfResult(reqReplyTime);
		return result;
	}

	/**
	 * 实现简单的POP3协议
	 * <p>Title: </p>
	 * <p>Description: NMS Group</p>
	 * <p>Copyright: Copyright (c) 2002</p>
	 * <p>Company: Broada</p>
	 * @author Maico Pang
	 * @version 1.0
	 */
	private class POP3Client {
		private Socket c;

		private BufferedReader reader;

		private PrintWriter writer;

		private int mailCount = 0;

		private double boxSize = 0; //单位 Kb(千字节)

		private boolean login = false; //是否登录

		private MonitorTask srv;

		private boolean isLog = true;

		public POP3Client(MonitorTask srv) {
			this.srv = srv;
		}

		/**
		 * 连接POP3服务
		 *
		 * 连接一定要收到一行回应，而且必须以“+OK”开头才算是POP3服务
		 * @param host
		 * @param port
		 * @param timeout
		 * @throws IOException
		 */
		public void connect(String host, int port, int timeout) throws IOException {
			if (c != null) {
				throw new IOException("Connect Already Exist");
			}
			c = new Socket();
			c.connect(new InetSocketAddress(host, port), timeout);
			c.setSoTimeout(timeout);
			reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(c.getOutputStream()));
			String str = reader.readLine();
			if (isLog) {
				if (logger.isDebugEnabled()) {
					logger.debug(composeLog(srv, "回应:" + str));
				}
			}
			if (str == null || (!str.startsWith("+OK"))) {
				close();
				throw new IOException("Protocol Error");
			}
		}

		/**
		 * 登录
		 * @param user
		 * @param password
		 * @throws IOException
		 */
		public void login(String user, String password) throws IOException {
			if (reader == null || writer == null) {
				throw new IOException("Not Connect");
			}
			String rcv = "";
			//      writer.println("USER " + user);
			//      writer.flush();
			writer.print("USER " + user);
			writer.print("\r\n");
			writer.flush();
			rcv = reader.readLine();
			if (logger.isDebugEnabled()) {
				logger.debug(composeLog(srv, "用户回应:" + rcv));
			}
			if (rcv == null || (!rcv.startsWith("+OK"))) {
				throw new IOException("Login Error");
			}
			//      writer.println("PASS " + password);
			//      writer.flush();
			writer.print("PASS " + password);
			writer.print("\r\n");
			writer.flush();
			rcv = reader.readLine();
			if (logger.isDebugEnabled()) {
				logger.debug(composeLog(srv, "密码回应:" + rcv));
			}
			if (rcv == null || (!rcv.startsWith("+OK"))) {
				throw new IOException("Password Incorrect");
			}
			login = true;
		}

		/**
		 * 执行
		 * @throws IOException
		 */
		public void doList() throws IOException {
			if (reader == null || writer == null) {
				throw new IOException("Not Connect");
			}
			if (!login) {
				throw new IOException("Not Login");
			}
			String rcv = "";
			writer.println("STAT");
			writer.flush();
			rcv = reader.readLine();
			if (logger.isDebugEnabled()) {
				logger.debug(composeLog(srv, "列表回应:" + rcv));
			}
			if (rcv == null || (!rcv.startsWith("+OK"))) {
				throw new IOException("Protocol Error");
			}
			String[] tokent = rcv.split(" ");
			if (tokent.length < 3) {
				throw new IOException("Protocol Error");
			}
			try {
				mailCount = Integer.parseInt(tokent[1]);
				boxSize = Double.parseDouble(tokent[2]) / 1024;
				boxSize = new BigDecimal(boxSize).setScale(3, 4).doubleValue();
			} catch (NumberFormatException ex) {
				throw new IOException("Protocol Error");
			}
		}

		public int getMailCount() {
			return mailCount;
		}

		public double getBoxSize() {
			return boxSize;
		}

		/**
		 * 退出关闭资源
		 */
		public void close() {
			try {
				if (writer != null) {
					writer.println("QUIT");
					writer.flush();
					writer.close();
					writer = null;
				}
			} catch (Exception ex) {
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex1) {
				}
				reader = null;
			}
			if (c != null) {
				try {
					c.close();
				} catch (IOException ex2) {
				}
				c = null;
			}
		}
	}

	public String composeLog(MonitorTask srv, String message) {
		return srv + message;
	}

	@Override
	public Serializable collect(CollectContext context) {
		return null;
	}
}