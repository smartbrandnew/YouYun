package com.broada.carrier.monitor.probe.impl.task.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.MonitorUtil;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.virtual.fusioncompute.FCServiceManager;
import com.broada.carrier.monitor.method.vmware.VSphereConnection;
import com.broada.carrier.monitor.method.vmware.VSphereException;
import com.broada.carrier.monitor.method.vmware.VSphereUtil;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.api.service.ProbeTaskService;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.openapi.entity.HostVO;
import com.broada.carrier.monitor.probe.impl.openapi.service.ResourceService;
import com.broada.carrier.monitor.probe.impl.util.PingUtil;
import com.broada.carrier.monitor.probe.impl.util.StringUtils;
import com.broada.carrier.monitor.probe.impl.yaml.ResourceType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.utils.StringUtil;
import com.huawei.esdk.fusioncompute.local.model.ClientProviderBean;
import com.huawei.esdk.fusioncompute.local.model.FCSDKResponse;
import com.huawei.esdk.fusioncompute.local.model.PageList;
import com.huawei.esdk.fusioncompute.local.model.common.LoginResp;
import com.huawei.esdk.fusioncompute.local.model.host.HostBasicInfo;
import com.huawei.esdk.fusioncompute.local.model.host.QueryHostListReq;
import com.huawei.esdk.fusioncompute.local.model.site.SiteBasicInfo;
import com.huawei.esdk.fusioncompute.local.model.vm.Nic;
import com.huawei.esdk.fusioncompute.local.model.vm.QueryVmsReq;
import com.huawei.esdk.fusioncompute.local.model.vm.VmConfig;
import com.huawei.esdk.fusioncompute.local.model.vm.VmInfo;
import com.huawei.esdk.fusioncompute.local.resources.common.AuthenticateResource;
import com.huawei.esdk.fusioncompute.local.resources.site.SiteResource;
import com.vmware.vim25.ArrayOfHostVirtualNic;
import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.HostVirtualNic;
import com.vmware.vim25.ManagedObjectReference;

public class ResourceUploadService {

	private final static Logger logger = LoggerFactory.getLogger(ResourceUploadService.class);
	@Autowired
	private ProbeServiceFactory probeFactory;
	@Autowired
	private ResourceService hostService;

	private static final String PROPERTY_HOST_UUID = "summary.hardware.uuid";
	private static final String PROPERTY_HOST_NAME = "summary.config.name";
	private static final String PROPERTY_HOST_VNIC = "config.network.vnic";
	private static final String PROPERTY_HOST_OS = "config.product.osType";
	private static final String PROPERTY_HOST_CONSOLE_VNIC = "config.network.consoleVnic";
	private static final String PROPERTY_HOST_POWERSTATE = "runtime.powerState";
	private static final String PROPERTY_HOST_VM = "vm";


	private static final String PROPERTY_VM_UUID = "config.uuid";
	private static final String PROPERTY_VM_NAME = "config.name";
	private static final String PROPERTY_VM_OS = "config.guestFullName";
	private static final String PROPERTY_VM_POWERSTATE = "runtime.powerState";
	private static final String PROPERTY_VM_IP_ADDRESS = "summary.guest.ipAddress";
	private static final String PROPERTY_VM_TEMPLATE = "config.template";

