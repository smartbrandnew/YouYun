package com.broada.carrier.monitor.client.impl.probe;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.carrier.monitor.server.api.entity.SyncStatus;
import com.broada.component.utils.lang.ThreadUtil;

public class ProbeSyncWindow extends JDialog {
	private static final long serialVersionUID = 1L;
	private JLabel lblMessage = new JLabel("");
	private JProgressBar pbrProgress = new JProgressBar();
	private int probeId;
	private Thread thread;

	public ProbeSyncWindow(Window owner) {
		super(owner);
		setTitle("探针同步进度");
		setModal(true);
		getContentPane().setLayout(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);		
		setPreferredSize(new Dimension(595, 228));

		JButton btnClose = new JButton("关闭");
		btnClose.addActionListener(new BtnCloseActionListener());
		btnClose.setBounds(468, 154, 93, 23);
		getContentPane().add(btnClose);

		JLabel lblNewLabel = new JLabel("状态：");
		lblNewLabel.setBounds(10, 25, 54, 15);
		getContentPane().add(lblNewLabel);
		
		pbrProgress.setBounds(10, 66, 551, 23);
		pbrProgress.setMinimum(0);
		pbrProgress.setMaximum(100);
		getContentPane().add(pbrProgress);
		
		lblMessage.setBounds(52, 25, 509, 15);
		getContentPane().add(lblMessage);
		pack();
		thread = ThreadUtil.createThread(new RefreshThread());
		thread.start();
	}
	
	public class RefreshThread implements Runnable {

		@Override
		public void run() {
			while (true) {				
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					break;
				}
				SyncStatus status = ServerContext.getProbeService().getProbeSyncStatus(probeId);
				lblMessage.setText(status.getMessage());
				pbrProgress.setValue(status.getProgress());
				ProbeSyncWindow.this.repaint();
				if (status.getProgress() >= 100)
					break;
			}
		}
		
	}

	public static void show(Window owner, int probeId) {
		ProbeSyncWindow window = new ProbeSyncWindow(owner);
		window.probeId = probeId;
		WinUtil.toCenter(window);
		window.setVisible(true);		
	}
	
	@Override
	public void dispose() {
		if (thread != null)
			thread.interrupt();
		super.dispose();
	}

	private class BtnCloseActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
}
