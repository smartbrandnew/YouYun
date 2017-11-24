package uyun.bat.gateway.agent.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceDetail;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.gateway.agent.entity.ResourceDetailVO;
import uyun.bat.gateway.agent.entity.ResourceInfo;
import uyun.bat.gateway.agent.entity.ResourceInfoVO;
import uyun.bat.gateway.agent.entity.Summary;
import uyun.bat.gateway.agent.entity.newentity.Device1;
import uyun.bat.gateway.api.service.ServiceManager;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.druid.util.StringUtils;

/**
 * 解析设备详情工具类
 */
public class HostDetail {
	private static final String en_to_ch = "{\"vendor_id\": \"厂商\",\"model_name\": \"型号\",\"cpu_cores\": \"物理核数\",\"cpu_logical_processors\": \"逻辑核数\",\"mhz\": \"主频(mhz)\",\"cache_size\": \"缓存大小\",\"total\": \"物理内存\",\"swap_total\": \"交换分区\",\"ipaddress\": \"IP\",\"ipaddressv6\": \"IPV6\",\"macaddress\": \"MAC\",\"os\": \"操作系统\",\"hostname\": \"主机名称\",\"kernel_name\": \"内核\",\"kernel_release\": \"内核版本\",\"kernel_version\": \"构建版本\",\"machine\": \"硬件平台\",\"machine_type\":\"设备类型\"}";
	private static final Map<String, String> enToChMap = (Map<String, String>) JSONUtils.parse(en_to_ch);

	public static ResourceDetailVO getResourceDetailById(String tenantId, String resourceId) {
		Resource res = ServiceManager.getInstance().getResourceService().queryResById(resourceId, tenantId);
		if (res == null)
			return new ResourceDetailVO();
		List<String> tags = new ArrayList<>();
		// 网络设备的摘要
		String devSuma = null;
		if (res.getTags() != null && res.getTags().size() > 0)
			for (ResourceTag tag : res.getTags()) {
				String t = tag.getKey() + ":" + tag.getValue();
				if (tag.getKey().equals("producer"))
					devSuma = tag.getValue();
				tags.add(t);
			}
		boolean onlineState = res.getOnlineStatus().getName().equals("在线") ? true : false;
		ResourceDetail resDetail = ServiceManager.getInstance().getResourceService().queryByResId(resourceId);
		if (resDetail == null) {
			Device1 d = new Device1(onlineState, tags);
			return new ResourceDetailVO(d);
		}
		List<ResourceInfo> resInfoList = new ArrayList<>();
		if (res.getResourceTypeName().equals("网络设备")) {
			Device1 d = new Device1(devSuma, onlineState, tags, resDetail.getAgentDesc());
			ResourceInfo r = new ResourceInfo("设备描述", resDetail.getDetail());
			resInfoList.add(r);
			return new ResourceDetailVO(d, resInfoList);
		}
		if (resDetail.getDetail().length() == 0) {
			Device1 d = new Device1("", onlineState, tags, resDetail.getAgentDesc());
			return new ResourceDetailVO(d);
		}
		Map<String, Object> map = (Map<String, Object>) JSONUtils.parse(resDetail.getDetail());
		// 对标签进行排序
		List<String> listName = sort(map.keySet());
		// 显示摘要格式如：Linux, 4 CPU, 4GB 内存, 256GB 磁盘, 10.1.1.1
		StringBuilder sbBig = new StringBuilder();
		for (String key : listName) {
			Map<String, String> map1 = new HashMap<String, String>();
			if (!key.equals("filesystem")) {
				map1 = (Map<String, String>) map.get(key);
				ResourceInfoVO infoVo = myIterator(map1, key);
				if (infoVo.getSuma().getType() != null)
					sbBig.append(infoVo.getSuma().getType() + ", ");
				if (infoVo.getSuma().getOs() != null)
					sbBig.append(infoVo.getSuma().getOs() + ", ");
				if (infoVo.getSuma().getCores() != null)
					sbBig.append(infoVo.getSuma().getCores() + " CPU, ");
				if (infoVo.getSuma().getTotal() != null)
					sbBig.append(infoVo.getSuma().getTotal() + " 内存, ");
				resInfoList.add(new ResourceInfo(infoVo.getName(), infoVo.getAttr()));
			} else {
				// 将磁盘按顺序排序 ;
				List<Disk> list = sortDisk((List<Object>) map.get(key));
				StringBuilder sb1 = new StringBuilder();
				double d = 0d;
				DecimalFormat dcmFmt = new DecimalFormat("0.0");
				for (Disk disk : list) {
					String value = dcmFmt.format(Double.parseDouble(disk.getKbSize().equals("Unknown") ? "0" : disk.getKbSize())
							/ (1024 * 1024));
					sb1.append(disk.getName() + " mounted on " + disk.getMountedOn() + " " + value + "GB");
					sb1.append(",");
					d += Double.parseDouble(value);
				}
				String str = sb1.deleteCharAt(sb1.length() - 1).toString();
				sbBig.append(dcmFmt.format(d) + "GB" + " 磁盘, ");
				ResourceInfo rInfo = new ResourceInfo();
				rInfo.setName("文件系统");
				rInfo.setAttr(str);
				resInfoList.add(rInfo);
			}

		}
		if (res.getIpaddr() != null && !res.getIpaddr().equals("unknown"))
			sbBig.append(res.getIpaddr() + ", ");
		String desc = sbBig.deleteCharAt(sbBig.length() - 2).toString();
		Device1 dev = new Device1(desc, onlineState, tags, resDetail.getAgentDesc());
		ResourceDetailVO detailVO = new ResourceDetailVO();
		detailVO.setDev(dev);
		detailVO.setInfo(resInfoList);
		return detailVO;
	}

