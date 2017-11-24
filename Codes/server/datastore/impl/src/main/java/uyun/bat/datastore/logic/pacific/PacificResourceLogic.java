package uyun.bat.datastore.logic.pacific;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.alibaba.druid.support.json.JSONUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.datastore.api.entity.*;
import uyun.bat.datastore.api.service.PacificResourceService;
import uyun.bat.datastore.dao.ResourceDetailDao;
import uyun.bat.datastore.dao.SimpleResourceDao;
import uyun.bat.datastore.entity.MetricSpanTime;
import uyun.bat.datastore.entity.ResourceIdTransform;
import uyun.bat.datastore.entity.SimpleResourceQuery;
import uyun.bat.datastore.entity.PacificResAttr;
import uyun.bat.datastore.logic.ResourceIdTransformLogic;
import uyun.bat.datastore.service.PacificManager;
import uyun.pacific.api.error.ClientException;
import uyun.pacific.api.query.QueryOperator;
import uyun.pacific.api.query.QueryParam;
import uyun.pacific.api.query.QueryParams;
import uyun.pacific.api.query.QueryResult;
import uyun.pacific.api.support.Identifier;
import uyun.pacific.api.support.NamedValue;
import uyun.pacific.api.support.OperationResult;
import uyun.pacific.api.support.OperationResult.ResultItem;
import uyun.pacific.api.support.ResOwner;
import uyun.pacific.resource.api.entity.AttributeValue;
import uyun.pacific.resource.api.entity.Tag;
import uyun.pacific.resource.api.entity.object.ResObject;
import uyun.pacific.resource.api.entity.object.ResSource;

public class PacificResourceLogic implements PacificResourceService {

	private static final Logger LOG = LoggerFactory.getLogger(PacificResourceLogic.class);

	@Autowired
	private SimpleResourceDao simpleResourceDao;
	@Autowired
	private ResourceDetailDao resourceDetailDao;
	@Autowired
	private PacificManager pacificManager ;
	@Autowired
	private ResourceIdTransformLogic resourceIdTransformLogic;

	public boolean delete(String tenantId, String resourceId) {

		Identifier identifier = new Identifier();
		identifier.setId(resourceId);
		ResOwner resOwner = ResOwner.MONITOR;
		OperationResult result = pacificManager.getPacificResObjectService().removeOwners(tenantId,
				new Identifier[] { identifier }, resOwner);
		if (result != null) {
			boolean sign = result.hasError();
			return !sign;
		}
		return true;
	}

	public String save(Resource resource) {
		ResObject resObject = changeResourceToResObject(resource);
		if(resObject == null) return null;
		String pacificResId = null;
		try {
			OperationResult result = pacificManager.getPacificResObjectService().saveResObject(resource.getTenantId(),
					resObject, false);
			if (result != null && result.getItems() != null && result.getItems().length > 0) {
				 pacificResId = result.getItems()[0].getItem();
			}
		} catch (Throwable e) {
			LOG.error("Store saveResObject error...", e);
		}
		return pacificResId;
	}

	private String getNetWorkClassCode(Resource res) {
		List<String> list = res.getResTags();
		for (String str : list) {
			if ("equipment:Router".equalsIgnoreCase(str))
				return PacificResAttr.ROUTER_CLASSCODE;
			else if ("equipment:Switch".equalsIgnoreCase(str))
				return PacificResAttr.SWITCHER_CLASSCODE;
		}
		return PacificResAttr.ROUTER_CLASSCODE;
	}

	private ResObject changeResourceToResObject(Resource resource) {
		if (resource == null) {
			return null;
		}
		String tenantId = resource.getTenantId();
		ResObject resObject = new ResObject();
		String classCode;
		if (resource.getType() != null) {
			if (ResourceType.SERVER.getId() == resource.getType().getId()){
				classCode = PacificResAttr.SERVER_CLASSCODE;
			}
			else if (ResourceType.VM.getId() == resource.getType().getId()){
				classCode = PacificResAttr.VM_CLASSCODE;
			}
			else {
				classCode = getNetWorkClassCode(resource);
			}
		} else {
			classCode = PacificResAttr.SERVER_CLASSCODE;
		}
		resObject.setClassCode(classCode);
		resObject.setTenantId(tenantId);
		resObject.setUpdateTime(resource.getModified());
		resObject.setCreateTime(resource.getCreateTime());
		ResSource source = new ResSource(ResOwner.MONITOR.name());
		source.setTime(new Date());
		resObject.setSources(Collections.singletonList(source));
		//TODO 170523 PM和DM说标签以store为准即只做store覆盖到monitor
		//List<Tag> tags = changeToTagList(resource.getResTagsAll());
		//resObject.setTags(tags);
		Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
		try {
			attributes = generateResDetailAttr(resource, classCode);
		} catch (Exception e) {
			LOG.warn("同步 store 异常/丰富资源信息异常", e);
		}
		// 服务器和虚拟机才有操作系统
		if (resource.getOs() != null && (PacificResAttr.SERVER_CLASSCODE.equals(classCode) || PacificResAttr.VM_CLASSCODE.equals(classCode))) {
			attributes.put(PacificResAttr.ATTRIBUTE_OS, new AttributeValue(resource.getOs()));
		}
		attributes.put(PacificResAttr.ATTRIBUTE_IPADDR, new AttributeValue(resource.getIpaddr()));
		attributes.put(PacificResAttr.ATTRIBUTE_HOSTNAME, new AttributeValue(resource.getHostname()));
		resObject.setAttrValues(attributes);
		return resObject;
	}

