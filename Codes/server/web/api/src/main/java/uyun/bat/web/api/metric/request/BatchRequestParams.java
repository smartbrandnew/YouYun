package uyun.bat.web.api.metric.request;

public class BatchRequestParams {
	private long from;
	private long to;
	private String[] q;
	private int interval;
	private String[] types;

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public String[] getQ() {
		return q;
	}

	public void setQ(String[] q) {
		this.q = q;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String[] getTypes() {
		return types;
	}

	public void setTypes(String[] types) {
		this.types = types;
	}

}
