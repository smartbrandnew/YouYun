package com.broada.carrier.monitor.impl.virtual.fusioncompute.cluster;

public class ClusterPerf {
	private String clusterName;
	private Double cpu_usage;
	private Double mem_usage;
	private Double logic_disk_usage;
	private Double nic_byte_in_usage;
	private Double nic_byte_in;
	private Double nic_byte_out_usage;
	private Double nic_byte_out;
	private String urn;
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public Double getCpu_usage() {
		return cpu_usage;
	}
	public void setCpu_usage(Double cpu_usage) {
		this.cpu_usage = cpu_usage;
	}
	public Double getMem_usage() {
		return mem_usage;
	}
	public void setMem_usage(Double mem_usage) {
		this.mem_usage = mem_usage;
	}
	public Double getLogic_disk_usage() {
		return logic_disk_usage;
	}
	public void setLogic_disk_usage(Double logic_disk_usage) {
		this.logic_disk_usage = logic_disk_usage;
	}
	public Double getNic_byte_in_usage() {
		return nic_byte_in_usage;
	}
	public void setNic_byte_in_usage(Double nic_byte_in_usage) {
		this.nic_byte_in_usage = nic_byte_in_usage;
	}
	public Double getNic_byte_in() {
		return nic_byte_in;
	}
	public void setNic_byte_in(Double nic_byte_in) {
		this.nic_byte_in = nic_byte_in;
	}
	public Double getNic_byte_out_usage() {
		return nic_byte_out_usage;
	}
	public void setNic_byte_out_usage(Double nic_byte_out_usage) {
		this.nic_byte_out_usage = nic_byte_out_usage;
	}
	public Double getNic_byte_out() {
		return nic_byte_out;
	}
	public void setNic_byte_out(Double nic_byte_out) {
		this.nic_byte_out = nic_byte_out;
	}
	public String getUrn() {
		return urn;
	}
	public void setUrn(String urn) {
		this.urn = urn;
	}

	
	
}