	// 统一资源库对应编码的属性参数
	public Map<String, AttributeValue> generateResDetailAttr(Resource resource, String classCode) {
		Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
		ResourceDetail resDetail = resourceDetailDao.queryByResId(resource.getId());
		// 暂时只考虑服务器和虚拟机
		if (resDetail != null && (PacificResAttr.SERVER_CLASSCODE.equals(classCode) || PacificResAttr.VM_CLASSCODE.equals(classCode))) {
			String detail = resDetail.getDetail();
			Map<String, Object> detailMap = (Map<String, Object>) JSONUtils.parse(detail);
			Map<String, Object> cpuMap = (Map<String, Object>) detailMap.get("cpu");
			if (cpuMap != null) {
				if (cpuMap.get("cpu_cores") != null) {
					String cpuCores = PacificResAttr.SERVER_CLASSCODE.equals(classCode) ? PacificResAttr.ATTRIBUTE_CPU_CORES : PacificResAttr.ATTRIBUTE_VM_CPU_CORES;
					attributes.put(cpuCores, new AttributeValue(Integer.valueOf(cpuMap.get("cpu_cores").toString())));
				}
				//VM没有cpuModel
				if (cpuMap.get("model_name") != null && PacificResAttr.SERVER_CLASSCODE.equals(classCode)) {
					attributes.put(PacificResAttr.ATTRIBUTE_CPU_MODEL,
							new AttributeValue(cpuMap.get("model_name").toString()));
				}
				//VM没有cpuFrequency
				if (cpuMap.get("mhz") != null && PacificResAttr.SERVER_CLASSCODE.equals(classCode)) {
					try {
						Double mhz = Double.valueOf(cpuMap.get("mhz").toString());
						Double ghz = mhz/1000;
						BigDecimal bd = new BigDecimal(ghz).setScale(2, RoundingMode.HALF_UP);
						attributes.put(PacificResAttr.ATTRIBUTE_CPU_FREQUENCY,
                                new AttributeValue(bd.doubleValue()));
					} catch (Exception e) {
						// 数据格式转化异常无视
					}
				}
			}
			Map<String, Object> memoryMap = (Map<String, Object>) detailMap.get("memory");
			if (memoryMap != null) {
				if (memoryMap.get("total") != null) {
					String total = memoryMap.get("total").toString();
					try {
						Double val;
						// 目前memory.total两种格式: xxxxkB/xxxx 不带单位默认为byte
						if (total.contains("kB")) {
							val = Double.valueOf(total.split("kB")[0]);
							val = val / (1024 * 1024);
						} else {
							val = Double.valueOf(total);
							val = val / (1024 * 1024 * 1024);
						}

						BigDecimal bd = new BigDecimal(val).setScale(2, RoundingMode.HALF_UP);
						attributes.put(PacificResAttr.ATTRIBUTE_MEM_SIZE, new AttributeValue(bd.doubleValue()));
					} catch (Exception e) {
						// 数据格式转化异常无视
					}
				}
			}
			if (detailMap.get("filesystem") instanceof ArrayList) {
				List fileSystems = (ArrayList) detailMap.get("filesystem");
				if (fileSystems.size() > 0) {
					Double val = 0.0;
					for (Object o : fileSystems) {
						Map<String, Object> one = (Map<String, Object>) o;
						if (one == null || one.get("kb_size") == null)
							continue;
						String valStr = one.get("kb_size").toString();
						Double d = 0.0;
						try {
							d = Double.valueOf(valStr);
							val = val + d;
						} catch (Exception e) {
							// 数据格式转化异常无视
						}
					}
					val = val / (1024 * 1024);
					BigDecimal bd = new BigDecimal(val).setScale(2, RoundingMode.HALF_UP);
					attributes.put(PacificResAttr.ATTRIBUTE_STORAGE_SIZE, new AttributeValue(bd.doubleValue()));
				}
			}
		}
		return attributes;
	}

