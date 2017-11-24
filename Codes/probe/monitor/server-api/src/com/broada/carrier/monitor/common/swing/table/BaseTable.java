package com.broada.carrier.monitor.common.swing.table;

import com.broada.carrier.monitor.server.api.entity.MonitorRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.Comparator;
import java.util.Date;

/**
 * 基础表格组件，实现了一以下功能：
 * 设置一般行为：默认行高、自动显示滚动条、自动修改所有列宽、允许排序、表格列头默认居中 
 * @author Jiangjw
 */
public class BaseTable extends JTable {
	private static final long serialVersionUID = 1L;
	
	public BaseTable() {
		this(new EmptyBeanTableModel());
	}
	
	public BaseTable(BaseTableModel model) {
		super(model);
		setRowHeight(24);
		setAutoscrolls(true);
		setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
		getTableHeader().setReorderingAllowed(false);
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) getTableHeader().getDefaultRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);		
	}

	/**
	 * 选中指定行
	 * @param rowIndex
	 */
	public void setSelected(int rowIndex) {
		if (rowIndex < 0)
			rowIndex = -1;
		getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
	}
	
	public BaseTableModel getModel() {
		return (BaseTableModel) super.getModel();
	}

	@Override
	public void setModel(TableModel dataModel) {
		if (!(dataModel instanceof BaseTableModel))
			throw new IllegalArgumentException("只允许使用BaseTableModel");
		
		super.setModel(dataModel);
		setColumnModel(((BaseTableModel) dataModel).getColumnModel());
		OrderNumberComparator comparater = new OrderNumberComparator();
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dataModel);
		for (int i = 0; i < getColumnCount(); i++) {
			sorter.setComparator(i, comparater);
		}
		super.setRowSorter(sorter);
	}

	private static class OrderNumberComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			if (o1.getClass() == o2.getClass()) {
				if (o1 instanceof Integer)
					return ((Integer) o1).compareTo((Integer) o2);
				else if (o1 instanceof Long)
					return ((Long) o1).compareTo((Long) o2);
				else if (o1 instanceof Date)
					return ((Date) o1).compareTo((Date) o2);
				else if (o1 instanceof MonitorRecord){
					MonitorRecord r1 = (MonitorRecord) o1;
					MonitorRecord r2 = (MonitorRecord) o2;
					return r1.getState().getDisplayName().compareTo(r2.getState().getDisplayName());
				}
			}			
			String s1 = o1.toString();
			String s2 = o2.toString();
			s1 = s1 == null ? "" : s1;
			s2 = s2 == null ? "" : s2;
			return s1.compareTo(s2);
		}
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		if (autoResizeMode != AUTO_RESIZE_OFF) {
			if (getParent() instanceof JViewport) {
				return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
			}
		}
		return false;
	}
}
