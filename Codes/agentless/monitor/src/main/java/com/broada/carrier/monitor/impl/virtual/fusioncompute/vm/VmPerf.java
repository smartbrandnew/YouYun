package com.broada.carrier.monitor.impl.virtual.fusioncompute.vm;

public class VmPerf {
	private String ip;
	private String hostname;
	private String uuid;
	private double cpu_usage;
	private double cpu_ready_time;
	private double mem_usage;
	private double disk_io_in;
	private double disk_io_out;
	private double nic_byte_in;
	private double nic_byte_out;
	private double disk_iowr_ticks;
	private double disk_usage;
	private double disk_req_in;
	private double disk_req_out;
	private double disk_iord_ticks;
	private String os;
	public double getCpu_usage() {
		return cpu_usage;
	}

	public void setCpu_usage(double cpu_usage) {
		this.cpu_usage = cpu_usage;
	}

	public double getCpu_ready_time() {
		return cpu_ready_time;
	}

	public void setCpu_ready_time(double cpu_ready_time) {
		this.cpu_ready_time = cpu_ready_time;
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

	public double getDisk_iowr_ticks() {
		return disk_iowr_ticks;
	}

	public void setDisk_iowr_ticks(double disk_iowr_ticks) {
		this.disk_iowr_ticks = disk_iowr_ticks;
	}

	public double getDisk_usage() {
		return disk_usage;
	}

	public void setDisk_usage(double disk_usage) {
		this.disk_usage = disk_usage;
	}

	public double getDisk_req_in() {
		return disk_req_in;
	}

	public void setDisk_req_in(double disk_req_in) {
		this.disk_req_in = disk_req_in;
	}

	public double getDisk_req_out() {
		return disk_req_out;
	}

	public void setDisk_req_out(double disk_req_out) {
		this.disk_req_out = disk_req_out;
	}

	public double getDisk_iord_ticks() {
		return disk_iord_ticks;
	}

	public void setDisk_iord_ticks(double disk_iord_ticks) {
		this.disk_iord_ticks = disk_iord_ticks;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	

	
}