	// 资源模型缺少字段、另关键属性目前只有IP，查询暂时用不到
	private Resource changeResObjectToResource(ResObject resObject) {
		if (resObject == null)
			return null;
		Resource resource = new Resource();
		AttributeValue defaultVal = new AttributeValue();
		defaultVal.setValue(new String[] {});
		resource.setCreateTime(resObject.getCreateTime());
		String hostname = (String) resObject.getAttrValues().getOrDefault(PacificResAttr.ATTRIBUTE_HOSTNAME,
				new AttributeValue(PacificResAttr.UNKNOWN)).getValue();
		resource.setHostname(hostname);
		String classCode = resObject.getClassCode();
		if (PacificResAttr.SERVER_CLASSCODE.equals(classCode))
			resource.setType(ResourceType.SERVER);
		else if (PacificResAttr.VM_CLASSCODE.equals(classCode))
			resource.setType(ResourceType.VM);
		else
			resource.setType(ResourceType.NETWORK);
		String ipaddr = (String) resObject.getAttrValues().getOrDefault(PacificResAttr.ATTRIBUTE_IPADDR,
				new AttributeValue("")).getValue();
		resource.setIpaddr(ipaddr);
		resource.setId(resObject.getId());
		resource.setModified(resObject.getUpdateTime());
		String os = (String) resObject.getAttrValues().getOrDefault(PacificResAttr.ATTRIBUTE_OS,
				new AttributeValue("")).getValue();
		resource.setOs(os);
		List<String> tags = changeToStrList(resObject.getTags());
		resource.setResTags(tags);
		resource.setTenantId(resObject.getTenantId());
		AttributeValue stateAttributeVal = resObject.getAttrValues().get(PacificResAttr.ATTRIBUTE_ONLINE_STATUS);
		if (stateAttributeVal != null)
			resource.setOnlineStatus(OnlineStatus.checkByName((String) stateAttributeVal.getValue()));
		return resource;
	}

	/**
	 * 转化统一资源库tag并去重
	 * @param tags
	 * @return
     */
	private List<Tag> changeToTagList(List<String> tags) {
		if (tags == null)
			return null;
		List<Tag> list = new ArrayList<Tag>();
		Set<String> tagsSet = new HashSet<String>(tags);
		for (String tag : tagsSet) {
			int index = tag.indexOf(":");
			if (index == -1) {
				list.add(new Tag(tag));
			} else {
				list.add(new Tag(tag.substring(0, index), tag.substring(index + 1)));
			}
		}
		return list;
	}

	private List<String> changeToStrList(List<Tag> tags) {
		if (tags == null)
			return null;
		List<String> list = new ArrayList<String>();
		for (Tag tag : tags) {
			if (tag == null || list.contains(tag.asText())) {
				continue;
			}
			list.add(tag.asText());
		}
		return list;
	}

	public boolean setTags(Resource resource) {
		Set<String> tagSet = new HashSet<String>();
		tagSet.addAll(resource.getResTags());
		tagSet.addAll(resource.getUserTags());
		tagSet.addAll(resource.getAgentlessTags());
		if (tagSet.size() <= 0) {
			return true;
		}
		Tag[] tagsArr = changeToTagArray(new ArrayList<String>(tagSet));
		Identifier i = new Identifier();
		String classCode;
		if (resource.getType() != null) {
			if (ResourceType.SERVER.getId() == resource.getType().getId()){
				classCode = PacificResAttr.SERVER_CLASSCODE;
			}
			else if (ResourceType.VM.getId() == resource.getType().getId()){
				classCode = PacificResAttr.VM_CLASSCODE;
			}
			else {
				classCode = getNetWorkClassCode(resource);
			}
		} else {
			classCode = PacificResAttr.SERVER_CLASSCODE;
		}
		i.setClassCode(classCode);
		List<NamedValue> keys = new ArrayList<NamedValue>();
		keys.add(new NamedValue(PacificResAttr.ATTRIBUTE_IPADDR, resource.getIpaddr()));
		i.setKeyAttrs(keys);
		OperationResult result = pacificManager.getPacificResObjectService().setTags(resource.getTenantId(),
				new Identifier[]{i}, ResOwner.MONITOR, tagsArr);
		if (result != null && result.getItems() != null && result.getItems().length > 0) {
			return result.getFirst().isSuccess();
		}
		return false;
	}

