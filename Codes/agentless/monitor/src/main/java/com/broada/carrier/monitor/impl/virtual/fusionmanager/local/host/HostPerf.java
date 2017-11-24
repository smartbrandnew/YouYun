package com.broada.carrier.monitor.impl.virtual.fusionmanager.local.host;

public class HostPerf {
	private String ip;
	private String id;
	private String hostname;
	private double cpu_usage;
	private double mem_usage;
	private double disk_io_in;
	private double disk_io_out;
	private double nic_byte_in;
	private double nic_byte_out;
	private double disk_usage;
	private double net_receive_pkg_rate;
	private double net_send_pkg_rate;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public double getCpu_usage() {
		return cpu_usage;
	}
	public void setCpu_usage(double cpu_usage) {
		this.cpu_usage = cpu_usage;
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
	public double getNic_byte_in() {
		return nic_byte_in;
	}
	public void setNic_byte_in(double nic_byte_in) {
		this.nic_byte_in = nic_byte_in;
	}
	public double getNic_byte_out() {
		return nic_byte_out;
	}
	public void setNic_byte_out(double nic_byte_out) {
		this.nic_byte_out = nic_byte_out;
	}
	public double getDisk_usage() {
		return disk_usage;
	}
	public void setDisk_usage(double disk_usage) {
		this.disk_usage = disk_usage;
	}
	public double getNet_receive_pkg_rate() {
		return net_receive_pkg_rate;
	}
	public void setNet_receive_pkg_rate(double net_receive_pkg_rate) {
		this.net_receive_pkg_rate = net_receive_pkg_rate;
	}
	public double getNet_send_pkg_rate() {
		return net_send_pkg_rate;
	}
	public void setNet_send_pkg_rate(double net_send_pkg_rate) {
		this.net_send_pkg_rate = net_send_pkg_rate;
	}
	
	

}
