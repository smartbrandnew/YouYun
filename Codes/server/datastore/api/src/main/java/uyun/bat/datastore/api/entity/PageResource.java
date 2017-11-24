package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 包含资源总条数和当前页的资源数
 * @author WIN
 *
 */
public class PageResource implements Serializable {
	private static final long serialVersionUID = 1L;
	private int count;
	private List<Resource> resources;
	private int onlineCount;
	private int offlineCount;

	
	public PageResource(int count, List<Resource> resources) {
		this.count = count;
		this.resources = resources;
	}

	public PageResource(int count, List<Resource> resources, int onlineCount, int offlineCount) {
		this.count = count;
		this.resources = resources;
		this.onlineCount = onlineCount;
		this.offlineCount = offlineCount;
	}
	public PageResource(){
		
	}

	public int getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(int onlineCount) {
		this.onlineCount = onlineCount;
	}

	public int getOfflineCount() {
		return offlineCount;
	}

	public void setOfflineCount(int offlineCount) {
		this.offlineCount = offlineCount;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	@Override
	public String toString() {
		return "PageResource [count=" + count + ", resources=" + resources + ", onlineCount=" + onlineCount
				+ ", offlineCount=" + offlineCount + "]";
	}
}
