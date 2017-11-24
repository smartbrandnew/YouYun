package uyun.bat.common.leaderselector;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.config.Config;

/**
 * 就目前而言LeaderLatch应该算满足需求.即一台机器确定为leader后,就一直是它除非死掉
 * LeaderSelector倒是可以更智能，可以释放，然后再选举为leader。不过使用上要考虑较多，还是先不用这个了
 * 
 * 结论：http://uyunsoft.cn/kb/pages/viewpage.action?pageId=14649586
 */
public class BatLeaderSelector {
	private static final Logger logger = LoggerFactory.getLogger(BatLeaderSelector.class);

	private static final String ROOT_CATALOG = "/bat";

	private static List<BatLeaderSelector> leaderSelectorList = new ArrayList<BatLeaderSelector>();

	public static void closeBatLeaderSelector() {
		for (int i = 0; i < leaderSelectorList.size(); i++) {
			BatLeaderSelector ll = leaderSelectorList.get(i);
			ll.close();
			i--;
		}
	}

	private ZkClient zkClient;
	private String path;
	private String data = "";

	private boolean isLeader = false;

	/**
	 * @param path 不能与其他业务逻辑重复,且只能单级目录。e.g. /uyun.bat.tenant.producedata
	 */
	public BatLeaderSelector(String path) {
		if (path == null || path.length() == 0)
			throw new IllegalArgumentException("illegal path.[path=" + path + "]");

		this.path = ROOT_CATALOG + path;

		data = generateData();

		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(String.format("BatLeaderSelector init error.[path=%s]", this.path), e);
		}
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

		leaderSelectorList.add(this);
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

	private void tryLeader() {
		if (!zkClient.exists(path)) {
			try {
				zkClient.createEphemeral(path, data);
				isLeader = true;
				if (logger.isInfoEnabled())
					logger.info(String.format("superior successfully.[data=%s]", data));
			} catch (Exception e) {
				isLeader = false;
				if (logger.isInfoEnabled())
					logger.info(String.format("elect for leader failed,wait for next elect。[data=%s]", data), e);
			}
		}

		if (logger.isInfoEnabled())
			logger.info(String.format("temporary point has built,wait for next leader elect。[path=%s]", path));
	}

	private String generateData() {
		String prefix = "";
		try {
			prefix = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			logger.warn("gain LocalHost message failed！", e);
		}

		return prefix + "-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 100);
	}

	public boolean isLeader() {
		return isLeader;
	}

	public void close() {
		try {
			zkClient.unsubscribeAll();
			zkClient.close();
		} catch (Exception e) {
			logger.warn("Close zkClient error:", e);
		}
		isLeader = false;
		leaderSelectorList.remove(this);
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Throwable {
		BatLeaderSelector leaderSelector1 = new BatLeaderSelector("/uyun.bat.tenant.producedata");
		BatLeaderSelector leaderSelector2 = new BatLeaderSelector("/uyun.bat.tenant.producedata");
		BatLeaderSelector leaderSelector3 = new BatLeaderSelector("/uyun.bat.tenant.producedata");
		System.out.println("leaderSelector1" + leaderSelector1.isLeader());
		System.out.println("leaderSelector2" + leaderSelector2.isLeader());
		System.out.println("leaderSelector3" + leaderSelector3.isLeader());
		{
			// 测试切换
			for (BatLeaderSelector temp : leaderSelectorList) {
				if (temp.isLeader) {
					temp.close();
					break;
				}
			}
			Thread.currentThread().sleep(1000);
			System.out.println("leaderSelector1" + leaderSelector1.isLeader());
			System.out.println("leaderSelector2" + leaderSelector2.isLeader());
			System.out.println("leaderSelector3" + leaderSelector3.isLeader());
			for (BatLeaderSelector temp : leaderSelectorList) {
				if (temp.isLeader) {
					temp.close();
					break;
				}
			}
			Thread.currentThread().sleep(1000);
			System.out.println("leaderSelector1" + leaderSelector1.isLeader());
			System.out.println("leaderSelector2" + leaderSelector2.isLeader());
			System.out.println("leaderSelector3" + leaderSelector3.isLeader());
			for (BatLeaderSelector temp : leaderSelectorList) {
				if (temp.isLeader) {
					temp.close();
					break;
				}
			}
		}
		// {
		// //测试停止全部
		// BatLeaderSelector.closeBatLeaderSelector();
		// }

	}
}
