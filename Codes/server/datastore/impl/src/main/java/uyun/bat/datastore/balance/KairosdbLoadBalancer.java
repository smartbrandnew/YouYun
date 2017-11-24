package uyun.bat.datastore.balance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.net.telnet.TelnetClient;
import org.kairosdb.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IPingStrategy;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

import uyun.bat.common.config.Config;

/**
 * kairosdb 均衡负载器 读写分离，默认采用 轮询 算法。 可以获取节点的监控日志
 *
 */
public class KairosdbLoadBalancer {
	private static final Logger logger = LoggerFactory.getLogger(KairosdbLoadBalancer.class);
	// 写请求采用 telnet 协议
	private final BaseLoadBalancer writeLoadBalancer;
	// 读请求采用 http 协议
	private final BaseLoadBalancer readLoadBalancer;
	// 删除/直接读写kairosdb请求采用 http 协议
	private final BaseLoadBalancer delLoadBalancer;
	// 默认 ping interval 10s
	private IPing pingRule = new TcpPing();

	private IPingStrategy pingStrategy = new ConcurrentPing();
	private List<Server> readServers = new ArrayList<Server>();
	private List<Server> writeServers = new ArrayList<Server>();
	private List<Server> delServers = new ArrayList<Server>();
	// 软负载查询连接池
	private static final Map<String, HttpClient> tscachedClientMap = new HashMap<String, HttpClient>();
	private static final Map<String, TelnetClient> telnetClientMap = new HashMap<String, TelnetClient>();
	private static final Map<String, HttpClient> kairosdbClientMap = new HashMap<String, HttpClient>();
	private static final Map<String, Server> writeServerMap = new HashMap<String, Server>();

	private void init() {
		String readIpaddrs = Config.getInstance().get("tscached.http.hosts", "localhost:8008");
		String[] readIpaddrArray = readIpaddrs.split(",");
		for (String ip : readIpaddrArray) {
			readServers.add(new Server(ip));
		}
		String writeIpaddrs = Config.getInstance().get("kairosdb.telnet.hosts", "localhost:14242");
		String[] writeIpaddrArray = writeIpaddrs.split(",");
		for (String ip : writeIpaddrArray) {
			writeServers.add(new Server(ip));
		}
		String delIpaddrs = Config.getInstance().get("kairosdb.http.hosts", "localhost:18080");
		String[] delIpaddrArray = delIpaddrs.split(",");
		for (String ip : delIpaddrArray) {
			delServers.add(new Server(ip));
		}
		initHttpClient();
		initTelnetClient();
	}

	private void initHttpClient() {
		//设置超时时间,默认2秒
		int timeout = Config.getInstance().get("datastore.MetricService.timeout", 2000);
		for (Server readServer : readServers) {
			try {
				HttpClient client = new HttpClient("http://" + readServer.getHost() + ":" + readServer.getPort(), timeout);
				tscachedClientMap.put(readServer.toString(), client);
			} catch (MalformedURLException e) {
				logger.warn("httpClient init error：", e);
			}
		}

		for (Server server : delServers) {
			try {
				HttpClient client = new HttpClient("http://" + server.getHost() + ":" + server.getPort(), timeout);
				kairosdbClientMap.put(server.toString(), client);
			} catch (MalformedURLException e) {
				logger.warn("httpClient init error：", e);
			}
		}
	}

	private void initTelnetClient() {
		for (Server writeServer : writeServers) {
			TelnetClient telnetClient = new TelnetClient();
			telnetClient.setDefaultTimeout(5000);
			writeServerMap.put(writeServer.toString(), writeServer);
			telnetClientMap.put(writeServer.toString(), telnetClient);
			try {
				telnetClient.connect(writeServer.getHost(), writeServer.getPort());
				if(telnetClient.isConnected()){
					telnetClient.getOutputStream().flush();
					telnetClient.setKeepAlive(true);
				}
			} catch (Exception e) {
				logger.warn("telnetClient init error： ", e);
			}
		}
	}

	public HttpClient getKairosdbClient() {
		Server server =getDelKaios();
		if (server != null) {
			HttpClient client = kairosdbClientMap.get(server.toString());
			if (client != null) {
				return client;
			}
		}
		if (kairosdbClientMap.values().size() > 0)
			return (HttpClient) kairosdbClientMap.values().toArray()[0];
		throw new RuntimeException("gain kairosdbClient failed,please check if \"kairosdb.http.hosts\" is right?");
	}

