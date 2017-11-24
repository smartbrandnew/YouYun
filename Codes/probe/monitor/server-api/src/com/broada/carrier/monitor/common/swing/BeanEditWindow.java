package com.broada.carrier.monitor.common.swing;

import com.broada.carrier.monitor.common.error.ServiceException;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 实体对象编辑窗口
 * @author Jiangjw
 */
public class BeanEditWindow<T> extends JDialog {
	private static final long serialVersionUID = 1L;
	private BeanEditPanel<T> panel;
	private T bean;

	public BeanEditWindow(Window owner) {
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

		JButton btnCacnel = new JButton("取消");
		btnCacnel.addActionListener(new BtnCacnelActionListener());
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING,
								gl_panel.createSequentialGroup().addContainerGap(381, Short.MAX_VALUE).addComponent(btnOk)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnCacnel).addGap(18))
				);
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup().addContainerGap()
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(btnOk).addComponent(btnCacnel))
								.addContainerGap(12, Short.MAX_VALUE))
				);
		panel.setLayout(gl_panel);
	}

	/**
	 * 显示编辑窗口
	 * @param owner
	 * @param panel 实体对象编辑面板
	 * @param bean 要编辑的实体对象，如果为null表示创建，并需要panel支持setData为null
	 * @return 如果用户编辑成功返回实体对象，否则返回null
	 */
	public static <T> T show(Window owner, BeanEditPanel<T> panel, T bean) {
		try {
			BeanEditWindow<T> window = new BeanEditWindow<T>(owner);
			window.panel = panel;
			window.setTitle(panel.getTitle());
			window.getContentPane().add(panel, BorderLayout.CENTER);
			window.pack();
			WinUtil.toCenter(window);
			panel.setData(bean);
			window.setVisible(true);
			return window.bean;
		} catch (Throwable e) {
			ErrorDlg.show(e);
			return null;
		}
	}

	private class BtnOkActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				T bean = panel.getData();
				if (bean == null)
					return;
				BeanEditWindow.this.bean = bean;
			} catch (Throwable error) {
				boolean process = false;
				if (error instanceof NullPointerException) {
					process = true;
					JOptionPane.showMessageDialog(BeanEditWindow.this, error.getMessage(), "提示", JOptionPane.INFORMATION_MESSAGE);
				} else if (error instanceof ServiceException && error.getMessage() != null) {
					ServiceException ex = (ServiceException) error;
					if (ex.getCode().equals("IllegalArgumentException")) {
						process = true;
						JOptionPane.showMessageDialog(BeanEditWindow.this, error.getMessage(), "提示",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
				if (!process)
					ErrorDlg.show("数据保存失败", error);
				return;
			}
			dispose();
		}
	}

	private class BtnCacnelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			bean = null;
			dispose();
		}
	}
}
