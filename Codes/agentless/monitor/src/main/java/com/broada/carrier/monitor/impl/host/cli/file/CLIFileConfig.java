package com.broada.carrier.monitor.impl.host.cli.file;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.StringUtil;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class CLIFileConfig extends JPanel {
	private static final long serialVersionUID = -7332092677670104191L;
	private static final int MAX_FILE_NUMBER = 100;
	private MonitorConfigContext context;
	private MonitorMethod method;

	public CLIFileConfig() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		jPanel1.setLayout(borderLayout2);
		this.setLayout(borderLayout1);
		jLabel1.setText("请填写要监测的文件");
		jButtonAdd.setText("添加监测文件");
		jButtonFresh.setText("刷新");
		jButtonDelete.setText("删除");
		this.add(jPanel1, java.awt.BorderLayout.NORTH);
		this.add(jScrollPane1, java.awt.BorderLayout.CENTER);
		jScrollPane1.getViewport().add(jTableFile);
		this.add(jPanel2, java.awt.BorderLayout.SOUTH);
		jPanel2.add(jButtonFresh);
		jPanel2.add(jButtonDelete);

		jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);
		jPanel1.add(jTextFieldFile, java.awt.BorderLayout.CENTER);
		jPanel1.add(jButtonAdd, java.awt.BorderLayout.EAST);
		jTableFile.setRowHeight(20);

		TableColumnModel tcm = jTableFile.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(15);
		tcm.getColumn(1).setPreferredWidth(220);
		tcm.getColumn(2).setPreferredWidth(45);
		tcm.getColumn(3).setPreferredWidth(45);

		jButtonDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jTableFile.getSelectedRowCount() < 0) {
					JOptionPane.showMessageDialog(CLIFileConfig.this, "请填要删除的文件", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(CLIFileConfig.this, "确实要把选定的文件从监测文件列表中删除吗？", "询问",
						JOptionPane.YES_NO_OPTION)) {
					return;
				}
				int[] selectedRows = jTableFile.getSelectedRows();
				List removing = new ArrayList();
				for (int index = 0; index < selectedRows.length; index++) {
					removing.add(fileList.get(selectedRows[index]));
				}
				fileList.removeAll(removing);
				hostFileTableModel.fireTableDataChanged();
			}
		});
		jButtonFresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				freshDiskTable();
			}
		});
		jButtonAdd.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (StringUtil.isNullOrBlank(jTextFieldFile.getText())) {
					JOptionPane.showMessageDialog(CLIFileConfig.this, "请填写要监测的文件路径", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				String filepath = jTextFieldFile.getText().trim();
				List files = null;
				try {
					List<CLIFileMonitorCondition> filePaths = new ArrayList<CLIFileMonitorCondition>();
					CLIFileMonitorCondition cliFileMonitorCondition = new CLIFileMonitorCondition();
					cliFileMonitorCondition.setField(filepath);
					filePaths.add(cliFileMonitorCondition);
					files = getProbeFileConditions(filePaths);
				} catch (Exception ex) {
					ErrorDlg.createErrorDlg(CLIFileConfig.this, "错误", "获取文件信息发生错误", ex).setVisible(true);
					return;
				}
				if (files == null || files.size() == 0) {
					JOptionPane.showMessageDialog(CLIFileConfig.this, "获取不到文件信息", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				for (int index = 0; index < files.size(); index++) {
					CLIFileMonitorCondition file = (CLIFileMonitorCondition) files.get(index);
					boolean in = false;
					for (int m = 0; m < fileList.size(); m++) {
						CLIFileMonitorCondition f = (CLIFileMonitorCondition) fileList.get(m);
						if (f.getField().equals(file.getField())) {
							in = true;
							break;
						}
					}
					if (!in) {
						fileList.add(file);
					}
				}
				hostFileTableModel.fireTableDataChanged();
			}
		});
	}

	JPanel jPanel1 = new JPanel();

	BorderLayout borderLayout1 = new BorderLayout();

	JLabel jLabel1 = new JLabel();

	BorderLayout borderLayout2 = new BorderLayout();

	JTextField jTextFieldFile = new JTextField();

	JButton jButtonAdd = new JButton();

	JScrollPane jScrollPane1 = new JScrollPane();

	JPanel jPanel2 = new JPanel();

	JButton jButtonFresh = new JButton();

	JButton jButtonDelete = new JButton();

	private List fileList = new ArrayList();

	HostFileTableModel hostFileTableModel = new HostFileTableModel();

	JTable jTableFile = new JTable(hostFileTableModel);

	public boolean verify() {
		CellEditor ce = jTableFile.getCellEditor();
		if (jTableFile.isEditing() && ce != null) {
			ce.stopCellEditing();
		}
		if (MAX_FILE_NUMBER < fileList.size()) {
			JOptionPane.showMessageDialog(CLIFileConfig.this, "已经达到最大监测文件数,最多可以监测100个文件,请删除部分文件", "错误",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(fileList.size() == 0){
			JOptionPane.showMessageDialog(CLIFileConfig.this, "没有监测的文件", "提示",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		return fileList.size() > 0;
	}

	/** ************************HostFileTableModel***************************** */
	class HostFileTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -5128876860113177831L;

		private String[] colnames = { "存在", "文件路径", "大小(MB)", "用户" };

		public HostFileTableModel() {
		}

		public int getRowCount() {
			return fileList.size();
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

			CLIFileMonitorCondition file = (CLIFileMonitorCondition) fileList.get(row);
			switch (column) {
			case 0:
				return file.isExists() ? "存在" : "不存在";
			case 1:
				return file.getField();
			case 2:
				return Double.toString(file.getSize());
			case 3:
				return file.getUser();
			default:
				return "";
			}
		}

		public Class getColumnClass(int columnIndex) {
			return String.class;
		}
	}
	
	public List getProbeFileConditions(List<CLIFileMonitorCondition> files){
		if (method == null) {
			JOptionPane.showMessageDialog(this, "请先配置监测方式", "错误", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
				
		List<CLIFileMonitorCondition> list = new ArrayList<CLIFileMonitorCondition>();
		try {
			CollectParams params = new CollectParams("CLI-HOSTFILE", context.getNode(), context.getResource(), method, files.toArray(new CLIFileMonitorCondition[0]));
			CLIFileMonitorCondition[] result = (CLIFileMonitorCondition[]) context.getServerFactory().getTaskService().collectTask(params);
			for (int i = 0; i < result.length; i++)
				list.add(result[i]);
		} catch (Exception ex) {
			ErrorDlg.createErrorDlg(CLIFileConfig.this, "错误", ex).setVisible(true);
			return list;
		}		
		
		return list;
	}
	
	public void freshDiskTable() {
		try {
		
			fileList = getProbeFileConditions(fileList);

		} catch (Exception ex) {
			ErrorDlg.createErrorDlg(CLIFileConfig.this, "错误", "更新文件信息发生错误", ex).setVisible(true);
			return;
		}
		
		hostFileTableModel.fireTableDataChanged();
	}

	public void setMethod(MonitorMethod method) {
		this.method = method;
		if (context != null)
			freshDiskTable();
	}

	public void setData(MonitorConfigContext data) {
		this.context = data;
		
		fileList.clear();	
		fileList.addAll(CLIFileMonitor.toList(data.getInstances()));
		
		if (method != null)
			freshDiskTable();
	}

	public boolean getData() {
		if (!verify()) 
			return false;
		
		context.setInstances(CLIFileMonitor.toInstances(fileList));
		
		return true;
	}
}