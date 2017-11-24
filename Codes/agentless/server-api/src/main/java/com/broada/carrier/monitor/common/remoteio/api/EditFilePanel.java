package com.broada.carrier.monitor.common.remoteio.api;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.broada.carrier.monitor.common.swing.WinUtil;

/**
 * 文件编辑面板
 * @author Jiangjw
 */
public class EditFilePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField txtFile;
	private JTextArea txtContent;
	private JDialog dialog;
	private boolean save;
	private Window owner;

	public EditFilePanel(Window owner) {
		this.owner = owner;
		
		setLayout(new BorderLayout(0, 0));
		setPreferredSize(new Dimension(670, 440));

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(0, 33));
		add(panel, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("文件名：");

		txtFile = new JTextField();
		txtFile.setColumns(80);
		txtFile.setToolTipText("当脚本文件较多时，使用/可以为脚本文件划分文件夹");

		JButton btnSave = new JButton("保存");
		btnSave.addActionListener(new BtnSaveActionListener());

		JButton btnCancel = new JButton("取消");
		btnCancel.addActionListener(new BtnCancelActionListener());
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
				gl_panel.createSequentialGroup().addGap(21).addComponent(lblNewLabel)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(txtFile)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnSave)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnCancel).addGap(12)));
		gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
				gl_panel.createSequentialGroup()
						.addGap(5)
						.addGroup(
								gl_panel.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblNewLabel)
										.addComponent(txtFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE).addComponent(btnSave)
										.addComponent(btnCancel))));
		panel.setLayout(gl_panel);

		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		txtContent = new JTextArea();
		scrollPane.setViewportView(txtContent);		
	}

	private class BtnSaveActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (getFile().trim().length() == 0) {
				JOptionPane.showMessageDialog(dialog, "请输入文件名。", "提示", JOptionPane.INFORMATION_MESSAGE);
				txtFile.grabFocus();
				return;
			}
			if (getContent().trim().length() == 0) {
				JOptionPane.showMessageDialog(dialog, "请输入脚本内容。", "提示", JOptionPane.INFORMATION_MESSAGE);
				txtContent.grabFocus();
				return;
			}
			
			save = true;
			dialog.setVisible(false);
		}
	}

	private class BtnCancelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dialog.setVisible(false);
		}
	}
	
	/**
	 * 显示一个对象框，用于编辑指定的文件
	 * @param file
	 * @param content
	 * @param fileEditable
	 * @return 编辑成功返回true，否则返回false
	 */
	public boolean show(String file, String content, boolean fileEditable) {
		save = false;
		txtFile.setText(file);
		txtFile.setEditable(fileEditable);
		txtContent.setText(content);			
		getDialog().setVisible(true);
		return save;
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
			dialog.setTitle("文件编辑");			
			dialog.getContentPane().add(this);
			dialog.pack();
			WinUtil.toCenter(dialog);
		}
		return dialog;
	}
	
	/**
	 * 返回用户编辑的文件名
	 * @return
	 */
	public String getFile() {
		return txtFile.getText();
	}
	
	/**
	 * 返回用户编辑的文件内容
	 * @return
	 */
	public String getContent() {
		return txtContent.getText();
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
}
