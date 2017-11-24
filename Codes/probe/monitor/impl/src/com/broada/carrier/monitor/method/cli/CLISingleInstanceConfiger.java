package com.broada.carrier.monitor.method.cli;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.swing.util.ErrorDlg;

public abstract class CLISingleInstanceConfiger extends SingleInstanceConfiger {
	private static final long serialVersionUID = 1L;

	public CLISingleInstanceConfiger() {
	}

	@Override
	protected JButton[] getButtons() {
		JButton btnDebug = new JButton("调试");
		btnDebug.addActionListener(new BtnDebugActionListener());
		return new JButton[] { btnDebug };
	}

	protected abstract void doTest();

	protected void doTest(String title, String command) {
		doTest(title, new String[] { command });
	}

	protected void doTest(String title, String[] commands) {
		CLIDebugDlg dlg = CLIDebugDlg.createDialog(this, title);
		if (getMethod() == null) {
			JOptionPane.showMessageDialog(this, "请先配置监测方式", "错误", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		new ExecuteThread(dlg, commands).start();
		dlg.setVisible(true);
	}

	class ExecuteThread extends Thread {
		private CLIDebugDlg dlg = null;

		private String[] commands;

		ExecuteThread(CLIDebugDlg dlg, String[] commands) {
			this.dlg = dlg;
			this.commands = commands;
		}

		public void run() {
			doProbeTest(commands, dlg);
		}

	}

	public void doProbeTest(String[] commands, CLIDebugDlg dlg) {
		String result = "";

		try {
			result = (String) getContext()
					.getServerFactory()
					.getProbeService()
					.executeMethod(getContext().getNode().getProbeId(),
							CLITester.class.getName(), "debug", getContext().getNode(), new CLIMonitorMethodOption(getMethod()),
							commands);
		} catch (Exception e2) {
			ErrorDlg.createErrorDlg(dlg, "远程探针执行方法失败,ProbeCode:" + getContext().getNode(), e2).setVisible(true);
			return;
		}
		dlg.addContent(result);
		dlg.addEnd();

	}

	private class BtnDebugActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			doTest();
		}

	}
}