	/**
	 * 创建 Store 资源内联对象
	 */
	private void createInlineResObjects(Resource res, String pacificResId) {
		//只有计算机设备才有后续的文件系统等信息
		if (res != null && pacificResId != null && ResourceType.SERVER.equals(res.getType())) {
			ResourceDetail resDetail = resourceDetailDao.queryByResId(res.getId());
			// 资源详情有可能没有
			if (resDetail == null)
				return;
			String tenantId = res.getTenantId();
			String detail = resDetail.getDetail();
			Map<String, Object> detailMap = null;
			try {
				detailMap = (Map<String, Object>) JSONUtils.parse(detail);
			} catch (Exception e) {
				// 数据格式转化异常无视
			}
			if (detailMap == null)
				return;
			List<ResObject> resObjectList = new ArrayList<ResObject>();
			// 读取磁盘信息
			if (detailMap.get("filesystem") instanceof ArrayList) {
				List fileSystems = (ArrayList) detailMap.get("filesystem");
				if (fileSystems.size() > 0) {
					for (Object o : fileSystems) {
						Map<String, Object> one = (Map<String, Object>) o;
						if (one == null || one.get("kb_size") == null)
							continue;
						// store 那边 name 不能为空
						// mounted_on 的值好像更加符合物理磁盘name属性
						String name = one.get("mounted_on") == null ? "/" : one.get("mounted_on").toString();
						name = "".equals(name.trim()) ? "/" : name;
						String valStr = one.get("kb_size").toString();
						Double d = 0.0;
						try {
							d = Double.valueOf(valStr);
							d = d / (1024 * 1024);
						} catch (Exception e) {
							// 数据格式转化异常无视
						}
						ResObject disk = new ResObject();
						disk.setTenantId(tenantId);
						disk.setClassCode(PacificResAttr.PHYSICAL_DISK);
						ResSource source = new ResSource(ResOwner.MONITOR.name());
						source.setTime(new Date());
						disk.setSources(Collections.singletonList(source));
						Map<String, AttributeValue> attMap = new HashMap<String, AttributeValue>();
						attMap.put(PacificResAttr.PHYSICAL_DISK_CAPACITY, new AttributeValue(d));
						attMap.put(PacificResAttr.PHYSICAL_DISK_NAME, new AttributeValue(name));
						disk.setAttrValues(attMap);
						resObjectList.add(disk);
					}
				}
			}
			// 读取网卡信息
			if (detailMap.get("network") != null) {
				Map<String, Object> netMap = (Map<String, Object>) detailMap.get("network");
				String macAddress = null;
				// 发现mac地址有两种类型
				if (netMap != null && netMap.get("macaddress") != null) {
					macAddress = netMap.get("macaddress").toString();
				}
				if (netMap != null && netMap.get("macddress") != null) {
					macAddress = netMap.get("macddress").toString();
				}
				if (macAddress != null && macAddress.trim().length() > 0) {
					ResObject nic = new ResObject();
					nic.setTenantId(tenantId);
					nic.setClassCode(PacificResAttr.NIC);
					ResSource source = new ResSource(ResOwner.MONITOR.name());
					source.setTime(new Date());
					nic.setSources(Collections.singletonList(source));
					Map<String, AttributeValue> attMap = new HashMap<String, AttributeValue>();
					attMap.put(PacificResAttr.NIC_MAC_ADDR, new AttributeValue(macAddress));
					attMap.put(PacificResAttr.NIC_MAC_NAME, new AttributeValue("无"));
					nic.setAttrValues(attMap);
					resObjectList.add(nic);
				}
			}
			ResObject[] resObjects;
			if (resObjectList.size() > 0) {
				resObjects = new ResObject[resObjectList.size()];
				resObjects = resObjectList.toArray(resObjects);
				OperationResult result = pacificManager.getPacificInlineObjectService()
						.createInlineResObjects(tenantId, pacificResId, false, resObjects);
				if (result != null && result.hasError() && result.obtainFailureItems() != null) {
					result.obtainFailureItems().forEach(item ->
							LOG.warn("pacific createInlineObject error: {}", item.getMessage())
					);
				}
			}
		}
	}

	private Tag[] changeToTagArray(List<String> tags) {
		if (tags != null && tags.size() > 0) {
			Set<String> tagsSet = new HashSet<String>(tags);
			List<Tag> tagList = new ArrayList<Tag>();
			for (String tag : tagsSet) {
				if (tag == null)
					continue;
				int index = tag.indexOf(":");
				if (index == -1) {
					tagList.add(new Tag(tag));
				} else {
					tagList.add(new Tag(tag.substring(0, index), tag.substring(index + 1)));
				}
			}
			return tagList.toArray(new Tag[tagList.size()]);
		}
		return new Tag[0];
	}

	public List<String> insert(String tenantId, List<Resource> resources) {
		List<String> successList = new ArrayList<>();
		if (resources.size() > 0) {
			ResObject[] resObjects = new ResObject[resources.size()];
			int index = 0;
			for (Resource res : resources) {
				resObjects[index] = changeResourceToResObject(res);
				index++;
			}
			// 按道理不需要审核
			OperationResult result = pacificManager.getPacificResObjectService().saveResObjects(tenantId, resObjects,
					false);
			if (result != null) {
				ResultItem[] items = result.getItems();

				if (items != null) {
					for (ResultItem item : items) {
						if (item.isSuccess())
							successList.add(item.getItem());
					}
				}
			}
		}
		return successList;
	}

	public Resource queryResByIpaddr(String tenantId, String ipaddr, ResourceClassCode classCode) {
		Identifier identifier = new Identifier();
		String code = classCode == null ? PacificResAttr.SERVER_CLASSCODE : (classCode.getClassCode().equals(PacificResAttr.SERVER_CLASSCODE) ? PacificResAttr.SERVER_CLASSCODE : PacificResAttr.VM_CLASSCODE);
		identifier.setClassCode(code);
		List<NamedValue> list = new ArrayList<>();
		list.add(new NamedValue(PacificResAttr.ATTRIBUTE_IPADDR, ipaddr));
		identifier.setKeyAttrs(list);
		ResObject resObject = pacificManager.getPacificResObjectService().getResObject(tenantId, identifier,
				new String[]{});
		return changeResObjectToResource(resObject);
	}

	public List<Resource> queryResByIpList(String tenantId, List<String> ipList) {
		List<Resource> list = new ArrayList<Resource>();
		if (ipList != null && ipList.size() > 0) {
			String[] arr  = ipList.stream()
					.filter(s -> s != null && s.trim().length() > 0)
					.toArray(String[]::new);
			if (arr != null) {
				QueryParams<ResObject> params = QueryParams.<ResObject>builder().withoutPaging().end();
				params.addQueryParam(new QueryParam(PacificResAttr.ATTRIBUTE_IPADDR, QueryOperator.IN, arr));
				QueryResult<ResObject> results = pacificManager.getPacificResObjectService()
						.queryResObjects(tenantId, params, null);
				if (results != null) {
					for (ResObject resObject : results) {
						Resource res = changeResObjectToResource(resObject);
						if (res != null)
							list.add(res);
					}
				}
			}
		}
		return list;
	}

