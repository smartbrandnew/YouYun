package com.broada.carrier.monitor.impl.host.cli.directory.win;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import com.broada.carrier.monitor.impl.common.ui.SpinnerCellEditor;
import com.broada.carrier.monitor.impl.host.cli.directory.CLIDirectory;
import com.broada.carrier.monitor.impl.host.cli.directory.CLIDirectoryParameter;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.DateUtil;
import com.broada.utils.ListUtil;
import com.broada.utils.StringUtil;

public class CLIDirectoryParamPanel extends JPanel {
	private static final long serialVersionUID = -4523620066140347500L;
	private static final DecimalFormat df = new DecimalFormat("########.###");

	/** 文件目录 */
	private List<CLIDirectory> lDirectories = new ArrayList<CLIDirectory>();
	private MonitorMethod method;

	/** 文件 */
	private Vector<CLIFile> vFiles = new Vector<CLIFile>();

	private CLIDirectory currDirectory = null;

	private int currRowIdx = -1;

	private SpinnerNumberModel snmSubdirLayerCount = new SpinnerNumberModel(1, 1, 3, 1);

	private final JSpinner spinnerSubdirLayerCount = new JSpinner(snmSubdirLayerCount);

	private DirectoryTableModel directoryModel = new DirectoryTableModel();
	private JTable jtDirectory = new JTable(directoryModel);

	private FilesTableModel fileModel = new FilesTableModel();
	private JTable jtFiles = new JTable(fileModel);

	private JPanel jPanel1 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JPanel jPanel2 = new JPanel();
	private JTextField jtxtDirectory = new JTextField();
	private JLabel jLabel1 = new JLabel();
	private JButton jbAdd = new JButton();
	private JButton jbDel = new JButton();
	private JPanel jPanel3 = new JPanel();
	private BorderLayout borderLayout4 = new BorderLayout();
	private FlowLayout flowLayout1 = new FlowLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JPanel jPanel4 = new JPanel();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JLabel jLabel2 = new JLabel();
	private JScrollPane jScrollPane2 = new JScrollPane();
	private MonitorConfigContext context;

	public CLIDirectoryParamPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setMethod(MonitorMethod method) {
		this.method = method;
	}

	public void setData(MonitorConfigContext context) {
		this.context = context;
		CLIDirectoryParameter param = new CLIDirectoryParameter(context.getTask().getParameter());

		CLIDirectory[] directories = param.getDirectories();		
		for (int i = 0; i < directories.length; i++) {			
			lDirectories.add(directories[i]);
		}
		directoryModel.fireTableDataChanged();
	}

	public boolean getData() {
		if (!verify())
			return false;
		
		CLIDirectoryParameter param = new CLIDirectoryParameter();
		param.setDirectories(lDirectories.toArray(new CLIDirectory[0]));
		context.getTask().setParameter(param.encode());
		return true;
	}

