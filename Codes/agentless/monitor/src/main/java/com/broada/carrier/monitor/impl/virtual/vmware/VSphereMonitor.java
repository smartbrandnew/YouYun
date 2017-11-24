package com.broada.carrier.monitor.impl.virtual.vmware;

import java.util.Map;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.MonitorUtil;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.vmware.VSphereConnection;
import com.broada.carrier.monitor.method.vmware.VSphereException;
import com.broada.carrier.monitor.method.vmware.VSphereMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.vmware.vim25.ArrayOfHostVirtualNic;
import com.vmware.vim25.HostVirtualNic;

/**
 * 代码与原来的AbstractVSphereMonitor保持一致
 * 
 * 
 * 监测器相关告警行为
 * 	a. 告警1：监测器状态翻转告警。
 * 	当对监测器进行停止监测设置时，监测器状态为未检测
 * 	当所有监测实例状态为正常时监测器状态为正常
 * 	当监测器无法连接vcenter时，监测器状态为未知
 * 	其他情况监测器状态为异常
 * 
 * 	当监测器状态在正常、未知、异常3种状态间转化时会发出监测器状态翻转告警
 * 	b. 告警2：hypervisor实例监测项目在异常和正常2种状态间转化时发出告警；告警中有独立字段说明
 * 	hypervisor名称、UUID。
 * 	c. 告警3：由于网络、密码等原因采集不到时，发送告警
 * 
 * 告警1通过bcc原有框架发送，不需要再开发
 * 告警2、3通过监测器自定义告警的方式发送
 * 	b. 告警2：hypervisor实例监测项目在异常和正常2种状态间转化时发出告警；告警中有独立字段说明
 * 	hypervisor名称、UUID。
 * 
 * 告警3：由于网络、密码等原因采集不到时，发送告警
 * @author Panhk
 * @version 1.0
 * @created 28-九月-2012 15:40:29
 */
public abstract class VSphereMonitor extends BaseMonitor {

	/**
   * 获取监测参数实体
   * @param srv
   * @param p
   * @return
   */
  protected VSphereMonitorMethodOption getOption(CollectContext context) {
    return new VSphereMonitorMethodOption(context.getMethod());
  }
  
  /**
   * 连接VSphere SDK
   * 返回VSphere SDK，该连接的状态为：已连接
   * 
   * @param parameter
   * @param ipAddr
   * @return
   * @throws CollectException
   */
	protected VSphereConnection connectVSphereSDK(String ipAddress, String userName , String password)
			throws CollectException {
		VSphereConnection connection = VSphereConnection.getVSphereConnection(VSphereConnection.getVSphereSDKUrl(ipAddress));
		try {
			connection.connect(userName, password);
		} catch (VSphereException e) {
			throw new CollectException("连接vSphere sdk失败", e);
		}
		return connection;
	}
	
	/**
	 * 通过value值判断性能状态
	 * 
	 * 注意：该方法只能区分正常和未知
	 * @param value
	 * @return
	 */
	protected MonitorState getStateByDoubleValue(double value) {
		if (MonitorUtil.isUnknownDoubleValue(value)) {
			return MonitorState.UNMONITOR;
		}
		return MonitorConstant.MONITORSTATE_NICER;
	}

	/**
	 * 通过value值判断性能状态
	 * 
	 * 注意：该方法只能区分正常和未知
	 * @param strValue
	 * @return
	 */
	protected MonitorState getStateByStringValue(String strValue) {
		if (MonitorUtil.isUnknownStringValue(strValue)) {
			return MonitorState.UNMONITOR;
		}
		return MonitorConstant.MONITORSTATE_NICER;
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
		// 获取ip地址
		String ipAddr = MonitorConstant.UNKNOWN_STRING_VALUE;

		if (hostInfo.get(vnicProperty) != null) {
			return getIpAdressFromHostVNic((ArrayOfHostVirtualNic) hostInfo.get(vnicProperty));
		}
		if (MonitorUtil.isUnknownStringValue(ipAddr) && hostInfo.get(consoleVNicProperty) != null) {
			return getIpAdressFromHostVNic((ArrayOfHostVirtualNic) hostInfo.get(consoleVNicProperty));

		}
		return ipAddr;
	}

	/**
	 * 从虚拟网卡列表中获取ip
	 * 
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
	
	/**
	 * 获取value值的描述
	 * 
	 * 注意：该方法只能区分正常和未知
	 * @param currValue
	 * @return
	 */
	protected String getDoubleValueDescr(String currValue) {
		if (MonitorUtil.isUnknownDoubleValue(currValue)) {
			return MonitorConstant.UNKNOWN_VALUE_STRING_DESCR;
		}
		return currValue;
	}
}