	/**
	 * 待资源模型属性确定
	 *
	 * @param tenantId
	 * @param isContainNetwork
	 * @return
	 */
	public List<Resource> queryAllRes(String tenantId, boolean isContainNetwork) {
		List<Resource> list = new ArrayList<Resource>();
		QueryParams<ResObject> params = QueryParams.<ResObject> builder().withoutPaging().end();
		params.setExpectedFields(PacificResAttr.ATTRIBUTE_IPADDR);
		params.addQueryParam(new QueryParam("classCode", QueryOperator.IN, new Serializable[]{PacificResAttr.SERVER_CLASSCODE,PacificResAttr.VM_CLASSCODE}));
		QueryResult<ResObject> results = pacificManager.getPacificResObjectService()
				.queryResObjects(tenantId, params, null);
		if (results != null) {
			for (ResObject resObject : results) {
				Resource res = changeResObjectToResource(resObject);
				if (res != null)
					list.add(res);
			}
		}
		return list;
	}

	public List<uyun.bat.common.tag.entity.Tag> queryResourceTags(String tenantId) {
		QueryParams<ResObject> params = QueryParams.<ResObject> builder().withoutPaging().end();
		Set<Tag> set = pacificManager.getPacificResObjectService().getTags(tenantId, params, ResOwner.MONITOR);
		List<uyun.bat.common.tag.entity.Tag> tags = new ArrayList<uyun.bat.common.tag.entity.Tag>();
		if (set != null) {
			for (Tag tag : set) {
				tags.add(new uyun.bat.common.tag.entity.Tag(tag.getKey(), tag.getValue()));
			}
		}
		return tags;
	}

	/**
	 * 根据租户ID和resId查询store的内置和自定义标签
	 * @param tenantId
	 * @param resId
	 * @return
	 */
	public Map<String, List<String>> queryStoreTags(String tenantId, String resId) {
		ResourceIdTransform resourceIdTransform = resourceIdTransformLogic.getTransformIdByIds(resId, tenantId);
		if(null == resourceIdTransform || null == resourceIdTransform.getUnitId())
			return null;
		Identifier identifier = new Identifier();
		identifier.setId(resourceIdTransform.getUnitId());
		List<Tag> list = pacificManager.getPacificResObjectService().getTags(tenantId, identifier, ResOwner.MONITOR);
		List<String> userTags = new ArrayList<>();
		List<String> builtinTags = new ArrayList<>();
		if (null != list) {
			for (Tag tag : list) {
				if (!tag.isBuiltin())
					userTags.add(tag.asText());
				else
					builtinTags.add(tag.asText());
			}
		}
		Map<String, List<String>> map = new HashMap<>();
		map.put("userTags", userTags);
		map.put("builtinTags", builtinTags);
		return map;
	}

	/**
	 * 根据租户ID和resId设置store的自定义标签
	 * @param tenantId
	 * @param resId
	 * @param tags
	 * @return
	 */
	public boolean setTags(String tenantId, String resId, List<String> tags) {
		ResourceIdTransform resourceIdTransform = resourceIdTransformLogic.getTransformIdByIds(resId, tenantId);
		if (null == resourceIdTransform || null == resourceIdTransform.getUnitId())
			return false;
		Identifier identifier = new Identifier();
		identifier.setId(resourceIdTransform.getUnitId());
		Tag[] storeTags =changeToTagArray(tags);
		pacificManager.getPacificResObjectService().setTags(tenantId, new Identifier[]{identifier}, ResOwner.MONITOR, storeTags);
		return true;
	}

	public List<String> queryResTagNames(String tenantId) {
		QueryParams<ResObject> params = QueryParams.<ResObject> builder().withoutPaging().end();
		Set<Tag> set = pacificManager.getPacificResObjectService().getTags(tenantId, params, ResOwner.MONITOR);
		List<String> tags = new ArrayList<String>();
		if (set != null) {
			for (Tag tag : set) {
				tags.add(tag.getKey());
			}
		}
		return tags;
	}

	/**
	 * 待资源模型属性确定
	 *
	 * @param tenantId
	 * @param id
	 * @return
	 */
	public Resource queryResById(String classCode, String tenantId, String id) {
		Identifier identifier = new Identifier();
		identifier.setId(id);
		identifier.setClassCode(classCode);
		ResObject resObject = pacificManager.getPacificResObjectService().getResObject(tenantId, identifier,
				new String[] {});
		return changeResObjectToResource(resObject);

	}

