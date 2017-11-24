package com.broada.carrier.monitor.client.impl.impexp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpFile;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpTask;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.component.utils.lang.ThreadUtil;

public class ImpProcessWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextArea area = new JTextArea();
	private JProgressBar impProgress = new JProgressBar();
	private JScrollPane pane = new JScrollPane();
	private Thread taskThread;
	private File file;
	private ProcessListener imp;

	public ImpProcessWindow(File file) {
		this.file = file;
		setTitle("导入监测任务");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(400, 300));
		setLayout(new BorderLayout());
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pane.getViewport().add(area);
		add(impProgress, BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
		setLocationRelativeTo(null);
		setIconImage(IconLibrary.getDefault().getImage(
				"resources/images/app.png"));
		pack();
		setVisible(true);
		
		imp = new ProcessListener(impProgress) {

			@Override
			public void progress() {
				if (impProgress.getValue() == 100) 
					appendMsg("\n导入完成。");
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

	public class ProcessThread implements Runnable {
		@Override
		public void run() {
			String path = file.getPath();
			String filename = path.replace("\\", "/");
			ImpExpFileReader reader = new ImpExpFileReader();
			try{
			ImpExpFile impFile = reader.read(filename);
			List<ImpExpTask> list = impFile.getTasks();
			int length = list.size();
			if (length == 0) {
				impProgress.setValue(100);
				area.append("导入任务不能为0");
			}
			for (int i = 1; i <= length; i++) {
				try {
					Thread.sleep(270);
				} catch (InterruptedException e) {
					break;
				}
				impProgress.setValue(100 * i / length);
				area.append("导入监测任务：" + list.get(i - 1).getName() + "完成\n");
				ImpProcessWindow.this.repaint();
				if (impProgress.getValue() >= 100) {
					area.append("导入完成，共导入" + length + "个监测任务。");
					break;
				}
			}
		 }catch (Exception e) {
			 impProgress.setValue(100);
				area.append("错误:  导入文件，"+e.getMessage());
			}
		}

	}

	public class TaskThread implements Runnable {
		@Override
		public void run() {
			String path = file.getPath();
			String filename = path.replace("\\", "/");
			try{
			ImpExpFileReader reader = new ImpExpFileReader();
			ImpExpFile impFile = reader.read(filename);
			List<ImpExpTask> list = impFile.getTasks();
			int length = list.size();
			if (length == 0) {
				return;
			}
			Importer imper = new Importer(impFile, imp);
			imper.impClient();
		}catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		}
	}

}
