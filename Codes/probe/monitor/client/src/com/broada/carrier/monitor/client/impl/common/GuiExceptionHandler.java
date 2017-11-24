package com.broada.carrier.monitor.client.impl.common;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.client.impl.MainWindow;
import com.broada.carrier.monitor.common.error.WarningException;
import com.broada.carrier.monitor.common.swing.ErrorDlg;

public class GuiExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GuiExceptionHandler.class);

	public void handle(Throwable error) {
		if (error instanceof WarningException) {
			logger.debug("警告异常：", error);
			JOptionPane.showMessageDialog(MainWindow.getDefault(), error.getMessage(), "操作失败", JOptionPane.WARNING_MESSAGE);
		} else {
			if (isTableError(error))
				logger.warn("未捕捉的表格异常：", error);
			else if (isTreeError(error))
				logger.warn("未捕捉的树异常: ", error);
			else
				ErrorDlg.show(error);
		}
	}

	private boolean isTableError(Throwable error) {
		StackTraceElement[] stack = error.getStackTrace();
		if (stack != null) {
			for (StackTraceElement element : stack)
				if (element.getClassName().contains("JTable"))
					return true;
		}
		return false;
	}

	private boolean isTreeError(Throwable error) {
		StackTraceElement[] stack = error.getStackTrace();
		if (stack != null) {
			for (StackTraceElement element : stack)
				if (element.getClassName().toUpperCase().contains("TREE"))
					return true;
		}
		return false;
	}
}
