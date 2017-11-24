package com.broada.carrier.monitor.impl.virtual.fusionmanager.local.vm;

public class VmPerf {
	private String id;
	private String hostname;
	private String ip;
	private double cpu_usage;
	private double mem_usage;
	private double disk_io_in;
	private double disk_io_out;
	private double nic_byte_in;
	private double nic_byte_out;
	private double disk_usage;
	private double disk_out_ps;
	private double disk_in_ps;
	private double cpu_ready_time;
	private double disk_read_delay;
	private double disk_write_delay;
	private String os;
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
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
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
	public double getDisk_out_ps() {
		return disk_out_ps;
	}
	public void setDisk_out_ps(double disk_out_ps) {
		this.disk_out_ps = disk_out_ps;
	}
	public double getDisk_in_ps() {
		return disk_in_ps;
	}
	public void setDisk_in_ps(double disk_in_ps) {
		this.disk_in_ps = disk_in_ps;
	}
	public double getCpu_ready_time() {
		return cpu_ready_time;
	}
	public void setCpu_ready_time(double cpu_ready_time) {
		this.cpu_ready_time = cpu_ready_time;
	}
	public double getDisk_read_delay() {
		return disk_read_delay;
	}
	public void setDisk_read_delay(double disk_read_delay) {
		this.disk_read_delay = disk_read_delay;
	}
	public double getDisk_write_delay() {
		return disk_write_delay;
	}
	public void setDisk_write_delay(double disk_write_delay) {
		this.disk_write_delay = disk_write_delay;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	

}
