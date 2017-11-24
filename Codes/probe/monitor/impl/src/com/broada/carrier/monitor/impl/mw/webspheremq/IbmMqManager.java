package com.broada.carrier.monitor.impl.mw.webspheremq;

import com.broada.carrier.monitor.common.pool.PoolConfig;
import com.broada.carrier.monitor.common.pool.PoolFactory;
import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.impl.mw.webspheremq.channel.IbmMqChannel;
import com.broada.carrier.monitor.impl.mw.webspheremq.channel.IbmMqChannelState;
import com.broada.utils.StringUtil;
import com.ibm.mq.*;
import com.ibm.mq.pcf.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * <p>
 * Title: IbmMqManager
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author plx (panlx@broada.com.cn)
 * @version 2.4
 */
public class IbmMqManager {

  private static final Log logger = LogFactory.getLog(IbmMqManager.class);

  private static MQSimpleConnectionManager connMgr = new MQSimpleConnectionManager();

  private MQQueueManager qMgr;

  //mdy by maico 2009-09-22 把PCFMessageAgent修改成成员变量，方便重复利用和控制
  private PCFMessageAgent pcfmessageagent = null;

  private Parameter param;
  private long responseTime;

  private static String CHANNEL_NAME = "SYSTEM.DEF.SVRCONN"; // SYSTEM.DEF.SVRCONN

  private IbmMqManager(Parameter param) {
  	this.param = param;
    MQEnvironment.hostname = param.ip;
    MQEnvironment.port = param.port;
    MQEnvironment.channel = CHANNEL_NAME;
    MQEnvironment.CCSID = param.ccsid;    
  }

  private void chkInit() {
    if (qMgr == null) {
      throw new NullPointerException("队列管理器还没有初始化,请先进行初始化.");
    }
  }

  /**
   * 初始化队列管理器
   * 
   * @throws IbmMqException
   */
  public synchronized void initQmgr() throws IbmMqException {
    if (qMgr != null) {
      return;
    }
    
    responseTime = 0;
    long start = System.currentTimeMillis();
    
    Hashtable hashtable = new Hashtable();
    hashtable.put("MQC.HOST_NAME_PROPERTY", param.ip);
    hashtable.put("MQC.PORT_PROPERTY", String.valueOf(param.port));
    if (qMgr == null) {
      try {
        qMgr = new MQQueueManager("", hashtable, connMgr);
      } catch (MQException mqexception) {
        IbmMqException ibmMqEx = new IbmMqException("构造MQQueueManager失败.");
        ibmMqEx.initCause(mqexception);
        throw ibmMqEx;
      }
      //mdy by maico 2009-09-22 把PCFMessageAgent放在外部定义，保证正常断开连接
      try {
        pcfmessageagent = new PCFMessageAgent(param.ip, param.port, CHANNEL_NAME);
        pcfmessageagent.setCharacterSet(param.ccsid);
      } catch (MQException e) {
        try {
          qMgr.close();
        } catch (MQException e1) {
          logger.debug("关闭MQQueueManager失败.", e1);
        }
        qMgr = null;
        IbmMqException ibmMqEx = new IbmMqException("构造PCFMessageAgent失败。");
        ibmMqEx.initCause(e);
        throw ibmMqEx;
      }
    }
    
    responseTime = System.currentTimeMillis() - start;
  }

  /**
   * mdf by maico 2009-09-22 修改为如果因为意外错误的话重试2次
   * @param s
   * @return
   * @throws IbmMqException
   */
  private MQQueue getQueue(String s) throws IbmMqException {
    return getQueue(s, 2);
  }

