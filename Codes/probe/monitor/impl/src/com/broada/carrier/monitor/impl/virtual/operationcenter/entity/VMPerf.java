package com.broada.carrier.monitor.impl.virtual.operationcenter.entity;

import java.util.ArrayList;
import java.util.List;

public class VMPerf {
	private String objId;
	private String name;
	private String ip_addr;
	
	private List<Metric> diskstate = new ArrayList<Metric>();
	private List<Metric> diskioinout = new ArrayList<Metric>();
	private List<Metric> virtrualkpi = new ArrayList<Metric>();
	private List<Metric> nicbyteinout = new ArrayList<Metric>();
	private List<Metric> cpuinhost = new ArrayList<Metric>();
	private List<Metric> commonstatus = new ArrayList<Metric>();
	
	public String getObjId() {
		return objId;
	}
	public void setObjId(String objId) {
		this.objId = objId;
	}
	public List<Metric> getDiskstate() {
		return diskstate;
	}
	public void setDiskstate(List<Metric> diskstate) {
		this.diskstate = diskstate;
	}
	public List<Metric> getDiskioinout() {
		return diskioinout;
	}
	public void setDiskioinout(List<Metric> diskioinout) {
		this.diskioinout = diskioinout;
	}
	public List<Metric> getVirtrualkpi() {
		return virtrualkpi;
	}
	public void setVirtrualkpi(List<Metric> virtrualkpi) {
		this.virtrualkpi = virtrualkpi;
	}
	public List<Metric> getNicbyteinout() {
		return nicbyteinout;
	}
	public void setNicbyteinout(List<Metric> nicbyteinout) {
		this.nicbyteinout = nicbyteinout;
	}
	public List<Metric> getCpuinhost() {
		return cpuinhost;
	}
	public void setCpuinhost(List<Metric> cpuinhost) {
		this.cpuinhost = cpuinhost;
	}
	public List<Metric> getCommonstatus() {
		return commonstatus;
	}
	public void setCommonstatus(List<Metric> commonstatus) {
		this.commonstatus = commonstatus;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setIp_addr(String ip_addr) {
		this.ip_addr = ip_addr;
	}
	public String getIp_addr() {
		return ip_addr;
	}
	
	public List<Metric> getMetrics(){
		List<Metric> metrics = new ArrayList<Metric>();
		if(diskstate.size() > 0)
			metrics.addAll(diskstate);
		if(diskioinout.size() > 0)
			metrics.addAll(diskioinout);
		if(virtrualkpi.size() > 0)
			metrics.addAll(virtrualkpi);
		if(nicbyteinout.size() > 0)
			metrics.addAll(nicbyteinout);
		if(cpuinhost.size() > 0)
			metrics.addAll(cpuinhost);
		if(commonstatus.size() > 0)
			metrics.addAll(commonstatus);
		return metrics;
	}
	
}