	private static final String[] TOPIC = new String[] { "platform", "cpu", "memory", "filesystem", "network" };

	private static List<String> sort(Set<String> set) {
		// 对字段进行按操作系统，cpu,内存，文件系统，网络排序
		List<String> list = new ArrayList<>();
		for (String temp : TOPIC) {
			if (set.contains(temp))
				list.add(temp);
		}
		return list;
	}

	private static class Disk implements Comparable<Disk> {
		private String kbSize;
		private String mountedOn;
		private String name;

		public String getMountedOn() {
			return mountedOn;
		}

		public String getKbSize() {
			return kbSize;
		}

		public String getName() {
			return name;
		}

		public Disk(String kbSize, String mountedOn, String name) {
			super();
			this.kbSize = kbSize;
			this.mountedOn = mountedOn;
			this.name = name;
		}

		private static Disk generate(Map<String, String> map) {
			Disk disk = new Disk(map.get("kb_size"), map.get("mounted_on"), map.get("name"));
			return disk;
		}

		@Override
		public int compareTo(Disk d) {
			return this.mountedOn.compareToIgnoreCase(d.getMountedOn());
		}
	}

	private static List<Disk> sortDisk(List<Object> l) {
		List<Disk> list = new ArrayList<>();
		for (Object o : l) {
			if (!(o instanceof Map))
				continue;
			Disk d = Disk.generate((Map<String, String>) o);
			list.add(d);
		}
		Collections.sort(list);
		return list;
	}

	private static ResourceInfoVO myIterator(Map<String, String> map, String key) {
		String name = key.equals("platform") ? "操作系统" : (key.equals("cpu") ? "处理器" : (key.equals("memory") ? "内存" : "网络"));
		ResourceInfoVO resInfo = new ResourceInfoVO(name);
		Summary suma = new Summary();
		DecimalFormat dcmFmt = new DecimalFormat("0.0");

		Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
		if (map.get("total") != null) {
			// 内存linux为KB window为B
			String mem = map.get("total").indexOf("kB") != -1 ? dcmFmt.format(Double.parseDouble(map.get("total").substring(
					0, map.get("total").length() - 2))
					/ (1024 * 1024)) : dcmFmt.format(Double.parseDouble(map.get("total")) / (1024 * 1024 * 1024));
			suma.setTotal(mem + "GB");
		}
		suma.setCores(map.get("cpu_cores"));
		suma.setOs(map.get("os"));
		if (null != map.get("machine_type"))
			suma.setType(map.get("machine_type").replace("VM", "虚拟机").replace("PM", "物理机"));
		resInfo.setSuma(suma);
		StringBuilder sb = new StringBuilder();
		while (entries.hasNext()) {
			Map.Entry<String, String> entry = entries.next();
			if (!key.equals("filesystem") && StringUtils.isEmpty(enToChMap.get(entry.getKey())))
				continue;
			sb.append(enToChMap.get(entry.getKey()));
			sb.append("：");
			if (!key.equals("memory"))
				sb.append(entry.getValue().replace("VM", "虚拟机").replace("PM", "物理机"));
			else {
				// 内存linux为KB window为B
				sb.append(entry.getValue().indexOf("kB") != -1 ? dcmFmt.format(Double.parseDouble(entry.getValue().substring(0,
						entry.getValue().length() - 2))
						/ (1024 * 1024))
						+ "GB" : dcmFmt.format(Double.parseDouble(entry.getValue()) / (1024 * 1024 * 1024)) + "GB");
			}
			sb.append(",");
		}
		String attr = sb.deleteCharAt(sb.length() - 1).toString();
		resInfo.setAttr(attr);
		return resInfo;
	}

}
