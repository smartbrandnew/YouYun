package com.broada.carrier.monitor.impl.host.snmp.info;

/**
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-12-3 下午05:45:26
 */
public class HostBaseInfo {
  public static final String[] keys = new String[] { "sysSoftWareInfo", "sysHardWareInfo", "hostName", "location", "memorySize", "processorCount", "intfList"};

  public static final String[] colNames = new String[] { "系统版本", "机器型号", "主机名", "MAC地址", "内存大小(MB)", "CPU核数(个)", "端口列表"};

  public static final String[] descs = new String[] { "操作系统版本信息", "机器型号", "主机名", "主机所在的物理地址", "主机的内存总数大小", "主机CPU内核数(个)", "主机物理端口列表" };
}
