package com.broada.carrier.monitor.probe.impl.dispatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.info.VCenterVmInfoMonitor;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.config.MetricMapper;
import com.broada.carrier.monitor.probe.impl.entity.MonitorResultCache;
import com.broada.carrier.monitor.probe.impl.entity.RemoteMapper;
import com.broada.carrier.monitor.probe.impl.logic.ProbeTaskServiceEx;
import com.broada.carrier.monitor.probe.impl.openapi.entity.CheckpointVO;
import com.broada.carrier.monitor.probe.impl.openapi.entity.PerfMetricVO;
import com.broada.carrier.monitor.probe.impl.openapi.service.MetricService;
import com.broada.carrier.monitor.probe.impl.sync.entity.LinkStat;
import com.broada.carrier.monitor.probe.impl.util.StringUtils;
import com.broada.carrier.monitor.probe.impl.util.UUIDUtils;
import com.broada.carrier.monitor.probe.impl.yaml.ResourceType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.common.util.Counter;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.utils.StringUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

public class MonitorResultUploader {
	private static final int UPLOAD_FAILED_RETRY = 0;
	private static final int UPLOAD_FAILED = 1;
	private static final int UPLOAD_SUCCEED = 2;
	
	private static final String CHECKED = "checked";
	private static final String FAILED = "failed";
	private static final String UNCHECKED = "unchecked";
	private static final boolean MonitorResultUploadMonitor = Boolean.valueOf(Config.getDefault().getProperty("monitor.result.upload.monitor", "false")).booleanValue();

	private static final Logger logger = LoggerFactory.getLogger(MonitorResultUploader.class);
	private Queue<MonitorResult> queue = new ConcurrentLinkedQueue<MonitorResult>();
	
	private static ConcurrentHashMap<String, String> xugu_node_ip_id = new ConcurrentHashMap<String, String>();
	
	/**
	 * 处理队列的长度
	 */
	private int queueMaxLength = 1000;

	/**
	 * 上报尝试间隔(毫秒)
	 */
	private int uploadTryInterval = 60000;

	/**
	 * 缓存信息输出间隔(毫秒)
	 */
	private int cacheOutputInterval = 300000;

	/**
	 * 每次上报的数量
	 */
	private int cacheUploadCountPer = 10;

	/**
	 * 最近一次连续上传失败的次数汇总，一旦有一次成功就清0
	 */
	private int uploadFailedLastCount = 0;
	private int uploadTimeoutLastCount = 0;
	private long lastTestServerTime = 0;
	private long serverTimeOffset = 0;
	private int uploadTimeoutRetries = 3;
	private long testServerTimeInterval = 5l * 60 * 1000;
	
	/**
	 * 用于记录oracle和dm数据库状态指标上传
	 */
	private Map<String, CheckpointVO> stateMap = new HashMap<String, CheckpointVO>(2);

	/**
	 * 采集结果缓存DAO
	 */
	@Autowired
	private ProbeTaskServiceEx taskService;
	@Autowired
	private MetricService metricService;
	@Autowired
	private ProbeServiceFactory probeFactory;
	private String probeCode;

	/**
	 * 监测采集结果上报服务动态代理
	 */
	private long uploadedCount;
	private int cacheCount;

	/**
	 * 上报计数器
	 */
	private Counter uploadCounter = new Counter();

	/**
	 * 缓存上报线程
	 */
	private CacheUploadThread cacheUploadThread;

	private static MonitorResultUploader instance;
	
	// 需要校验的ip
	private static ConcurrentMap<String, Set<String>> ips_verify = new ConcurrentHashMap<String, Set<String>>();
	
	/**
	 * 校验配置正确性
	 */
	private ConcurrentMap<String, Map<String, String>> verify_stat = new ConcurrentHashMap<String, Map<String, String>>();
	
	@SuppressWarnings("unchecked")
	public void addVerifyStat(String pluginName, String ip){
		if(!existOrNot(pluginName, ip)){
			Map<String, String> tmp = new HashMap<String, String>();
			tmp.put(ip, UNCHECKED);
			verify_stat.put(pluginName, tmp);
			if(ips_verify.get(pluginName) == null)
				ips_verify.put(pluginName, new HashSet<String>(Arrays.asList(new String[]{ip})));
			else
				ips_verify.get(pluginName).add(ip);
		}
	}
	
