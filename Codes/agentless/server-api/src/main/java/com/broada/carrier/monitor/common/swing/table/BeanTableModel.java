package com.broada.carrier.monitor.common.swing.table;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;

import com.broada.carrier.monitor.common.error.WarningException;
import com.broada.carrier.monitor.common.util.BeanUtil;

public class BeanTableModel<T> extends BaseTableModel {
	private static final long serialVersionUID = 1L;
	private List<T> rows = new LinkedList<T>();
	private List<T> filterRows;	
	private BeanTableRowFilter<T> filter;
	
	public BeanTableModel() {
		super(new BaseTableColumn[0]);
	}

	public BeanTableModel(BaseTableColumn[] columns) {
		super(columns);
	}
	
	public void addRow(T row) {
		getRows().add(row);		
		fireTableDataChanged();
	}
	
	public void removeRow(T row) {
		getRows().remove(row);
		fireTableDataChanged();
	}
	
	public int getRowIndex(T row) {
		return getRows().indexOf(row);
	}
	
	public T getRow(T row) {
		int index = getRowIndex(row);
		if (index < 0)
			return null;
		return getRow(index);
	}
	
	public void setSelectedRows(BaseTable table, List<T> selectedRows) {
		table.setSelected(-1);
		if (selectedRows == null || selectedRows.isEmpty())
			return;		
		for (T row : selectedRows) {
			int index = getRows().indexOf(row);
			if (index < 0)
				continue;
			index = table.convertRowIndexToView(index);
			table.getSelectionModel().addSelectionInterval(index, index);
		}			
	}
	
	public T getRow(int index) {
		return getRows().get(index);
	}
	
	protected List<T> getRowsInner() {
		return rows;
	}

	public List<T> getRows() {
		if (filter == null)
			return getRowsInner();
		else {			
			if (filterRows == null) {
				filterRows = new LinkedList<T>();
				for (T row : getRowsInner()) {
					if (filter.match(row))
						filterRows.add(row);
				}
			}
			return filterRows;
		}
	}

	public BeanTableRowFilter<T> getFilter() {
		return filter;
	}

	public void setFilter(BeanTableRowFilter<T> filter) {
		this.filter = filter;
		this.filterRows = null;
		fireTableDataChanged();
	}

	public void setRows(T[] rows) {
		this.rows.clear();
		if (rows != null) {
			for (T row : rows)
				this.rows.add(row);
		}
		this.filterRows = null;
		fireTableDataChanged();
	}

	public void setRows(List<T> rows) {
		this.rows = rows;
		this.filterRows = null;
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return getRows().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		BaseTableColumn column = getColumn(columnIndex);
		T bean = getRow(rowIndex);
		if (bean == null)
			return null;
		return BeanUtil.checkPropertyValue(bean, column.getCode());
	}
		
	public T getSelectedRow(JTable table) {
		int index = table.getSelectedRow();
		if (index < 0) 
			return null;		
		index = table.getRowSorter().convertRowIndexToModel(index);
		if (index >= getRowCount())
			return null;
		return getRow(index);
	}

	public T checkSelectedRow(JTable table) {
		T row = getSelectedRow(table);
		if (row == null) 
			throw new WarningException("必须选择一行数据");
		return row;
	}
	
	public List<T> getSelectedRows(JTable table) {
		int[] indexs = table.getSelectedRows();				
		if (indexs == null || indexs.length == 0) 
			return null;
		
		List<T> list = new ArrayList<T>(indexs.length);
		for (int i = 0; i < indexs.length; i++) {
			if (indexs[i] >= table.getRowCount())
				continue;
			int index = table.getRowSorter().convertRowIndexToModel(indexs[i]);
			list.add(getRow(index));
		}
		return list;
	}
	
	public List<T> checkSelectedRows(JTable table) {
		List<T>	rows = getSelectedRows(table);
		if (rows == null || rows.isEmpty()) 
			throw new WarningException("必须选择一行数据");
		return rows;
	}
}
