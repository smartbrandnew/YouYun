package uyun.bat.web.api.resource.entity;

public class Indication {
	private String id;
	private Double indication;
	private boolean state;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getIndication() {
		return indication;
	}

	public void setIndication(Double indication) {
		this.indication = indication;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}
}
