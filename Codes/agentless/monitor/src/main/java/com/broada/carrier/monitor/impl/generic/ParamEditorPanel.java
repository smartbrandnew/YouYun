package com.broada.carrier.monitor.impl.generic;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.broada.numen.agent.script.entity.DynamicParam;

public class ParamEditorPanel extends JScrollPane {
	private static final long serialVersionUID = 1L;
	private static final int VALUE_COLUMN_INDEX = 1;

	private DefaultTableModel paramModel = null;
	private JTable paramTable = new DynamicParamEditTable();
	private DynamicParam[] params;

	public ParamEditorPanel() {
		try {
			jbInit();

			paramModel = new DefaultTableModel(new Object[] { "参数名称", "配置值" }, 0) {
				private static final long serialVersionUID = -4891297702973081129L;

				/*
				 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
				 */
				public boolean isCellEditable(int row, int column) {
					return column == VALUE_COLUMN_INDEX;
				}
			};
			paramTable.setModel(paramModel);
			paramTable.setRowHeight(20);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {		
		this.setPreferredSize(new Dimension(350, 200));
		this.getViewport().add(paramTable);
	}

	public void setParams(DynamicParam[] params) {
		paramModel.setNumRows(0);
		this.params = params;
		if (params == null)
			return;

		for (int i = 0; i < params.length; i++) {
			paramModel.addRow(new Object[] { params[i].getName(),
					params[i].getValue() });
		}
	}
	
	public DynamicParam[] getParams() {
		return params;
	}

	public void applyModify() {
		if (params == null || params.length == 0) {
			return;
		}
		if (paramTable.isEditing())
			paramTable.getCellEditor().stopCellEditing();
		for (int i = 0; i < params.length; i++)
			params[i].setValue((String) paramModel.getValueAt(i, VALUE_COLUMN_INDEX));
	}
	
  public static boolean showEdit(String dialogCaption, String paramsCaption, DynamicParam[] params) {
  	ParamEditorPanel paramEditor = new ParamEditorPanel();
    paramEditor.setBorder(BorderFactory.createTitledBorder(BorderFactory
        .createEtchedBorder(), paramsCaption));
    paramEditor.setParams(params);
    
    JOptionPane op = new JOptionPane(paramEditor, JOptionPane.PLAIN_MESSAGE,
        JOptionPane.OK_CANCEL_OPTION);            
    JDialog dlg = op.createDialog(null, dialogCaption);
    dlg.setVisible(true);
    if (String.valueOf(op.getValue()) .equals( String.valueOf(JOptionPane.OK_OPTION)) ){  
    	paramEditor.applyModify();
    	return true;
    } else
    	return false;
  }	
  
  /*
   * 扩展JTable以实现不同的单元格，使用不同的CellRenderer。
   */
  private class DynamicParamEditTable extends JTable {
		private static final long serialVersionUID = 9149345067669661802L;
		
		/*
		 * @see javax.swing.JTable#getCellEditor(int, int)
		 */
		public TableCellEditor getCellEditor(int row, int column) {
			if (row < params.length && params[row].isEncrypted() && column == VALUE_COLUMN_INDEX) {
				return new DefaultCellEditor(new JPasswordField());
			}
  		return super.getCellEditor(row, column);
  	}

		/*
		 * @see javax.swing.JTable#getCellRenderer(int, int)
		 */
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (row < params.length && params[row].isEncrypted() && column == VALUE_COLUMN_INDEX) {
				return new PasswordCellRenderer();
			}
  		return super.getCellRenderer(row, column);
  	}
	};
	
	private class PasswordCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -6770314950013839578L;

		/*
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setFiledValue(value, row, column);
			return component;
		}
		
		private void setFiledValue(Object value, int row, int column) {
			if (value == null) {
				return;
			}
			String str = (String)value;
			byte[] data = new byte[str.length()];
			for (int i = 0; i < data.length; i++) {
				data[i] = '*';
			}
			super.setValue(new String(data));
		}
  };
  
}