	private List<HostVO> getAllResource() {
		boolean virsualPlaform = false;
		List<HostVO> hosts = new ArrayList<HostVO>();
		MonitorNode[] nodes =  probeFactory.getNodeService().getNodes();
		for (MonitorNode node : nodes) {
			boolean skip = false;       // 该节点是否跳过上报
			String type = "";
			HostVO host = new HostVO();
			host.setId(node.getId());
			String hostname=node.getHost();
			if(StringUtils.isNullOrBlank(hostname))
				hostname=node.getIp();
			host.setName(adapterHostName(hostname));
			host.setIp(node.getIp());
			// 判断是云平台还是一般主机
			ProbeTaskService taskService = probeFactory.getTaskService();
			MonitorTask[] tasks = taskService.getTasks();
			List<MonitorMethod> methods = new ArrayList<MonitorMethod>();
			for(MonitorTask task:tasks){
				if(!task.getNodeId().equals(host.getId())) continue;   // 查询对应主机的方法
				MonitorMethod method = probeFactory.getMethodService().getMethod(task.getMethodCode());
				if(method != null && method.getTypeId().equalsIgnoreCase("ProtocolVmware")){
					virsualPlaform = true;
					methods.add(method);
				} else if(method.getTypeId().equalsIgnoreCase("ProtocolXugu")){
					skip = true;
				} else if(method.getTypeId().equalsIgnoreCase("ProtocolFusionCompute")){
					virsualPlaform = true;
					methods.add(method);
					type = "ProtocolFusionCompute";
				}
			}
			if(!virsualPlaform){
				if(skip) continue;
				host.setOnline_state(PingUtil.ping(host.getIp(), 3));
				if(!host.isOnline_state()) continue;
				List<String> tags = new ArrayList<String>();
				String tagStr = node.getTags();
				if (StringUtils.isNotNullAndTrimBlank(tagStr)) {
					String[] arr = tagStr.split(";");
					tags.addAll(Arrays.asList(arr));
				}
				host.setTags(tags);
				String os = node.getOs();   // agentless只会安装在windows或linux上
				if(StringUtil.isNullOrBlank(node.getTypeId()) ||
						node.getTypeId().equals(ResourceType.SERVER.getCode()))
					host.setType(ResourceType.SERVER.getCode());
				else 
					host.setType(node.getTypeId());
				host.setOs(os);
				host.setModified(new Date());
				hosts.add(host);
			} else if(StringUtil.isNullOrBlank(type)){
				// 云平台id取ip
				// 查询改主机下对应的在线虚拟机列表
				for(MonitorMethod method:methods){
					String userName = (String) method.getProperties().get("username");
					String password = (String) method.getProperties().get("password");
					String ip = host.getIp();
					hosts.addAll(collectVsphere(node, ip, userName, password));
				}
			} else if("ProtocolFusionCompute".equalsIgnoreCase(type)){
				// 处理FusionCompute
				for(MonitorMethod method:methods){
					String userName = (String) method.getProperties().get("username");
					String password = (String) method.getProperties().get("password");
					String port = String.valueOf(method.getProperties().get("port", 7443));
					String ip = host.getIp();
					hosts.addAll(collectFusionCompute(node, ip, port, userName, password));
				}
			}
		}
		return hosts;
	}

	/**
	 * 上报FusionCompute资源
	 * @param node
	 * @param ip
	 * @param port
	 * @param userName
	 * @param password
	 * @return
	 */
	private List<HostVO> collectFusionCompute(MonitorNode node,
			String ip, String port, String userName, String password) {
		ClientProviderBean bean  = new ClientProviderBean();
		bean.setServerIp(ip);
		bean.setServerPort(port);
		bean.setUserName(userName);
		return getFusionComputeHosts(node, bean, password);
	}

	public void uploadResource() {
		List<HostVO> hosts = getAllResource();
		//每批100、分批上报
		if (hosts.size() > 0) {
			int index = hosts.size() / 100;
			if (index == 0)
				hostService.postHosts(hosts);
			else {
				for (int i = 0; i < index; i++) {
					List<HostVO> list = hosts.subList(100 * i, 100 * (i + 1));
					hostService.postHosts(list);
				}
				hostService.postHosts(hosts.subList(100 * index, hosts.size()));
			}
		}
	}

