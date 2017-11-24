package com.broada.carrier.monitor.common.swing.table;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public abstract class BaseTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private BaseTableColumn[] columns;
	private TableColumnModel columnModel;

	public BaseTableModel(BaseTableColumn[] columns) {
		this.columns = columns;
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	public BaseTableColumn getColumn(int index) {
		return columns[index];
	}	
	
	public BaseTableColumn getColumn(String code) {
		for (int i = 0; i < columns.length; i++)
			if (columns[i].getCode().equals(code))
				return columns[i];
		return null;
	}		

	public TableColumnModel getColumnModel() {
		if (columnModel == null) {
			columnModel = new DefaultTableColumnModel();
			for (int i = 0; i < getColumnCount(); i++)
				columnModel.addColumn(createColumn(i, getColumn(i)));
		}
		return columnModel;
	}

	protected static TableColumn createColumn(int index, BaseTableColumn column) {
		TableColumn result = new TableColumn();
		result.setHeaderValue(column.getName());
		if (column.getMinWidth() > 0)
			result.setMinWidth(column.getMinWidth());
		if (column.getMaxWidth() > 0) 
			result.setMaxWidth(column.getMaxWidth());					
		if (column.getPreferredWidth() > 0)
			result.setPreferredWidth(column.getPreferredWidth());		
		result.setModelIndex(index);		
		
		TableCellEditor editor = column.getCellEditor();
		if (editor != null)
			result.setCellEditor(editor);
		
		TableCellRenderer render = column.getCellRenderer();
		if (render != null)
			result.setCellRenderer(render);
		return result;
	}
}
