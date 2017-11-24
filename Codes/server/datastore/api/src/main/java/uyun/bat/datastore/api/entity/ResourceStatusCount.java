package uyun.bat.datastore.api.entity;

/**
 * 获取资源在线和离线总数类
 */
public class ResourceStatusCount {
	private String label;
	private int count;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