	public boolean verify() {
		if (this.jtDirectory.getCellEditor() != null && jtDirectory.isEditing()) {
			jtDirectory.getCellEditor().stopCellEditing();
		}

		if (method == null) {
			JOptionPane.showMessageDialog(this, "获取监测参数信息失败", "错误", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (ListUtil.isNullOrEmpty(lDirectories)) {
			JOptionPane.showMessageDialog(this, "至少选择一个监测目录", "错误", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		boolean wacthed = false;
		for (int i = 0; i < lDirectories.size(); i++) {
			CLIDirectory directory = (CLIDirectory) lDirectories.get(i);
			if (directory.getIsWacthed().booleanValue()) {
				wacthed = true;
				break;
			}
		}
		if (!wacthed) {
			JOptionPane.showMessageDialog(this, "至少选择一个监测目录", "错误", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			vFiles.clear();
			vFiles.addAll(getCLIFiles().values());
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(this, "请检查文件目录是否正确", "错误", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	private Map getCLIFiles() throws Exception {
		CLIDirectory[] directories = new CLIDirectory[lDirectories.size()];
		for (int i = 0; i < lDirectories.size(); i++) {
			CLIDirectory directory = (CLIDirectory) lDirectories.get(i);
			if (directory.getIsWacthed().booleanValue()) {
				directories[i] = directory;
			} else {
				directories[i] = null;
			}
		}
		StringBuffer buff = new StringBuffer();
		Map map = getProbeFilesMap(buff, directories, new HashMap());
		if (buff.toString().length() > 0) {
			throw new Exception(buff.toString());
		}
		return map;
	}

	private Map getProbeFilesMap(StringBuffer buff, CLIDirectory[] directories, Map fileCntMap) {
		HashMap map = new HashMap();

		try {
			CollectParams params = new CollectParams(context.getTask().getTypeId(),
					context.getNode(), context.getResource(), method, directories);
			return (Map) context.getServerFactory().getTaskService().collectTask(params);
		} catch (Exception e) {
			ErrorDlg.createErrorDlg(this, "错误", e).setVisible(true);
			return map;
		}
	}

	private void jbInit() {
		this.setLayout(borderLayout1);
		jPanel1.setLayout(borderLayout2);
		jPanel1.setBackground(new Color(236, 233, 216));
		jPanel2.setBackground(new Color(236, 233, 216));
		jPanel2.setMinimumSize(new Dimension(10, 10));
		jPanel2.setPreferredSize(new Dimension(20, 130));
		jPanel2.setLayout(borderLayout4);
		jLabel1.setBackground(Color.lightGray);
		jLabel1.setPreferredSize(new Dimension(60, 15));
		jLabel1.setText("文件目录：");
		jbAdd.setText("添加");
		jbAdd.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				jbAdd_mouseClicked(e);
			}
		});
		jbDel.setText("删除");
		jbDel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				jbDel_mouseClicked(e);
			}
		});
		jPanel3.setLayout(flowLayout1);
		jPanel3.setBackground(new Color(236, 233, 216));
		jtxtDirectory.setMinimumSize(new Dimension(40, 24));
		jtxtDirectory.setPreferredSize(new Dimension(220, 24));
		jPanel4.setLayout(borderLayout3);
		jLabel2.setText("文件列表：(请双击上面列出的目录项显示)");
		this.add(jPanel1, java.awt.BorderLayout.CENTER);
		jPanel2.add(jPanel3, java.awt.BorderLayout.NORTH);
		jPanel3.add(jLabel1, null);
		jPanel3.add(jtxtDirectory, null);
		jPanel3.add(jbAdd, null);
		jPanel3.add(jbDel, null);
		jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);
		jScrollPane1.getViewport().add(jtDirectory);
		jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);
		jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);
		jPanel4.add(jLabel2, java.awt.BorderLayout.NORTH);
		jPanel4.add(jScrollPane2, java.awt.BorderLayout.CENTER);
		jScrollPane2.getViewport().add(jtFiles);

		jtDirectory.setRowHeight(20);
		jtDirectory.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableColumnModel tc = jtDirectory.getColumnModel();
		tc.getColumn(3).setCellEditor(new SpinnerCellEditor(spinnerSubdirLayerCount));
		tc.getColumn(0).setPreferredWidth(60);
		tc.getColumn(1).setPreferredWidth(200);
		tc.getColumn(1).setMinWidth(200);	
		tc.getColumn(2).setPreferredWidth(70);
		tc.getColumn(2).setMaxWidth(70);
		tc.getColumn(2).setMinWidth(70);
		tc.getColumn(3).setPreferredWidth(60);

