package uyun.bat.monitor.api.entity;

import java.util.List;

public class PageNotifyRecord {
	private int count;
	private List<NotifyRecord> NotifyRecords;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<NotifyRecord> getNotifyRecords() {
		return NotifyRecords;
	}

	public void setNotifyRecords(List<NotifyRecord> notifyRecords) {
		NotifyRecords = notifyRecords;
	}

}
