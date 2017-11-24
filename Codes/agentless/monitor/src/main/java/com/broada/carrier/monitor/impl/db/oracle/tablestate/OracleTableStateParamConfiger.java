package com.broada.carrier.monitor.impl.db.oracle.tablestate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.swing.util.ErrorDlg;

/**
 * <p>
 * Title: oracle表状态监测
 * </p>
 * <p>
 * Description: 产品部
 * </p>
 * <p>
 * Copyright: Copyright (c) 2010
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author caikang
 * @version 3.3
 */

public class OracleTableStateParamConfiger implements MonitorConfiger {
	private static final Log logger = LogFactory.getLog(OracleTableStateParamConfiger.class);

	/**
	 * 
	 * OracleTableStateParamConfiger的内部类TableStateParamPanel
	 * 
	 * @author Administrator
	 * 
	 */
	class TableStateParamPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private List<OracleTableState> tState = new ArrayList<OracleTableState>();
		private BorderLayout borderLayout1 = new BorderLayout();
		private BorderLayout borderLayout2 = new BorderLayout();		
		private JPanel jPanel1 = new JPanel();
		private JScrollPane jScrollPane1 = new JScrollPane();
		private JPanel jPanel2 = new JPanel();
		private JButton jBtnGetData = new JButton();
		private JButton jBtnGetMore = new JButton();
		private OracleTableStateModel model = new OracleTableStateModel();
		private JTable jTable1 = new JTable(model);
		private JLabel jLabel = new JLabel();
		private JTextField filterTextField = new JTextField();
		private JSpinner sprFilterTextField = new JSpinner();
		private int pageIndex;