	/**
	 * 待资源模型属性确定
	 *
	 * @param tenantId
	 * @return
	 */
	/*
	 * public Resource queryResByAgentId(String agentId, String tenantId) {
	 * QueryParams<ResObject> params = new QueryParams<ResObject>();
	 * List<QueryParam> list = new ArrayList<QueryParam>(); QueryParam param =
	 * new QueryParam(ATTRIBUTE_AGENTID, QueryOperator.EQ, agentId);
	 * list.add(param); params.setQueryItems(list); QueryResult<ResObject>
	 * result =
	 * pacificManager.getPacificResObjectService().queryResObjects(tenantId,
	 * params, ResOwner.MONITOR); Iterator<ResObject> iterator =
	 * result.iterator(); while (iterator.hasNext()) { ResObject resObject =
	 * iterator.next(); return changeResObjectToResource(resObject); } return
	 * null; }
	 *
	 *
	 * public PageResource queryAllRes(String tenantId, int pageNo, int
	 * pageSize, OnlineStatus onlineStatus) { QueryParams<ResObject> params =
	 * new QueryParams<ResObject>(); params.setPageNum(pageNo);
	 * params.setPageSize(pageSize); params.setNeedCount(true); String status =
	 * onlineStatus.getName(); params.addQueryParam(new
	 * QueryParam(ATTRIBUTE_ONLINESTATUS, QueryOperator.EQ, status));
	 * QueryResult<ResObject> result =
	 * pacificManager.getPacificResObjectService().queryResObjects(tenantId,
	 * params, ResOwner.MONITOR); int count = (int) result.getTotalRecords();
	 * List<Resource> resources = new ArrayList<Resource>(); Iterator<ResObject>
	 * iterator = result.iterator(); while (iterator.hasNext()) { ResObject
	 * resObject = iterator.next(); Resource res =
	 * changeResObjectToResource(resObject); if (res != null)
	 * resources.add(res); } PageResource pg = new PageResource(count,
	 * resources); return pg; }
	 *
	 * public PageResource queryResListByCondition(ResourceOpenApiQuery query) {
	 * QueryParams<ResObject> params = new QueryParams<ResObject>(); String
	 * hostname = query.getHostname(); if (hostname != null)
	 * params.addQueryParam(new QueryParam(ATTRIBUTE_HOSTNAME,
	 * QueryOperator.CONTAIN_CI, hostname)); String ipaddr = query.getIpaddr();
	 * if (ipaddr != null) params.addQueryParam(new QueryParam(ATTRIBUTE_IPADDR,
	 * QueryOperator.CONTAIN_CI, ipaddr)); int pageNo = query.getPageNo(); int
	 * pageSize = query.getPageSize(); if (pageNo <= 0) throw new
	 * IllegalException("起始页必须大于0"); if (pageSize <= 0) throw new
	 * IllegalException("每页展示数目必须大于0"); Date updateTime =
	 * query.getMinUpdateTime(); if (updateTime != null)
	 * params.addQueryParam(new QueryParam(ATTRIBUTE_LAST_COLLECT_TIME,
	 * QueryOperator.GT, updateTime)); String resourceType =
	 * query.getResourceType(); if (resourceType != null) {
	 * params.addQueryParam(new QueryParam(ATTRIBUTE_TYPE, QueryOperator.EQ,
	 * resourceType)); } List<String> apps = query.getApps(); if (apps != null)
	 * params.addQueryParam(new QueryParam(ATTRIBUTE_APP, QueryOperator.IN,
	 * apps.toArray(new String[] {}))); List<String> tags = query.getTags();
	 * List<Tag> tagList = new ArrayList<Tag>(); if (tags != null) { for (String
	 * tag : tags) { if (tag != null) { int index = tag.indexOf(":"); if (index
	 * == -1) { tagList.add(new Tag(tag)); } else { tagList.add(new
	 * Tag(tag.substring(0, index), tag.substring(index + 1))); } } }
	 * params.addQueryParam(new QueryParam(ATTRIBUTE_TAGS, QueryOperator.IN,
	 * tagList.toArray(new Tag[] {}))); } params.setPageNum(pageNo);
	 * params.setPageSize(pageSize); String tenantId = query.getTenantId();
	 * QueryResult<ResObject> result =
	 * pacificManager.getPacificResObjectService().queryResObjects(tenantId,
	 * params, ResOwner.MONITOR); List<Resource> resources = new
	 * ArrayList<Resource>(); for (ResObject resObt : result) { Resource res =
	 * changeResObjectToResource(resObt); if (res != null) resources.add(res); }
	 * int count = (int) result.getTotalRecords(); PageResource pg = new
	 * PageResource(); pg.setCount(count); pg.setResources(resources); return
	 * pg; }
	 */
	public List<String> getResTagsByTag(String tenantId, String tags) {
		List<String> list = new ArrayList<String>();
		QueryParams<ResObject> params = QueryParams.<ResObject> builder().withoutPaging().end();
		String[] keys = tags.split(";");
		params.addQueryParam(new QueryParam(PacificResAttr.ATTRIBUTE_TAGS, QueryOperator.IN, keys));
		Set<Tag> set = pacificManager.getPacificResObjectService().getTags(tenantId, params, ResOwner.MONITOR);
		if (set != null) {
			for (Tag t : set) {
				list.add(t.asText());
			}
		}
		list.removeAll(Arrays.asList(keys));
		return list;
	}

