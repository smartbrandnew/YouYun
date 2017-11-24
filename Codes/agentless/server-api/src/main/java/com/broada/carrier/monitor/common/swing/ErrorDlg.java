package com.broada.carrier.monitor.common.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.common.error.ServiceException;
import com.broada.component.utils.error.ErrorUtil;

/**
 * 通用错误对话框
 * @author Jiangjw
 */
public class ErrorDlg extends JDialog {
	private static final Log logger = LogFactory.getLog(ErrorDlg.class);
	private static final long serialVersionUID = 1L;
	private JTextArea txtDetail = new JTextArea();
	private JTextArea txtMessage = new JTextArea();
	private JPanel panelMessage = new JPanel();
	private String message;
	private JButton btnShowDetail = new JButton("显示详细");
	private JButton btnCopy = new JButton("复制错误");

	public ErrorDlg() {
		this(null, null, null);
	}

	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */
	public ErrorDlg(Window owner, String msg, Throwable error) {
		this(owner, null, msg, true, error);
	}
	
	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */
	public ErrorDlg(Window owner, String title, boolean modal, Throwable error) {
		this(owner, title, null, true, error);
	}	

	/**
	 * 构造函数
	 * @param owner 父窗口
	 * @param title 窗口标题，如果为null则为“错误”
	 * @param msg 错误消息，如果为null则从error中提取
	 * @param modal 是否为模式窗口
	 * @param error 异常
	 */
	public ErrorDlg(Window owner, String title, String msg, boolean modal, Throwable error) {
		super(owner == null ? WinUtil.getMainWindow() : owner);
		init(owner);
		setTitle(title == null ? "错误" : title);
		setModal(modal);
		message = msg;
		if (message == null)
			message = createErrorMessage(error);
		txtMessage.setBorder(null);
		txtMessage.setText(message);
		txtMessage.setSelectionStart(0);
		txtMessage.setSelectionEnd(0);
		txtDetail.setText(createErrorDetail(message, error));
		txtDetail.setSelectionStart(0);
		txtDetail.setSelectionEnd(0);
		setShowDetail(false);		
		if (error instanceof NullPointerException) {
			logger.warn(message, error);
		} else {
			logger.warn(message);
			logger.debug("堆栈：", error);
		}
	}

	private void setShowDetail(boolean show) {
		if (show) {
			panelMessage.setPreferredSize(new Dimension(0, 400));			
			btnShowDetail.setText("隐藏详细");
		} else {
			panelMessage.setPreferredSize(new Dimension(0, 0));
			btnShowDetail.setText("显示详细");
		}
		panelMessage.setVisible(show);
		pack();
		WinUtil.toCenter(this);
	}
	
	private boolean isShowDetail() {
		return panelMessage.isVisible();
	}

	private static String createErrorDetail(String message, Throwable error) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
				
		ps.println("错误消息：");
		ps.println(message);
		ps.println();
		
		if (error == null)
			return os.toString();

		ps.println("异常树：");
		Throwable cause = error;
		for (int i = 1; cause != null; i++) {
			ps.printf("%d. %s\n", i, cause);
			cause = cause.getCause();
		}
		ps.println();
		
		if (error instanceof ServiceException) {
			ps.println("服务异常堆栈：");		
			ps.println(((ServiceException) error).getStack());
		}

		ps.println("本地异常堆栈：");		
		error.printStackTrace(ps);

