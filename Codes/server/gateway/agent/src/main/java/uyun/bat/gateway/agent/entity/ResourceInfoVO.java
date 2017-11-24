package uyun.bat.gateway.agent.entity;

public class ResourceInfoVO extends ResourceInfo {
	private Summary suma;

	public Summary getSuma() {
		return suma;
	}

	public void setSuma(Summary suma) {
		this.suma = suma;
	}

	public ResourceInfoVO() {
		super();
	}

	public ResourceInfoVO(String name) {
		super(name);
	}

}