	/*
	 * public List<String> getAuthorizationResIds(String tenantId, int ttl) {
	 * int hours = ttl * 24; Calendar calendar =
	 * Calendar.getInstance(Locale.CHINA); calendar.setTime(new Date());
	 * calendar.set(Calendar.HOUR, -hours); Date date = calendar.getTime();
	 * QueryParams<ResObject> params = new QueryParams<ResObject>();
	 * params.addQueryParam(new QueryParam(ATTRIBUTE_LAST_COLLECT_TIME,
	 * QueryOperator.LT, date)); params.addQueryParam(new
	 * QueryParam(ATTRIBUTE_ONLINESTATUS, QueryOperator.EQ,
	 * OnlineStatus.OFFLINE.getName())); params.setExpectedFields(ATTRIBUTE_ID);
	 * QueryResult<ResObject> result =
	 * pacificManager.getPacificResObjectService().queryResObjects(tenantId,
	 * params, ResOwner.MONITOR); List<String> ids = new ArrayList<String>(); if
	 * (result != null) { for (ResObject resObject : result) {
	 * ids.add(resObject.getId()); } } return ids; }
	 *
	 * public List<SimpleResource> queryByTenantIdAndTags(String tenantId,
	 * List<uyun.bat.datastore.api.entity.Tag> tags) { List<SimpleResource>
	 * resources = new ArrayList<SimpleResource>(); List<String> list = new
	 * ArrayList<String>(); List<String> hosts = new ArrayList<>(); if (null !=
	 * tags && !tags.isEmpty()) { for (uyun.bat.datastore.api.entity.Tag tag :
	 * tags) { if (null == tag.getKey()) { continue; }
	 * //标签如果包含host需要单独查询hostname字段 if ("host".equals(tag.getKey())) { if (null
	 * != tag.getValue()) hosts.add(tag.getValue()); } else { String str =
	 * tag.changeToString(); if (str != null) list.add(str); } } } List<Tag>
	 * tagList = new ArrayList<Tag>(); QueryParams<ResObject> params = new
	 * QueryParams<ResObject>(); if (list.size() > 0) { for (String t : list) {
	 * int index = t.indexOf(":"); if (index == -1) { tagList.add(new Tag(t)); }
	 * else { tagList.add(new Tag(t.substring(0, index), t.substring(index +
	 * 1))); } } } if (tagList.size() > 0) params.addQueryParam(new
	 * QueryParam(ATTRIBUTE_TAGS, QueryOperator.IN, tagList.toArray(new Tag[]
	 * {}))); if (hosts.size() > 0) params.addQueryParam(new
	 * QueryParam(ATTRIBUTE_HOSTNAME, QueryOperator.IN, hosts.toArray(new
	 * String[] {}))); params.setExpectedFields(ATTRIBUTE_ID,
	 * ATTRIBUTE_HOSTNAME, ATTRIBUTE_IPADDR, ATTRIBUTE_LAST_COLLECT_TIME,
	 * ATTRIBUTE_TENANTID); QueryResult<ResObject> result =
	 * pacificManager.getPacificResObjectService().queryResObjects(tenantId,
	 * params, ResOwner.MONITOR); if (result != null) { for (ResObject resObject
	 * : result) { SimpleResource sp =
	 * changeResObjectToSimpleResource(resObject); if (resObject != null)
	 * resources.add(sp); } } return resources; }
	 *
	 * private SimpleResource changeResObjectToSimpleResource(ResObject
	 * resObject) { SimpleResource sp = new SimpleResource(); String ipaddr =
	 * (String) resObject.getAttrValues().getOrDefault(ATTRIBUTE_IPADDR, new
	 * AttributeValue("")) .getValue(); sp.setIpaddr(ipaddr); String hostname =
	 * (String) resObject.getAttrValues().getOrDefault(ATTRIBUTE_HOSTNAME, new
	 * AttributeValue("")) .getValue(); sp.setResourceName(hostname);
	 * sp.setResourceId(resObject.getId());
	 * sp.setTenantId(resObject.getTenantId()); AttributeValue
	 * lastCollectAttribute =
	 * resObject.getAttrValues().get(ATTRIBUTE_LAST_COLLECT_TIME); if
	 * (lastCollectAttribute != null) { Date lastCollectTime = (Date)
	 * lastCollectAttribute.getValue(); sp.setLastCollectTime(lastCollectTime);
	 * } return sp; }
	 */

	public List<String> updateResBatch(String tenantId, List<Resource> resources) {
		return insert(tenantId, resources);
	}

	public long deleteAuthorizationRes(String tenantId, List<String> ids) {
		long successCount = 0l;
		if (ids.size() > 0) {
			Identifier[] identifers = new Identifier[ids.size()];
			int index = 0;
			for (String id : ids) {
				identifers[index] = new Identifier();
				identifers[index].setId(id);
				index++;
			}
			OperationResult result = pacificManager.getPacificResObjectService().removeOwners(tenantId, identifers,
					ResOwner.MONITOR);
			if (result != null) {
				ResultItem[] items = result.getItems();
				if (items != null) {
					for (ResultItem item : items) {
						if (item.isSuccess())
							successCount++;
					}
				}
			}
		}
		return successCount;
	}

	/*
	 * 以下接口暂不使用，待统一资源库模型属性字段确定后，再考虑增加。
	 */

