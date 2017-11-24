package uyun.bat.gateway.agent.entity;

public class DataValue {
	private String unit;
	private long timestamp;
	private double value;
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "DataValue [unit=" + unit + ", timestamp=" + timestamp + ", value=" + value + "]";
	}
	
	
	
	

}