	public HttpClient getTscachedClient() {
		Server server = getReadKaios();
		if (server != null) {
			HttpClient client = tscachedClientMap.get(server.toString());
			if (client != null) {
				return client;
			}
		}
		if (tscachedClientMap.values().size() > 0)
			return (HttpClient) tscachedClientMap.values().toArray()[0];
		throw new RuntimeException("gain kairosdbClient failed,check if \"tscached.http.hosts\" is right?");
	}
	
	private Set<TelnetClient> errorTelnetClientSet = Collections.synchronizedSet(new HashSet<TelnetClient>());
	
	public void onTelnetClientError(TelnetClient client) {
		try {
			client.disconnect();
		} catch (Exception e1) {
			logger.warn("kairosdb telnet_client close error :" + client.getLocalAddress() + "]," + e1.getMessage());
		}
		errorTelnetClientSet.add(client);
	}

	public TelnetClient getTelnetClient() {
		Server server = getWriteKaios();
		if (server != null) {
			TelnetClient client = telnetClientMap.get(server.toString());

			if (client == null) {
				throw new RuntimeException("init error : telnetClientMap with writeLoadBalancer。");
			} else {
				if (errorTelnetClientSet.contains(client)) {
					// 按理说server能连上，可以尝试telnet链接
					synchronized (client) {
						try {
							client.disconnect();
						} catch (Exception e1) {
							logger.warn("kairosdb telnet_client close error :" + client.getLocalAddress() + "],"
									+ e1.toString());
						}
						try {
							client.connect(server.getHost(), server.getPort());
							if (client.isConnected()) {
								client.getOutputStream().flush();
								client.setKeepAlive(true);

								errorTelnetClientSet.remove(client);
								return client;
							}
						} catch (Exception e) {
							throw new RuntimeException(
									"kairosdb connect error [" + server.toString() + "]," + e.getMessage());
						}
						throw new RuntimeException("kairosdb connect error: " + server.toString());
					}
				} else {
					return client;
				}
			}
		} else {
			throw new RuntimeException("no connection available.");
		}

	}

	public KairosdbLoadBalancer() {
		init();
		writeLoadBalancer = new BaseLoadBalancer(pingRule, new WeightedResponseTimeRule(), pingStrategy);
		readLoadBalancer = new BaseLoadBalancer(pingRule, new WeightedResponseTimeRule(), pingStrategy);
		delLoadBalancer = new BaseLoadBalancer(pingRule,  new WeightedResponseTimeRule(), pingStrategy);
		writeLoadBalancer.addServers(writeServers);
		readLoadBalancer.addServers(readServers);
		delLoadBalancer.addServers(delServers);
	}

	public LoadBalancerStats getWriteLoadBalancerStats() {
		return writeLoadBalancer.getLoadBalancerStats();
	}

	public LoadBalancerStats getReadLoadBalancerStats() {
		return readLoadBalancer.getLoadBalancerStats();
	}

	public LoadBalancerStats getdelLoadBalancerStats() {
		return delLoadBalancer.getLoadBalancerStats();
	}

	public Server getReadKaios() {
		Server server = null;
		try {
			server = readLoadBalancer.chooseServer("kairosRead");
		} catch (Exception e) {
		}
		return server;
	}

	public Server getWriteKaios() {
		Server server = null;
		try {
			server = writeLoadBalancer.chooseServer("kairosWrite");
		} catch (Exception e) {
		}
		return server;
	}

	public Server getDelKaios() {
		Server server = null;
		try {
			server = delLoadBalancer.chooseServer("kairosDel");
		} catch (Exception e) {
		}
		return server;
	}

	private static class ConcurrentPing implements IPingStrategy {
		@Override
		public boolean[] pingServers(final IPing ping, final Server[] servers) {
			ExecutorService executor = Executors.newFixedThreadPool(4);
			final IPing iPing = new TcpPing();
			boolean[] results = new boolean[servers.length];
			for (int i = 0; i < servers.length; i++) {
				final int j = i;
				try {
					results[i] = executor.submit(new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return iPing.isAlive(servers[j]);
						}
					}).get();
				} catch (Exception e) {
				}
			}
			executor.shutdown();
			return results;
		}
	}

	private static class TcpPing implements IPing {
		@Override
		public boolean isAlive(Server server) {
			String ip = server.getHost();
			int port = server.getPort();
			boolean result = false;
			Socket socket = null;
			try {
				socket = new Socket();
				socket.setSoTimeout(5000);
				InetSocketAddress endpoint = new InetSocketAddress(ip, port);
				socket.connect(endpoint);
				result = true;
				return result;
			} catch (IOException e) {
				result = false;
			} finally {
				if (socket != null) {
					try {
						socket.close();
						return result;
					} catch (IOException e) {
					}
				}
			}
			return result;
		}
	}
}
