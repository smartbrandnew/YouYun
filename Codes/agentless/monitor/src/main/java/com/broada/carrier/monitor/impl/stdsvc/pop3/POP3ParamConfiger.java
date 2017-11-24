package com.broada.carrier.monitor.impl.stdsvc.pop3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.impl.common.NumenMonitorConfiger;
import com.broada.carrier.monitor.impl.common.ui.SimpleNotePanel;
import com.broada.swing.util.ErrorDlg;

/**
 * POP3 监测类型的监测参数配置界面
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class POP3ParamConfiger extends NumenMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private SimpleNotePanel notePanel = new SimpleNotePanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JPanel jPanBody = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private SpinnerNumberModel snm_port = new SpinnerNumberModel(1, 1, 65535, 1);
	private JSpinner jSpnPort = new JSpinner(snm_port);
	private SpinnerNumberModel snm_timeout = new SpinnerNumberModel(3, 1,
			Integer.MAX_VALUE, 1);
	private JSpinner jSpnTimeout = new JSpinner(snm_timeout);
	private JPanel jPanParam = new JPanel();
	private JPanel jPanWonted = new JPanel();
	private TitledBorder titledBorder1;
	private JCheckBox jChkMailCount = new JCheckBox();
	private SpinnerNumberModel snm_mailcount = new SpinnerNumberModel(1, 0,
			Integer.MAX_VALUE - 1, 1);
	private JSpinner jSpnMailCount = new JSpinner(snm_mailcount);
	private JCheckBox jChkBoxSize = new JCheckBox();
	private SpinnerNumberModel snm_boxsize = new SpinnerNumberModel(1024d, 0d,
			Double.MAX_VALUE, 0.1);
	private JSpinner jSpnBoxSize = new JSpinner(snm_boxsize);
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel5 = new JLabel();
	private JPasswordField jTxtPasswd = new JPasswordField();
	private JTextField jTxtUser = new JTextField();
	private JLabel jLabel6 = new JLabel();
	private JLabel jLabel7 = new JLabel();
	private JCheckBox jChkReplyTime = new JCheckBox();
	private SpinnerNumberModel pop3_replyTime = new SpinnerNumberModel(3, 1, 65535, 1);
	private JSpinner jSpnReplyTime = new JSpinner(pop3_replyTime);
	/*对应编辑的参数*/
	private POP3Parameter param = new POP3Parameter();

	public POP3ParamConfiger() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
				white, new Color(148, 145, 140)), "正常判断条件");
		this.setLayout(borderLayout1);
		this.setPreferredSize(new java.awt.Dimension(762, 268));
		notePanel.setNote("监测指定的POP3服务是否正常,可以设定端口、超时时间" +
											"和正常判断条件,如果要监测邮件数和邮箱的使用量请输入要" +
											"监测的用户名和密码.");
		jLabel1.setToolTipText("");
		jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel1.setText("端口(Port)");
		jLabel1.setBounds(new Rectangle(17, 8, 100, 23));
		jPanBody.setLayout(borderLayout2);
		jSpnPort.setBounds(new Rectangle(148, 7, 77, 24));
		jSpnTimeout.setBounds(new Rectangle(148, 39, 77, 24));
		jLabel2.setBounds(new Rectangle(17, 40, 100, 23));
		jLabel2.setText("延时(Timeout)");
		jLabel2.setToolTipText("");
		jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel3.setHorizontalAlignment(SwingConstants.LEFT);
		jLabel3.setToolTipText("");
		jLabel3.setText("(秒)");
		jLabel3.setBounds(new Rectangle(229, 39, 100, 23));
		jPanParam.setLayout(null);
		jPanParam.setMinimumSize(new Dimension(1, 1));
		jPanParam.setPreferredSize(new Dimension(1, 100));
		jPanParam.setRequestFocusEnabled(true);
		jPanParam.setToolTipText("");
		jPanParam.setBounds(new Rectangle(0, 0, 10, 10));
		jPanWonted.setBorder(titledBorder1);
		jPanWonted.setPreferredSize(new Dimension(10, 60));
		jPanWonted.setRequestFocusEnabled(true);
		jPanWonted.setToolTipText("");
		jPanWonted.setBounds(new Rectangle(10, 10, 10, 10));
		jPanWonted.setLayout(null);
		jChkMailCount.setBounds(new Rectangle(18, 57, 129, 22));
		jChkMailCount.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				jChkMailCount_itemStateChanged(e);
			}
		});
		jChkMailCount.setText("邮箱邮件数小于");
		jChkMailCount.setOpaque(false);
		jSpnMailCount.setEnabled(false);
		jSpnMailCount.setBounds(new Rectangle(148, 55, 79, 24));
		jChkBoxSize.setOpaque(false);
		jChkBoxSize.setToolTipText("");
		jChkBoxSize.setText("邮箱使用量小于");
		jChkBoxSize.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				jChkBoxSize_itemStateChanged(e);
			}
		});
		jChkBoxSize.setBounds(new Rectangle(18, 94, 130, 22));
		jSpnBoxSize.setEnabled(false);
		jSpnBoxSize.setBounds(new Rectangle(148, 92, 79, 24));
		jLabel4.setText("(K字节)");
		jLabel4.setBounds(new Rectangle(231, 93, 65, 23));
		jLabel5.setBounds(new Rectangle(195, 73, 54, 22));
		jLabel5.setText("密码");
		jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel5.setToolTipText("");
		jTxtPasswd.setBounds(new Rectangle(252, 73, 119, 22));
		jTxtPasswd.setText("");
		jTxtPasswd.setEditable(true);
		jTxtPasswd.setToolTipText("");
		jTxtPasswd.setEnabled(false);
		jTxtUser.setEnabled(false);
		jTxtUser.setToolTipText("");
		jTxtUser.setText("");
		jTxtUser.setBounds(new Rectangle(84, 73, 106, 22));
		jLabel6.setToolTipText("");
		jLabel6.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel6.setText("用户名");
		jLabel6.setBounds(new Rectangle(25, 73, 58, 22));
		this.add(notePanel, BorderLayout.SOUTH);
		this.add(jPanBody, BorderLayout.CENTER);
		jPanParam.add(jLabel1, null);
		jPanParam.add(jLabel2, null);
		jPanParam.add(jLabel3, null);
		jPanParam.add(jSpnPort, null);
		jPanParam.add(jSpnTimeout, null);
		jPanParam.add(jTxtPasswd, null);
		jPanParam.add(jTxtUser, null);
		jPanParam.add(jLabel6, null);
		jPanParam.add(jLabel5, null);
		jPanBody.add(jPanParam, BorderLayout.NORTH);
		jPanBody.add(jPanWonted, BorderLayout.CENTER);
		jPanWonted.add(jChkMailCount, null);
		jPanWonted.add(jChkBoxSize, null);
		jPanWonted.add(jSpnBoxSize, null);
		jPanWonted.add(jSpnMailCount, null);
		jPanWonted.add(jLabel4, null);
		jChkReplyTime.setBounds(new Rectangle(18, 22, 120, 25));
		jChkReplyTime.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				jChkReplyTime_itemStateChanged(e);
			}
		});
		jChkReplyTime.setEnabled(true);
		jChkReplyTime.setToolTipText("");
		jChkReplyTime.setText("响应时间小于等于");
		jSpnReplyTime.setEnabled(false);
		jSpnReplyTime.setBounds(new Rectangle(161, 22, 76, 20));
		jLabel7.setHorizontalAlignment(SwingConstants.LEFT);
		jLabel7.setToolTipText("");
		jLabel7.setText("(秒)");
		jLabel7.setBounds(new Rectangle(242, 22, 100, 23));
		jPanWonted.add(jLabel7, null);
		jPanWonted.add(jChkReplyTime, null);
		jPanWonted.add(jSpnReplyTime, null);
	}

	/**
	 * 设置初始值
	 * @param xml
	 */
	public void setParameters(String xml) {
		param = new POP3Parameter(xml);
		jSpnPort.setValue(new Integer(param.getPort()));
		jSpnTimeout.setValue(new Integer(param.getTimeout() / 1000));
		jChkMailCount.setSelected(param.isChkMailCount());
		boolean showUserInfo = false;
		if (jChkMailCount.isSelected()) {
			jSpnMailCount.setValue(new Integer(param.getMailCount()));
			showUserInfo = true;
		}
		jChkBoxSize.setSelected(param.isChkBoxSize());
		if (jChkBoxSize.isSelected()) {
			jSpnBoxSize.setValue(new Double(param.getBoxSize()));
			showUserInfo = true;
		}
		if (showUserInfo) {
			jTxtUser.setText(param.getUser());
			jTxtPasswd.setText(param.getPassword());
		}
		jSpnTimeout.setValue(new Integer(param.getTimeout() / 1000));
		jChkReplyTime.setSelected(param.isChkReplyTime());
		if (jChkReplyTime.isSelected()) {
			this.jSpnReplyTime.setValue(new Integer(param.getReplyTime()));
		}
	}

	/**
	 * 获取编辑结果
	 * @return
	 */
	public String getParameters() {
		param.setPort(snm_port.getNumber().intValue());
		param.setTimeout(snm_timeout.getNumber().intValue() * 1000);
		boolean saveUserInfo = false;
		if (jChkMailCount.isSelected()) {
			param.setMailCount(snm_mailcount.getNumber().intValue());
			saveUserInfo = true;
		} else {
			param.setMailCount(Integer.MIN_VALUE);
		}
		if (jChkReplyTime.isSelected()) {
			param.setReplyTime(pop3_replyTime.getNumber().intValue());
		} else {
			param.setReplyTime(-1);
		}
		if (jChkBoxSize.isSelected()) {
			param.setBoxSize(snm_boxsize.getNumber().doubleValue());
			saveUserInfo = true;
		} else {
			param.setBoxSize(Double.NaN);
		}
		if (saveUserInfo) {
			param.setPassword(new String(jTxtPasswd.getPassword()));
			param.setUser(jTxtUser.getText());
		} else {
			param.setUser(null);
		}
		try {
			return param.getParameters();
		} catch (Exception ex) {
			ErrorDlg.createErrorDlg(this, "错误", "POP3服务监测参数转换成字符串版本错误.", true, ex).
					setVisible(true);
			return "";
		}
	}

	public boolean verify() {
		if (jChkMailCount.isSelected() || jChkBoxSize.isSelected()) {
			if (jTxtUser.getText().trim().length() == 0) {
				JOptionPane.showMessageDialog(this, "要校验邮箱邮件数或使用量一定要输入登录的用户名和密码！");
				jTxtUser.requestFocus();
				return false;
			}
		}
		return true;
	}

	public Component getConfigUI() {
		return this;
	}

	void setUserInfoEnabled(boolean b) {
		jTxtUser.setEnabled(b);
		jTxtPasswd.setEnabled(b);
	}

	/**
	 * 关联操作
	 * @param e
	 */
	void jChkBoxSize_itemStateChanged(ItemEvent e) {
		jSpnBoxSize.setEnabled(jChkBoxSize.isSelected());
		if (jChkBoxSize.isSelected()) {
			setUserInfoEnabled(true);
		} else if (!jChkMailCount.isSelected()) {
			setUserInfoEnabled(false);
		}
	}

	/**
	 * 关联操作
	 * @param e
	 */
	void jChkMailCount_itemStateChanged(ItemEvent e) {
		jSpnMailCount.setEnabled(jChkMailCount.isSelected());
		if (jChkMailCount.isSelected()) {
			setUserInfoEnabled(true);
		} else if (!jChkBoxSize.isSelected()) {
			setUserInfoEnabled(false);
		}
	}

	void jChkReplyTime_itemStateChanged(ItemEvent e) {
		if (jChkReplyTime.isSelected())
			jSpnReplyTime.setEnabled(true);
		else
			jSpnReplyTime.setEnabled(false);
	}

}
