package uyun.bat.web.impl.service.rest.event;
import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.event.api.entity.*;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.web.api.event.envity.*;
import uyun.bat.web.api.event.service.EventWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
 
import uyun.whale.common.util.text.DateUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(protocol = "rest")
@Path("v2/events")
public class EventRESTService implements EventWebService {

	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public MineEvent searchEvent(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
								 @QueryParam("current")  @DefaultValue("1")  int current,
								 @QueryParam("pageSize")  @DefaultValue("10")  int pageSize,
								 @QueryParam("searchValue") String searchValue, @QueryParam("serverity") String serverity,
								 @QueryParam("beginTime") long beginTime, @QueryParam("endTime") long endTime,
								 @QueryParam("granularity") int granularity) {

		 
		searchValue = searchValue.replace(" / ",";");
		PageEvent pageEvents = ServiceManager.getInstance().getEventService()
				.searchEvent(tenantId, current, pageSize, searchValue, serverity, beginTime, endTime,granularity);
		List<Event> events = pageEvents.getRows();
		List<EventVO> eventVOs=new ArrayList<>();
		if (null != events && events.size() > 0) {
			for (Event event : events) {
				String monitorName="";
				String monitorType="";
				String resName="";
				if (event.getMonitorId() != null) {
					Monitor monitor = ServiceManager.getInstance().getMonitorService()
							.getMonitorById(tenantId, event.getMonitorId());
					monitorName=null == monitor ? "" : monitor.getName();
					monitorType=null == monitor ? null : monitor.getMonitorType().getCode();
				}
				if (event.getResId() != null) {
					Resource resource = ServiceManager.getInstance().getResourceService().queryResById(event.getResId(), tenantId);
					resName=null == resource ? "" : resource.getHostname();
				}
				EventMeta meta=event.getEventMeta();
				EventVO eventVO=new EventVO(event.getId(),event.getOccurTime(),event.getResId(),event.getMsgTitle(),
						event.getMsgContent(),event.getServerity(),event.getMonitorId(),event.getRelateCount(),event.getFirstRelateTime()
						,monitorName,resName,monitorType,event.getFaultId(),new EventMetaVO(meta.getTotal(),meta.getMeta()));
				eventVOs.add(eventVO);
			}
		}
		MineEvent mineEvent=new MineEvent(pageSize,current,eventVOs,pageEvents.getTotal(),pageEvents.getTotalPage(),pageEvents.getBeginTime(),pageEvents.getEndTime());
		return mineEvent;
	}

	@GET
	@Path("graph/list")
	@Produces(MediaType.APPLICATION_JSON)
	public EventGraphDataVO searchEventGraphData(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
												 @QueryParam("searchValue") String searchValue, @QueryParam("beginTime") long beginTime,
												 @QueryParam("endTime") long endTime, @QueryParam("granularity") int granularity) {
		 
		searchValue = searchValue.replace(" / ",";");
		if(searchValue.contains(" "))
			throw new IllegalArgumentException("parameter illegal,Can not have spaces!");
		EventGraphData eventGraphData= ServiceManager.getInstance().getEventService()
				.searchEventGraphData(tenantId, searchValue, beginTime, endTime, granularity);
		List<EventGraphBuild> eventGraphBuilds=eventGraphData.getGraphs();
		List<EventGraphBuildVO> graphs=new ArrayList<>();
		if (null!=eventGraphBuilds&&eventGraphBuilds.size()>0){
			for(EventGraphBuild eventGraphBuild:eventGraphBuilds){
				EventGraphBuildVO buildVO=new EventGraphBuildVO();
				EventAlert eventAlert=eventGraphBuild.getAlerts();
				EventAlertVO eventAlertVO=new EventAlertVO(eventAlert.getSuccess(),eventAlert.getInfo(),eventAlert.getWarnning(),eventAlert.getCritical());
				buildVO.setTotal(eventGraphBuild.getTotal());
				buildVO.setTime(eventGraphBuild.getTime());
				buildVO.setAlerts(eventAlertVO);
				graphs.add(buildVO);
			}
		}
		return new EventGraphDataVO(eventGraphData.getBeginTime(),eventGraphData.getEndTime(),eventGraphData.getDiffTime(),graphs);
	}

	@GET
	@Path("relates/query")
	@Produces(MediaType.APPLICATION_JSON)
	public MineEvent getEventsByFaultId(
			@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("eventId") String eventId,
			@QueryParam("faultId") String faultId,
			@QueryParam("current")@DefaultValue("1")  int current,
			@QueryParam("pageSize")@DefaultValue("10") int pageSize) {
		 
		PageEvent pageEvents = ServiceManager.getInstance().getEventService()
				.getEventsByFaultId(tenantId, eventId, faultId, current, pageSize);
		List<Event> events = pageEvents.getRows();
		List<EventVO> eventVOs=new ArrayList<>();
		if (null != events && events.size() > 0) {
			for (Event event : events) {
				String monitorName="";
				String monitorType="";
				String resName="";
				if (event.getMonitorId() != null) {
					Monitor monitor = ServiceManager.getInstance().getMonitorService()
							.getMonitorById(tenantId, event.getMonitorId());
					monitorName=null == monitor ? "" : monitor.getName();
					monitorType=null == monitor ? null : monitor.getMonitorType().getCode();
				}
				if (event.getResId() != null) {
					Resource resource = ServiceManager.getInstance().getResourceService().queryResById(event.getResId(), tenantId);
					resName=null == resource ? "" : resource.getHostname();
				}
				EventVO eventVO=new EventVO(event.getId(),event.getOccurTime(),event.getResId(),event.getMsgTitle(),
						event.getMsgContent(),event.getServerity(),event.getMonitorId()
						,monitorName,resName,monitorType);
				eventVOs.add(eventVO);
			}
		}
		EventMeta meta=pageEvents.getMetas();
		MineEvent mineEvent=new MineEvent(pageSize,pageEvents.getCurrentPage(),eventVOs,pageEvents.getTotal(),pageEvents.getTotalPage(),new EventMetaVO(meta.getCurrentPage(),meta.getTotal(),meta.getMeta()));
		return mineEvent;
	}

	@GET
	@Path("count/query")
	@Produces(MediaType.APPLICATION_JSON)
	public int getEventCount(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
							 @QueryParam("beginTime") long beginTime) {
		 
		return ServiceManager.getInstance().getEventService().getEventCount(tenantId, new Date(beginTime),new Date());
	}

}