	/**
	 * 获取vsphere平台的机器资源
	 */
	private List<HostVO> collectVsphere(MonitorNode node, String ipAddress, String userName, String password) 
	throws CollectException {
		// 再次区分是上报主机还是虚拟机
		File yamlDir = new File(Config.getYamlDir());
		File[] files = yamlDir.listFiles();
		boolean vmware = false;
		boolean hypervisor = false;
		if(files != null){
			for(File file:files){
				String fileName = file.getName().replace(".yaml", "");
				if(fileName.equalsIgnoreCase("hypervisor"))
					hypervisor = true;
				else if(fileName.equalsIgnoreCase("vmware"))
					vmware = true;
			}
		}
		List<HostVO> hosts = new ArrayList<HostVO>();
		if(hypervisor)
			hosts.addAll(collectHypervisor(node, ipAddress, userName, password));
		if(vmware)
			hosts.addAll(collectVmware(node, ipAddress, userName, password));
		return hosts;
	}

	/**
	 * 连接vSphere sdk
	 * @param ipAddress
	 * @param userName
	 * @param password
	 * @return
	 * @throws CollectException
	 */
	protected VSphereConnection connectVSphereSDK(String ipAddress, String userName , String password) throws CollectException {
		VSphereConnection connection = VSphereConnection.getVSphereConnection(VSphereConnection.getVSphereSDKUrl(ipAddress));
		try {
			connection.connect(userName, password);
		} catch (VSphereException e) {
			throw new CollectException("连接vSphere sdk失败", e);
		}
		return connection;
	}

	/**
	 * 根据esx的标准交换机和分布式交换机获取esx的ip地址
	 * 获取hypervisor的ip
	 * @param hostInfo
	 * @param vnicProperty
	 * @param consoleVNicProperty
	 * @return
	 */
	protected String getIpAdress(Map<String, Object> hostInfo, String vnicProperty, String consoleVNicProperty) {
		String ipAddr = MonitorConstant.UNKNOWN_STRING_VALUE;
		if (hostInfo.get(vnicProperty) != null)
			return getIpAdressFromHostVNic((ArrayOfHostVirtualNic) hostInfo.get(vnicProperty));
		if (MonitorUtil.isUnknownStringValue(ipAddr) && hostInfo.get(consoleVNicProperty) != null)
			return getIpAdressFromHostVNic((ArrayOfHostVirtualNic) hostInfo.get(consoleVNicProperty));
		return ipAddr;
	}

	/**
	 * 从虚拟网卡列表中获取ip
	 * @param vNics
	 * @return
	 */
	private String getIpAdressFromHostVNic(ArrayOfHostVirtualNic vNics) {
		String ipAddress = MonitorConstant.UNKNOWN_STRING_VALUE;
		int lastKeyNum = Integer.MAX_VALUE;
		if (vNics.getHostVirtualNic() != null) {
			for (HostVirtualNic nic : vNics.getHostVirtualNic()) {
				String key = nic.getKey();
				int index = key.indexOf("vmk");
				if (index != -1) {
					int keyNum = Integer.parseInt(key.substring(index + 3));
					if (keyNum < lastKeyNum) {
						lastKeyNum = keyNum;
						ipAddress = nic.getSpec().getIp().getIpAddress();
					}
				}
			}
		}
		return ipAddress;
	}

	public String adapterHostName(String hostName){
		if(StringUtil.isNullOrBlank(hostName) || hostName.equals("未知")) return "";
		return hostName.substring(0, hostName.length() > 64? 64:hostName.length());
	}

