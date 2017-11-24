package com.broada.carrier.monitor.impl.db.sybase.segment;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.sybase.SybaseEnhanceManager;
import com.broada.carrier.monitor.method.sybase.SybaseMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import org.apache.commons.beanutils.BeanMap;

import java.io.Serializable;
import java.util.*;


public class SybaseSegmentMonitor implements Monitor {
  public final static String[] CONDITION_FIELDS = new String[]{"segData","segIndex"};
  
  private static Map<String,String> MAPINFO = new HashMap<String,String>();
  static {
    MAPINFO.put("totalSize", "SYBASE-SEGMENT-1");
    MAPINFO.put("segData", "SYBASE-SEGMENT-2");
    MAPINFO.put("segIndex", "SYBASE-SEGMENT-3");
    MAPINFO.put("segUnused", "SYBASE-SEGMENT-4");
  }

  @Override public MonitorResult monitor(MonitorContext context) {
    boolean state = true;

    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    
    SybaseMonitorMethodOption option = new SybaseMonitorMethodOption(context.getMethod());
    //连接参数的获取
    String ip = context.getNode().getIp();
    int port = option.getPort();
    String user = option.getUsername();
    String pass = option.getPassword();
    

    List<SybaseSegment> tsList = Collections.EMPTY_LIST;
    SybaseEnhanceManager manager = null;
    try {
      manager = new SybaseEnhanceManager(ip,null,port,user,pass);
      long replyTime = System.currentTimeMillis();
      tsList = manager.getSegments();
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (Exception e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("无法获取数据段列表.");
      return result;      
    } finally{
      if(manager!=null){
        manager.close();
      }
    }

    List<MonitorInstance> instances = new ArrayList<MonitorInstance>(Arrays.asList(context.getInstances()));
    
    List<PerfResult> perfList = new ArrayList<PerfResult>();
    for (Iterator iter = instances.iterator(); iter.hasNext();) {
      MonitorInstance mi = (MonitorInstance) iter.next();
      
      String instKey = mi.getCode();
      for (int i = 0, size = tsList.size(); i < size; i++) {
        SybaseSegment ts =  tsList.get(i);
        if (ts != null & ts.getName().equalsIgnoreCase(instKey)) {
          List<PerfItemMap> nameForIndex = new ArrayList<PerfItemMap>();
          BeanMap tsMap = new BeanMap(ts);
          for (Iterator it = tsMap.keyIterator(); it.hasNext();) {
            String key = (String) it.next();
            if (MAPINFO.containsKey(key)) {
              PerfItemMap perfItemMap = new PerfItemMap(MAPINFO.get(key),key,tsMap.get(key));
              nameForIndex.add(perfItemMap);
            }        
          }          

          perfList.addAll(Arrays.asList(SybaseEnhanceManager.assemblePerf(nameForIndex, instKey)));
          
          String tsKey = ts.getName();
          Map conditionMap = new HashMap();
          Map conditionMsg = new HashMap();
          
          conditionMap.put(tsKey+"-"+CONDITION_FIELDS[0], ts.getSegData());
          conditionMap.put(tsKey+"-"+CONDITION_FIELDS[1], ts.getSegIndex());
          conditionMsg.put(tsKey+"-"+CONDITION_FIELDS[0], "数据段大小");
          conditionMsg.put(tsKey+"-"+CONDITION_FIELDS[1], "索引段大小");
          
        }
      }
      
    }
    
    result.setPerfResults(perfList.toArray(new PerfResult[0]));

    if (!state) {
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      result.setResultDesc(manager.getResultDesc().toString());
    } else {
      result.setState(MonitorConstant.MONITORSTATE_NICER);
      result.setResultDesc("监测一切正常");
    }
    
    return result;  
  }

  @Override public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();

		List<SybaseSegment> tsList = Collections.EMPTY_LIST;

		SybaseEnhanceManager sm = null;
    SybaseMonitorMethodOption option = new SybaseMonitorMethodOption(context.getMethod());
		try {
      String ip = context.getNode().getIp();
      String sid ="";
      int port = option.getPort();
      String user = option.getUsername();
      String passwd = option.getPassword();
			sm = new SybaseEnhanceManager(ip,sid,port,user,passwd);
			tsList = sm.getSegments();
		} catch (ClassNotFoundException e) {
			throw new CollectException("无效的Sybase数据库jdbc连接驱动", e);
		} catch (Exception e) {
			throw new CollectException("无法获取数据段列表", e);
		} finally {
			if (sm != null) {
				sm.close();
			}
		}

		List<PerfResult> perfList = new ArrayList<PerfResult>();
		for (int i = 0, size = tsList.size(); i < size; i++) {
			SybaseSegment ts = tsList.get(i);
			List<PerfItemMap> nameForIndex = new ArrayList<PerfItemMap>();
			BeanMap tsMap = new BeanMap(ts);
			for (Iterator it = tsMap.keyIterator(); it.hasNext();) {
				String key = (String) it.next();
				if (MAPINFO.containsKey(key)) {
					PerfItemMap perfItemMap = new PerfItemMap(MAPINFO.get(key), key, tsMap.get(key));
					nameForIndex.add(perfItemMap);
				}
			}
			for (Iterator it = nameForIndex.iterator(); it.hasNext();) {
				PerfItemMap perfItemMap = (PerfItemMap) it.next();
				PerfResult perf = new PerfResult(perfItemMap.getCode(), true);
				Object value = perfItemMap.getValue();
				if (value instanceof String) {
					perf.setStrValue((String) value);
				} else if (value instanceof Double) {
					perf.setValue(((Double) value).doubleValue());
				}
				perf.setInstanceKey(ts.getName());
				perfList.add(perf);
			}
		}
		result.setPerfResults(perfList.toArray(new PerfResult[0]));
		return result;
	}

}
