package com.broada.carrier.monitor.client.impl.impexp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpFile;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.component.utils.lang.ThreadUtil;

public class ExpProcessWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextArea area = new JTextArea();
	private JProgressBar expProgress = new JProgressBar();
	private JScrollPane pane = new JScrollPane();
	private Thread taskThread;
	private File file;
	private ProcessListener exp;

	public ExpProcessWindow(File file) {
		this.file = file;
		setTitle("导出监测任务");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(400, 300));
		setLayout(new BorderLayout());
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pane.getViewport().add(area);
		add(expProgress, BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
		setLocationRelativeTo(null);
		setIconImage(IconLibrary.getDefault().getImage(
				"resources/images/app.png"));
		pack();
		setVisible(true);
		
		exp = new ProcessListener(expProgress) {

			@Override
			public void progress() {
				if (expProgress.getValue() == 100) 
					appendMsg("\n导出完成。");
			}

			@Override
			public void appendMsg(String appendMsg) {
				area.append(appendMsg);
			}

			@Override
			public void setMsg(String msg) {
				area.setText(msg);
			}

		};
		
		taskThread = ThreadUtil.createThread(new TaskThread());
		taskThread.start();
	}
	
	public class TaskThread implements Runnable {
		@Override
		public void run() {
			String path = file.getPath();
			String filename = path.replace("\\", "/");
			Exporter exporter = new ExporterCarrier(exp);
			ImpExpFile impFile = exporter.exp();
			ImpExpFileWriter writer = new ImpExpFileWriter();
			writer.write(impFile, filename);
		}
	}
}
