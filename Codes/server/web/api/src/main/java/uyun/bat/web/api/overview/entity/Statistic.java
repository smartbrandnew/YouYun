package uyun.bat.web.api.overview.entity;

public class Statistic {
	private String tag;
	private long resource;
	private long warn;
	private long error;
	private long info;

	public Statistic() {
		super();
	}

	public Statistic(String tag, long resource, long warn, long error, long info) {
		super();
		this.tag = tag;
		this.resource = resource;
		this.warn = warn;
		this.error = error;
		this.info = info;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getResource() {
		return resource;
	}

	public void setResource(long resource) {
		this.resource = resource;
	}

	public long getWarn() {
		return warn;
	}

	public void setWarn(long warn) {
		this.warn = warn;
	}

	public long getError() {
		return error;
	}

	public void setError(long error) {
		this.error = error;
	}

	public long getInfo() {
		return info;
	}

	public void setInfo(long info) {
		this.info = info;
	}

}
