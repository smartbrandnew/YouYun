package uyun.bat.web.impl.testservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorCount;
import uyun.bat.monitor.api.entity.MonitorCountVO;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.NotifyRecord;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.api.entity.PageMonitor;
import uyun.bat.monitor.api.entity.PageNotifyRecord;
import uyun.bat.monitor.api.service.MonitorService;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

public class MonitorServiceTest implements MonitorService{
	private static final String TENANT_ID = "94baaadca64344d2a748dff88fe7159e";
	private static final String USER_ID = "94baaadca64344d2a748dff88fe7159e";
	@Override
	public PageMonitor getMonitorsByFilter(String tenantId, int currentPage, int pageSize, String name,
			List<MonitorState> state, Boolean mute) {
		// TODO Auto-generated method stub
		MonitorState monitorState = MonitorState.OK;
		List<String> list = new ArrayList<String>();
		list.add(USER_ID);
		PageMonitor pm = new PageMonitor();
		List<Monitor> monitors = new ArrayList<Monitor>();
		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(USER_ID);
		monitor.setMessage("testmetrictestmetric");
		monitor.setName("testmetric");
		monitor.setQuery("avg:system.cpu.system{host:10.1.11.9} by {resourceId}");
		monitor.setMonitorType(MonitorType.METRIC);
		monitor.setTenantId(TENANT_ID);
		monitor.setNotify(true);
		monitor.setNotifyUserIdList(list);
		monitor.setModified(new Date());
		monitor.setMonitorState(monitorState);
		monitor.setEnable(true);
		Options options = new Options();
		options.addThreshold(Options.ALERT, "last_5m < 3");
		options.addThreshold(Options.WARNING, "last_10h > 0");
		monitor.setOptions(options);
		monitor.setCreateTime(new Date());
		monitors.add(monitor);
		pm.setMonitors(monitors);
		return pm;
	}

	@Override
	public List<Monitor> getMonitorList(String tenantId) {
		// TODO Auto-generated method stub
		MonitorState monitorState = MonitorState.OK;
		List<String> list = new ArrayList<String>();
		list.add(USER_ID);
		List<Monitor> monitors = new ArrayList<Monitor>();
		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(USER_ID);
		monitor.setMessage("testmetrictestmetric");
		monitor.setName("testmetric");
		monitor.setQuery("avg:system.cpu.system{host:10.1.11.9} by {resourceId}");
		monitor.setMonitorType(MonitorType.METRIC);
		monitor.setTenantId(TENANT_ID);
		monitor.setNotify(true);
		monitor.setNotifyUserIdList(list);
		monitor.setModified(new Date());
		monitor.setMonitorState(monitorState);
		monitor.setEnable(true);
		Options options = new Options();
		options.addThreshold(Options.ALERT, "last_5m < 3");
		options.addThreshold(Options.WARNING, "last_10h > 0");
		monitor.setOptions(options);
		monitor.setCreateTime(new Date());
		monitors.add(monitor);
		return monitors;
	}

	@Override
	public Monitor getMonitorById(String tenantId, String monitorId) {
		// TODO Auto-generated method stub
		MonitorState monitorState = MonitorState.OK;
		List<String> list = new ArrayList<String>();
		list.add(USER_ID);
		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(USER_ID);
		monitor.setMessage("testmetrictestmetric");
		monitor.setName("testmetric");
		monitor.setQuery("avg:system.cpu.system{host:10.1.11.9} by {resourceId}");
		monitor.setMonitorType(MonitorType.METRIC);
		monitor.setTenantId(TENANT_ID);
		monitor.setNotify(true);
		monitor.setNotifyUserIdList(list);
		monitor.setModified(new Date());
		monitor.setMonitorState(monitorState);
		monitor.setEnable(true);
		Options options = new Options();
		options.addThreshold(Options.ALERT, "last_5m < 3");
		options.addThreshold(Options.WARNING, "last_10h > 0");
		monitor.setOptions(options);
		monitor.setCreateTime(new Date());
		return monitor;
	}

	@Override
	public Monitor createMonitor(Monitor monitor) {
		// TODO Auto-generated method stub
		System.out.println("createMonitor SUCCESS!");
		return null;
	}

	@Override
	public Monitor updateMonitor(Monitor monitor) {
		// TODO Auto-generated method stub
		System.out.println("updateMonitor SUCCESS!");
		return null;
	}

	@Override
	public void deleteMonitor(Monitor monitor) {
		// TODO Auto-generated method stub
		System.out.println("DeleteMonitor SUCCESS!");
	}

	@Override
	public List<MonitorCount> getCount(String tenantId) {
		// TODO Auto-generated method stub
		MonitorCount mc = new MonitorCount();
		mc.setCount(10);
		mc.setEnable(true);
		mc.setState(1);
		List<MonitorCount> list =new ArrayList<MonitorCount>();
		list.add(mc);
		return list;
	}

	@Override
	public PageNotifyRecord getNotifyRecordList(String tenantId, String monitorId, int currentPage, int pageSize,
			String timeRange) {
		// TODO Auto-generated method stub
		PageNotifyRecord pnr =new PageNotifyRecord();
		List<NotifyRecord> notifyRecords = new ArrayList<NotifyRecord>();
		NotifyRecord notifyRecord = new NotifyRecord();
		notifyRecord.setId(UUIDTypeHandler.createUUID());
		notifyRecord.setMonitorId(monitorId);
		notifyRecord.setContent("张三家被盗");
		notifyRecord.setName("张三");
		notifyRecord.setTime(new Date());
		notifyRecord.setTenantId(TENANT_ID);
		notifyRecords.add(notifyRecord);
		pnr.setNotifyRecords(notifyRecords);
		return pnr;
	}

	@Override
	public void deleteAutoRecoverRecordByMonitorId(String tenantId, String monitorId) {

	}

	@Override
	public List<MonitorCountVO> getMonitorCountByDate(Date startTime, Date endTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MonitorCountVO> getMonitorCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getIdListByTenantId(String tenantId) {
		return null;
	}

	@Override
	public String getAlertNameByMonitorType(String type, Monitor monitor) {
		return null;
	}
}