	/**
	 * 设置状态<p>
	 * 无原始数据直接设置对应值<p>
	 * 原始数据为UNCHECKD直接设置值
	 * 原始数据为FAILED,本次设置为CHECKED,表明该应用是能够上传的，设置为CHECKED
	 * 原始数据为FAILED,本次设置为FALSE,直接跳过
	 * 原始数据为CHECKED,本次设置为FALSE/CHECKED,表明该应用是能够上传数据的
	 * @param pluginName
	 * @param ip
	 * @param stat
	 */
	public void setVerifyStat(String pluginName, String ip, String stat){
		if(!existOrNot(pluginName, ip)){
			Map<String, String> tmp = new HashMap<String, String>();
			tmp.put(ip, stat);
			verify_stat.put(pluginName, tmp);
		}else{
			String last = verify_stat.get(pluginName).get(ip);
			if(last.equals(UNCHECKED) || (last.equals(FAILED) && stat.equals(CHECKED)))
				verify_stat.get(pluginName).put(ip, stat);
		}
	}
	
	public String getVerifyStat(String pluginName, String ip){
		if(!existOrNot(pluginName, ip)) return null;
		return verify_stat.get(pluginName).get(ip);
	}
	
	public void addVerifyStat(String pluginName, Set<String> ips){
		if(ips.isEmpty()) return ;
		for(String ip:ips){
			addVerifyStat(pluginName.toLowerCase(), ip);
		}
	}
	
	public void clearStatMap(){
		verify_stat.clear();
	}

	/**
	 * 获取默认实例
	 * 
	 * @return
	 */
	public static MonitorResultUploader getDefault() {
		if (instance == null) {
			synchronized (MonitorResultUploader.class) {
				if (instance == null)
					instance = new MonitorResultUploader();
			}
		}
		return instance;
	}

	public Date getServerTime() {
		/*
		 * long time = System.currentTimeMillis(); if (time - lastTestServerTime
		 * > testServerTimeInterval) { synchronized (this) { if (time -
		 * lastTestServerTime > testServerTimeInterval) { long probeTime =
		 * System.currentTimeMillis(); lastTestServerTime = probeTime; try {
		 * Date serverTime = serverFactory.getSystemService() .getTime();
		 * serverTimeOffset = probeTime - serverTime.getTime(); if
		 * (logger.isDebugEnabled()) logger.debug("服务端比探针端快：{}",
		 * Unit.ms.formatPrefer(0 - serverTimeOffset)); } catch (Throwable e) {
		 * logger.debug("服务端时间获取失败", e); } } } time =
		 * System.currentTimeMillis(); }
		 */
		return new Date();
	}

	/**
	 * 获取当前探针与服务器的时间差，单位ms > 0表示，当前探针时间快于服务器时间 < 0表示，当前探针时间慢于服务器时间
	 * 
	 * @return
	 */
	public long getServerTimeOffset() {
		return serverTimeOffset;
	}

	private class CacheUploadThread extends Thread {
		public CacheUploadThread() {
			super("CacheUploadThread");
		}

