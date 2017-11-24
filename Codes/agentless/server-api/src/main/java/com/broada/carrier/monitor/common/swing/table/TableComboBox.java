package com.broada.carrier.monitor.common.swing.table;

import com.broada.carrier.monitor.common.util.ObjectUtil;

import javax.swing.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TableComboBox extends JTextField {
	private static final long serialVersionUID = 1L;
	private BeanTableModel<Object> tableModel;
	private TableDataProvider<Object> dataProvider;
	private int pageSize;
	private BeanSelectTablePanel tablePanel;
	private PopupPanel popupPanel;
	private String lastKey;
	private ObjectDecoder decoder;
	private Object value;

	public TableComboBox(BeanTableModel<Object> tableModel, TableDataProvider<Object> dataProvider, int pageSize,
			ObjectDecoder decoder) {
		this.tableModel = tableModel;
		this.dataProvider = dataProvider;
		this.pageSize = pageSize;
		this.decoder = decoder;
		this.addKeyListener(new KeyListener());
	}

	private BeanSelectTablePanel getTablePanel() {
		if (tablePanel == null) {
			tablePanel = new BeanSelectTablePanel(tableModel, dataProvider, pageSize);
			tablePanel.getTable().addMouseListener(new TableMouseListener());
			tablePanel.getTable().addKeyListener(new TableKeyListener());
		}
		return tablePanel;
	}

	private PopupPanel getPopupPanel() {
		if (popupPanel == null)
			popupPanel = new PopupPanel();
		return popupPanel;
	}

	private class TableMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2)
				selectCurrentRow();
		}
	}

	private class TableKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
				selectCurrentRow();
		}
	}

	private class KeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DOWN)
				getPopupPanel().show(true);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			String text = getText();
			if (ObjectUtil.equals(text, lastKey))
				return;
			if (e.getModifiers() == 2 && e.getKeyCode() == 67)
				return;
			lastKey = text;
			if (!getPopupPanel().isVisible())
				getPopupPanel().show(false);
			getTablePanel().refresh(lastKey);
			TableComboBox.this.requestFocus();
		}
	}

	private class PopupPanel extends JPopupMenu {
		private static final long serialVersionUID = 1L;

		public PopupPanel() {
			setPreferredSize(new Dimension(400, 200));
			setBorder(BorderFactory.createLineBorder(Color.black));
			setLayout(new BorderLayout());
			setLightWeightPopupEnabled(true);
			add(getTablePanel(), BorderLayout.CENTER);
		}
		
		@Override
		@SuppressWarnings("deprecation")
		public void show(boolean selectFirst) {
			if (!TableComboBox.this.isEnabled() || !TableComboBox.this.isEditable())
				return;
			super.show(TableComboBox.this, 0, TableComboBox.this.getHeight());
			getTablePanel().refresh(TableComboBox.this.getText());
			if (selectFirst && getTablePanel().getTable().getRowCount() > 0) {
				getTablePanel().getTable().requestFocus();
				getTablePanel().getTable().setSelected(0);
			}
		}
	}

	private void selectCurrentRow() {
		Object obj = tableModel.getSelectedRow(getTablePanel().getTable());
		if (obj == null)
			return;
		setValue(obj);
		getPopupPanel().setVisible(false);
	}

	public void setValue(Object value) {
		String text = decoder.getName(value);
		setText(text);
		lastKey = text;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}
}