  /**
   * add by maico 2009-09-22 修改支持重试
   * @param s
   * @param tryTimes
   * @return
   * @throws IbmMqException
   */
  private MQQueue getQueue(String s, int tryTimes) throws IbmMqException {
    chkInit();
    MQQueue mqqueue = null;
    try {      
      mqqueue = qMgr.accessQueue(s, MQC.MQOO_BROWSE | MQC.MQOO_INQUIRE, null, null, null);    	
    } catch (MQException e) {
      if (e.reasonCode == 2085) {
        /**
         * 當應用程式執行對伺服器遠端讀取 Microsoft Message Queue Server (MSMQ) 儲存體檔案 可能會意外累積在 %windir%\system32\msmq\storage 目錄遠端 MSMQ 伺服器上。 该异常属于正常情况,所以有队列获取不到的情况应属正常.
         */
        if (logger.isInfoEnabled()) {
          logger.info("远程获取队列意外积累在错误的远端伺服器上,该情况属于正常情况,再重试" + tryTimes + "次.", e);
        }
        //myf by maico 2009-09-22 加入重试,重试tryTimes后还抛出同样的异常的话放弃并抛出异常
        if (tryTimes >= 0) {
          tryTimes--;
          return getQueue(s, tryTimes);
        } else {
          IbmMqException ibmMqEx = new IbmMqException("重试之后还无法成功获取队列.");
          ibmMqEx.initCause(e);
          throw ibmMqEx;
        }
      } else {
        //myf by maico 2009-09-22 如果是别的异常，还是需要抛出
        IbmMqException ibmMqEx = new IbmMqException("访问队列时发生MQException异常.");
        ibmMqEx.initCause(e);
        throw ibmMqEx;
      }
    } catch (Exception exception) {
      IbmMqException ibmMqEx = new IbmMqException("无法访问队列。");
      ibmMqEx.initCause(exception);
      throw ibmMqEx;
    }
    return mqqueue;
  }

  private void closeQueue(MQQueue mqqueue) {
    try {
      if (mqqueue != null) {
        mqqueue.close();
      }
    } catch (Exception exception) {
    }
  }

  /**
   * 关闭链接并释放
   */
  private void close() {
    try {
      if (qMgr != null) {
      	qMgr.close();
        qMgr.disconnect();        
      }
      qMgr = null;
    } catch (Exception e) {
      logger.debug("关闭MQQueueManager失败.", e);
    }

    try {
      if (pcfmessageagent != null) {
        pcfmessageagent.disconnect();       
      }
      pcfmessageagent = null;
    } catch (Exception e) {
      logger.debug("关闭PCFMessageAgent失败.", e);
    }
  }

