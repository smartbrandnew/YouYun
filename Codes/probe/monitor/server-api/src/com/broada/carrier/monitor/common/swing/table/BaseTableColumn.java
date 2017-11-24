package com.broada.carrier.monitor.common.swing.table;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class BaseTableColumn {
	private String code;
	private String name;
	private int minWidth;
	private int preferredWidth;
	private int maxWidth;
	private TableCellRenderer cellRenderer;
	private TableCellEditor cellEditor;

	public BaseTableColumn(String code, String name) {
		this(code, name, 0, 0, 0, null);
	}

	public BaseTableColumn(String code, String name, int preferredWidth) {
		this(code, name, 0, preferredWidth, 0, null);
	}

	public BaseTableColumn(String code, String name, int preferredWidth, TableCellRenderer cellRenderer) {
		this(code, name, 0, preferredWidth, 0, cellRenderer);
	}

	public int getPreferredWidth() {
		return preferredWidth;
	}

	public BaseTableColumn(String code, String name, TableCellRenderer cellRenderer) {
		this(code, name, 0, 0, 0, cellRenderer);
	}
	
	public BaseTableColumn(String code, String name, int minWidth, int preferredWidth, int maxWidth,
			TableCellRenderer cellRenderer, TableCellEditor cellEditor) {
		this.code = code;
		this.name = name;
		this.minWidth = minWidth;
		this.preferredWidth = preferredWidth;
		this.maxWidth = maxWidth;
		this.cellRenderer = cellRenderer;
		this.cellEditor = cellEditor;
	}

	public BaseTableColumn(String code, String name, int minWidth, int preferredWidth, int maxWidth,
			TableCellRenderer cellRenderer) {
		this(code, name, minWidth, preferredWidth, maxWidth, cellRenderer, null);
	}

	public TableCellRenderer getCellRenderer() {
		return cellRenderer;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public int getMinWidth() {
		return minWidth;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public TableCellEditor getCellEditor() {
		return cellEditor;
	}

}
