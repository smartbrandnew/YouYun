package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 基本信息
 * 
 * @author pippo 
 * Create By 2014-5-13 下午7:06:03
 */
public class BasicInfo implements Serializable{
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -5087084240165507979L;
	//实例名
	private String title;
	//厂商
	private String mfg;
	//名称
	private String name;
	//序列号
	private String serial;
	//型号
	private String partNum;
	//输入电压范围
	private String inVoltRange;
	//输入频率范围
	private String inFreqRange;
	//额定功率
	private String capacity;
	//特性
	private String flags;
	private String allBasic;
	public String getMfg() {
		return mfg;
	}
	public String getMfgText() {
		if (checkNotEmpty(mfg)) {
			return "厂商:"+mfg;
		}
		return null;
	}
	public void setMfg(String mfg) {
		this.mfg = mfg;
	}
	public String getName() {
		return name;
	}
	public String getNameText() {
		if (checkNotEmpty(name)) {
			return "产品:"+name;
		}
		return null;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSerial() {
		return serial;
	}
	public String getSerialText() {
		if (checkNotEmpty(serial)) {
			return "序列号:"+serial;
		}
		return null;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public String getPartNum() {
		return partNum;
	}
	public String getPartNumText() {
		if (checkNotEmpty(partNum)) {
			return "型号:"+partNum;
		}
		return null;
	}
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}
	public String getInVoltRange() {
		return inVoltRange;
	}
	public String getInVoltRangeText() {
		if (checkNotEmpty(inVoltRange)) {
			return "输入电压:"+inVoltRange;
		}
		return null;
	}
	public void setInVoltRange(String inVoltRange) {
		this.inVoltRange = inVoltRange;
	}
	public String getInFreqRange() {
		return inFreqRange;
	}
	public String getInFreqRangeText() {
		if (checkNotEmpty(inFreqRange)) {
			return "输入频率:"+inFreqRange;
		}
		return null;
	}
	public void setInFreqRange(String inFreqRange) {
		this.inFreqRange = inFreqRange;
	}
	public String getCapacity() {
		return capacity;
	}
	public String getCapacityText() {
		if (checkNotEmpty(capacity)) {
			return "额定功率:"+capacity;
		}
		return null;
	}
	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}
	public String getFlags() {
		return flags;
	}
	public String getFlagsText() {
		if (checkNotEmpty(flags)) {
			return "特性:"+flags;
		}
		return null;
	}
	public void setFlags(String flags) {
		this.flags = flags;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public boolean isNotEmpty(){
		if (isEmpty(mfg) && isEmpty(name) && isEmpty(serial) && isEmpty(partNum) && isEmpty(inVoltRange) && isEmpty(inFreqRange) && isEmpty(capacity) && isEmpty(flags)) {
			return false;
		}
		return true;
	}
	
	private boolean isEmpty(String str){
		if (str == null  || "".equals(str)) {
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return "BasicInfo [title=" + title + ", mfg=" + mfg + ", name=" + name + ", serial=" + serial + ", partNum="
				+ partNum + ", inVoltRange=" + inVoltRange + ", inFreqRange=" + inFreqRange + ", capacity=" + capacity
				+ ", flags=" + flags + "]";
	}
	public String getAllBasic() {
		if (checkNotEmpty(allBasic)) {
			return allBasic;
		}
		List<String> list = new ArrayList<String>();
		if (getMfgText() != null) {
			list.add(getMfgText());
		}
		if (getNameText() != null) {
			list.add(getNameText());
		}
		if (getSerialText() != null) {
			list.add(getSerialText());
		}
		if (getPartNumText() != null) {
			list.add(getPartNumText());
		}
		if (getInVoltRangeText() != null) {
			list.add(getInVoltRangeText());
		}
		if (getInFreqRangeText() != null) {
			list.add(getInFreqRangeText());
		}
		if (getCapacityText() != null) {
			list.add(getCapacityText());
		}
		if (getFlagsText() != null) {
			list.add(getFlagsText());
		}
		return list.toString().substring(1, list.toString().length()-1);
	
		
	}
	public void setAllBasic(String allBasic) {
		this.allBasic = allBasic;
	}
	
	private boolean checkNotEmpty(String d){
		if (d == null || "".equals(d.trim())) {
			return false;
		}
		return true;
	}
}
