package com.broada.carrier.monitor.probe.impl.openapi.entity;

public class Memory {
	
	private String swap;
	private String total;
	
	public Memory() {
		// TODO Auto-generated constructor stub
	}
	public Memory(String swap, String total) {
		this.swap = swap;
		this.total = total;
	}
	
	public String getSwap() {
		return swap;
	}
	public void setSwap(String swap) {
		this.swap = swap;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	
	
}
