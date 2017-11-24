package com.broada.carrier.monitor.common.swing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.broada.component.utils.error.ErrorUtil;

/**
 * Swing的一些工具类
 * @author Jiangjw
 */
public class WinUtil {
	private static JFileChooser chooser;
	private static Window mainWindow;

	/**
	 * Mdf by Maico pang
	 *
	 * 修改为无论如何都要保证窗口的标题栏和操作按钮在可视范围内
	 * @param w
	 */
	public static void toCenter(Window w) {
		Point location;
		Dimension w_size = w.getSize();
		Dimension s_size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

		if (w.getOwner() != null && w.getOwner().isShowing()) {
			Point offset = w.getOwner().getLocationOnScreen();
			Dimension d = w.getOwner().getSize();
			location = new Point(offset.x + d.width / 2 - w_size.width / 2,
					offset.y + d.height / 2 - w_size.height / 2);
		} else {
			location = new Point(s_size.width / 2 - w_size.width / 2,
					s_size.height / 2 - w_size.height / 2);
		}
		if (location.y < 0) {
			location.y = 0;
		}
		if (location.x > s_size.width - w_size.width) {
			location.x = s_size.width - w_size.width;
		}
		w.setLocation(location);
	}

	/**
	 * 获取某个指定组件的所在窗口
	 * @param parentComponent
	 * @return
	 */
	public static Window getWindowForComponent(Component parentComponent) {
		if (parentComponent == null) {
			return JOptionPane.getRootFrame();
		}
		if (parentComponent instanceof Frame || parentComponent instanceof Dialog) {
			return (Window) parentComponent;
		}
		return getWindowForComponent(parentComponent.getParent());
	}

	/**
	 * 根据组件创建，显示一个输入对话框
	 * <p>
	 * <code>
	 * JPanel panel = new JPanle();
	 * panel.add(new JTextField());
	 * WinUtil.showInputDialog(this,panel,"输入名字");
	 * </code>
	 * 
	 * @param parent 父窗口组件
	 * @param component 显示内容组件
	 * @param title 标题
	 * @return 返回用户是否是确定或是取消
	 */
	public static boolean showInputDialog(Component parent, Component component, String title) {
		JOptionPane optionPane = new JOptionPane(component, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null, null);
		// optionPane.setWantsInput(true);
		JDialog dlg = optionPane.createDialog(parent, title);
		dlg.setBounds(dlg.getX(), dlg.getY() + 50, dlg.getWidth() - 50, dlg.getHeight() - 100);
		dlg.setModal(true);
		dlg.setResizable(true);
		dlg.setVisible(true);
		if (optionPane.getValue() == null) {
			return false;
		}
		int value = ((Integer) optionPane.getValue()).intValue();
		return (value == JOptionPane.OK_OPTION);
	}

	/**
	 * 显示一个错误消息
	 * @param parent
	 * @param message
	 * @param error
	 */
	public static void showErrorMsg(Component parent, String message, Throwable error) {
		message = ErrorUtil.createMessage(message, error);
		JOptionPane.showMessageDialog(parent, message, "错误消息", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * 显示打开文件对话框
	 * @param parent
	 * @param ext
	 * @param descr
	 * @return
	 */
	public static File showOpenFile(Component parent, String ext, String descr) {
		return showChooserFile(parent, ext, descr, true, null);
	}

	/**
	 * 显示打开文件对话框
	 * @param parent
	 * @return
	 */
	public static File showOpenFile(Component parent) {
		return showOpenFile(parent, null, null);
	}

	/**
	 * 显示打开文件对话框
	 * @param parent
	 * @param ext
	 * @param descr
	 * @return
	 */
	public static File[] showOpenFiles(Component parent, String ext, String descr) {
		return showChooserFiles(parent, ext, descr, true, null, true);
	}

	/**
	 * 显示打开文件对话框
	 * @param parent
	 * @return
	 */
	public static File[] showOpenFiles(Component parent) {
		return showOpenFiles(parent, null, null);
	}

	/**
	 * 显示修改文件对话框
	 * @param parent
	 * @param ext
	 * @param descr
	 * @param filename
	 * @return
	 */
	public static File showSaveFile(Component parent, String ext, String descr, String filename) {
		return showChooserFile(parent, ext, descr, false, filename);
	}

	private static File showChooserFile(Component parent, String ext, String descr, boolean isOpen, String filename) {
		File[] files = showChooserFiles(parent, ext, descr, false, filename, false);
		if (files == null)
			return null;
		return files[0];
	}

	private static File[] showChooserFiles(Component parent, String ext, String descr, boolean isOpen, String filename,
			boolean multi) {
		if (chooser == null) {
			chooser = new JFileChooser();
		}
		if (ext == null)
			chooser.setFileFilter(null);
		else
			chooser.setFileFilter(new DefaultFileFilter(ext, descr));
		chooser.setMultiSelectionEnabled(multi);
		chooser.setSelectedFile(filename == null ? null : new File(filename));

		int ret;
		if (isOpen)
			ret = chooser.showOpenDialog(parent);
		else
			ret = chooser.showSaveDialog(parent);
		if (ret == JFileChooser.APPROVE_OPTION) {
			if (multi)
				return chooser.getSelectedFiles();
			else
				return new File[] { chooser.getSelectedFile() };
		} else
			return null;
	}

	private static class DefaultFileFilter extends FileFilter {
		private String ext;
		private String descr;

		public DefaultFileFilter(String ext, String descr) {
			if (!(ext.charAt(0) == '.'))
				ext = "." + ext;
			this.ext = ext;
			this.descr = descr;
		}

		@Override
		public boolean accept(File f) {
			return f.getName().endsWith(ext);
		}

		@Override
		public String getDescription() {
			return descr;
		}
	}

	/**
	 * 切换当前程序状态到忙状态，目前有以下界面体现
	 * 1. 鼠标指针被设置为等待状态
	 */
	public static void switchBusy() {
		for (Window win : Window.getWindows())
			win.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	/**
	 * 切换当前程序状态为空闲状态，恢复由{@link #switchBusy()}设置的各种状态
	 */
	public static void switchIdle() {
		for (Window win : Window.getWindows())
			win.setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * 设置主窗口
	 * @param mainWindow
	 */
	public static void setMainWindow(Window mainWindow) {
		WinUtil.mainWindow = mainWindow;
	}

	/**
	 * 获取主窗口
	 * @return
	 */
	public static Window getMainWindow() {
		return mainWindow;
	}

}