		//行选中事件
		jtDirectory.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() == 2) {// 点击几次，这里是双击事件
					if (method == null) {
						JOptionPane.showMessageDialog(CLIDirectoryParamPanel.this, "获取监测参数信息失败", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int rowIdx = jtDirectory.getSelectedRow();
					currRowIdx = rowIdx;
					CLIDirectory directory = (CLIDirectory) lDirectories.get(currRowIdx);
					if (directory == null) {
						return;
					}
					currDirectory = directory;
					freshFileInfo();
				}
			}
		});

		jtFiles.setRowHeight(20);

		TableColumnModel tc2 = jtFiles.getColumnModel();
		tc2.getColumn(0).setPreferredWidth(200);
		tc2.getColumn(1).setPreferredWidth(70);
		tc2.getColumn(2).setPreferredWidth(60);
		tc2.getColumn(3).setPreferredWidth(130);
		tc2.getColumn(4).setPreferredWidth(130);
		tc2.getColumn(5).setPreferredWidth(60);
	}

	private void freshFileInfo() {
		StringBuffer buff = new StringBuffer();
		Map files = new HashMap();
		if (currDirectory != null) {
			files = getProbeFilesMap(buff, new CLIDirectory[] { currDirectory }, new HashMap());
		}
		if (buff.toString().length() > 0) {
			JOptionPane.showMessageDialog(CLIDirectoryParamPanel.this, "获取文件信息失败", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		vFiles.clear();
		vFiles.addAll(files.values());
		fileModel.fireTableDataChanged();
	}

	/**
	 * 文件目录表
	 *
	 * @author lixy Jun 16, 2008 1:33:35 PM
	 */
	private class DirectoryTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 3029327200245186340L;
		String[] columns = { "监控", "目录", "监控子目录", "层数" };

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return lDirectories.size();
		}

		public String getColumnName(int column) {
			if (column < 0 || column > columns.length) {
				return "未知列";
			}
			return columns[column];
		}

		public Object getValueAt(int rowIdx, int colIdx) {
			CLIDirectory dir = (CLIDirectory) lDirectories.get(rowIdx);
			switch (colIdx) {
			case 0:
				return dir.getIsWacthed();
			case 1:
				return dir.getPath();
			case 2:
				return dir.getIsWacthSubdir();
			case 3:
				return dir.getSubdirLayerCount();
			default:
				return "";
			}
		}

		public boolean isCellEditable(int rowIdx, int colIdx) {
			CLIDirectory dir = (CLIDirectory) lDirectories.get(rowIdx);
			return colIdx == 0
					|| dir.getIsWacthed().booleanValue()
					&& (colIdx == 2 || (dir.getIsWacthSubdir().booleanValue() && colIdx == 3));
		}

		public void setValueAt(Object aValue, int rowIdx, int colIdx) {
			CLIDirectory dir = (CLIDirectory) lDirectories.get(rowIdx);
			switch (colIdx) {
			case 0:
				dir.setIsWacthed((Boolean) aValue);
				return;
			case 2:
				dir.setIsWacthSubdir((Boolean) aValue);
				return;
			case 3:
				dir.setSubdirLayerCount((Integer) aValue);
				return;
			}
		}

		public Class getColumnClass(int colIdx) {
			return getValueAt(0, colIdx).getClass();
		}
	}

	/**
	 * 某一目录对应的文件列表
	 *
	 * @author lixy Jun 16, 2008 1:34:51 PM
	 */
	private class FilesTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 6978133515354228467L;
		String[] columns = { "名称", "大小(M)", "类型", "创建时间", "修改时间", "状态" };

		public int getColumnCount() {			
			return columns.length;
		}

		public int getRowCount() {			
			return vFiles.size();
		}

		public String getColumnName(int column) {
			if (column < 0 || column > columns.length) {
				return "未知列";
			}
			return columns[column];
		}

		public Object getValueAt(int rowIdx, int colIdx) {
			CLIFile file = (CLIFile) vFiles.get(rowIdx);
			switch (colIdx) {
			case 0:
				return file.getName();
			case 1:
				return new Double(df.format(file.getSize().doubleValue() / 1024d));
			case 2:
				return file.getFiletype();
			case 3:
				if (file.getCreateTime() == null)
					return "";
				return DateUtil.DATETIME_FORMAT.format(file.getCreateTime());
			case 4:
				if (file.getModifyTime() == null)
					return "";
				return DateUtil.DATETIME_FORMAT.format(file.getModifyTime());
			case 5:
				return file.getStatus();
			default:
				return "";
			}
		}

		public boolean isCellEditable(int rowIdx, int colIdx) {
			return false;
		}

		public Class getColumnClass(int colIdx) {
			return getValueAt(0, colIdx).getClass();
		}
	}

	public void jbAdd_mouseClicked(MouseEvent e) {
		if (StringUtil.isNullOrBlank(jtxtDirectory.getText())) {
			JOptionPane.showMessageDialog(CLIDirectoryParamPanel.this, "请填监测目录的完整路径。", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (isDirExisted(jtxtDirectory.getText().trim())) {
			JOptionPane.showMessageDialog(CLIDirectoryParamPanel.this, "文件目录" + jtxtDirectory.getText().trim() + "已经存在",
					"提示", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		CLIDirectory dirCond = new CLIDirectory();
		dirCond.setPath(jtxtDirectory.getText().trim().toLowerCase());

		lDirectories.add(dirCond);
		directoryModel.fireTableDataChanged();
	}

	private boolean isDirExisted(String dir) {
		dir = dir.toLowerCase();
		if (ListUtil.isNullOrEmpty(lDirectories)) {
			return false;
		}
		for (int i = 0; i < lDirectories.size(); i++) {
			CLIDirectory directory = (CLIDirectory) lDirectories.get(i);
			if (directory.getPath().equals(dir)) {
				return true;
			}
		}
		return false;
	}

	public void jbDel_mouseClicked(MouseEvent e) {
		int rowIdx = jtDirectory.getSelectedRow();
		if (rowIdx == -1) {
			JOptionPane.showMessageDialog(CLIDirectoryParamPanel.this, "请选中监测目录列表中将被删除的目录记录。", "提示",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		CLIDirectory dirCond = (CLIDirectory) lDirectories.get(rowIdx);
		if (currDirectory != null && dirCond.getPath().equals(currDirectory.getPath())) {
			currDirectory = null;
			freshFileInfo();
		}
		lDirectories.remove(rowIdx);
		directoryModel.fireTableDataChanged();
	}
}