	/**
	 * 获取FusionCompute监测到的主机和虚拟机信息
	 * @return
	 */
	private List<HostVO> getFusionComputeHosts(MonitorNode node, ClientProviderBean bean, String password){
		List<HostVO> hosts = new ArrayList<HostVO>();
		AuthenticateResource authenticateResource = FCServiceManager.getUserService(bean);
		if(authenticateResource == null){
			logger.error("Failed to get authenticateResource object!");
			return hosts;
		}
		FCSDKResponse<LoginResp> loginResp = authenticateResource.login(bean.getUserName(), password);
		if (!"00000000".equals(loginResp.getErrorCode())) {
			// 鉴权失败
			logger.error("Failed to Login FC System!");
			return hosts;
		}
		SiteResource siteResource = FCServiceManager.getSiteResource(bean);
		if(siteResource == null){
			logger.error("Failed to get siteResource object!");
			return hosts;
		}
		FCSDKResponse<List<SiteBasicInfo>> sites = siteResource.querySites();
		if(sites != null){
			List<SiteBasicInfo> siteInfos = sites.getResult();
			if(siteInfos != null && !siteInfos.isEmpty()){
				for(SiteBasicInfo siteInfo : siteInfos){
					FCSDKResponse<PageList<HostBasicInfo>> hostInfos = FCServiceManager.getHostResource(bean)
					.queryHostList(siteInfo.getUrn(), new QueryHostListReq());
					if(hostInfos != null){
						PageList<HostBasicInfo> pages = hostInfos.getResult();
						if(pages != null){
							List<HostBasicInfo> baseInfos = pages.getList();
							if(baseInfos != null && !baseInfos.isEmpty()){
								for(HostBasicInfo baseInfo:baseInfos){
									HostVO vo = genarateFusionComputeHost(node, baseInfo);
									if(vo != null)
										hosts.add(vo);
								}
							}
						}
						logger.info("查询到FusionCompute主机数:" + hosts.size());
					}
					int hostNum = hosts.size();
					FCSDKResponse<PageList<VmInfo>> vmInfos = FCServiceManager.getVmResource(bean)
					.queryVMs(new QueryVmsReq(), siteInfo.getUrn());
					if(vmInfos != null){
						PageList<VmInfo> pages = vmInfos.getResult();
						if(pages != null){
							List<VmInfo> baseInfos = pages.getList();
							if(baseInfos != null && !baseInfos.isEmpty()){
								for(VmInfo baseInfo:baseInfos){
									HostVO vo = genarateFusionComputeVM(node, baseInfo);
									if(vo != null)
										hosts.add(vo);
								}
							}
						}
						logger.info("查询到FusionCompute虚拟机数:" + (hosts.size() - hostNum));
					}
				}
			}
		}
		return hosts;
	}

	/**
	 * 封装虚拟机
	 * @param node
	 * @param baseInfo
	 * @return
	 */
	private HostVO genarateFusionComputeVM(MonitorNode node, VmInfo baseInfo) {
		HostVO host = new HostVO();
		String id = "";
		id = baseInfo.getUuid();
		if(StringUtil.isNullOrBlank(id))
			id = baseInfo.getUrn();
		if(StringUtil.isNullOrBlank(id)) return null;
		host.setId(id);
		host.setName(baseInfo.getName());
		String ip = getFusionComputeVmIp(baseInfo);
		if(StringUtil.isNullOrBlank(ip)) return null;
		if(ip.equals(HostIpUtil.getZeroIP()))
			logger.info("发现虚拟机:" + baseInfo.getName() + "的ip为:"+HostIpUtil.getZeroIP());
		host.setIp(ip);
		host.setModified(new Date());
		host.setOnline_state(ip.equals(HostIpUtil.getZeroIP())? true:PingUtil.ping(ip, 3));
		if(!host.isOnline_state()) return null;
		List<String> tags = new ArrayList<String>();
		String tagStr = node.getTags();
		if (StringUtils.isNotNullAndTrimBlank(tagStr)) {
			String[] arr = tagStr.split(";");
			tags.addAll(Arrays.asList(arr));
		}
		host.setTags(tags);
		host.setType(ResourceType.VM.getCode());
		if(baseInfo.getOsOptions() != null)
			host.setOs(baseInfo.getOsOptions().getOsType());
		logger.info("查询到fc云平台一台虚拟机:" + host);
		return host;
	}

