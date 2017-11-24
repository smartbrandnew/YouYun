package com.broada.carrier.monitor.client.impl.impexp.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.client.impl.impexp.util.Logger;

public class Table {
	private Map<MapObject, String> properties = new HashMap<MapObject, String>();
	private List<String> columns = new ArrayList<String>();
	private List<Row> rows = new ArrayList<Row>();

	public void setProperty(MapObject field, String value) {
		properties.put(field, value);
	}

	public void addColumn(String value) {
		if (getRowCount() > 0)
			throw new IllegalStateException();
		columns.add(value);
	}

	public int getColumnCount() {
		return columns.size();
	}

	public void addRow(Row row) {
		rows.add(row);
	}

	public Row createRow() {
		return new Row(getColumnCount());
	}

	public int getRowCount() {
		return rows.size();
	}

	@Override
	public String toString() {
		return String.format("%s[props: %s cols: %d rows: %d]", getClass().getSimpleName(), properties, getColumnCount(),
				getRowCount());
	}

	public String checkCell(MapObject col, int row) throws IllegalArgumentException {
		String value = getCell(col, row);
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(String.format("%s,%d", col.getId(), row + 1));
		return value;
	}

	public Row getRow(int row) {
		return rows.get(row);
	}

	public String getCell(MapObject col, int row) {
		int colIndex = columns.indexOf(col.getId());
		if (colIndex < 0)
			return null;
		return getCell(colIndex, row);
	}

	public String getCell(MapObject col, int row, String defaultValue) {
		String value = getCell(col, row);
		if (value == null || value.isEmpty())
			value = defaultValue;
		return value;
	}

	public String getColumn(int col) {
		return columns.get(col);
	}

	public String getCell(int col, int row) {
		Row result = getRow(row);
		return result.getValue(col);
	}

	public String getProperty(MapObject code) {
		return properties.get(code);
	}

	public String checkProperty(MapObject code) {
		String value = getProperty(code);
		if (value == null)
			throw new IllegalArgumentException("缺少表属性：" + code);
		return value;
	}

	public int getCell(MapObject col, int row, int defaultValue) {
		int value = defaultValue;
		String text = getCell(col, row);
		if (text != null && !text.isEmpty()) {
			try {
				value = Integer.parseInt(text);
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN, String.format("值[%s]不是一个正确的整形", text)));
			}
		}
		return value;
	}

}
