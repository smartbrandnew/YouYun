package com.broada.carrier.monitor.impl.common.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-12-19 上午10:17:56
 */
public class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
  private static final long serialVersionUID = -8623421185643018234L;

  private final JSpinner spinner = new JSpinner();

  public SpinnerCellEditor(JSpinner spinner) {
    this.spinner.setModel(spinner.getModel());
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    spinner.setValue(value);
    return spinner;
  }

  public boolean isCellEditable(EventObject evt) {
    if (evt instanceof MouseEvent) {
      return ((MouseEvent) evt).getClickCount() >= 1;
    }
    return true;
  }

  public Object getCellEditorValue() {
    return spinner.getValue();
  }
  
  public boolean stopCellEditing() {
    try {
      spinner.commitEdit();
    } catch (ParseException e) {
      return false;
    }
    return super.stopCellEditing();
  }
}
