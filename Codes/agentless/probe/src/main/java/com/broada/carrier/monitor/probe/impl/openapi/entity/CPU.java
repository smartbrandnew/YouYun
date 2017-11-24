package com.broada.carrier.monitor.probe.impl.openapi.entity;

public class CPU {
	
	private int cpu_logic_processors;
	private String cpu_cores;
	private String mhz;
	private String processor_type;
	
	public CPU(int cpu_logic_processors, String cpu_cores, String mhz, String processor_type) {
		this.cpu_logic_processors = cpu_logic_processors;
		this.cpu_cores = cpu_cores;
		this.mhz = mhz;
		this.processor_type = processor_type;
	}
	public CPU() {
		// TODO Auto-generated constructor stub
	}
	
	public int getCpu_logic_processors() {
		return cpu_logic_processors;
	}

	public void setCpu_logic_processors(int cpu_logic_processors) {
		this.cpu_logic_processors = cpu_logic_processors;
	}

	public String getCpu_cores() {
		return cpu_cores;
	}

	public void setCpu_cores(String cpu_cores) {
		this.cpu_cores = cpu_cores;
	}

	public String getMhz() {
		return mhz;
	}

	public void setMhz(String mhz) {
		this.mhz = mhz;
	}

	public String getProcessor_type() {
		return processor_type;
	}

	public void setProcessor_type(String processor_type) {
		this.processor_type = processor_type;
	}
	
}
