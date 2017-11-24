package com.broada.carrier.monitor.impl.virtual.fusioncompute.host;

public class HostPerf {
	private String hostname;
	private String ip;
	private double cpu_usage;
	private double dom0_cpu_usage;
	private double dom0_mem_usage;
	private double domU_cpu_usage;
	private double domU_mem_usage;
	private double mem_usage;
	private double disk_io_in;
	private double disk_io_out;
	private double logic_disk_usage;
	private double nic_pkg_send;
	private double nic_pkg_rcv;
	private double nic_byte_in_usage;
	private double nic_byte_in;
	private double nic_byte_out_usage;
	private double nic_byte_out;
	private String urn;
	private String os;
	public double getCpu_usage() {
		return cpu_usage;
	}
	public void setCpu_usage(double cpu_usage) {
		this.cpu_usage = cpu_usage;
	}
	public double getDom0_cpu_usage() {
		return dom0_cpu_usage;
	}
	public void setDom0_cpu_usage(double dom0_cpu_usage) {
		this.dom0_cpu_usage = dom0_cpu_usage;
	}
	public double getDom0_mem_usage() {
		return dom0_mem_usage;
	}
	public void setDom0_mem_usage(double dom0_mem_usage) {
		this.dom0_mem_usage = dom0_mem_usage;
	}
	public double getDomU_cpu_usage() {
		return domU_cpu_usage;
	}
	public void setDomU_cpu_usage(double domU_cpu_usage) {
		this.domU_cpu_usage = domU_cpu_usage;
	}
	public double getDomU_mem_usage() {
		return domU_mem_usage;
	}
	public void setDomU_mem_usage(double domU_mem_usage) {
		this.domU_mem_usage = domU_mem_usage;
	}
	public double getMem_usage() {
		return mem_usage;
	}
	public void setMem_usage(double mem_usage) {
		this.mem_usage = mem_usage;
	}
	public double getDisk_io_in() {
		return disk_io_in;
	}
	public void setDisk_io_in(double disk_io_in) {
		this.disk_io_in = disk_io_in;
	}
	public double getDisk_io_out() {
		return disk_io_out;
	}
	public void setDisk_io_out(double disk_io_out) {
		this.disk_io_out = disk_io_out;
	}
	public double getLogic_disk_usage() {
		return logic_disk_usage;
	}
	public void setLogic_disk_usage(double logic_disk_usage) {
		this.logic_disk_usage = logic_disk_usage;
	}
	public double getNic_pkg_send() {
		return nic_pkg_send;
	}
	public void setNic_pkg_send(double nic_pkg_send) {
		this.nic_pkg_send = nic_pkg_send;
	}
	public double getNic_pkg_rcv() {
		return nic_pkg_rcv;
	}
	public void setNic_pkg_rcv(double nic_pkg_rcv) {
		this.nic_pkg_rcv = nic_pkg_rcv;
	}
	public double getNic_byte_in_usage() {
		return nic_byte_in_usage;
	}
	public void setNic_byte_in_usage(double nic_byte_in_usage) {
		this.nic_byte_in_usage = nic_byte_in_usage;
	}
	public double getNic_byte_in() {
		return nic_byte_in;
	}
	public void setNic_byte_in(double nic_byte_in) {
		this.nic_byte_in = nic_byte_in;
	}
	public double getNic_byte_out_usage() {
		return nic_byte_out_usage;
	}
	public void setNic_byte_out_usage(double nic_byte_out_usage) {
		this.nic_byte_out_usage = nic_byte_out_usage;
	}
	public double getNic_byte_out() {
		return nic_byte_out;
	}
	public void setNic_byte_out(double nic_byte_out) {
		this.nic_byte_out = nic_byte_out;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUrn() {
		return urn;
	}
	public void setUrn(String urn) {
		this.urn = urn;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	
	

	
	
}
