package uyun.bat.common.mq;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import uyun.bat.common.config.Config;

/**
 * 持久化消息订阅之消息监听容器
 */
public class MQTopicListenerContainer extends DefaultMessageListenerContainer {
	private static final Logger logger = LoggerFactory.getLogger(MQTopicListenerContainer.class);

	private static final String ROOT_CATALOG = "/bat";
	private ZkClient zkClient;
	private String path = ROOT_CATALOG + "/uyun.bat.common.mq.MQTopicListenerContainer.";
	private String data = "";
	private boolean isLeader = false;
	/**
	 * 是否是spring初始化的调用
	 */
	private boolean isFirst = true;

	public void start() {
		if (isFirst) {
			isFirst = false;
			return;
		}
		if (isLeader)
			super.start();
	}

	public void setClientId(String clientId) {
		this.path = path + clientId;

		data = ClientIdGenerator.generateClientId(clientId);
		if (zkClient != null)
			zkClient.close();
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(String.format("MQTopicListenerContainer init error.[path=%s]", path), e);
		}
		super.setClientId(clientId);
	}

	private void init() throws Exception {
		String dubboRegistryAddress = (String) Config.getInstance().get("zk.url");
		String zkServers = dubboRegistryAddress.replace("zookeeper://", "").replace("?backup=", ",").trim();
		zkClient = new ZkClient(zkServers, 10000, 10000);
		initDirectory();
		tryLeader();
		zkClient.subscribeDataChanges(path, new IZkDataListener() {
			public void handleDataChange(String dataPath, Object data) throws Exception {
				// 另一台机器成为leader
			}

			public void handleDataDeleted(String dataPath) throws Exception {
				if (logger.isInfoEnabled())
					logger.info(String.format("handleDataDeleted.dataPath=%s", dataPath));
				// 另一台机器挂了
				tryLeader();
			}
		});

	}

	private void tryLeader() {
		if (!zkClient.exists(path)) {
			try {
				zkClient.createEphemeral(path, data);
				isLeader = true;
				if (logger.isInfoEnabled())
					logger.info(String.format("superior successfully.[data=%s],start MQTopicListenerContainer。", data));
				start();
			} catch (Exception e) {
				isLeader = false;
				if (logger.isInfoEnabled())
					logger.info(String.format("elect for leader failed。waiting for next elect。[data=%s]", data), e);
			}
		}
		if (logger.isInfoEnabled())
			logger.info(String.format("temporary point has been built,wait for next leader elect。[path=%s]", path));
	}
	
	private void initDirectory() {
		if (zkClient.exists(ROOT_CATALOG))
			return;
		Exception except = null;
		try {
			zkClient.createPersistent(ROOT_CATALOG);
		} catch (Exception e) {
			except = e;
		}
		if (!zkClient.exists(ROOT_CATALOG)) {
			logger.warn(String.format("create root directory[%s]failed.[%s]", ROOT_CATALOG, except != null ? except.getMessage()
					: "exception is null"));
			if (logger.isDebugEnabled())
				logger.debug("Stack：", except);
		}
	}
}
