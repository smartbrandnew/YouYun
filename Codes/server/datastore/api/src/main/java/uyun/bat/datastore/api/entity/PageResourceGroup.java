package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 包含资源总条数和当前页的资源数
 * @author WIN
 *
 */
public class PageResourceGroup implements Serializable {
	private static final long serialVersionUID = 1L;
	private int count;
	private List<ResourceGroup> resourceGroups;
	private int onlineCount;
	private int offlineCount;
	
	
	
	public PageResourceGroup(int count, List<ResourceGroup> resourceGroups) {
		this.count = count;
		this.resourceGroups = resourceGroups;
	}

	public PageResourceGroup(int count, List<ResourceGroup> resourceGroups, int onlineCount, int offlineCount) {
		this.count = count;
		this.resourceGroups = resourceGroups;
		this.onlineCount = onlineCount;
		this.offlineCount = offlineCount;
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

	public List<ResourceGroup> getResourceGroups() {
		return resourceGroups;
	}

	public void setResourceGroups(List<ResourceGroup> resourceGroups) {
		this.resourceGroups = resourceGroups;
	}

	@Override
	public String toString() {
		return "PageResourceGroup [count=" + count + ", resourceGroups=" + resourceGroups + ", onlineCount=" + onlineCount
				+ ", offlineCount=" + offlineCount + "]";
	}

}
