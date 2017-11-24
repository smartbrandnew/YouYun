package com.broada.carrier.monitor.common.remoteio.api;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.component.utils.text.DateUtil;
import com.broada.component.utils.text.Unit;

/**
 * 远程文件管理用户界面
 * @author Jiangjw
 */
public class RemoteFileMgrPanel extends JPanel {
	private static final String CHARSET = "GBK";
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JButton btnSelect;
	private RemoteIOClient client;
	private RemoteFileTableModel model;
	private String title;
	private JDialog dialog;
	private String dir;
	private RemoteFile selected;		
	private EditFilePanel editFilePanel;
	private Window owner;
	private JTextField seachTextField;

	/**
	 * 构建一个管理界面
	 * @param owner 父窗口
	 * @param client 远程管理客户端
	 * @param title 窗口标题
	 * @param dir 服务端工作目录（使用相对路径）
	 */
	public RemoteFileMgrPanel(Window owner, RemoteIOClient client, String title, String dir) {
		this.owner = owner;
		this.client = client;
		this.title = title;
		if (!dir.endsWith("/"))
			dir += "/";
		this.dir = dir;

		setPreferredSize(new Dimension(620, 370));
		setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		add(toolBar, BorderLayout.NORTH);

		JButton btnAppend = new JButton("添加");
		btnAppend.addActionListener(new BtnAppendActionListener());
		btnAppend.setIcon(new ImageIcon(RemoteFileMgrPanel.class.getResource("append.png")));
		toolBar.add(btnAppend);

		JButton btnCreate = new JButton("新建");
		btnCreate.addActionListener(new BtnCreateActionListener());
		btnCreate.setIcon(new ImageIcon(RemoteFileMgrPanel.class.getResource("create.png")));
		toolBar.add(btnCreate);

		JButton btnEdit = new JButton("修改");
		btnEdit.addActionListener(new BtnEditActionListener());
		btnEdit.setIcon(new ImageIcon(RemoteFileMgrPanel.class.getResource("edit.png")));
		toolBar.add(btnEdit);

		JButton btnDelete = new JButton("删除");
		btnDelete.addActionListener(new BtnDeleteActionListener());
		btnDelete.setIcon(new ImageIcon(RemoteFileMgrPanel.class.getResource("delete.png")));
		toolBar.add(btnDelete);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator);
		
		//查询过滤文件名
		seachTextField = new JTextField(8);
		toolBar.add(seachTextField);

		JButton subSearch = new JButton("查询");
		subSearch.addActionListener(new BtnSearchActionListener());
		subSearch.setIcon(new ImageIcon(RemoteFileMgrPanel.class.getResource("search.png")));
		toolBar.add(subSearch);
		
