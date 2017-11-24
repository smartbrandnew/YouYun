package com.broada.carrier.monitor.common.swing.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;

public class BeanSelectTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;	
	private BeanTable<Object> table;
	private TableDataProvider<Object> dataProvider;
	private JPanel panel = new JPanel();
	private JButton btnFillMore = new JButton("加载更多");
	private int pageIndex;
	private int pageSize;
	private String key;

	public BeanSelectTablePanel(BeanTableModel<Object> tableModel, TableDataProvider<Object> dataProvider, int pageSize) {
		setPreferredSize(new Dimension(400, 280));
		setLayout(new BorderLayout(0, 0));
		
		this.table = new BeanTable<Object>(tableModel);
		this.table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.dataProvider = dataProvider;
		this.pageSize = pageSize;

		add(panel, BorderLayout.SOUTH);
		btnFillMore.addActionListener(new BtnFillMoreActionListener());
		panel.add(btnFillMore);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);

		scrollPane.setViewportView(table);
		refresh("");
	}
	
	public void refresh(String key) {
		if (this.key == null) {
			if (key == null)
				return;
			else
				this.key = key;
		} else {
			if (key == null)
				this.key = key;
			else if (this.key.equals(key))
				return;
			else
				this.key = key;
		}
		
		pageIndex = 0;		
		Page<Object> page = dataProvider.getData(PageNo.createByIndex(pageIndex, pageSize), key);
		table.getModel().setRows(page.getRows());
		btnFillMore.setEnabled(page.isMore());
	}

	private class BtnFillMoreActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			pageIndex++;
			Page<Object> page = dataProvider.getData(PageNo.createByIndex(pageIndex, pageSize), key);
			for (Object row : page.getRows())
				table.getModel().addRow(row);
			btnFillMore.setEnabled(page.isMore());
		}
	}

	public Object getSelectedObject() {
		return table.getSelectedObject();
	}

	public void setSelectedObject(Object item) {
		table.setSelectedObject(item);
	}

	public BeanTable<Object> getTable() {
		return table;
	}	
}
