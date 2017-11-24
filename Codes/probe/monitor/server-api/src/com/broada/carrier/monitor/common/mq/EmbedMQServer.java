package com.broada.carrier.monitor.common.mq;

import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.JMSException;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.component.utils.error.ErrorUtil;
import com.broada.mq.client.FailoverConnection;

/**
 * <pre>
 * JVM内MQServer服务
 * 此服务对mqserver进行了简化，包含以下特性：
 * 1. 只提供JVM内的网络连接
 * 2. 固定的内存消耗：64MB内存使用，100MB临时使用
 * @author Jiangjw
 */
public class EmbedMQServer {
	private static final Logger logger = LoggerFactory.getLogger(EmbedMQServer.class);
	private static EmbedMQServer instance;
	private BrokerService broker;
	private FailoverConnection connection;
	private boolean useJmx = false;
	
	/**
	 * <pre>
	 * 构造一个MQServer
	 * 根据系统属性mq.embed.useJmx决定，是否启动jmx
	 */
	public EmbedMQServer() {		
		this.useJmx = Boolean.parseBoolean(System.getProperty("mq.embed.useJmx", Boolean.toString(useJmx)));
	}

	/**
	 * 启动mq服务
	 * @throws Exception
	 */
	public void startup() throws Exception {
		if (isRunning()) {
			logger.warn("MQServer启动失败，已经处于启动状态");
			return;
		}
		
		broker = new BrokerService();				
		broker.setUseJmx(useJmx);
		broker.setPersistent(false);
		broker.getSystemUsage().getMemoryUsage().setLimit(64 * 1024 * 1024);
		broker.getSystemUsage().getTempUsage().setLimit(100 * 1024 * 1024);
		broker.getSystemUsage().setSendFailIfNoSpace(true);		
		broker.setSplitSystemUsageForProducersConsumers(true);
		broker.setProducerSystemUsagePortion(70);
		broker.setConsumerSystemUsagePortion(30);		
		ConstantPendingMessageLimitStrategy pendingMessageLimitStrategy = new ConstantPendingMessageLimitStrategy();		
		pendingMessageLimitStrategy.setLimit(50);	
		PolicyEntry policy = new PolicyEntry(); 
		policy.setAdvisoryForFastProducers(true);
	  policy.setAdvisoryForConsumed(true);
    policy.setAdvisoryForDelivery(true);
    policy.setAdvisoryForDiscardingMessages(true);
    policy.setAdvisoryForSlowConsumers(true);
    policy.setAdvisoryWhenFull(true);
    policy.setProducerFlowControl(false);		
		policy.setPendingMessageLimitStrategy(pendingMessageLimitStrategy);			
		PolicyMap policyMap = new PolicyMap();			
		policyMap.setDefaultEntry(policy);
		broker.setDestinationPolicy(policyMap);
		broker.start();					
		
		logger.debug("MQServer已启动");
	}
	
	/**
	 * 关闭mq服务
	 * @throws Exception
	 */
	public void shutdown() throws Exception {
		if (!isRunning()) {
			logger.warn("MQServer关闭失败，已经处于关闭状态");
			return;
		}
		
		if (connection != null) {
			connection.close();
			connection = null;
		}		
		broker.stop();
		broker = null;
		logger.info("MQServer已关闭。");
	}
	
	/**
	 * 获取jvm内的mq连接
	 * @return
	 * @throws JMSException
	 */
	public FailoverConnection getVmConnection() throws JMSException {
		if (connection == null) {
			connection = new FailoverConnection(getVmConnectionURI().toString() + "?jms.useAsyncSend=true", "vm.connection");
			connection.connect();
		}
		return connection;
	}	
	
	/**
	 * 获取jvm内的mq连接URI
	 * @return
	 */
	public URI getVmConnectionURI() {
		try {
			return new URI("vm://localhost");
		} catch (URISyntaxException e) {
			logger.warn(String.format("构建URI失败。错误：%s", e));
			logger.debug("堆栈：", e);
			return null;
		}
	}			
	
	/**
	 * 确定mq是否正在运行
	 * @return
	 */
	public boolean isRunning() {
		return broker != null;
	}

	/**
	 * 获取默认实例
	 * @return
	 */
	public static EmbedMQServer getDefault() {
		if (instance == null) {
			synchronized (EmbedMQServer.class) {
				if (instance == null) {
					instance = new EmbedMQServer();
					try {
						instance.startup();
					} catch (Exception e) {
						ErrorUtil.exit(logger, "内置MQ服务无法启动", e, 1);						
					}
				}
			}
		}
		return instance;
	}
}
