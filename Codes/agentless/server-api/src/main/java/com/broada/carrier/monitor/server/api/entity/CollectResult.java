package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.broada.carrier.monitor.common.util.AnyObject;
import com.broada.component.utils.text.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CollectResult implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final long RESPONSETIME_NORESPONSE = -1;

	private int taskId;
	private Date time;
	private String message;
	private long responseTime;
	private CollectMonitorState state;
	private List<MonitorItem> items;
	private List<MonitorResultRow> rows;
	private String extra;
	private int progress;

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public CollectResult(String message) {
		this(CollectMonitorState.SUCCESSED, message, null);
	}

	public CollectResult() {
		this(CollectMonitorState.SUCCESSED, null, null);
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public CollectResult(CollectMonitorState state, String message, PerfResult[] perfResults) {
		this(0, new Date(), state, message, perfResults, 0);
	}

	public CollectResult(int taskId, Date time, CollectMonitorState state, String message, PerfResult[] perfResults,
			int progress) {
		this.time = time;
		this.message = message;
		this.state = state;
		this.progress = progress;
		setPerfResults(perfResults);
	}

	public CollectResult(PerfResult[] perfResults) {
		this(0, new Date(), CollectMonitorState.SUCCESSED, null, perfResults, 0);
	}

	public CollectResult(PerfResult perfResult) {
		this(new PerfResult[] { perfResult });
	}

	public CollectResult(CollectMonitorState state) {
		this(state, null, null);
	}

	public CollectResult(CollectMonitorState state, String message) {
		this(state, message, null);
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	@JsonIgnore
	public <T> T getExtraObject(Class<T> cls) {
		return AnyObject.decode(extra, cls);
	}

	public void setExtraObject(Object extra) {
		this.extra = AnyObject.encode(extra);
	}

	public CollectMonitorState getState() {
		return state;
	}

	public void setState(CollectMonitorState state) {
		this.state = state;
	}

	public List<MonitorResultRow> getRows() {
		return rows;
	}

	@JsonIgnore
	public int getRowCount() {
		return rows == null ? 0 : rows.size();
	}

	public void setRows(List<MonitorResultRow> rows) {
		this.rows = rows;
	}

	public List<MonitorItem> getItems() {
		return items;
	}

	public void addItem(MonitorItem item) {
		if (items == null)
			items = new ArrayList<MonitorItem>(5);
		items.add(item);
	}

	public void setItems(List<MonitorItem> items) {
		this.items = items;
	}

	public void addRow(MonitorResultRow row) {
		removeRow(row.getInstCode());

		if (rows == null)
			rows = new ArrayList<MonitorResultRow>();

		rows.add(row);
	}

	public MonitorResultRow removeRow(String instKey) {
		MonitorResultRow exists = getRow(instKey);
		if (exists != null)
			rows.remove(exists);
		return exists;
	}

	public MonitorResultRow getRow(String instKey) {
		if (rows == null)
			return null;

		for (MonitorResultRow row : rows) {
			if (instKey == row.getInstCode())
				return row;
			if (instKey != null && instKey.equals(row.getInstCode()))
				return row;
		}

		return null;
	}

	public void addPerfResult(PerfResult result) {
		MonitorResultRow row = null;
		if (rows == null)
			rows = new ArrayList<MonitorResultRow>();
		for (MonitorResultRow item : rows) {
			if ((item.getInstCode() == null && result.getInstKey() == null)
					|| item.getInstCode().equals(result.getInstKey())) {
				row = item;
				break;
			}
		}
		if (row == null) {
			row = new MonitorResultRow(result.getInstKey());
			rows.add(row);
		}

		row.setIndicator(result.getItemCode(), result.getValue());
	}

	public void setPerfResults(PerfResult[] perfResults) {
		if (perfResults == null) {
			rows = null;
			return;
		}

		rows = new ArrayList<MonitorResultRow>();

		for (PerfResult result : perfResults) {
			addPerfResult(result);
		}
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s[taskId: %d time: %s state: %s rows: %d message: %s progress: %d]",
				getClass().getSimpleName(), getTaskId(), DateUtil.format(getTime()), getState(),
				rows == null ? 0 : rows.size(), getMessage(), getProgress()));
		if (rows != null) {
			for (int i = 0; i < rows.size(); i++)
				sb.append("\n").append(i + 1).append(". ").append(rows.get(i));
		}
		return sb.toString();
	}

	// TODO 2015-05-03 14:48:58 最后需要删除
	@JsonIgnore
	public void setResultDesc(String string) {
		setMessage(string);
	}

	public String getResultDesc() {
		return getMessage();
	}
}
