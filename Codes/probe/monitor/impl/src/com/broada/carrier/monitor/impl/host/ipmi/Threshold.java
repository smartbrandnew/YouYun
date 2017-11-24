package com.broada.carrier.monitor.impl.host.ipmi;



/**
 * 服务器类型
 * 
 * @author pippo 
 * Create By 2013-7-30 下午7:00:29
 */
public class Threshold{
	
	private boolean isExist = false;
  private double value;
  
	public Threshold() {
		super();
	}
	
	public Threshold(boolean label) {
		super();
		this.isExist = label;
	}
	
	public Threshold(boolean label, double value) {
		super();
		this.isExist = label;
		this.value = value;
	}

	public boolean isExist() {
		return isExist;
	}
	public void setExist(boolean label) {
		this.isExist = label;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}

}