  /**
   * 获取所有队列
   * 
   * @return
   * @throws IbmMqException
   */
  public List<IbmMqQueue> getAllQueues() throws IbmMqException {
    List<IbmMqQueue> qList = new ArrayList<IbmMqQueue>();
    String qNames[] = null;
    try {
      PCFMessage pcfmessage = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);//18
      pcfmessage.addParameter(CMQC.MQCA_Q_NAME, "*");//2016
      pcfmessage.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_ALL);//20,1001
      PCFMessage apcfmessage1[] = pcfmessageagent.send(pcfmessage);
      qNames = (String[]) apcfmessage1[0].getParameterValue(CMQCFC.MQCACF_Q_NAMES);//3011
    } catch (Exception exception) {
      IbmMqException ibmMqEx = new IbmMqException("无法获取队列。");
      ibmMqEx.initCause(exception);
      throw ibmMqEx;
    }

    if (qNames != null && qNames.length > 0) {
      for (int i = 0; i < qNames.length; i++) {
        if (qNames[i].indexOf("AMQ.") != -1 || (qNames[i].indexOf("SYSTEM.") != -1 && qNames[i].indexOf("DEAD.") == -1)) {
          continue;
        }
        MQQueue mqQueue = null;
        try {
          mqQueue = getQueue(qNames[i].trim());
          IbmMqQueue queue = new IbmMqQueue();
          if (mqQueue != null) {
            mqQueue.isOpen();
            //队列消息的最大字节数
            int maxMsgLength = mqQueue.getMaximumMessageLength();
            queue.setMaxMsgLength(new Integer(maxMsgLength));
            int openInput = mqQueue.getOpenInputCount();
            queue.setOpenInput(new Integer(openInput));
            int openOutput = mqQueue.getOpenOutputCount();
            queue.setOpenOutput(new Integer(openOutput));
            int getAllowed = mqQueue.getInhibitGet();//MQC.MQQA_GET_INHIBITED=1 ,MQC.MQQA_GET_ALLOWED=0
            queue.setGetAllowed(getAllowed);
            int putAllowed = mqQueue.getInhibitPut();//MQC.MQQA_PUT_INHIBITED=1 ,MQC.MQQA_PUT_ALLOWED=0
            queue.setPutAllowed(putAllowed);
            int queueType = mqQueue.getQueueType();
            queue.setQueueType(new Integer(queueType));
            queue.setQName(qNames[i].trim());
            queue.setMaxValue(new Integer(mqQueue.getMaximumDepth()));
            queue.setCurValue(new Integer(mqQueue.getCurrentDepth()));
            qList.add(queue);
          }
        } catch (Exception e) {
          // 非本地队列获取的时候会有异常，属于正常情况。
        } finally {
          closeQueue(mqQueue);
        }
      }
    }
    return qList;
  }

  /**
   * 获取所有通道
   * 
   * @return
   * @throws IbmMqException
   */
  public List getAllChannels() throws IbmMqException {
    List qList = new ArrayList();
    String[] channelNames = null;
    try {
      channelNames = getChannelNames();
    } catch (Exception ex) {
      IbmMqException ibmMqEx = new IbmMqException("无法获取通道名称");
      ibmMqEx.initCause(ex);
      throw ibmMqEx;
    }

    if (channelNames != null && channelNames.length > 0) {
      for (int i = 0; i < channelNames.length; i++) {
        // 过滤系统定义的通道,但显示服务器连接通道SYSTEM.DEF.SVRCONN的通道属性
        if (channelNames[i].indexOf("SYSTEM.") != -1 && !channelNames[i].trim().equalsIgnoreCase(CHANNEL_NAME)) {
          continue;
        }
        String channelName = channelNames[i].trim();
        IbmMqChannel mqChannel = new IbmMqChannel();
        // 设置通道名称
        mqChannel.setCName(channelName);
        try {
          PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS);
          request.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, channelName);
          request.addParameter(CMQCFC.MQIACH_CHANNEL_INSTANCE_TYPE, CMQC.MQOT_CURRENT_CHANNEL);
          int[] attrs = { CMQCFC.MQCACH_CHANNEL_NAME, CMQCFC.MQCACH_CONNECTION_NAME, CMQCFC.MQIACH_MSGS,
              CMQCFC.MQIACH_CHANNEL_STATUS, CMQCFC.MQIACH_BYTES_SENT, CMQCFC.MQIACH_BYTES_RCVD,
              CMQCFC.MQCACH_LAST_MSG_DATE, CMQCFC.MQCACH_LAST_MSG_TIME, CMQCFC.MQIACH_CHANNEL_TYPE };
          request.addParameter(CMQCFC.MQIACH_CHANNEL_INSTANCE_ATTRS, attrs);
          PCFMessage[] responses = pcfmessageagent.send(request);
          if (responses != null && responses.length > 0) {
            int state = responses[0].getIntParameterValue(CMQCFC.MQIACH_CHANNEL_STATUS);
            // 设置通道状态
            mqChannel.setCState(new Integer(state));
            // 设置发送字节数
            int sendBytes = responses[0].getIntParameterValue(CMQCFC.MQIACH_BYTES_SENT);
            mqChannel.setSendByte(new Integer(sendBytes));
            // 设置接收字节数
            int rcvdBytes = responses[0].getIntParameterValue(CMQCFC.MQIACH_BYTES_RCVD);
            mqChannel.setReceiveByte(new Integer(rcvdBytes));
            // 发送(最后消息或处理 MQI调用)的日期
            String lastDate = (String) responses[0].getParameterValue(CMQCFC.MQCACH_LAST_MSG_DATE);
            // 发送(最后消息或处理 MQI调用)的时间
            String lastTime = (String) responses[0].getParameterValue(CMQCFC.MQCACH_LAST_MSG_TIME);
            // 设置上次消息的时间
            mqChannel.setLastMsgTime(parseDateTime(lastDate, lastTime));
            // 设置通道消息个数(发送或接收的消息数，或处理的 MQI 调用数)
            int affairNum = responses[0].getIntParameterValue(CMQCFC.MQIACH_MSGS);
            mqChannel.setAffairNum(new Integer(affairNum));
            // 设置通道类型
            int type = responses[0].getIntParameterValue(CMQCFC.MQIACH_CHANNEL_TYPE);
            mqChannel.setCType(new Integer(type));
          }
          qList.add(mqChannel);
        } catch (PCFException pcfEx) {
          /**
           * 如果通道未启动或者由于时间超时变为不活动状态时,通过PCF API去获取数据时,会抛出异常
           */
          switch (pcfEx.reasonCode) {
          case CMQCFC.MQRCCF_CHL_STATUS_NOT_FOUND: {
            if (logger.isInfoEnabled()) {
              logger.info("通道:" + channelName + "可能未启动或者超时,状态不可监测", pcfEx);
            }
            // 设置为状态为不活动
            mqChannel.setCState(new Integer(0));
            mqChannel.setCType(new Integer(-1));
            qList.add(mqChannel);
            break;
          }
          default:
            if (logger.isErrorEnabled()) {
              logger.error("PCF Exception NOT!! handled in ChannelStatus");
            }
          }
        } catch (Exception exception) {
          IbmMqException ibmMqEx = new IbmMqException("无法获取通道。");
          ibmMqEx.initCause(exception);
          throw ibmMqEx;
        }
      }
    }
    return qList;
  }

  /**
   * 获取所有通道名称
   * 
   * @return
   * @throws MQException
   * @throws PCFException
   * @throws IOException
   */
  private String[] getChannelNames() throws MQException, PCFException, IOException {
    String[] channelNames = null;
    PCFMessage pcfmessage = new PCFMessage(CMQCFC.MQCMD_INQUIRE_CHANNEL_NAMES); // 20
    pcfmessage.addParameter(CMQCFC.MQCACH_CHANNEL_NAME, "*"); // 3510
    PCFMessage[] apcfmessage1 = pcfmessageagent.send(pcfmessage);
    for (int i = 0; i < apcfmessage1.length; i++) {
      if (apcfmessage1[i].getParameterValue(CMQCFC.MQCACH_CHANNEL_NAMES) == null)
        continue;
      channelNames = (String[]) apcfmessage1[i].getParameterValue(CMQCFC.MQCACH_CHANNEL_NAMES);
    }
    return channelNames;
  }

  /**
   * 把String类型的日期和时间转化成相应的Date型
   * 
   * @param lastDate
   *          上次消息的日期
   * @param lastTime
   *          上次消息的时间
   * @return 如果日期和时间两者之一为空或者null,则返回null
   */
  private Date parseDateTime(String lastDate, String lastTime) {
    if (StringUtil.isNullOrBlank(lastDate) || StringUtil.isNullOrBlank(lastTime)) {
      //return null;
      //mdf by maico 2009-09-22 修改为如果从服务队列里获取到的时间为null,那么返回本地的当前时间
      return new Date();
    }
    SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");
    Date date = null;
    try {
      date = simpledateformat.parse(lastDate + " " + lastTime);
    } catch (ParseException e) {
      logger.error("解析时间出错,日期:" + lastDate + "时间" + lastTime);
    }
    return date;
  }

  public static void doGet() {
  	IbmMqManager m = new IbmMqManager(new Parameter(HostIpUtil.getRemote1114IP(), 1417, 1208));
    List list = null;
    try {
      m.initQmgr();
      list = m.getAllQueues();
      
      list = m.getAllChannels();
      for (int i = 0; i < list.size(); i++) {
        IbmMqChannel chan = (IbmMqChannel) list.get(i);
        logger.info("channelName:" + chan.getCName());
        logger.info("rvdBytes:" + chan.getReceiveByte());
        logger.info("state:" + chan.getCState() + "，对应的中文状态："
            + IbmMqChannelState.getMQChState(chan.getCState().intValue()));
        logger.info("sendBytes:" + chan.getSendByte());
        logger.info("msgCount:" + chan.getAffairNum());
        logger.info("lastMsgTime:" + chan.getLastMsgTime());
        logger.info("ctype:" + chan.getCType() + "，对应的中文状态："
            + IbmMqChannelState.getMQChType(chan.getCType().intValue()));
      }      
      
      list = m.getAllQueues();
      for (int i = 0; i < list.size(); i++) {
      	IbmMqQueue queue = (IbmMqQueue) list.get(i);
      	logger.info(queue.getQName());
      }
    } catch (IbmMqException e) {
      e.printStackTrace();
    } finally {
      m.close();
    }


    logger.info("??");
  }

  private static KeyedObjectPool pool = null;

	public static IbmMqManager get(Parameter param) throws Exception {
		if (pool == null) {
			if (!PoolFactory.getDefault().contains(IbmMqManagerFactory.POOL_ID)) {
				int timeout = Integer.parseInt(System.getProperty("numen.ibmmq.pool.timeout", "60")) * 1000;
				PoolConfig poolConfig = new PoolConfig(IbmMqManagerFactory.POOL_ID, new IbmMqManagerFactory(), 1, 60 * 1000, timeout);
				PoolFactory.getDefault().registry(poolConfig);			
			}
			pool = (KeyedObjectPool) PoolFactory.getDefault().check(IbmMqManagerFactory.POOL_ID);
		}
		return (IbmMqManager) pool.borrowObject(param);
	}
	
	public static void release(IbmMqManager manager) {
		try {
			pool.returnObject(manager.param, manager);
		} catch (Throwable e) {
			logger.warn(String.format("归还IBMMQ连接失败，连接[%s]。错误：%s", manager, e));
			logger.debug("堆栈：", e);
		}
	}

	public long getResponseTime() {
		return responseTime;
	}
	
	private static class IbmMqManagerFactory implements KeyedPoolableObjectFactory {
		public static final String POOL_ID = "ibmmq";
		
		public void activateObject(Object key, Object obj) throws Exception {				
		}

		public void destroyObject(Object key, Object obj) throws Exception {
			if (logger.isDebugEnabled())
				logger.debug("连接尝试关闭：" + obj);
			IbmMqManager manager = (IbmMqManager) obj;
			manager.close();
		}

		public Object makeObject(Object key) throws Exception {
			if (logger.isDebugEnabled())
				logger.debug("连接尝试建立：" + key);
			Parameter param = (Parameter) key;
			IbmMqManager manager = new IbmMqManager(param);
			manager.initQmgr();
			return manager;
		}

		public void passivateObject(Object key, Object obj) throws Exception {
		}

		public boolean validateObject(Object key, Object obj) {
			if (Boolean.parseBoolean(System.getProperty("numen.ibmmq.pool.validate", "true"))) {
				IbmMqManager manager = (IbmMqManager) obj;
				try {
					manager.getAllQueues();
					return true;
				} catch (IbmMqException e) {
					return false;
				}			
			} else
				return true;	
		}
	}
	
  public static class Parameter {
  	private String ip;
  	private int port;
  	private int ccsid;

  	public Parameter(String ip, int port, int ccsid) {
			super();
			this.ip = ip;
			this.port = port;
			this.ccsid = ccsid;
		}

		@Override
		public int hashCode() {
			return ip.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			Parameter another = (Parameter) obj;
			return this.ip.equals(another.ip) && this.port == another.port && this.ccsid == another.ccsid;
		}

		@Override
		public String toString() {
			return String.format("%s[%s:%d %d]", getClass().getName(), ip, port, ccsid);
		}		
  }

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getName(), param);
	}
}
