package com.broada.carrier.monitor.impl.storage.netapp.info;

/**
 * 
 * @author Shoulw (shoulw@broada.com.cn) Create By 2016-5-9 下午05:45:26
 */
public class HostBaseInfo {
  public static final String[] keys = new String[] { "sysSoftWareInfo"/*, "sysHardWareInfo"*/, "hostName", "location"/*, "memorySize", "processorCount"*/ };

  public static final String[] colNames = new String[] { "设备概况"/*"系统版本" ,"机器型号"*/, "主机名", "所在地址"/*, "内存大小(MB)", "CPU个数(个)" */};

  public static final String[] descs = new String[] { "设备概况" /*"操作系统版本信息", "机器型号"*/, "主机名", "主机所在的物理地址"/*, "主机的内存总数大小", "主机CPU个数(个)" */};
}