	/**
	 * 封装host
	 * @param baseInfo
	 * @return
	 */
	private HostVO genarateFusionComputeHost(MonitorNode node, HostBasicInfo baseInfo) {
		HostVO host = new HostVO();
		host.setId(baseInfo.getUrn());
		host.setName(baseInfo.getName());
		if(StringUtil.isNullOrBlank(baseInfo.getIp())) return null;
		if(baseInfo.getIp().equals(HostIpUtil.getZeroIP()))
			logger.info("发现主机:" + baseInfo.getName() + "的ip为:"+HostIpUtil.getZeroIP());
		host.setIp(baseInfo.getIp());
		host.setModified(new Date());
		host.setOnline_state(baseInfo.getIp().equals(HostIpUtil.getZeroIP())?true:PingUtil.ping(baseInfo.getIp(), 3));
		if(!host.isOnline_state()) return null;
		List<String> tags = new ArrayList<String>();
		String tagStr = node.getTags();
		if (StringUtils.isNotNullAndTrimBlank(tagStr)) {
			String[] arr = tagStr.split(";");
			tags.addAll(Arrays.asList(arr));
		}
		host.setTags(tags);
		host.setType(ResourceType.SERVER.getCode()); // 不能探明操作系统
		logger.info("检测到fc云平台一台主机:" + host);
		return host;
	}

	/**
	 * 获取虚拟机Ip
	 * @return
	 */
	private String getFusionComputeVmIp(VmInfo vmInfo){
		String[] ips = null;
		VmConfig cfg = vmInfo.getVmConfig();
		if(cfg != null){
			List<Nic> nics = cfg.getNics();
			if(nics != null && !nics.isEmpty()){
				ips = new String[nics.size()];
				for(int i=0;i<nics.size();i++)
					ips[i] = nics.get(i).getIp();
			}
		}
		// 虚拟机有ip
		if(ips == null)
			return null;
		if(ips.length == 1)
			return ips[0];
		else
			return getMinIp(ips);
	}

	/**
	 * 返回最小ip
	 * @param ips
	 * @return
	 */
	private String getMinIp(String[] ips){
		String ipMin = ips[0];
		String[] ipArray = ipMin.split("\\.");
		for(int i=1;i<ips.length;i++){
			String[] currArray = ips[i].split("\\.");
			for(int j=0;j<4;j++){
				if(Integer.valueOf(ipArray[j]).intValue() > Integer.valueOf(currArray[j]).intValue()){
					ipMin = ips[i];
					ipArray = ipMin.split("\\.");
					break;
				} else if(Integer.valueOf(ipArray[j]).intValue() == Integer.valueOf(currArray[j]).intValue())
					continue;
				else
					break;
			}
		}
		return ipMin;
	}

	/**
	 * 上报hypervisor主机信息
	 * @param node
	 * @param ipAddress
	 * @param userName
	 * @param password
	 * @return
	 * @throws CollectException
	 */
	private List<HostVO> collectHypervisor(MonitorNode node, String ipAddress, String userName, 
			String password) throws CollectException {
		List<HostVO> hosts = new ArrayList<HostVO>();
		Map<ManagedObjectReference, Map<String, Object>> infoMap = null;
		VSphereConnection connection = null;
		try {
			connection = connectVSphereSDK(ipAddress, userName, password);
			String[][] VM_INFO = new String[][] {  new String[] {"HostSystem", PROPERTY_HOST_UUID, PROPERTY_HOST_NAME, 
					PROPERTY_HOST_VNIC, PROPERTY_HOST_CONSOLE_VNIC, PROPERTY_HOST_VM, PROPERTY_HOST_POWERSTATE, PROPERTY_HOST_OS}};
			infoMap = VSphereUtil.retrieveProperties(connection, VM_INFO);
		} catch (VSphereException e) {
			throw new CollectException("获取主机基本信息[uuid]失败", e);
		} finally {
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (VSphereException e) {
				throw new CollectException("断开vSphere连接失败", e);
			}
		}
		if(infoMap != null){
			for(ManagedObjectReference object:infoMap.keySet()){
				Map<String, Object> hostInfo = infoMap.get(object);
				if(hostInfo.get(PROPERTY_HOST_POWERSTATE) != null){
					HostSystemPowerState state = (HostSystemPowerState) hostInfo.get(PROPERTY_HOST_POWERSTATE);
					if(!state.getValue().equals("poweredOn")) continue;
					String hostIp = getIpAdress(hostInfo, PROPERTY_HOST_VNIC, PROPERTY_HOST_CONSOLE_VNIC);
					if(StringUtils.isBlank(hostIp)) continue;   // IP为空的不上传
					HostVO h_host = new HostVO();
					h_host.setOnline_state(true);
					h_host.setId(hostIp);
					h_host.setName(adapterHostName(hostInfo.get(PROPERTY_VM_NAME) == null ? "未知" :MonitorUtil.getAvailableStringValue(hostInfo.get(PROPERTY_VM_NAME))));
					h_host.setIp(hostIp);
					h_host.setModified(new Date());
					List<String> tags = new ArrayList<String>();
					String tagStr = node.getTags();
					if (StringUtils.isNotNullAndTrimBlank(tagStr)) {
						String[] arr = tagStr.split(";");
						tags.addAll(Arrays.asList(arr));
					}
					h_host.setTags(tags);
					h_host.setType(ResourceType.SERVER.getCode());
					h_host.setOs(MonitorUtil.getAvailableStringValue(hostInfo.get(PROPERTY_HOST_OS)));
					hosts.add(h_host);
				}
			}
		}
		return hosts;
	}

