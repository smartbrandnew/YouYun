package uyun.bat.gateway.agent.util;

import com.alibaba.druid.support.json.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 收集目前monitor能监控的集成
 */
public abstract class IntegrationUtil {
	public static final String STORE = "存储";
	public static final String SERVICE = "服务器";
	public static final String DATABASE = "数据库";
	public static final String PLATFORM = "中间件";
	public static final String ROUTER = "路由器";
	public static final String SWITCH = "交换机";
	public static final String STORE_EN = "Store";
	public static final String SERVICE_EN = "Service";
	public static final String DATABASE_EN = "Database";
	public static final String PLATFORM_EN = "Middleware";
	public static final String ROUTER_EN = "Router";
	public static final String SWITCH_EN = "Switch";
	/**
	 * 未区分
	 */
	// docker domino hypervisor jenkins jvm kubernetes openstack postfix vmware vsphere wsnd

	/**
	 * 存储
	 */
	public static final String storeAPPs = "{\"dellequallogic\": \"存储\",\"emccelerra\": \"存储\",\"emcsymm\": \"存储\",\"emcvnx\": \"存储\",\"hds\": \"存储\",\"hp3par\": \"存储\""+
			",\"hpeva\": \"存储\",\"huawei\": \"存储\",\"ibmds\": \"存储\",\"ibmsvc\": \"存储\",\"netappfas\": \"存储\",\"netapp\": \"存储\"}";
	public static final Map<String, String> storeMap = (Map<String, String>) JSONUtils.parse(storeAPPs);


	/**
	 * 服务器（除存储设备之外的设备都算）
	 */
	public static final String serviceAPPs = "{\"system\": \"服务器\",\"http\": \"服务器\",\"https\": \"服务器\",\"dns\": \"服务器\",\"tcp\": \"服务器\",\"ntp\": \"服务器\""+
			",\"sftp\": \"服务器\",\"pingdom\": \"服务器\",\"ftp\": \"服务器\",\"udp\": \"服务器\",\"smtp\": \"服务器\",\"ipmi\": \"服务器\",\"pop3\": \"服务器\",\"icmp\": \"服务器\"}";
	public static final Map<String, String> serviceMap = (Map<String, String>) JSONUtils.parse(serviceAPPs);

	/**
	 * 数据库
	 */
	public static final String databaseAPPs = "{\"mysql\": \"数据库\",\"postgresql\": \"数据库\",\"sqlserver\": \"数据库\",\"oracle\": \"数据库\",\"db2\": \"数据库\",\"informix\": \"数据库\",\"sybase\": \"数据库\"," +
			"\"cassandra\": \"数据库\",\"mongodb\": \"数据库\",\"couchbase\": \"数据库\",\"couchdb\": \"数据库\",\"shentong\": \"数据库\",\"dm\": \"数据库\",\"mssql\": \"数据库\"}";
	public static final Map<String, String> databaseMap = (Map<String, String>) JSONUtils.parse(databaseAPPs);

	/**
	 * 中间件
	 */
	public static final String platformAPPs = "{\"apache\": \"中间件\",\"tomcat\": \"中间件\",\"iis\": \"中间件\",\"nginx\": \"中间件\",\"jboss\": \"中间件\",\"lighttpd\": \"中间件\",\"weblogic\": \"中间件\",\"resin\": \"中间件\"," +
			"\"tongweb\": \"中间件\",\"exchange\": \"中间件\",\"redis\": \"中间件\",\"memcache\": \"中间件\",\"activemq\": \"中间件\"\t,\"rabbitmq\": \"中间件\",\"kafka\": \"中间件\",\"websphere\": \"中间件\"," +
			"\"tux\": \"中间件\",\"zookeeper\": \"中间件\",\"etcd\": \"中间件\",\"haproxy\": \"中间件\",\"elasticsearch\": \"中间件\",\"hdfs\": \"中间件\",\"mapreduce\": \"中间件\",\"mesos\": \"中间件\"}";
	public static final Map<String, String> platformMap = (Map<String, String>) JSONUtils.parse(platformAPPs);

	/**
	 * 路由器
	 */
	//通过资源的tag判断equipment:Router
	/**
	 * 交换机
	 */
	//通过资源的tag判断 equipment:Switch

	/**
	 * 返回所有的APPs
	 */
	public static Map<String, String> getAllMap() {
		Map<String, String> map = new HashMap<>();
		map.putAll(storeMap);
		map.putAll(serviceMap);
		map.putAll(databaseMap);
		map.putAll(platformMap);
		return map;
	}
}
