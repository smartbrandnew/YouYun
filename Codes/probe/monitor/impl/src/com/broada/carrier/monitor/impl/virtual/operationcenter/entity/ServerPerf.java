package com.broada.carrier.monitor.impl.virtual.operationcenter.entity;

import java.util.ArrayList;
import java.util.List;

public class ServerPerf {
	
	private String objId;
	private String name;
	private String ip_addr;
	
	private List<Metric> cpustate = new ArrayList<Metric>();
	private List<Metric> memstate = new ArrayList<Metric>();
	private List<Metric> nicstate = new ArrayList<Metric>();
	private List<Metric> diskiostate = new ArrayList<Metric>();
	private List<Metric> diskstate = new ArrayList<Metric>();
	private List<Metric> nicusagestate = new ArrayList<Metric>();
	private List<Metric> dom0 = new ArrayList<Metric>();
	private List<Metric> domu = new ArrayList<Metric>();
	private List<Metric> hostdatastore = new ArrayList<Metric>();
	private List<Metric> clouddisk = new ArrayList<Metric>();
	private List<Metric> storageadapter = new ArrayList<Metric>();
	private List<Metric> necontrlableratestat = new ArrayList<Metric>();
	private List<Metric> avgresponsestat = new ArrayList<Metric>();
	private List<Metric> dev_mem_group = new ArrayList<Metric>();
	private List<Metric> dev_used_mem_group = new ArrayList<Metric>();
	
	public List<Metric> getCpustate() {
		return cpustate;
	}
	public void setCpustate(List<Metric> cpustate) {
		this.cpustate = cpustate;
	}
	public List<Metric> getMemstate() {
		return memstate;
	}
	public void setMemstate(List<Metric> memstate) {
		this.memstate = memstate;
	}
	public List<Metric> getNicstate() {
		return nicstate;
	}
	public void setNicstate(List<Metric> nicstate) {
		this.nicstate = nicstate;
	}
	public List<Metric> getDiskiostate() {
		return diskiostate;
	}
	public void setDiskiostate(List<Metric> diskiostate) {
		this.diskiostate = diskiostate;
	}
	public List<Metric> getDiskstate() {
		return diskstate;
	}
	public void setDiskstate(List<Metric> diskstate) {
		this.diskstate = diskstate;
	}
	public List<Metric> getNicusagestate() {
		return nicusagestate;
	}
	public void setNicusagestate(List<Metric> nicusagestate) {
		this.nicusagestate = nicusagestate;
	}
	public List<Metric> getDom0() {
		return dom0;
	}
	public void setDom0(List<Metric> dom0) {
		this.dom0 = dom0;
	}
	public List<Metric> getDomu() {
		return domu;
	}
	public void setDomu(List<Metric> domu) {
		this.domu = domu;
	}
	public List<Metric> getHostdatastore() {
		return hostdatastore;
	}
	public void setHostdatastore(List<Metric> hostdatastore) {
		this.hostdatastore = hostdatastore;
	}
	public List<Metric> getClouddisk() {
		return clouddisk;
	}
	public void setClouddisk(List<Metric> clouddisk) {
		this.clouddisk = clouddisk;
	}
	public List<Metric> getStorageadapter() {
		return storageadapter;
	}
	public void setStorageadapter(List<Metric> storageadapter) {
		this.storageadapter = storageadapter;
	}
	public List<Metric> getNecontrlableratestat() {
		return necontrlableratestat;
	}
	public void setNecontrlableratestat(List<Metric> necontrlableratestat) {
		this.necontrlableratestat = necontrlableratestat;
	}
	public List<Metric> getAvgresponsestat() {
		return avgresponsestat;
	}
	public void setAvgresponsestat(List<Metric> avgresponsestat) {
		this.avgresponsestat = avgresponsestat;
	}
	public List<Metric> getDev_mem_group() {
		return dev_mem_group;
	}
	public void setDev_mem_group(List<Metric> dev_mem_group) {
		this.dev_mem_group = dev_mem_group;
	}
	public List<Metric> getDev_used_mem_group() {
		return dev_used_mem_group;
	}
	public void setDev_used_mem_group(List<Metric> dev_used_mem_group) {
		this.dev_used_mem_group = dev_used_mem_group;
	}
	public String getObjId() {
		return objId;
	}
	public void setObjId(String objId) {
		this.objId = objId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp_addr() {
		return ip_addr;
	}
	public void setIp_addr(String ip_addr) {
		this.ip_addr = ip_addr;
	}
	
	public List<Metric> getMetrics(){
		List<Metric> metrics = new ArrayList<Metric>();
		if(cpustate.size() > 0)
			metrics.addAll(cpustate);
		if(memstate.size() > 0)
			metrics.addAll(memstate);
		if(nicstate.size() > 0)
			metrics.addAll(nicstate);
		if(diskiostate.size() > 0)
			metrics.addAll(diskiostate);
		if(diskstate.size() > 0)
			metrics.addAll(diskstate);
		if(nicusagestate.size() > 0)
			metrics.addAll(nicusagestate);
		if(dom0.size() > 0)
			metrics.addAll(dom0);
		if(domu.size() > 0)
			metrics.addAll(domu);
		if(hostdatastore.size() > 0)
			metrics.addAll(hostdatastore);
		if(clouddisk.size() > 0)
			metrics.addAll(clouddisk);
		if(storageadapter.size() > 0)
			metrics.addAll(storageadapter);
		if(necontrlableratestat.size() > 0)
			metrics.addAll(necontrlableratestat);
		if(diskstate.size() > 0)
			metrics.addAll(diskstate);
		if(avgresponsestat.size() > 0)
			metrics.addAll(avgresponsestat);
		if(dev_mem_group.size() > 0)
			metrics.addAll(dev_mem_group);
		if(dev_used_mem_group.size() > 0)
			metrics.addAll(dev_used_mem_group);
		return metrics;
	}
	
}
