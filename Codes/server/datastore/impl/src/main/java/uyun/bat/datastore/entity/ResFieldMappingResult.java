package uyun.bat.datastore.entity;

public class ResFieldMappingResult {
	private String ipaddr;
	private String type;
	public String getIpaddr() {
		return ipaddr;
	}
	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "ResFieldMappingResult [ipaddr=" + ipaddr + ", type=" + type + "]";
	}
	
}