		@Override
		public void run() {
			logger.debug("监测结果上报线程已启动");

			boolean needWait;
			long lastOutput = 0;
			uploadedCount = 0;
			cacheCount = taskService.getResultCachesCount();
			List<MonitorResult> results = new ArrayList<MonitorResult>(cacheUploadCountPer);
			int uploadTimes = 1;
			while (!isInterrupted()) {
				if(MonitorResultUploadMonitor)
					logger.error("上报线程第 {} 次开始上报时间:{}", uploadTimes, new Date(System.currentTimeMillis()));
				try {
					needWait = false;
					MonitorResultCache[] caches = taskService.getResultCaches(PageNo.createByIndex(0,
							cacheUploadCountPer));
					if (caches != null && caches.length > 0) {
						int uploadRet = doUpload(caches);
						
						// 处理上报结果
						resolveStat(caches);
						
						if (uploadRet != UPLOAD_FAILED_RETRY) {
							taskService.deleteResultCaches(caches);
							cacheCount -= caches.length;
							uploadedCount += caches.length;
						} else
							needWait = true;
						if(MonitorResultUploadMonitor)
							logger.error("上报线程第 {} 次上报 {} 条数据库缓存结束时间:{},发生异常情况:{}", new Object[]{uploadTimes, 
								caches.length , new Date(System.currentTimeMillis()), needWait});
					}
					if (!queue.isEmpty()) {
						if (cacheCount > 0 || queue.size() >= queueMaxLength) {
							while (!queue.isEmpty()) {
								taskService.saveResultCache(new MonitorResultCache(queue.poll()));
								cacheCount++;
							}
						} else {
							// 未存库且队列可容纳量未达到最大
							results.clear();
							for (int i = 0; i < cacheUploadCountPer && !queue.isEmpty(); i++)
								results.add(queue.poll());

							int uploadRet = doUpload(results.toArray(new MonitorResult[0]));
							
							resolveStat(results.toArray(new MonitorResult[0]));
							if (uploadRet == UPLOAD_FAILED_RETRY) {
								needWait = true;
								for (MonitorResult result : results) {
									taskService.saveResultCache(new MonitorResultCache(result));
									cacheCount++;
								}
							} else {
								uploadedCount += results.size();
							}
						}
						if(MonitorResultUploadMonitor)
							logger.error("上报线程第 {} 次上报 {} 条队列缓存结束时间:{},发生异常情况:{}", new Object[]{uploadTimes, 
								results.size() , new Date(System.currentTimeMillis()), needWait});
					}
					// 处理状态信息
					if(!verify_stat.isEmpty())
						uploadStat(verify_stat);
					
					if (needWait) // 连接服务端或服务端错误，需要等指定时间再尝试
						Thread.sleep(uploadTryInterval);
					else if ((queue.isEmpty() && cacheCount == 0)) {
						Thread.sleep(3000); // 如果队列都为空的情况下，至少还是休眠一小段时间，避免不断的响应监测结果上报请求
						synchronized (queue) {
							if ((queue.isEmpty() && cacheCount == 0))
								queue.wait(uploadTryInterval);
						}
					}

					long now = System.currentTimeMillis();

					uploadCounter.record(now, uploadedCount);

					if (now - lastOutput >= cacheOutputInterval) {
						logger.info(String.format("采集结果上报缓存[数据库：%d 内存队列：%d 已上报：%d 上报速率：%.4f]", cacheCount,
								queue.size(), uploadedCount, uploadCounter.getIncreaseRate() == null ? 0
										: uploadCounter.getIncreaseRate().getIncreaseRate()));
						lastOutput = now;
					}
				} catch (Exception e) {
					if (e instanceof InterruptedException) {
						logger.warn(String.format("采集结果上传线程被中断，将退出。错误：%s", e));
						logger.debug("堆栈：", e);
						break;
					} else {
						logger.warn(String.format("采集结果上传线程失败。错误：%s", e));
						logger.debug("堆栈：", e);
					}

					try {
						Thread.sleep(uploadTryInterval);
					} catch (InterruptedException e1) {
						logger.warn(String.format("采集结果上传线程被中断，将退出。错误：%s", e));
						logger.debug("堆栈：", e);
						break;
					}
				}
				if(MonitorResultUploadMonitor)
					logger.error("上报线程第 {} 次结束上报时间:{}", uploadTimes, new Date(System.currentTimeMillis()));
				uploadTimes ++;
			}

			cacheUploadThread = null;
			logger.info("监测结果上报线程已关闭");
		}
	}

	public boolean isRunning() {
		return this.cacheUploadThread != null;
	}

	public void startup(String probeCode) {
		if (isRunning()) {
			logger.warn("启动采集结果上传线程失败。错误：线程已启动，不需要再次启动");
			return;
		}

		this.setProbeCode(probeCode);
		cacheUploadThread = new CacheUploadThread();
		cacheUploadThread.start();
	}

	public void shutdown() {
		if (!isRunning()) {
			logger.warn("关闭采集结果上传线程失败。错误：线程已关闭，不需要再次关闭");
			return;
		}

		cacheUploadThread.interrupt();
		try {
			cacheUploadThread.join(3000);
		} catch (InterruptedException e) {
			logger.warn(String.format("关闭采集结果上传线程在等待过程中被停止。错误：%s", e));
			logger.debug("堆栈：", e);
		}
		if (cacheUploadThread.isAlive())
			logger.warn("关闭采集结果上传线程失败。错误：线程被有及时退出");
	}

	/**
	 * 进行上报操作
	 * 
	 * @param caches
	 * @return
	 */
	private int doUpload(MonitorResultCache[] caches) {
		if (caches == null || caches.length == 0)
			return UPLOAD_SUCCEED;

		MonitorResult[] results = new MonitorResult[caches.length];
		for (int i = 0; i < results.length; i++)
			results[i] = caches[i].getResult();

		return doUpload(results);
	}

