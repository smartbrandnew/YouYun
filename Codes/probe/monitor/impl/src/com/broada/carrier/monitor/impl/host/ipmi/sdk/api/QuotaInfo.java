package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * 指标信息
 * 
 * @author pippo 
 * Create By 2014-4-1 下午3:14:34
 */
public class QuotaInfo implements Serializable {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -7807715548414018178L;
	//实例名
	private String name;
	//风扇转速
	private double fanSpeed;
	//温度
	private double temperature;
	//电压
	private double voltage;
	//电流
	private double current;
	//功率
	private double power;
	//实例标识
	private EntityType type;

	public EntityType getType() {
		return type;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getFanSpeed() {
		return fanSpeed;
	}
	
	public String getFanSpeedText() {
		if (checkNotEmpty(fanSpeed)) {
			return "转速:" + fanSpeed + SensorType.FAN.getLabel();
		}
		return null;
	}

	public void setFanSpeed(double fanSpeed) {
		this.fanSpeed = fanSpeed;
	}

	public double getTemperature() {
		return temperature;
	}
	
	public String getTemperatureText() {
		if (checkNotEmpty(temperature)) {
			return "温度:" + temperature + SensorType.TEM.getLabel();
		}
		return null;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public double getVoltage() {
		return voltage;
	}
	
	public String getVoltageText() {
		if (checkNotEmpty(voltage)) {
			return "电压:" + voltage + SensorType.VOL.getLabel();
		}
		return null;
	}

	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}

	public double getCurrent() {
		return current;
	}
	
	public String getCurrentText() {
		if (checkNotEmpty(current)) {
			return "电流:" + current + SensorType.CUR.getLabel();
		}
		return null;
	}

	public void setCurrent(double current) {
		this.current = current;
	}

	@Override
	public String toString() {
		return "QuotaInfo [name=" + name + ", fanSpeed=" + fanSpeed
				+ ", temperature=" + temperature + ", voltage=" + voltage
				+ ", current=" + current + ", power=" + power + "]";
	}

	public double getPower() {
		return power;
	}
	
	public String getPowerText() {
		if (checkNotEmpty(power)) {
			return "功率:" + power + SensorType.POW.getLabel();
		}
		return null;
	}

	public void setPower(double power) {
		this.power = power;
	}
	
	public String getAllQuota() {
		List<String> list = new ArrayList<String>();
		if (getCurrentText() != null) {
			list.add(getCurrentText());
		}
		if (getTemperatureText() != null) {
			list.add(getTemperatureText());
		}
		if (getVoltageText() != null) {
			list.add(getVoltageText());
		}
		if (getPowerText() != null) {
			list.add(getPowerText());
		}
		if (getFanSpeedText() != null) {
			list.add(getFanSpeedText());
		}
		return list.toString().substring(1, list.toString().length()-1);
	}
	
	private boolean checkNotEmpty(double d){
		if (d == 0) {
			return false;
		}
		return true;
	}
}