	/**
	 * 根据日期查询资源数统计接口，暂不使用，沿用mysql旧资源表，由于要新建表，待资源模型统一后再使用
	 *
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@Deprecated
	public List<ResourceCount> getResCountByDate(Date startTime, Date endTime) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		return simpleResourceDao.getResCountByDate(map);
	}

	/**
	 * 查询资源统计数，暂不使用，沿用mysql旧资源表，由于要新建表，待资源模型统一后再使用
	 *
	 * @return
	 */
	@Deprecated
	public List<ResourceCount> getResCount() {
		return simpleResourceDao.getResCount();
	}

	/**
	 * 根据租户ID查询
	 *
	 * @param tenantId
	 * @return
	 */
	@Deprecated
	public List<ResourceStatusCount> getResStatusCount(String tenantId) {
		return simpleResourceDao.getResStatusCount(tenantId);
	}

	/**
	 * 根据资源Id获取资源ID
	 *
	 * @param list
	 * @return
	 */
	@Deprecated
	public List<String> getResIdInId(List<String> list) {
		if (list.size() > 0)
			return simpleResourceDao.getResIdInId(list);
		return new ArrayList<String>();
	}

	/**
	 * 获取所有租户ID
	 *
	 * @return
	 */
	@Deprecated
	public List<String> getAllTenantId() {
		return simpleResourceDao.getAllTenantId();
	}

	/**
	 * 根据租户ID获取资源统计数
	 *
	 * @param tenantId
	 * @return
	 */
	@Deprecated
	public int getResCountByTenantId(String tenantId) {
		return simpleResourceDao.getResCountByTenantId(tenantId);
	}

	/**
	 * 获取租户指标时间跨度，运营接口，暂不使用
	 *
	 * @return
	 */
	@Deprecated
	public List<MetricSpanTime> getMetricSpanTime() {
		return simpleResourceDao.getMetricSpanTime();
	}

	/**
	 * 根据租户id获取所有的资源ID，暂不使用
	 *
	 * @param tenantId
	 * @return
	 */
	@Deprecated
	public List<String> getAllResId(String tenantId) {
		return simpleResourceDao.getAllResId(tenantId);
	}

	/**
	 * 获取资源简要字段，判断资源上下线逻辑需要
	 *
	 * @param tags
	 * @param onlineStatus
	 * @param lastCollectTime
	 * @return
	 */
	@Deprecated
	public List<SimpleResource> query(List<uyun.bat.common.tag.entity.Tag> tags, OnlineStatus onlineStatus,
									  long lastCollectTime) {
		List<String> list = new ArrayList<String>();
		for (uyun.bat.common.tag.entity.Tag tag : tags) {
			String str = tag.toString();
			if (str != null)
				list.add(str);
		}
		SimpleResourceQuery query = new SimpleResourceQuery(list, new Date(lastCollectTime), onlineStatus.getName());
		return simpleResourceDao.getSimpleResource(query);
	}

	/**
	 * 批量出入simpleResource
	 *
	 * @param list
	 * @return
	 */
	@Deprecated
	public long batchInsert(List<SimpleResource> list) {
		if (list != null && list.size() > 0)
			return simpleResourceDao.batchInsert(list);
		return 0l;
	}

	/**
	 * 批量更新simpleResource
	 *
	 * @param list
	 * @return
	 */
	@Deprecated
	public long batchUpdate(List<SimpleResource> list) {
		if (list != null && list.size() > 0)
			return simpleResourceDao.batchUpdate(list);
		return 0l;
	}

	/**
	 * 保存simpleResource
	 *
	 * @param simpleResource
	 * @return
	 */
	@Deprecated
	public boolean save(SimpleResource simpleResource) {
		if (simpleResource != null)
			simpleResourceDao.save(simpleResource);
		return true;
	}

	/**
	 * 删除simpleResource
	 *
	 * @param tenantId
	 * @param resourceId
	 * @return
	 */
	@Deprecated
	public boolean deleteSimpleResource(String tenantId, String resourceId) {
		return simpleResourceDao.delete(tenantId, resourceId) == 1 ? true : false;
	}

	/**
	 * 批量删除simpleResource
	 *
	 * @param tenantId
	 * @param resourceIds
	 * @return
	 */
	public long batchDelete(String tenantId, List<String> resourceIds) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tenantId", tenantId);
		map.put("resourceIds", resourceIds);
		return simpleResourceDao.batchDelete(map);
	}

	/**
	 * 暂时依赖Mysql旧表来实现，目前统一资源库API不支持多字段OR查询
	 */
	@Deprecated
	public PageResourceGroup queryByFilterAndGroupByTag(String tenantId, String filter, String groupBy, int pageNo,
														int size, OnlineStatus onlineStatus) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 暂时依赖Mysql旧表来实现，目前统一资源库API不支持多字段OR查询
	 */
	@Deprecated
	public PageResource queryByKey(String tenantId, String key, int pageNo, int size, OnlineStatus onlineStatus) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 暂时依赖Mysql旧表来实现，目前统一资源库API不支持多字段OR查询
	 */
	@Deprecated
	public PageResource queryByKeyAndSortBy(String tenantId, String filter, ResourceOrderBy orderBy, int pageNo,
											int size, OnlineStatus onlineStatus) {
		throw new UnsupportedOperationException();
	}

	/*
	 * 以上接口暂不使用，待统一资源库模型属性字段确定后，再考虑增加。
	 */

}