	/**
	 * 上报vmware主机信息
	 * @param node
	 * @param ipAddress
	 * @param userName
	 * @param password
	 * @return
	 * @throws CollectException
	 */
	private List<HostVO> collectVmware(MonitorNode node, String ipAddress, String userName, 
			String password) throws CollectException {
		List<HostVO> hosts = new ArrayList<HostVO>();
		Map<ManagedObjectReference, Map<String, Object>> infoMap = null;
		VSphereConnection connection = null;
		try {
			connection = connectVSphereSDK(ipAddress, userName, password);
			String[][] VM_INFO = new String[][] { new String[] { "VirtualMachine", PROPERTY_VM_UUID, PROPERTY_VM_NAME,
					PROPERTY_VM_OS, PROPERTY_VM_POWERSTATE, PROPERTY_VM_IP_ADDRESS, PROPERTY_VM_TEMPLATE}};
			infoMap = VSphereUtil.retrieveProperties(connection, VM_INFO);
		} catch (VSphereException e) {
			throw new CollectException("获取虚拟机机基本信息[uuid]失败", e);
		} finally {
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (VSphereException e) {
				throw new CollectException("断开vSphere连接失败", e);
			}
		}
		if(infoMap != null){
			for(ManagedObjectReference object:infoMap.keySet()){
				Map<String, Object> vmInfo = infoMap.get(object);
				if(vmInfo.get(PROPERTY_VM_TEMPLATE) != null && 
						"true".equals(vmInfo.get(PROPERTY_VM_TEMPLATE).toString()))
					continue;
				String vm_state = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_POWERSTATE));
				if(!vm_state.equalsIgnoreCase("poweredOn")) continue;
				String vm_name = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_NAME));
				String vm_os = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_OS));
				String vm_ip = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_IP_ADDRESS));
				if(StringUtils.isBlank(vm_ip)) continue;
				HostVO v_host = new HostVO();
				v_host.setOnline_state(true);
				String hostId = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_UUID)) 
				+ MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_NAME));
				v_host.setId(hostId);
				v_host.setName(adapterHostName(StringUtil.isNullOrBlank(vm_name) ? "未知" : vm_name ));
				v_host.setIp(vm_ip);
				v_host.setModified(new Date());
				List<String> tags = new ArrayList<String>();
				String tagStr = node.getTags();
				if (StringUtils.isNotNullAndTrimBlank(tagStr)) {
					String[] arr = tagStr.split(";");
					tags.addAll(Arrays.asList(arr));
				}
				v_host.setTags(tags);
				v_host.setType(ResourceType.VM.getCode());
				v_host.setOs(vm_os);
				hosts.add(v_host);
			}
		}
		return hosts;
	}

}