		public TableStateParamPanel() {
			try {
				jbInit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void jbInit() {
			this.setLayout(borderLayout1);
			jPanel1.setLayout(borderLayout2);
			jPanel1.setFont(new java.awt.Font("Dialog", 0, 11));// 设置此组件的字体。
			jScrollPane1.setFont(new java.awt.Font("Dialog", 0, 11));
			jScrollPane1.setEnabled(true);			
			
			jPanel2.setPreferredSize(new Dimension(400, 30));
			jPanel2.setLayout(new FlowLayout());
			
			JPanel panel = new JPanel();
			
			jLabel.setText("过滤：");
			panel.add(jLabel);
			
			filterTextField.setEnabled(true);
			filterTextField.setColumns(12);
			panel.add(filterTextField);
						
			JLabel label = new JLabel();
			label.setText("表个数：");
			panel.add(label);
			
			sprFilterTextField.setValue(10);
			panel.add(sprFilterTextField);
			jPanel2.add(panel, BorderLayout.CENTER);

			jBtnGetData.setEnabled(true);
			jBtnGetData.setText("重新加载");		
			jPanel2.add(jBtnGetData, BorderLayout.EAST);
			
			jBtnGetMore.setEnabled(true);
			jBtnGetMore.setText("加载更多");		
			jPanel2.add(jBtnGetMore, BorderLayout.EAST);
			
			jPanel1.add(jPanel2, BorderLayout.NORTH);
			jScrollPane1.setViewportView(jTable1);
			jPanel1.add(jScrollPane1, BorderLayout.CENTER);
			this.add(jPanel1, BorderLayout.CENTER);
			jBtnGetData.addActionListener(new TableStateParamPanel_jBtnGetData_actionAdapter(this));// 按下获取数据按钮
			jBtnGetMore.addActionListener(new TableStateParamPanel_jBtnGetMore_actionAdapter(this));// 按下获取数据按钮
		}

		public Component getConfigUI() {
			return this;
		}

		public void jBtnGetData_actionPerformed(ActionEvent e, String jtfText) {
			String toBigJtfText = jtfText.toUpperCase();
			int tableNum = ((Number)sprFilterTextField.getValue()).intValue();			
			pageIndex = 0;
			tState.clear();
			resetTableStateInfo(toBigJtfText, tableNum, pageIndex);
		}
		
		public void jBtnGetMore_actionPerformed(ActionEvent e, String jtfText) {
			String toBigJtfText = jtfText.toUpperCase();
			int tableNum = ((Number)sprFilterTextField.getValue()).intValue();
			pageIndex++;
			resetTableStateInfo(toBigJtfText, tableNum, pageIndex);
		}

		private void resetTableStateInfo(String jtfText, int tableNum, int pageIndex) {
			if (method == null) {
				JOptionPane.showMessageDialog(this, "获取Oracle数据库登录配置参数失败", "错误", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			fillTState(jtfText, tableNum);
			model.fillCells(tState);
		}

		private void fillTState(String jtfText, int tableNum) {
		  try {
		  	CollectRequest request = CollectRequest.createTablesRequest(tableNum, pageIndex, jtfText);
		  	CollectParams params = new CollectParams(data.getTask().getTypeId(), data.getNode(), data.getResource(), method, null);
		  	params.setParameterObject(request);
		  	ArrayList<OracleTableState> tss = (ArrayList<OracleTableState>) data.getServerFactory().getTaskService().collectTask(params);		  	
		  	tState.addAll(tss);       	
      } catch (Throwable e) {
        ErrorDlg.createErrorDlg(this, "错误",e.getMessage(), e).setVisible(true);
      }
		}

		public boolean verify() {
			this.jBtnGetData.requestFocus();
			return true;
		}

		public MonitorInstance[] getMonitorInstances() {
			List<OracleTableState> selectedTStates = model.getSelectedContent();
			MonitorInstance[] instances = new MonitorInstance[selectedTStates.size()];// instances——一个数组
			for (int i = 0; i < selectedTStates.size(); i++) {
				OracleTableState tableState = (OracleTableState) selectedTStates.get(i);
				instances[i] = new MonitorInstance();
				instances[i].setInstanceKey(tableState.getTablename());// 实例的标识关键字
				instances[i].setInstanceName(tableState.getTablename());// 实例的名称
			}
			return instances;
		}

		/**
		 * 
		 * TableStateParamPanel的内部类OracleTableSpaceModel
		 * 
		 * @author Administrator
		 * 
		 */
		private class OracleTableStateModel extends AbstractTableModel {
			private static final long serialVersionUID = 1L;
			private List<OracleTableState> oneRow = new ArrayList<OracleTableState>();			
			private String[] cloumns = { "监控", "表名", "数据大小(MB)","索引大小(MB)"};
			private RefreshThread thread;

			public OracleTableStateModel() {

			}
			
			public void refresh() {				
				if (thread != null) 
					thread.interrupt();									
				
				thread = new RefreshThread();
				thread.start();
			}
			
			private class RefreshThread extends Thread {
				public RefreshThread() {
					super("OracleTableStateRefreshThread");
				}
				
				@Override
				public void run() {
					logger.debug("刷新线程已启动");
					try {
						OracleTableState[] rows = oneRow.toArray(new OracleTableState[0]);
						for (OracleTableState row : rows) {
							if (isInterrupted())
								break;
							
							if (row.getTablesize() >= 0) 
								continue;
							
							refresh(row);
						}
					} catch (Throwable e) {
						ErrorUtil.warn(logger, "表大小刷新制作", e);;
					} finally {
						logger.debug("刷新线程已关闭");						
					}
				}
			}
			
			
			private void refresh(OracleTableState row) throws InterruptedException {
			  try {
			  	CollectRequest request = CollectRequest.createTableDetailRequest(row.getUsername() + "." + row.getTablename());			  	
			  	CollectParams params = new CollectParams(data.getTask().getTypeId(), data.getNode(), data.getResource(), method, null);
			  	params.setParameterObject(request);
			  	OracleTableState rt = (OracleTableState) data.getServerFactory().getTaskService().collectTask(params);
	        row.setTablesize(rt.getTablesize());
	        row.setIndexsize(rt.getIndexsize());
	      } catch (Throwable e) {
	        ErrorUtil.warn(logger, "刷新数据失败", e);
	      }			
				this.fireTableDataChanged();
			}					

			public int getColumnCount() {
				return cloumns.length;
			}

			public int getRowCount() {
				return oneRow.size();
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				OracleTableState tableState = (OracleTableState) oneRow.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return tableState.getIsWacthed();
				case 1:
					return tableState.getUsername()+"."+tableState.getTablename();
				case 2:
					return tableState.getTablesize() < 0 ? "" : String.format("%.1f", tableState.getTablesize());
				case 3:
					return tableState.getIndexsize() < 0 ? "" : String.format("%.1f", tableState.getIndexsize());
				default:
					return "";
				}
			}

			public void fillCells(List<OracleTableState> fillList) {
				// 把所有合法的都给oneRow
				if (fillList == null) {
					return;
				}
				if (oneRow != null) {
					oneRow.clear();
				}
				for (Iterator<OracleTableState> iter = fillList.iterator(); iter.hasNext();) {
					OracleTableState ots = (OracleTableState) iter.next();
					oneRow.add(ots);
				}
				fireTableDataChanged();
				refresh();
			}

			public String getColumnName(int column) {
				return cloumns[column];
			}

			public Class<?> getColumnClass(int columnIndex) {
				return getValueAt(0, columnIndex).getClass();
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				if (columnIndex == 0) {
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				OracleTableState ots = (OracleTableState) oneRow.get(rowIndex);
				switch (columnIndex) {
				case 0:
					ots.setIsWacthed((Boolean) value);
				}
			}

			/**
			 * 获取选择了的表格，然后进入下一步。
			 * 
			 * @return
			 */
			public List<OracleTableState> getSelectedContent() {
				List<OracleTableState> SelectedTableState = new ArrayList<OracleTableState>();
				for (Iterator<OracleTableState> iter = oneRow.iterator(); iter.hasNext();) {
					OracleTableState ots = iter.next();
					if (ots.getIsWacthed() == true) {
						SelectedTableState.add(ots);
					}
				}
				return SelectedTableState;
			}
		}

		public class TableStateParamPanel_jBtnGetData_actionAdapter implements java.awt.event.ActionListener {
			TableStateParamPanel tspp;

			TableStateParamPanel_jBtnGetData_actionAdapter(TableStateParamPanel tspp) {
				this.tspp = tspp;
			}

			public void actionPerformed(ActionEvent e) {
				tspp.jBtnGetData_actionPerformed(e, filterTextField.getText());
			}
		}
		
		public class TableStateParamPanel_jBtnGetMore_actionAdapter implements java.awt.event.ActionListener {
			TableStateParamPanel tspp;

			TableStateParamPanel_jBtnGetMore_actionAdapter(TableStateParamPanel tspp) {
				this.tspp = tspp;
			}

			public void actionPerformed(ActionEvent e) {
				tspp.jBtnGetMore_actionPerformed(e, filterTextField.getText());
			}
		}
	}
	
	private TableStateParamPanel panel = new TableStateParamPanel();
	private MonitorConfigContext data;
	private MonitorMethod method;

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public boolean getData() {
		if (!panel.verify())
			return false;
			
		return true;
	}

	@Override
	public void setData(MonitorConfigContext data) {
		this.data = data;		
	}

	@Override
	public void setMethod(MonitorMethod method) {		
		this.method = method;		
	}
}