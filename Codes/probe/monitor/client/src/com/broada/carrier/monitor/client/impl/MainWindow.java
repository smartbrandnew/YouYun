package com.broada.carrier.monitor.client.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.client.impl.impexp.GuiAction;
import com.broada.carrier.monitor.client.impl.probe.ProbeManageWindow;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.ThreadUtil;

public class MainWindow extends JFrame {
	private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
	private static final long serialVersionUID = 1L;
	private static MainWindow instance;
	private JLabel lblSystemInfos;
	private JLabel lblMessage;
	private MainPanel panelTaskNav;

	/**
	 * 获取默认实例
	 * 
	 * @return
	 */
	public static MainWindow getDefault() {
		return instance;
	}

	public MainWindow() throws Exception {
		instance = this;
		WinUtil.setMainWindow(this);
		setTitle("Broadview COSS 监测客户端");
		setPreferredSize(new Dimension(1100, 700));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(IconLibrary.getDefault().getImage("resources/images/app.png"));

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(getContentPane(), popupMenu);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		lblSystemInfos = new JLabel("系统信息");
		lblSystemInfos.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.white, new Color(
				103, 101, 98), new Color(148, 145, 140)));
		lblSystemInfos.setPreferredSize(new Dimension(1100, 20));
		panel.add(lblSystemInfos, BorderLayout.WEST);

		lblMessage = new JLabel();
		lblMessage.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.white, new Color(103,
				101, 98), new Color(148, 145, 140)));
		panel.add(lblMessage);

		panelTaskNav = new MainPanel();
		getContentPane().add(panelTaskNav, BorderLayout.CENTER);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menu = new JMenu("系统");
		menuBar.add(menu);

		JMenuItem miManageProbe = new JMenuItem("探针管理");
		miManageProbe.addActionListener(new MiManageProbeActionListener());
		menu.add(miManageProbe);

		JMenuItem exportTask = new JMenuItem("导出任务");
		exportTask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_exportTask_actionPerformed(e);
			}
		});
		menu.add(exportTask);

		JMenuItem importTask = new JMenuItem("导入任务");
		importTask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_importTask_actionPerformed(e);
			}
		});
		menu.add(importTask);

		JMenuItem miExit = new JMenuItem("退出");
		miExit.addActionListener(new MiExitActionListener());
		menu.add(miExit);

		pack();

		ThreadUtil.createThread(new StatusBarRefresher()).start();
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	@Override
	public void dispose() {
		try {
			ServerContext.logout();
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "注销失败", e);
		}
		System.exit(0);
	}

	public static void display() throws Throwable {
		MainWindow window = new MainWindow();
		WinUtil.toCenter(window);
		window.setVisible(true);
	}

	private class StatusBarRefresher implements Runnable {

		@Override
		public void run() {
			while (true) {
				SystemInfo[] infos = ServerContext.getSystemService().getInfos();

				StringBuilder sb = new StringBuilder();

				SystemInfo stopedTasksCount = getInfo(infos, "stopedTasksCount");
				SystemInfo successedTasksCount = getInfo(infos, "successedTasksCount");
				SystemInfo failedTasksCount = getInfo(infos, "failedTasksCount");
				int stoped = null == stopedTasksCount ? 0 : Integer.valueOf(stopedTasksCount.getValue().toString());
				int success = null == successedTasksCount ? 0 : Integer.valueOf(successedTasksCount.getValue().toString());
				int failed = null == failedTasksCount ? 0 : Integer.valueOf(failedTasksCount.getValue().toString());
				sb.append("任务[");
				sb.append("总数：").append(stoped + success + failed);
				sb.append(" 未监测：").append(stoped);
				sb.append(" 正常：").append(success);
				sb.append(" 异常：").append(failed);
				sb.append("]  ");

				SystemInfo processedTasksCount = getInfo(infos, "processedTasksCount");
				SystemInfo processedTasksSpeed = getInfo(infos, "processedTasksSpeed30m");
				if (processedTasksCount != null || processedTasksSpeed != null) {
					sb.append("结果处理[");
					if (processedTasksCount != null)
						sb.append("数量：").append(processedTasksCount.getValue());
					if (processedTasksSpeed != null)
						sb.append(" 速率：").append(processedTasksSpeed.getValue()).append("tps");
					sb.append("]  ");
				}

				lblSystemInfos.setText(sb.toString());

				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private SystemInfo getInfo(SystemInfo[] infos, String code) {
			for (SystemInfo info : infos) {
				if (info.getCode().equalsIgnoreCase(code))
					return info;
			}
			return null;
		}

	}

	private class MiManageProbeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ProbeManageWindow.show(MainWindow.this);
		}
	}

	private class MiExitActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MainWindow.this.dispose();
		}
	}

	protected void do_exportTask_actionPerformed(ActionEvent e) {
		GuiAction.exp();
	}

	protected void do_importTask_actionPerformed(ActionEvent e) {
		GuiAction.imp();
	}
}