	/**
	 * 进行上报操作
	 * 
	 * @param result
	 * @return 上报失败并需要重试返回 UPLOAD_FAILED_RETRY，上报失败并不需要重试返回
	 *         UPLOAD_FAILED，上报成功返回UPLOAD_SUCCEED
	 */
	private int doUpload(MonitorResult[] results) {
		if (results == null || results.length == 0)
			return UPLOAD_SUCCEED;

		try {
			if (logger.isDebugEnabled()) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < results.length; i++)
					sb.append("\t[").append(i).append("]").append(results[i]).append("\n");
				logger.debug("采集结果上报开始：\n" + sb.toString());
			}
			List<PerfMetricVO> perfList = new ArrayList<PerfMetricVO>();
			List<CheckpointVO> stateList = new ArrayList<CheckpointVO>();
			for (MonitorResult result : results) {
				MonitorTask task = taskService.getTask(result.getTaskId());
				// 重启或者其它操作可能导致脏数据，对于获取不到task信息直接跳过
				if (task == null)
					continue;
				String nodeId = task.getNodeId();
				MonitorNode node = probeFactory.getNodeService().getNode(nodeId);
				if (node == null)
					continue;
				String typeId = task.getTypeId();
				String ip = task.getIp();
				String os = node.getOs();
				String resourceType = node.getTypeId();
				String host = task.getHost();
				String hostId = task.getNodeId();
				List<MonitorResultRow> rows = result.getRows();
				if (rows != null) {
					for (MonitorResultRow row : rows) {
						if (typeId.startsWith("VMWARE-VM")) {
							ip = (String) row.getIndicator(VCenterVmInfoMonitor.ITEM_VM_NET_IP_ADDRESS);
							os = (String) row.getIndicator(VCenterVmInfoMonitor.ITEM_VM_OS);
							resourceType = ResourceType.VM.getCode();
							host = row.getInstName();
							if ("未知".equals(host))
								host = null;
							hostId = row.getInstCode();
						} else if("HYPERVISOR-VCENTER-INFO".equalsIgnoreCase(typeId)) {
							ip = (String) row.getIndicator("HYPERVISOR-VCENTER-INFO-1");
							os = "ESXi";
							resourceType = ResourceType.SERVER.getCode();
							host = row.getInstName();
							if ("未知".equals(host))
								host = null;
							hostId = row.getInstCode();
						} else if ("HYPERVISOR-VCENTER-CPU".equalsIgnoreCase(typeId)
								|| "HYPERVISOR-VCENTER-RAM".equalsIgnoreCase(typeId)) {
							ip = row.getInstCode();
							os = "ESXi";
							hostId = row.getInstCode();
							resourceType = ResourceType.SERVER.getCode();
							host = row.getInstName();
							if ("未知".equals(host))
								host = null;
						} else if ("FusionCompute-Host".equalsIgnoreCase(typeId)) {
							ip = (String) row.getIndicator("ip");
							os = "FusionCompute";
							hostId = row.getInstCode();
							resourceType = ResourceType.SERVER.getCode();
							host = row.getInstName();
						} else if ("FUSIONMANAGER_LOCAL-Host".equalsIgnoreCase(typeId)) {
							ip = (String) row.getIndicator("ip");
							os = "FusionCompute";
							hostId = row.getInstCode();
							resourceType = ResourceType.SERVER.getCode();
							host = row.getInstName();
						} else if ("FusionCompute-Cluster".equalsIgnoreCase(typeId)) {
							String clusterName = row.getInstName();
							if ("null".equalsIgnoreCase(clusterName) && clusterName != null)
								row.addTag("clusterName:" + clusterName);
							ip = null;
							host = null;
							hostId = null;
						} else if ("FUSIONMANAGER_LOCAL-Cluster".equalsIgnoreCase(typeId)) {
							String clusterName = row.getInstName();
							if ("null".equalsIgnoreCase(clusterName) && clusterName != null)
								row.addTag("clusterName:" + clusterName);
							ip = null;
							host = null;
							hostId = null;
						} else if ("FusionCompute-VM".equalsIgnoreCase(typeId)) {
							ip = (String) row.getIndicator("ip");
							hostId = row.getInstCode();
							resourceType = ResourceType.VM.getCode();
							host = row.getInstName();
							os = (String) row.getIndicator("os");
						}else if ("FUSIONMANAGER_LOCAL-VM".equalsIgnoreCase(typeId)) {
							ip =null;
							hostId = row.getInstCode();
							resourceType = ResourceType.VM.getCode();
							host = row.getInstName();
							os = (String) row.getIndicator("os");
						}else if ("FUSIONMANAGER_TOP-VM".equalsIgnoreCase(typeId)) {
							ip = null;
							hostId = row.getInstCode();
							resourceType = ResourceType.VM.getCode();
							host = row.getInstName();
							os = (String) row.getIndicator("os");
						}else if(typeId.startsWith("XUGU-")){
							hostId = row.getInstCode();
							String nodeIp = (String) row.getIndicator("nodeIp");
							if(!StringUtils.isNullOrBlank(nodeIp))
								xugu_node_ip_id.put(hostId, nodeIp);
							ip = xugu_node_ip_id.get(hostId);
							host = hostId;
							resourceType = ResourceType.SERVER.getCode();
							// TODO 前方确定目前的操作系统为linux
							os = "linux";
						}
						// TODO 暂时为oracle实现单机多实例监测
						String sid = "";
						String service_name = "";
						if(typeId.startsWith("ORACLE-")){
							MonitorMethod method = probeFactory.getMethodService().getMethod(task.getMethodCode());
							sid = (String) method.getProperties().get("sid");
							service_name = (String) method.getProperties().get("service_name");
						}
						for (String key : row.keySet()) {
							if (key != null && key.startsWith("inst-")) {
								String metricName = key.substring(5);
								Object value = row.getIndicator(metricName);
								if (value == null)
									continue;
								RemoteMapper mapper = MetricMapper.getInstance().getRemoteMetricType(task.getTypeId(),
										metricName);
								// 处理ORACLE，DM数据库可用性
								createExtStateData(task, hostId, ip, row);
								if (mapper != null && StringUtils.isNotNullAndTrimBlank(mapper.getRemoteName())) {
									if (mapper.getMetricType().getIndex() == 0) {
										double val = Double.parseDouble(value.toString());
										PerfMetricVO metric = new PerfMetricVO();
										metric.setMetric(mapper.getRemoteName());
										metric.setTimestamp(System.currentTimeMillis());
										metric.setValue(val);
										metric.setHostId(hostId);
										metric.setHost(adapterHostName(host));
										Set<String> tags = new HashSet<String>(task.getTagList());
										// 动态监测指标
										String code = row.getInstCode();
										if(task.getTypeId().equalsIgnoreCase("CLI-PROCESS") && StringUtils.isNotBlank(code))
											tags.add("processName:" + code);
										if(task.getTypeId().startsWith("ORACLE") && StringUtils.isNotBlank(sid))
											tags.add("sid:" + sid);
										if(task.getTypeId().startsWith("ORACLE") && StringUtils.isNotBlank(service_name))
											tags.add("service_name:" + service_name);
										if (ip != null && !"null".equalsIgnoreCase(ip))
											tags.add("ip:" + ip);
										if (os != null)
											tags.add("os:" + os);
										if (resourceType != null)
											tags.add("resourceType:" + resourceType);
										tags.add("host:" + adapterHostName(host));
										//添加monitor result row中的tag
										tags.addAll(row.getTags());
										if(typeId.startsWith("VCENTER-VM"))
											tags.add("vmSource:vmware");
										List<String> tag_tmp = new ArrayList<String>(tags);
										metric.setTags(tag_tmp);
										perfList.add(metric);
										// 性能指标分批上报
										if (perfList.size() >= 100) {
											int index = perfList.size() / 100;
											if (index == 0) {
												boolean sign = postPerfMetrics(perfList);
												if (sign)
													perfList.clear();
											} else {
												List<PerfMetricVO> successList = new ArrayList<PerfMetricVO>();
												for (int i = 0; i < index; i++) {
													List<PerfMetricVO> perfList1 = perfList.subList(100 * i,
															100 * (i + 1));
													boolean sign = postPerfMetrics(perfList1);
													if (sign)
														successList.addAll(perfList1);
												}
												List<PerfMetricVO> perfList2 = new ArrayList<PerfMetricVO>(
														perfList.subList(100 * index, perfList.size()));
												boolean sign = postPerfMetrics(perfList2);
												if (sign)
													successList.addAll(perfList2);
												perfList.removeAll(successList);
											}
										}
									} else if (mapper.getMetricType().getIndex() == 1) {
										String name = mapper.getRemoteName();
										if (name == null || value == null)
											continue;
										if (name.length() >= 65)
											name = name.substring(0, 65);
										CheckpointVO checkPoint = new CheckpointVO();
										checkPoint.setHost_id(hostId);
										checkPoint.setTimestamp(System.currentTimeMillis());
										checkPoint.setState(name);
										String val = value.toString();
										if (val.length() >= 50)
											val = val.substring(0, 50);
										checkPoint.setValue(val);
										Set<String> tags = new HashSet<String>(task.getTagList());
										if (ip != null && !"null".equalsIgnoreCase(ip))
											tags.add("ip:" + ip);
										tags.add("host:" + adapterHostName(host));
										//添加monitor result row中的tag
										tags.addAll(row.getTags());
										if(typeId.startsWith("VCENTER-VM"))
											tags.add("vmSource:vmware");
										List<String> tag_tmp = new ArrayList<String>(tags);
										checkPoint.setTags(tag_tmp.toArray(new String[] {}));
										stateList.add(checkPoint);
										if (stateList.size() >= 50) {
											int index = stateList.size() / 50;
											if (index == 0) {
												boolean sign = postCheckPoints(stateList);
												if (sign)
													stateList.clear();
											} else {
												List<CheckpointVO> successList = new ArrayList<CheckpointVO>();
												for (int i = 0; i < index; i++) {
													List<CheckpointVO> stateList1 = stateList.subList(50 * i,
															50 * (i + 1));
													boolean sign = postCheckPoints(stateList1);
													if (sign)
														successList.addAll(stateList1);
												}
												List<CheckpointVO> stateList2 = new ArrayList<CheckpointVO>(
														stateList.subList(50 * index, stateList.size()));
												boolean sign = postCheckPoints(stateList2);
												if (sign)
													successList.addAll(stateList2);
												stateList.removeAll(successList);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			if (perfList.size() > 0) {
				boolean sign = postPerfMetrics(perfList);
				if (sign)
					perfList.clear();
			}
			if (stateList.size() > 0) {
				boolean sign = postCheckPoints(stateList);
				if (sign)
					stateList.clear();
			}
			if(stateMap.size() > 0){
				Set<String> keys = stateMap.keySet();
				List<CheckpointVO> list = new ArrayList<CheckpointVO>();
				for(String key:keys){
					CheckpointVO cp = new CheckpointVO();
					CheckpointVO cache = stateMap.get(key);
					if(cache == null) continue;
					BeanUtils.copyProperties(cache, cp);
					cache = null;
					list.add(cp);
				}
				if(list.size() > 0){
					boolean sign = postCheckPoints(list);
					list = null;
					stateMap.clear();
					if(logger.isDebugEnabled() && sign)
						logger.debug("ORACLE/DM数据库可用性数据上报完成");
					if(!sign)
						logger.debug("ORACLE/DM数据库可用性数据上报失败");
				}
			}
			
			uploadFailedLastCount = 0;
			if (logger.isDebugEnabled())
				logger.debug("采集结果上报完成");
			return UPLOAD_SUCCEED;
		} catch (Throwable e) {
			uploadTimeoutLastCount++;
			// 清空所有内容
			stateMap.clear();
			if (uploadTimeoutLastCount >= uploadTimeoutRetries) {
				ErrorUtil.warn(logger,
						String.format("采集结果上报失败[连续%d次]，放弃此部份数据：%s", uploadTimeoutLastCount, Arrays.toString(results)),
						e);
				uploadTimeoutLastCount = 0;
				return UPLOAD_FAILED;
			}
			return UPLOAD_FAILED_RETRY;
		}
	}

	private boolean postPerfMetrics(List<PerfMetricVO> list) {
		if (list.size() > 0) {
			boolean sign = metricService.postPerfMetrics(list);
			if (sign)
				logger.debug("性能指标上报成功,共成功:{}个", list.size());
			else
				logger.warn("性能指标上报失败,失败数量:{}个", list.size());
			
			return sign;
		}
		return true;

	}

	private boolean postCheckPoints(List<CheckpointVO> list) {
		if (list.size() > 0) {
			boolean sign = metricService.postCheckPoints(list);
			if (sign)
				logger.debug("状态指标上报成功,共成功:{}个", list.size());
			else
				logger.warn("状态指标上报失败,失败数量:{}个", list.size());
			return sign;
		}
		return true;
	}
	
	/**
	 * 发送上报状态概述信息
	 * @param stats
	 * @return
	 */
	public boolean postLinkStat(List<LinkStat> stats) {
		if (stats.size() > 0) {
			boolean sign = metricService.postLinkStat(stats);
			if (sign)
				logger.debug("链接概述信息上报成功,共成功:{}个", stats.size());
			else
				logger.error("链接概述信息上报失败,失败数量:{}个", stats.size());
			return sign;
		}
		return true;
	}

	/**
	 * 获取上报速率
	 * 
	 * @return 上报速率
	 */
	public Counter.Rate getUploadRate() {
		return uploadCounter.getIncreaseRate();
	}

	/*
	 * @see
	 * com.broada.srvmonitor.logic.MonitorResultUploadLogic#uploadMonitorResult
	 * (com.broada.srvmonitor.model.MonitorResult )
	 */
	public void upload(MonitorResult monitorResult) {
		if (!isRunning()) {
			logger.warn("提交监测结果失败。错误：监测结果上报线程未启动");
			return;
		}
		synchronized (queue) {
			queue.add(monitorResult);
			queue.notifyAll();
		}
	}

	public void setCacheUploadCountPer(int cacheUploadCountPer) {
		this.cacheUploadCountPer = cacheUploadCountPer;
	}

	public boolean uploadIsHealth() {
		return isRunning();
	}

	public int getQueueMaxLength() {
		return queueMaxLength;
	}

	public void setQueueMaxLength(int queueMaxLength) {
		this.queueMaxLength = queueMaxLength;
	}

	public int getUploadTryInterval() {
		return uploadTryInterval;
	}

	public void setUploadTryInterval(int uploadTryInterval) {
		this.uploadTryInterval = uploadTryInterval;
	}

	public long getUploadedCount() {
		return uploadedCount;
	}

	public int getWaitUploadCount() {
		return cacheCount + queue.size();
	}

	public int getUploadFailedLastCount() {
		return uploadFailedLastCount;
	}
	
	/**
	 * 创建扩展的状态指标数据(ORACLE,DM)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void createExtStateData(MonitorTask task, String hostId, String ip, MonitorResultRow row){
		String typeId = task.getTypeId();
		if(!(typeId.startsWith("ORACLE") || typeId.startsWith("DM"))) return ;
		int index = typeId.indexOf("-");
		String app = typeId.substring(0, (index > 0? index : 0)).toLowerCase();
		if(StringUtils.isBlank(app)) return ;
		RemoteMapper mapper = MetricMapper.getInstance().getRemoteMetricType((app + "-connect").toUpperCase(),
				app + ".can_connect");
		if(mapper == null) return;
		String name = mapper.getRemoteName();
		if (name == null)
			return ;
		if (name.length() >= 65)
			name = name.substring(0, 65);
		CheckpointVO checkPoint = stateMap.get(app);
		if(checkPoint == null){
			checkPoint = new CheckpointVO();
			checkPoint.setHost_id(hostId);
			checkPoint.setTimestamp(System.currentTimeMillis());
			checkPoint.setState(name);
			checkPoint.setValue("1");
			List<String> tags = task.getTagList();
			if (ip != null && !"null".equalsIgnoreCase(ip))
				tags.add("ip:" + ip);
			//添加monitor result row中的tag
//			tags.addAll(row.getTags());
			checkPoint.setTags(tags.toArray(new String[] {}));
		}else{
			// 更新标签和时间戳
			checkPoint.setTimestamp(System.currentTimeMillis());
			Set<String> set = new HashSet<String>();
			set.addAll(Arrays.asList(checkPoint.getTags()));
//			set.addAll(row.getTags());
			checkPoint.setTags(set.toArray(new String[0]));
		}
		stateMap.put(app, checkPoint);
	}

	public void setStateMap(Map<String, CheckpointVO> stateMap) {
		this.stateMap = stateMap;
	}

	public Map<String, CheckpointVO> getStateMap() {
		return stateMap;
	}

	public void setLastTestServerTime(long lastTestServerTime) {
		this.lastTestServerTime = lastTestServerTime;
	}

	public long getLastTestServerTime() {
		return lastTestServerTime;
	}

	public void setTestServerTimeInterval(long testServerTimeInterval) {
		this.testServerTimeInterval = testServerTimeInterval;
	}

	public long getTestServerTimeInterval() {
		return testServerTimeInterval;
	}

	public void setProbeCode(String probeCode) {
		this.probeCode = probeCode;
	}

	public String getProbeCode() {
		return probeCode;
	}
	
	/**
	 * 判断缓存是否有对应状态
	 * @param pluginName
	 * @param ip
	 * @return
	 */
	private boolean existOrNot(String pluginName, String ip){
		if(!verify_stat.isEmpty() && verify_stat.containsKey(pluginName) 
				&& verify_stat.get(pluginName).containsKey(ip))
			return true;
		return false;
	}
	
	/**
	 * 处理监测结果
	 * @param caches
	 */
	private void resolveStat(MonitorResultCache[] caches){
		if (caches == null || caches.length == 0) return;
		for(MonitorResultCache cache:caches){
			try{
				MonitorResult result = cache.getResult();
				MonitorTask task = taskService.getTask(result.getTaskId());
				// 重启或者其它操作可能导致脏数据，对于获取不到task信息直接跳过
				if (task == null) continue;
				MonitorNode node = probeFactory.getNodeService().getNode(task.getNodeId());
				int position = task.getTypeId().indexOf("-");
				String pluginName = task.getTypeId().substring(0, position > 0 ? position:task.getTypeId().length()).toLowerCase();
				// 节点为空或不需要校验本插件，或需要校验的插件的ip不是该节点的ip
				if (node == null || ips_verify.get(pluginName) == null || 
						(ips_verify.get(pluginName) != null && !ips_verify.get(pluginName).contains(node.getIp()))) continue;
				String stat = result.getState().name();
				if(stat.equalsIgnoreCase("SUCCESSED"))
					stat = CHECKED;
				else if(stat.equalsIgnoreCase("FAILED"))
					stat = FAILED;
				else
					stat = UNCHECKED;
				setVerifyStat(pluginName, node.getIp(), stat);
			}catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	/**
	 * 处理监测结果
	 * @param caches
	 */
	private void resolveStat(MonitorResult[] caches){
		if (caches == null || caches.length == 0) return;
		for(MonitorResult cache:caches){
			try{
				MonitorTask task = taskService.getTask(cache.getTaskId());
				// 重启或者其它操作可能导致脏数据，对于获取不到task信息直接跳过
				if (task == null) continue;
				MonitorNode node = probeFactory.getNodeService().getNode(task.getNodeId());
				int position = task.getTypeId().indexOf("-");
				String pluginName = task.getTypeId().substring(0, position > 0 ? position:task.getTypeId().length()).toLowerCase();
				if (node == null || ips_verify.get(pluginName) == null || 
						(ips_verify.get(pluginName) != null && !ips_verify.get(pluginName).contains(node.getIp()))) continue;
				String stat = cache.getState().name();
				if(stat.equalsIgnoreCase("SUCCESSED"))
					stat = CHECKED;
				else if(stat.equalsIgnoreCase("FAILED"))
					stat = FAILED;
				else
					stat = UNCHECKED;
				setVerifyStat(pluginName, node.getIp(), stat);
			}catch (Exception e) {
				logger.error(e.getMessage());
			}
			
		}
	}
	
	// 上传量不超过100
	public int uploadStat(Map<String, Map<String, String>> stats){
		List<LinkStat> lists = new ArrayList<LinkStat>();
		Map<String, Set<String>> plugin_ips = new HashMap<String, Set<String>>();
		for(Map.Entry<String, Map<String, String>> entryO:stats.entrySet()){
			Set<String> ips = new HashSet<String>();
			List<String> checkedList = new ArrayList<String>();
			List<String> failedList = new ArrayList<String>();
			LinkStat stat = new LinkStat(UUIDUtils.getProbeId());
			stat.setName(entryO.getKey());
			for(Map.Entry<String, String> entryI:stats.get(entryO.getKey()).entrySet()){
				if(StringUtil.isNullOrBlank(entryI.getValue())) continue;
				if(entryI.getValue().equals(UNCHECKED)) continue;
				else if(entryI.getValue().equals(CHECKED))
					checkedList.add(entryI.getKey());
				else if(entryI.getValue().equals(FAILED))
					failedList.add(entryI.getKey());
				// 删除ip
				ips.add(entryI.getKey());
				stats.get(entryO.getKey()).remove(entryI.getKey());
			}
			if(!ips.isEmpty()){
				plugin_ips.put(entryO.getKey(), ips);
				stat.setCheckedList(checkedList);
				stat.setFailedList(failedList);
				lists.add(stat);
				// 删除 pluginName
				stats.remove(entryO.getKey());
			}
		}
		try{
			if(lists.size() < 1) return UPLOAD_SUCCEED;
			boolean sign = metricService.postLinkStat(lists);
 			if(sign){
 				for(String key:ips_verify.keySet()){
 					for(String keyp:plugin_ips.keySet()){
 						if(!key.equalsIgnoreCase(keyp)) continue;
 						Set<String> ipsForVerify = ips_verify.get(key);   //  待校验的ip
 						ipsForVerify.removeAll(plugin_ips.get(keyp));    //  从待校验的ip中去掉已校验的ip
 						if(ipsForVerify.isEmpty()) ips_verify.remove(key);
 					}
 				}
 				logger.debug("成功上报" + lists.size() + "个链接概述信息");
 				return UPLOAD_SUCCEED;
 			}
		}catch (Exception e) {
			logger.error("链接概述信息上报失败...");
		}
		return UPLOAD_FAILED;
	}
	
	public String adapterHostName(String hostName){
		if(StringUtil.isNullOrBlank(hostName)) return "";
		return hostName.substring(0, hostName.length() > 64? 64:hostName.length());
	}
	
}