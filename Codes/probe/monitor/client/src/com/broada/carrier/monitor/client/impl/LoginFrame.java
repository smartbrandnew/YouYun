package com.broada.carrier.monitor.client.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.client.impl.config.Config;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.component.utils.lang.ThreadUtil;
import com.broada.module.autosync.client.api.Sync;
import com.broada.module.autosync.client.api.SyncDaemon;
import com.broada.swing.laf.VisualStudio2005LookAndFeel;
import com.broada.swing.panel.ImagePanel;
import com.broada.swing.util.ErrorDlg;
import com.broada.swing.util.WinUtil;

/**
 * 登录窗口
 * 从ncc中迁移过来
 * 
 * 主要工作内容：
 * 1. 加载缓存文件，获取缓存的上一次登录信息
 * 2.	登录操作：获取服务端连接、检查连接的可用性、检查登录信息的合法性
 * 3. 登录成功后保存登录信息到缓存文件中，并启动连接检查服务、登录检查服务进行周期性的持续检查，
 *    以便在客户端于服务端不再匹配的时候自动退出
 * @author panhk
 *
 */
public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 6824348657253416349L;
	private static final Log logger = LogFactory.getLog(LoginFrame.class);
	// 启动logo
	// private Logo logo = new Logo("images/logo.jpg");
	// 用户登录信息
	private String serverip = "127.0.0.1";
	private String username = "admin";
	private String password = "";

	private JPanel contentPane;
	private JTextField jTextFieldServer = new JTextField();

	private JTextField jTextFieldUser = new JTextField();
	private JPasswordField jPasswordField = new JPasswordField();
	private JButton jButtonOk;
	private JButton jButtonCancel;

	private static String loginImageFileDir = Config.getResourceDir() + "/images/";
	private static String loginInfoTempFile = Config.getTempDir() + "/logininfotemp";
	private static SyncDaemon daemon;
	private boolean result = false;
	private Properties props = new Properties();

	public LoginFrame() throws Exception {
		preInit();
		jbInit();
	}

	/**
	 * 加载保存登录新的临时文件
	 */
	private void preInit() {
		FileInputStream fis = null;
		try {
			File file = new File(loginInfoTempFile);
			if (!file.exists()) {
				file.createNewFile();
			}
			fis = new FileInputStream(file);
			props.load(fis);
			serverip = props.getProperty("serverip", serverip);
			username = props.getProperty("username", username);
		} catch (Exception e) {
			logger.debug("读取缓存文件[" + loginInfoTempFile + "]失败，应该是首次登录", e);
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	private void jbInit() throws Exception {		
		this.setResizable(false);		
		this.setSize(new Dimension(405, 260));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		setIconImage(IconLibrary.getDefault().getImage("resources/images/app.png"));
		this.setTitle("登录监测客户端");

		ImagePanel jPanelPic = new ImagePanel();
		jPanelPic.setBackgroundImg(getLoginImage("login_banner.png"));
		Dimension bannerSize = new Dimension(405, 72);
		jPanelPic.setPreferredSize(bannerSize);
		jPanelPic.setSize(bannerSize);
		jPanelPic.setMaximumSize(bannerSize);

		jButtonOk = new JButton();
		jButtonCancel = new JButton();
		JLabel jLabelServer = new JLabel();
		JLabel jLabelUser = new JLabel();
		JLabel jLabelPassword = new JLabel();

		ImagePanel panel = new ImagePanel();
		panel.setPreferredSize(new Dimension(405, 300));
		panel.setBackgroundImg(getLoginImage("login_background.png"));
		jButtonOk.setBounds(new Rectangle(169, 114, 84, 25));
		jButtonOk.setText("登录");
		jButtonCancel.setText("取消");
		jButtonCancel.setBounds(new Rectangle(263, 114, 84, 25));
		jTextFieldServer.setText(serverip);
		jTextFieldServer.setBounds(new Rectangle(104, 20, 243, 24));
		jLabelServer.setText("服务器地址:");
		jLabelServer.setBounds(new Rectangle(9, 24, 100, 18));
		jLabelUser.setText("用户名:");
		jLabelUser.setBounds(new Rectangle(9, 53, 100, 18));
		jTextFieldUser.setText(username);
		jTextFieldUser.setBounds(new Rectangle(104, 53, 243, 24));
		jLabelPassword.setText("密码:");
		jLabelPassword.setBounds(new Rectangle(9, 82, 100, 24));

		jPasswordField.setText(password);
		jPasswordField.setBounds(new Rectangle(104, 82, 243, 24));

		getRootPane().setDefaultButton(jButtonOk);
		panel.setLayout(null);
		panel.add(jLabelServer, null);
		panel.add(jTextFieldServer, null);
		panel.add(jLabelUser, null);
		panel.add(jTextFieldUser, null);
		panel.add(jLabelPassword, null);
		panel.add(jPasswordField, null);
		panel.add(jButtonOk, null);
		panel.add(jButtonCancel, null);
		contentPane.add(jPanelPic, BorderLayout.NORTH);
		contentPane.add(panel, BorderLayout.CENTER);

		jButtonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				jButtonOk_actionPerformed(e);
			}
		});
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonCancel_actionPerformed(e);
			}
		});
	}

	protected void jButtonCancel_actionPerformed(ActionEvent e) {
		Runtime.getRuntime().halt(0);
	}

	public String verify() {
		String userid = jTextFieldUser.getText();
		char[] passwd = jPasswordField.getPassword();
		String serverip = jTextFieldServer.getText();

		if (TextUtil.isEmpty(userid)) {
			return "请输入帐号";
		}

		if (passwd.length == 0) {
			return "请输入密码";
		}

		if (TextUtil.isEmpty(serverip)) {
			return "请输入服务器地址";
		}

		return null;
	}
	
	private class LoginThread implements Runnable {
		public void run() {
			try {
				String ret = verify();
				if (ret != null) {
					JOptionPane.showMessageDialog(LoginFrame.this, ret, "登录失败", JOptionPane.WARNING_MESSAGE);
					return;
				}
	
				// 连接
				String serverip = String.valueOf(jTextFieldServer.getText());
				
				try {
					ServerContext.connect(serverip);
				} catch (Exception err) {					
					ErrorDlg.createErrorDlg(LoginFrame.this, "连接失败", "连接服务端失败，服务端可能未成功启动！", err).setVisible(true);
					return;
				}
				
				// 保存登录信息到文件中
				username = jTextFieldUser.getText();
				props.setProperty("serverip", serverip);
				props.setProperty("username", username);
	
				try {
					saveLoginInfo();
				} catch (IOException err) {
					logger.warn("保存客户端登录信息失败。错误：" + err);
					logger.debug("堆栈：", err);
				}
				
				Sync.getDefault().sync(
						"carrier-client",
						String.format("%s/monitor/autosync/", ServerContext.getServerUrl()));
				if (daemon == null) {
					daemon = new SyncDaemon(Sync.getDefault(), Config.getDefault().getAutosyncCheckInterval());			
					daemon.startup();
				}
	
				// 登录
				try {
					String passwd = new String(jPasswordField.getPassword());
					ServerContext.login(username, passwd);					
				} catch (Throwable e) {
					JOptionPane.showMessageDialog(LoginFrame.this, e.getMessage(), "登录失败", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				result = true;
				ConnectTask.setServerSystemProperty();
				new Timer("连接检查服务").schedule(new ConnectTask(), 30000, 30000);
				LoginFrame.this.dispose();												
			} finally {
				jButtonOk.setEnabled(true);
				jButtonCancel.setEnabled(true);
			}
		}
	}

	/**
	 * 1. 连接服务端 2. 登录 3. 保存登录信息，打开客户端界面
	 * 
	 * @param e
	 */
	protected void jButtonOk_actionPerformed(ActionEvent e) {
		jButtonOk.setEnabled(false);
		jButtonCancel.setEnabled(false);
		Thread thread = ThreadUtil.createThread(new LoginThread());
		thread.start();
	}
	
	@Override
	public void dispose() {
		synchronized (this) {
			this.notify();
		}
		super.dispose();
	}

	private void saveLoginInfo() throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(loginInfoTempFile);
			props.store(fos, "登录信息");// property类关键的store方法
			fos.close();			
		} catch (Exception err) {
			logger.warn("保存登录信息到临时文件[" + loginInfoTempFile + "]失败。错误：" + err);
			logger.debug("堆栈：", err);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	public static boolean showLogin() throws Exception {
		configureUI();
		LoginFrame frame = new LoginFrame();
		WinUtil.toCenter(frame);
		frame.setVisible(true);
		synchronized (frame) {
			frame.wait();
		}
		return frame.result;
	}

	private BufferedImage getLoginImage(String fileName) {
		String filePath = loginImageFileDir + fileName;
		try {
			return ImageIO.read(new File(filePath));
		} catch (IOException e) {
			throw new RuntimeException("无法读取图片[" + filePath + "]", e);
		}
	}

	private static void configureUI() {
		try {
			UIManager.setLookAndFeel(VisualStudio2005LookAndFeel.class.getName());
		} catch (Exception e) {
			logger.warn("初始化界面皮肤错误,将采用默认界面.", e); //$NON-NLS-1$
		}
	}

	/**
	 * 从ncc复制过来的servermgr代码，所以TextRes作一个占位，避免修改过多代码。将来也方便做i18n
	 */
	public static class TextRes {
		public static String get(String text) {
			return text;
		}
	}
}
