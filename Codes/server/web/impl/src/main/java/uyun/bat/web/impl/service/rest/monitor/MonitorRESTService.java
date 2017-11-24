package uyun.bat.web.impl.service.rest.monitor;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.Checkperiod;
import uyun.bat.monitor.api.common.util.CollectionUtil;
import uyun.bat.monitor.api.common.util.PeriodUtil;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.AutoRecoveryParams;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorCount;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.NotifyRecord;
import uyun.bat.monitor.api.entity.PageMonitor;
import uyun.bat.monitor.api.entity.PageNotifyRecord;
import uyun.bat.web.api.monitor.entity.MineMonitor;
import uyun.bat.web.api.monitor.entity.MineMonitorHost;
import uyun.bat.web.api.monitor.entity.MineNotifyRecord;
import uyun.bat.web.api.monitor.entity.MonitorHostStateVO;
import uyun.bat.web.api.monitor.entity.MonitorHostVO;
import uyun.bat.web.api.monitor.entity.MonitorParam;
import uyun.bat.web.api.monitor.entity.MonitorVO;
import uyun.bat.web.api.monitor.entity.NotifyRecordVO;
import uyun.bat.web.api.monitor.entity.PaginationUser;
import uyun.bat.web.api.monitor.entity.SingleMonitor;
import uyun.bat.web.api.monitor.entity.UserInfo;
import uyun.bat.web.api.monitor.service.MonitorWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;

