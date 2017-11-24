package com.broada.carrier.monitor.client.impl.impexp;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.broada.carrier.monitor.client.impl.MainWindow;
import com.broada.carrier.monitor.common.swing.WinUtil;

public class GuiAction {
	public static void exp() {
		JFileChooser expFile = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Excel", "xls");
		expFile.setFileFilter(filter);
		expFile.setName("请输入导出文件名");
		expFile.setBounds(300, 300, 600, 500);
		expFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
		expFile.setSelectedFile(new File("*.xls"));
		expFile.setDialogType(JFileChooser.SAVE_DIALOG);
		expFile.setApproveButtonText("保存");
		int result = expFile.showSaveDialog(MainWindow.getDefault());
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = expFile.getSelectedFile();
			ExpProcessWindow window = new ExpProcessWindow(file);
			WinUtil.toCenter(window);
		}
	}

	public static void imp() {
		JFileChooser impFile = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Excel", "xls");
		impFile.setFileFilter(filter);
		impFile.setName("请选择导入文件");
		impFile.setSelectedFile(new File("*.xls"));
		impFile.setBounds(300, 300, 600, 500);
		impFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int result = impFile.showOpenDialog(MainWindow.getDefault());
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = impFile.getSelectedFile();
			ImpProcessWindow window = new ImpProcessWindow(file);
			WinUtil.toCenter(window);
		}
	}
}