		return os.toString();
	}

	private void init(Window owner) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if (owner != null)
			setIconImage(Toolkit.getDefaultToolkit().getImage(ErrorDlg.class.getResource("/com/broada/swing/util/warn16.png")));

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(500, 120));
		getContentPane().add(panel, BorderLayout.NORTH);

		JButton btnOk = new JButton("确定");
		btnOk.setBounds(390, 76, 81, 23);
		btnOk.addActionListener(new BtnOkActionListener());

		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setBounds(25, 22, 32, 32);
		lblNewLabel_1.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel_1.setIcon(new ImageIcon(ErrorDlg.class.getResource("error32.png")));
		btnCopy.setBounds(208, 76, 81, 23);
		
		btnCopy.addActionListener(new BtnCopyActionListener());
		btnShowDetail.setBounds(299, 76, 81, 23);
		btnShowDetail.addActionListener(new BtnShowDetailActionListener());
		panel.setLayout(null);
		panel.add(lblNewLabel_1);
		panel.add(btnCopy);
		panel.add(btnShowDetail);
		panel.add(btnOk);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBorder(null);
		scrollPane_1.setBounds(67, 10, 423, 59);
		panel.add(scrollPane_1);
				
		txtMessage.setBackground(UIManager.getColor("Panel.background"));
		txtMessage.setLineWrap(true);
		txtMessage.setTabSize(4);
		txtMessage.setEditable(false);
		scrollPane_1.setViewportView(txtMessage);

		getContentPane().add(panelMessage, BorderLayout.CENTER);
		panelMessage.setLayout(new BorderLayout(0, 0));

		txtDetail.setEditable(false);
		txtDetail.setTabSize(2);
		JScrollPane scrollPane = new JScrollPane(txtDetail);
		panelMessage.add(scrollPane);

		pack();
	}

	private static String createErrorMessage(Throwable error) {
		String errMsg = error.getMessage();
		if (errMsg == null) {
			Throwable cause = error.getCause();
			while (cause != null) {
				if (cause.getMessage() != null) {
					errMsg = cause.getMessage();
					break;
				}
			}
			if (errMsg == null)
				errMsg = error.toString();
		}
		return errMsg;
	}

	private class BtnCopyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {			
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Clipboard clipboard = toolkit.getSystemClipboard();
			clipboard.setContents(new StringSelection(txtDetail.getText()), null);
		}
	}

	private class BtnShowDetailActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setShowDetail(!isShowDetail());
		}
	}

	private class BtnOkActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ErrorDlg.this.dispose();
		}
	}
	
	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */	
	public static ErrorDlg createErrorDlg(Frame owner, String title, String msg, boolean modal, Throwable error) {
		return new ErrorDlg(owner, title, msg, modal, error);
	}
	
	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */	
	public static ErrorDlg createErrorDlg(Dialog owner, String title, String msg, boolean modal, Throwable error) {
		return new ErrorDlg(owner, title, msg, modal, error);
	}
	
	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */	
	public static ErrorDlg createErrorDlg(Component component, String title, String msg, boolean modal, Throwable error) {		
		return new ErrorDlg(WinUtil.getWindowForComponent(component), title, msg, modal, error);
	}

	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */	
	public static ErrorDlg createErrorDlg(Component component, String title, String msg, Throwable error) {		
		return new ErrorDlg(WinUtil.getWindowForComponent(component), title, msg, true, error);
	}
	
	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */	
	public static ErrorDlg createErrorDlg(Component component, String title, boolean modal, Throwable error) {		
		return new ErrorDlg(WinUtil.getWindowForComponent(component), title, null, modal, error);
	}
		
	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */	
	public static ErrorDlg createErrorDlg(Component component, boolean modal, Throwable error) {
		return new ErrorDlg(WinUtil.getWindowForComponent(component), null, null, modal, error);
	}
	
	/**
	 * {@link #ErrorDlg(Window, String, String, boolean, Throwable)}
	 */	
	public static ErrorDlg createErrorDlg(Component component, String title, Throwable error) {		
		return new ErrorDlg(WinUtil.getWindowForComponent(component), title, null, true, error);
	}

	/**
	 * {@link #show(Window, String, Throwable)}
	 */
	public static void show(Throwable error) {
		new ErrorDlg(null, null, error).setVisible(true);
	}
	
	/**
	 * {@link #show(Window, String, Throwable)}
	 */
	public static void show(String message) {
		new ErrorDlg(null, message, null).setVisible(true);
	}
	
	/**
	 * 使用错误对话框显示一个指定的错误
	 * @param owner 父窗口
	 * @param message 错误消息，如果为null则从error中提取
	 * @param error 异常，如果为null则只显示message
	 */
	public static void show(Window owner, String message, Throwable error) {
		new ErrorDlg(owner, ErrorUtil.createMessage(message, error), error).setVisible(true);
	}
	
	/**
	 * {@link #show(Window, String, Throwable)}
	 */	
	public static void show(String message, Throwable error) {
		new ErrorDlg(null, ErrorUtil.createMessage(message, error), error).setVisible(true);
	}
}
