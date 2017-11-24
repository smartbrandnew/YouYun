package com.broada.carrier.monitor.impl.icmp;

import java.util.ArrayList;
import java.util.List;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import com.broada.swing.layout.VerticalFlowLayout;
import java.util.Collections;

/**
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class AddressTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	BorderLayout borderLayout1 = new BorderLayout();
	JButton jBtnAdd = new JButton();
	JButton jBtnDel = new JButton();
	JPanel jPanAct = new JPanel();
	JScrollPane jScrollPane1 = new JScrollPane();
	AddressTableModel model = new AddressTableModel();
	JTable jTabAddrs = new JTable(model);

	public AddressTablePanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		jPanAct.setLayout(new VerticalFlowLayout());
		jBtnDel.setBounds(new Rectangle(0, 0, 56, 26));
		jBtnDel.setText("删除");
		jBtnDel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBtnDel_actionPerformed(e);
			}
		});
		jBtnAdd.setBounds(new Rectangle(0, 0, 56, 26));
		jBtnAdd.setText("添加");
		jBtnAdd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jBtnAdd_actionPerformed(e);
			}
		});
		this.setLayout(borderLayout1);
		jPanAct.add(jBtnAdd, null);
		jPanAct.add(jBtnDel, null);
		this.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(jTabAddrs);
		this.add(jPanAct, BorderLayout.EAST);
		jTabAddrs.setRowHeight(20);

	}

	public List getAddressTable() {
		return model.getContent();
	}

	public void setAddressTable(List addrs) {
		model.setContent(addrs);
	}

	private final static class AddressTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] colnames = { "目标地址" };
		private List addrs = new ArrayList(0);

		public AddressTableModel() {

		}

		public int getRowCount() {
			return addrs.size();
		}

		public int getColumnCount() {
			return colnames.length;
		}

		public String getColumnName(int column) {
			if (column < 0 || column >= colnames.length) {
				return "未知列";
			}
			return colnames[column];
		}

		public Object getValueAt(int row, int column) {
			return addrs.get(row);
		}

		/*
		public boolean isCellEditable(int rowIndex, int columnIndex) {
		  return true;
		}
		*/
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			addrs.set(rowIndex, aValue);
		}

		public void addAddrss(String addr) {
			addrs.add(addr);
			int row = addrs.size();
			fireTableRowsInserted(row, row);
		}

		public String getAddress(int row) {
			if (row < 0 || row >= addrs.size()) {
				return null;
			} else {
				return (String) addrs.get(row);
			}
		}

		public String removeAddr(int row) {
			String addr = (String) addrs.remove(row);
			if (addr != null) {
				fireTableRowsDeleted(row, row);
			}
			return addr;
		}

		public List getContent() {
			return Collections.unmodifiableList(addrs);
		}

		/**
		 * 向表格模型添加数据
		 * @param addrs
		 */
		public void setContent(List content) {
			if (addrs != null) {
				addrs.clear();
			}
			this.addrs = new ArrayList(content);
			fireTableDataChanged();
		}
	}

	/*************************** Actions ********************************/

	void jBtnAdd_actionPerformed(ActionEvent e) {
		String addr = JOptionPane.showInputDialog(this, "请输入监测目标IP地址或者域名:");
		if (addr != null && addr.trim().length() > 0) {
			model.addAddrss(addr);
		}
	}

	void jBtnDel_actionPerformed(ActionEvent e) {
		int row = jTabAddrs.getSelectedRow();
		String addr = model.getAddress(row);
		if (addr == null) {
			JOptionPane.showMessageDialog(this, "请先选择要删除的目标地址！");
			return;
		}
		model.removeAddr(row);
	}
}