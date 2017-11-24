package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;
import java.util.List;

/**
 * 底盘信息
 * 
 * @author pippo 
 * Create By 2014-5-13 下午7:06:03
 */
public class ChassisInfo implements Serializable{
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -5087084240165507279L;
	private Boolean isWacthed = Boolean.FALSE;
	private String name;
	private String value;
	//电源启用情况
	private String systemPower;
	//功率过载
	private boolean powerOverload;
	//电源连锁
	private String powerInterlock;
	//主电源故障
	private boolean mainPowerFault;
	//功率控制故障
	private boolean powerControlFault;
	//机箱启用
	private String chassisIntrusion;
	//面板锁定
	private String panelLockout;
	//驱动故障
	private boolean driverFault;
	//散热故障
	private boolean radiatingFault;
	private List<HealthInfo> powers;
	
	public String getSystemPower() {
		return systemPower;
	}
	public void setSystemPower(String systemPower) {
		this.systemPower = systemPower;
	}
	public boolean isPowerOverload() {
		return powerOverload;
	}
	public void setPowerOverload(boolean powerOverload) {
		this.powerOverload = powerOverload;
	}
	public String getPowerInterlock() {
		return powerInterlock;
	}
	public void setPowerInterlock(String powerInterlock) {
		this.powerInterlock = powerInterlock;
	}
	public boolean isMainPowerFault() {
		return mainPowerFault;
	}
	public void setMainPowerFault(boolean mainPowerFault) {
		this.mainPowerFault = mainPowerFault;
	}
	public boolean isPowerControlFault() {
		return powerControlFault;
	}
	public void setPowerControlFault(boolean powerControlFault) {
		this.powerControlFault = powerControlFault;
	}
	public String getChassisIntrusion() {
		return chassisIntrusion;
	}
	public void setChassisIntrusion(String chassisIntrusion) {
		this.chassisIntrusion = chassisIntrusion;
	}
	public String getPanelLockout() {
		return panelLockout;
	}
	public void setPanelLockout(String panelLockout) {
		this.panelLockout = panelLockout;
	}
	public boolean isDriverFault() {
		return driverFault;
	}
	public void setDriverFault(boolean driverFault) {
		this.driverFault = driverFault;
	}
	public boolean isRadiatingFault() {
		return radiatingFault;
	}
	public void setRadiatingFault(boolean radiatingFault) {
		this.radiatingFault = radiatingFault;
	}
	@Override
	public String toString() {
		return "ChassisInfo [systemPower=" + systemPower + ", powerOverload=" + powerOverload + ", powerInterlock="
				+ powerInterlock + ", mainPowerFault=" + mainPowerFault + ", powerControlFault=" + powerControlFault
				+ ", chassisIntrusion=" + chassisIntrusion + ", panelLockout=" + panelLockout + ", driverFault=" + driverFault
				+ ", radiatingFault=" + radiatingFault + "]";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Boolean getIsWacthed() {
		return isWacthed;
	}
	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	public List<HealthInfo> getPowers() {
		return powers;
	}
	public void setPowers(List<HealthInfo> powers) {
		this.powers = powers;
	}
	
	public String getFieldValue(ChassisType type) {
		switch (type) {
		case SYSTEMPOWER: return ChassisInterpretation.resolveOn(getSystemPower()).getLabel();
		case POWEROVERLOAD: return ChassisInterpretation.resolveBool(isPowerOverload()).getLabel();
		case POWERINTERLOCK: return ChassisInterpretation.resolveActive(getPowerInterlock()).getLabel();
		case MAINPOWERFAULT: return ChassisInterpretation.resolveBool(isMainPowerFault()).getLabel();
		case POWERCONTROLFAULT: return	ChassisInterpretation.resolveBool(isPowerControlFault()).getLabel();
		case CHASSISINTRUSION: return ChassisInterpretation.resolveActive(getChassisIntrusion()).getLabel();
		case PANELLOCKOUT: return ChassisInterpretation.resolveActive(getPanelLockout()).getLabel();
		case DRIVERFAULT: return	ChassisInterpretation.resolveBool(isDriverFault()).getLabel();
		case RADIATINGFAULT: return ChassisInterpretation.resolveBool(isRadiatingFault()).getLabel();
		default: throw new IllegalArgumentException("未知的类型：" + type);
		}
	}
	
}