import uyun.bird.tenant.api.entity.Pagination;
import uyun.bird.tenant.api.entity.User;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service(protocol = "rest")
@Path("v2/monitors")
public class MonitorRESTService implements MonitorWebService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("query")
	public MineMonitor getMonitorsByFilter(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("currentPage") String currentPage, @QueryParam("pageSize") String pageSize,
			@QueryParam("filterValue") String filtertValue, @QueryParam("filterType") String monitorState) {
		 
		if (StringUtils.isEmpty(currentPage))
			currentPage = "1";
		if (StringUtils.isEmpty(pageSize))
			pageSize = "10";
		int current = Integer.parseInt(currentPage);
		int size = Integer.parseInt(pageSize);
		Boolean enable = null;
		List<MonitorState> list = new ArrayList<>();
		if (!StringUtils.isEmpty(monitorState)) {
			String[] str = monitorState.split(";");
			if (str.length >= 0) {
				for (String s : str) {
					if ("silent".equals(s)) {
						enable = false;
						continue;
					}
					MonitorState state = MonitorState.checkByCode(s);
					list.add(state);
				}
			}
		}
		PageMonitor pm = ServiceManager.getInstance().getMonitorService()
				.getMonitorsByFilter(tenantId, current, size, filtertValue, list, enable);
		List<Monitor> ms = pm.getMonitors();
		List<MonitorVO> monitorList = new ArrayList<MonitorVO>();
		if (ms != null && ms.size() > 0) {
			for (Monitor m : ms) {
				MonitorVO mv = new MonitorVO();
				mv.setEnable(m.getEnable());
				mv.setId(m.getId());
				mv.setName(m.getName());
				mv.setStatus(m.getMonitorState().getCode());
				mv.setType(m.getMonitorType().getCode());
				monitorList.add(mv);
			}
		}
		MineMonitor monitors = new MineMonitor();
		monitors.setMonitors(monitorList);
		monitors.setCurrentPage(current);
		monitors.setTotal(pm.getCount());
		return monitors;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public SingleMonitor getMonitorById(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("id") String id) {
		 
		Monitor monitor = ServiceManager.getInstance().getMonitorService().getMonitorById(tenantId, id);
		if (monitor != null) {
			SingleMonitor m = new SingleMonitor();
			m.setId(id);
			m.setMessage(monitor.getMessage());
			m.setMonitorStatus(monitor.getMonitorState().getCode());
			m.setMonitorType(monitor.getMonitorType().getCode());
			m.setName(monitor.getName());
			m.setNotifyUserIdList(monitor.getNotifyUserIdList());
			m.setOptions(monitor.getOptions());
			m.setQuery(monitor.getQuery());
			m.setEnable(monitor.getEnable());
			AutoRecoveryParams arp =monitor.getAutoRecoveryParams();
			if (arp != null)
				arp.setParams(URL.decode(monitor.getAutoRecoveryParams().getParams()));
			m.setAutoRecoveryParams(arp);
			return m;
		}
		return null;
	}

	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public void createMonitor(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@HeaderParam(TenantConstants.COOKIE_USERID) String userId, MonitorParam mParam) {
		 
		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setTenantId(tenantId);
		monitor.setModified(new Date());
		monitor.setCreatorId(mParam.getCreatorId());
		monitor.setMessage(mParam.getMessage());
		monitor.setMonitorState(MonitorState.OK);
		monitor.setMonitorType(MonitorType.checkByCode(mParam.getMonitorType()));
		monitor.setName(mParam.getName());
		monitor.setNotify(mParam.isNotify());
		monitor.setNotifyUserIdList(mParam.getNotifyUserIdList());
		monitor.setOptions(mParam.getOptions());
		monitor.setQuery(mParam.getQuery());
		monitor.setEnable(mParam.getEnable());
		monitor.setCreateTime(new Date());
		monitor.setAutoRecoveryParams(mParam.getAutoRecoveryParams());
		ServiceManager.getInstance().getMonitorService().createMonitor(monitor);
	}

	@POST
	@Path("update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateMonitor(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, MonitorParam mParam) {
		 
		Monitor monitor = new Monitor();
		monitor.setId(mParam.getId());
		monitor.setMessage(mParam.getMessage());
		if(mParam.getMonitorStatus() != null && mParam.getMonitorStatus().length()>0)
			monitor.setMonitorState(MonitorState.checkByCode(mParam.getMonitorStatus()));
		monitor.setMonitorType(MonitorType.checkByCode(mParam.getMonitorType()));
		monitor.setName(mParam.getName());
		monitor.setNotify(mParam.isNotify());
		monitor.setNotifyUserIdList(mParam.getNotifyUserIdList());
		monitor.setOptions(mParam.getOptions());
		monitor.setQuery(mParam.getQuery());
		monitor.setTenantId(tenantId);
		monitor.setModified(new Date());
		monitor.setEnable(mParam.getEnable());
		monitor.setAutoRecoveryParams(mParam.getAutoRecoveryParams());
		ServiceManager.getInstance().getMonitorService().updateMonitor(monitor);
		ServiceManager.getInstance().getMonitorService().deleteAutoRecoverRecordByMonitorId(tenantId,monitor.getId());
	}

	@POST
	@Path("delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public void deleteMonitor(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, Monitor monitor) {
		 
		monitor.setTenantId(tenantId);
		ServiceManager.getInstance().getMonitorService().deleteAutoRecoverRecordByMonitorId(tenantId,monitor.getId());
		ServiceManager.getInstance().getMonitorService().deleteMonitor(monitor);
	}

	@POST
	@Path("runStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public void runStatus(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, Monitor monitor) {
		 
		monitor.setTenantId(tenantId);
		monitor.setModified(new Date());
		ServiceManager.getInstance().getMonitorService().updateMonitor(monitor);
	}

	@GET
	@Path("count")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Integer> getCount(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 
		List<Integer> count = new ArrayList<Integer>();
		List<MonitorCount> monitorCount = ServiceManager.getInstance().getMonitorService().getCount(tenantId);
		// 计算正常，告警，紧急，全部，静默的监测器的总数
		int ok = 0, info=0, warn = 0, alert = 0, all = 0, silent = 0;
		for (MonitorCount c : monitorCount) {
			int temp = c.getCount();
			if (c.isEnable() == false)
				silent += temp;
			else {
				if (c.getState() == 2)
					ok = temp;
				if (c.getState() == 4)
					info = temp;
				if (c.getState() == 5)
					warn = temp;
				if (c.getState() == 8)
					alert = temp;
			}
		}
		all = silent + ok + warn + alert;
		count.add(all);
		count.add(alert);
		count.add(warn);
		count.add(info);
		count.add(ok);
		count.add(silent);
		int authorityCount = Integer.parseInt(Config.getInstance().get("tenant.authority.monitor.num").toString());
		count.add(authorityCount);
		return count;
	}

	@GET
	@Path("user")
	@Produces(MediaType.APPLICATION_JSON)
	public PaginationUser getUser(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
								  @QueryParam("realname") String realname,
								  @QueryParam("currentPage") @DefaultValue("1") Integer currentPage,
								  @QueryParam("pageSize") @DefaultValue("20") Integer pageSize,
								  @QueryParam("userIdList") List<String> userIdList) {
		Pagination<User> userList = ServiceManager.getInstance().getUserService()
				.listBy(tenantId, null, realname, null, null, null, currentPage, pageSize);
		List<User> selectedUsers = null;
		if (userIdList != null && userIdList.size() > 0) {
			selectedUsers = ServiceManager.getInstance().getUserService().listByUserIds(userIdList.toArray(new String[0]));
		}
		List<UserInfo> users = new ArrayList<UserInfo>();
		if (selectedUsers != null && selectedUsers.size() > 0) {
			for (User selectedUser : selectedUsers) {
				UserInfo userInfo =
						new UserInfo(selectedUser.getUserId(), selectedUser.getRealname(), true);
				users.add(userInfo);
			}
		}
		if (userList != null) {
			if (userList.getRecords() != null && userList.getRecords().size() > 0) {
				for (User u : userList.getRecords()) {
					// 已经包含在已选择用户中则不再显示
					if (userIdList != null && userIdList.contains(u.getUserId())) {
						continue;
					}
					UserInfo userInfo = new UserInfo();
					userInfo.setId(u.getUserId());
					userInfo.setName(u.getRealname());
					users.add(userInfo);
				}
			}
			return new PaginationUser(currentPage, pageSize, userList.getTotalPage(), userList.getTotal(), users);
		} else {
			return new PaginationUser();
		}
	}

	@GET
	@Path("notifyRecord")
	@Produces(MediaType.APPLICATION_JSON)
	public MineNotifyRecord getNotifyRecordList(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("monitorId") String monitorId, @QueryParam("currentPage") @DefaultValue("1") int currentPage,
			@QueryParam("pageSize") @DefaultValue("20") int pageSize, @QueryParam("timeRange") String timeRange) {
		 
		PageNotifyRecord pnr = ServiceManager.getInstance().getMonitorService()
				.getNotifyRecordList(tenantId, monitorId, currentPage, pageSize, timeRange);
		List<NotifyRecord> nr = pnr.getNotifyRecords();
		List<NotifyRecordVO> notifyRecordVOs = new ArrayList<>();
		if (nr != null && nr.size() > 0) {
			for (NotifyRecord n : nr) {
				NotifyRecordVO nRecordVO = new NotifyRecordVO();
				nRecordVO.setId(n.getId());
				nRecordVO.setName(n.getName());
				nRecordVO.setContent(n.getContent());
				nRecordVO.setTime(n.getTime());
				notifyRecordVOs.add(nRecordVO);
			}
		}
		MineNotifyRecord mineNotifyRecord = new MineNotifyRecord();
		mineNotifyRecord.setLists(notifyRecordVOs);
		mineNotifyRecord.setCurrentPage(currentPage);
		mineNotifyRecord.setTotal(pnr.getCount());
		return mineNotifyRecord;
	}

	@GET
	@Path("isExist")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean isMonitorExist(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 
		int count = ServiceManager.getInstance().getMonitorService().getMonitorList(tenantId).size();
		if (count > 0)
			return true;
		return false;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("hosts/query")
	public MineMonitorHost getMonitorHostsByFilter(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("monitorId") String monitorId, @QueryParam("tagName") String tagName,
			@QueryParam("timeRange") String timeRange, @QueryParam("currentPage") @DefaultValue("1") int currentPage,
			@QueryParam("pageSize") @DefaultValue("5") int pageSize) {
		 
		MineMonitorHost pageMonitorHost = new MineMonitorHost();
		List<MonitorHostVO> hosts = new ArrayList<MonitorHostVO>();

		pageMonitorHost.setHosts(hosts);
		pageMonitorHost.setCurrentPage(currentPage);

		Monitor monitor = ServiceManager.getInstance().getMonitorService().getMonitorById(tenantId, monitorId);
		String state = StateUtil.generateState(monitor.getMonitorType());
		String[] tags = buildQueryTags(tenantId, monitorId, tagName);
		String[] objectIds = ServiceManager.getInstance().getTagService().queryObjectIds(tags);
		if (null == objectIds || objectIds.length < 1) {
			pageMonitorHost.setNow(System.currentTimeMillis());
			return pageMonitorHost;
		}
		int count=objectIds.length;
		List<String> objects = Arrays.asList(objectIds);
		objects = CollectionUtil.getListByPage(objects, currentPage, pageSize);
		PeriodUtil.Period pd = PeriodUtil.generatePeriod(timeRange);
		if (null == pd) {
			pageMonitorHost.setNow(System.currentTimeMillis());
			return pageMonitorHost;
		}
		for (String objectId : objects) {
			List<MonitorHostStateVO> hostStates = new ArrayList<MonitorHostStateVO>();
			Checkperiod[] checkperiods = ServiceManager.getInstance().getStateService()
					.getCheckperiods(tenantId, state, objectId, pd.getStart(), pd.getEnd());
			MonitorHostVO monitorHost = null;
			if (null == checkperiods || checkperiods.length < 1) {
				//如果此时间段内没有状态记录，只能获取最后一次的状态
				Checkperiod checkperiod = ServiceManager.getInstance().getStateService()
						.getLastCheckperiod(tenantId, state, objectId);
				if (null != checkperiod) {
					hostStates.add(new MonitorHostStateVO("0", pd.getEnd()-pd.getStart(), checkperiod.getDescr()));
					monitorHost = new MonitorHostVO(generateHostName(checkperiod.getTags(), monitor.getQuery()), hostStates);
				}
			}else{
				for (Checkperiod checkperiod : checkperiods) {
					MonitorHostStateVO monitorHostState = new MonitorHostStateVO();
					monitorHostState.setState(getState(checkperiod.getValue()));
					long periodTime = checkperiod.getLastTime() - checkperiod.getFirstTime();
					if (periodTime == 0) {
						periodTime = pd.getEnd() - checkperiod.getFirstTime();
					}
					monitorHostState.setDuration(periodTime);
					monitorHostState.setDescr(checkperiod.getDescr());
					hostStates.add(monitorHostState);
				}
				//如果没有检查点增加一个未知的，除主机监测器
				long interval = System.currentTimeMillis() - checkperiods[checkperiods.length - 1].getLastTime();
				if (!"host".equals(monitor.getMonitorType().getCode()) && interval > 20 * 1000)
					hostStates.add(new MonitorHostStateVO("0", interval, checkperiods[checkperiods.length - 1].getDescr()));
				monitorHost = new MonitorHostVO(generateHostName(checkperiods[0].getTags(), monitor.getQuery()), hostStates);
			}
			hosts.add(monitorHost);
		}
		pageMonitorHost.setCount(count);
		pageMonitorHost.setNow(System.currentTimeMillis());
		return pageMonitorHost;
	}

	private String getState(String checkperiodValue){
		MonitorState monitorState ;
		try {
			monitorState = MonitorState.checkByCode(checkperiodValue);
			return monitorState.getValue()+"";
		} catch (Exception e) {
		}
		return null;
	}

	//暂时只考虑主机名搜索，后续可能还会修改
	private String[] buildQueryTags(String tenantId, String monitorId, String filterValue) {
		List<String> list = new ArrayList<>();
		list.add(StateUtil.TENANT_ID + ":" + tenantId);
		list.add(StateUtil.MONITOR_ID + ":" + monitorId);
		if (null != filterValue && filterValue.length() > 0) {
			String[] temp=filterValue.split(":");
			if (temp.length==1){
				list.add("host:"+temp[0]);
			}else if (temp.length==2){
				list.add(filterValue);
			}
		}
		return list.toArray(new String[0]);
	}

	private String generateHostName(String[] tags, String query) {
		Pattern groupBy = Pattern.compile("[{}]");
		String[] temp = groupBy.split(query, 0);
		String group = null;
		if (temp.length == 4) {
			String[] groups = temp[3].split(";");
			if (groups.length == 2) {
				group = groups[1];
			}
		}
		String host = "";
		String groupName = "";
		for (String tag : tags) {
			if (group != null && !group.trim().equals("")) {
				if (tag.startsWith(group)) {
					groupName = tag.substring(group.length() + 1);
				}
			}
			if (tag.startsWith("host:")) {
				host = tag.substring(5);
			}
		}
		if (!StringUtils.isEmpty(host) && !StringUtils.isEmpty(groupName)) {
			return host + " " + groupName;
		}
		return host;
	}

}
