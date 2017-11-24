package com.broada.carrier.monitor.common.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * 通用确认窗口，用于任意panel的展现，同时总会提供一个确认按钮
 * @author Jiangjw
 */
public class ShowWindow extends JDialog {
	private static final long serialVersionUID = 1L;
	
	public ShowWindow(Window owner) {
		super(owner);
		setModal(true);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 42));
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnOk = new JButton("确定");
		btnOk.addActionListener(new BtnOkActionListener());
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap(377, Short.MAX_VALUE)
					.addComponent(btnOk)
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnOk)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
	}

	/**
	 * 显示确认窗口
	 * @param owner 父窗口
	 * @param title 标题
	 * @param component 要展现的组件，一般是某个panel
	 */
	public static void show(Window owner, String title, Component component) {
		ShowWindow window = new ShowWindow(owner);
		window.getContentPane().add(component, BorderLayout.CENTER);
		window.setTitle(title);
		window.pack();
		WinUtil.toCenter(window);				
		window.setVisible(true);
	}

	private class BtnOkActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {			
			dispose();
		}
	}
}