		btnSelect = new JButton("选择");
		btnSelect.addActionListener(new BtnSelectActionListener());
		btnSelect.setIcon(new ImageIcon(RemoteFileMgrPanel.class.getResource("select.png")));
		toolBar.add(btnSelect);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		model = new RemoteFileTableModel();
		table = new JTable(model, new RemoteFileColumnModel());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSorter(new TableRowSorter<TableModel>(model));
		table.addKeyListener(new TableKeyListener());
		table.addMouseListener(new TableMouseListener());
		table.getRowSorter().toggleSortOrder(RemoteFileColumnModel.COLUMN_FILE);
		scrollPane.setViewportView(table);
	}

	/**
	 * 显示选择界面
	 * @return 如果成功选择了一个文件，则返回其远程文件对象，否则返回null
	 */
	public RemoteFile showSelect() {
		selected = null;
		refresh();		
		getDialog().setVisible(true);
		return selected;
	}
	
	/**
	 * 显示建立界面
	 * @return 如果成功创建了一个文件，则返回其远程文件对象，否则返回null
	 */
	public RemoteFile showCreate() {
		if (!getEditFilePanel().show("", "", true))
			return null;
		
		try {				
			String file = dir + getEditFilePanel().getFile();
			client.save(file, getEditFilePanel().getContent(), CHARSET);
			return processFilepath(client.get(file));
		} catch (IOException e1) {
			ErrorDlg.show(dialog, "保存文件失败", e1);
			return null;
		}
	}
	
	/**
	 * 显示编辑界面
	 * @param file
	 * @return 如果成功编辑了此文件，则返回其远程文件对象，否则返回null
	 */
	public boolean showEdit(String file) {
		try {								
			String content = client.read(dir + file, CHARSET);
			if (!getEditFilePanel().show(file, content, false))
				return false;
			client.save(dir + file, getEditFilePanel().getContent(), CHARSET);
			return true;
		} catch (IOException e1) {
			ErrorDlg.show(dialog, "保存文件失败", e1);
			return false;
		}
	}

	private JDialog getDialog() {
		if (dialog == null) {
			if (owner != null && owner instanceof JDialog)
				dialog = new JDialog((JDialog)owner);
			else if (owner != null && owner instanceof JFrame)
				dialog = new JDialog((JFrame)owner);
			else
				dialog = new JDialog();
			dialog.setModal(true);
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			dialog.setTitle(title);
			dialog.getContentPane().add(this);
			dialog.pack();
			WinUtil.toCenter(dialog);
		}
		return dialog;
	}

	private class RemoteFileTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private RemoteFile[] rows;

		public void setRows(RemoteFile[] rows) {
			this.rows = rows;
		}

		public int getRowCount() {
			return rows == null ? 0 : rows.length;
		}

		public int getColumnCount() {
			return RemoteFileColumnModel.COLUMNS;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rows == null || rowIndex >= rows.length)
				return null;

			RemoteFile file = rows[rowIndex];
			switch (columnIndex) {
			case RemoteFileColumnModel.COLUMN_FILE:
				return file.getFile();
			case RemoteFileColumnModel.COLUMN_SIZE:
				return file.getSize();
			case RemoteFileColumnModel.COLUMN_LAST_MODIFIED:
				return file.getLastModified();
			default:
				return null;
			}
		}

		public RemoteFile getRow(int row) {
			if (rows == null || row >= rows.length)
				return null;

			return rows[row];
		}
	}

	private static abstract class LabelTableCellRender implements TableCellRenderer {
		protected JLabel label = new JLabel();

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			label.setText(getText(value));
			label.setFont(table.getFont());
			label.setOpaque(true);
			if (isSelected)
				label.setBackground(table.getSelectionBackground());
			else
				label.setBackground(table.getBackground());
			return label;
		}

		protected abstract String getText(Object value);
	}

	private static class SizeTableCellRender extends LabelTableCellRender {
		public SizeTableCellRender() {
			label.setHorizontalAlignment(JLabel.RIGHT);
		}

		@Override
		protected String getText(Object value) {
			return Unit.B.formatPrefer((Long) value);
		}
	}

	private static class DateTableCellRender extends LabelTableCellRender {
		public DateTableCellRender() {
			label.setHorizontalAlignment(JLabel.CENTER);
		}

		@Override
		protected String getText(Object value) {
			return DateUtil.format(new Date((Long) value));
		}
	}

	private static class RemoteFileColumnModel extends DefaultTableColumnModel {
		private static final long serialVersionUID = 1L;
		public static final int COLUMNS = 3;
		public static final int COLUMN_FILE = 0;
		public static final int COLUMN_SIZE = 1;
		public static final int COLUMN_LAST_MODIFIED = 2;

		public RemoteFileColumnModel() {
			addColumn("文件", COLUMN_FILE, 250, null);
			addColumn("大小", COLUMN_SIZE, 15, new SizeTableCellRender());
			addColumn("修改时间", COLUMN_LAST_MODIFIED, 35, new DateTableCellRender());
		}

		private void addColumn(String name, int index, int width, TableCellRenderer render) {
			TableColumn tableColumn = new TableColumn(index);
			tableColumn.setHeaderValue(name);
			tableColumn.setPreferredWidth(width);
			if (render != null)
				tableColumn.setCellRenderer(render);
			addColumn(tableColumn);
		}
	}
	
	private RemoteFile processFilepath(RemoteFile file) {
		if (file.getFile().startsWith(dir))
			file.setFile(file.getFile().substring(dir.length()));
		return file;
	}
	
	private void refresh() {
		int row = table.getSelectedRow();
		RemoteFile[] files = client.list(dir);
		if (files != null) {
			for (RemoteFile file : files)
				processFilepath(file);				
		}
		model.setRows(files);
		model.fireTableDataChanged();
		if (row >= 0 && row < table.getRowCount())
			table.getSelectionModel().setSelectionInterval(row, row);
	}	

	private class BtnAppendActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			File[] files = WinUtil.showOpenFiles(dialog);
			if (files == null || files.length == 0)
				return;
			
			try {
				for (File file : files)
					client.save(dir + file.getName(), file);
				refresh();
			} catch (IOException e1) {
				ErrorDlg.show(dialog, "保存文件失败", e1);
			}			
		}		
	}

	private class BtnCreateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (showCreate() != null) 
				refresh();
		}
	}

	private class BtnEditActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			editRow();
		}
	}

	private class BtnDeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			RemoteFile file = getCurrentRow();
			if (file == null)
				return;
			
			try {
				client.delete(dir + file.getFile());
				refresh();
			} catch (Throwable e1) {
				ErrorDlg.show(dialog, "删除文件失败", e1);
			}						
		}
	}
	
	private class BtnSearchActionListener implements ActionListener{
	 
		@Override
		public void actionPerformed(ActionEvent e) {
			RemoteFile[] files = client.list(dir);
			if(files == null)
				return;
			
			for (RemoteFile file : files)
				processFilepath(file);
			
			if(seachTextField.getText().equals("")){
				model.setRows(files);
				model.fireTableDataChanged();
				return;
			}
			
			List<RemoteFile> ls = new ArrayList<RemoteFile>();
			for (RemoteFile file : files) {
				if (file.getFile().indexOf(seachTextField.getText()) > -1) 
					ls.add(file);
			}
			model.setRows(ls.toArray(new RemoteFile[ls.size()]));
			model.fireTableDataChanged();
			
			int row = table.getSelectedRow();
			if (row >= 0)
				table.getSelectionModel().setSelectionInterval(row, row);
		}
	}
	

	private class BtnSelectActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			selectCurrentRow();
		}
	}
	
	private RemoteFile getCurrentRow() {
		int row = table.getSelectedRow();
		if (row < 0)
			return null;
		row = table.convertRowIndexToModel(row);
		return model.getRow(row);
	}

	private void selectCurrentRow() {
		selected = getCurrentRow();
		if (selected == null)
			return;
		dialog.setVisible(false);
	}
	
	private void editRow(){
		RemoteFile file = getCurrentRow();
		if (file == null)
			return;
		
		if (showEdit(file.getFile()))
			refresh();			
	}

	private class TableMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2)
				editRow();
		}
	}

	private class TableKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == '\n')
				selectCurrentRow();
		}
	}
	
	/**
	 * 关闭窗口
	 */
	public void dispose() {
		if (dialog != null) {
			dialog.dispose();
			dialog = null;
		}
	}
	
	private EditFilePanel getEditFilePanel() {
		if (editFilePanel == null)
			editFilePanel = new EditFilePanel(dialog == null ? owner : dialog);
		return editFilePanel;
	}	
}
