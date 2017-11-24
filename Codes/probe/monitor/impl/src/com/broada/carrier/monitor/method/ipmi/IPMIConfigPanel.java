package com.broada.carrier.monitor.method.ipmi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIParameter;
import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;

/**
 * IPMI协议监测的基本信息设置面板
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: NMS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author pippo
 */

public class IPMIConfigPanel extends BaseMethodConfiger {
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 2281429902267539686L;
	private BorderLayout borderLayout1 = new BorderLayout();
	JPanel jPanParam = new JPanel();
	JLabel jLabelIP = new JLabel();
	JTextField jSpnIP = new JTextField();
	JLabel jLabelMfg = new JLabel();
	JComboBox jCmbMfg = new JComboBox();
	JLabel jLabelPASS = new JLabel();
	JPasswordField jTxtPASS = new JPasswordField();
	JLabel jLabelUser = new JLabel();
	JTextField jSpnUser = new JTextField();

	private IPMIMonitorMethodOption param;

	// NUM-469
	private JButton jButtonTest = new JButton(" 测 试 ");
	private JTextField txtLevel;

	public IPMIConfigPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jPanParam.setLayout(null);
		jLabelIP.setToolTipText("");
		jLabelIP.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelIP.setText("BMC IP");
		jLabelIP.setBounds(new Rectangle(-1, 4, 86, 23));
		jSpnIP.setBounds(new Rectangle(92, 4, 142, 24));
		jPanParam.setMinimumSize(new Dimension(1, 1));
		jPanParam.setPreferredSize(new Dimension(1, 70));
		jPanParam.setRequestFocusEnabled(true);
		jPanParam.setToolTipText("");
		jLabelMfg.setBounds(new Rectangle(197, 4, 89, 23));
		jLabelMfg.setText("服务器厂商");
		jLabelMfg.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelMfg.setToolTipText("");
		jCmbMfg.setBounds(new Rectangle(293, 4, 70, 24));
		jLabelUser.setToolTipText("");
		jLabelUser.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelUser.setText("用户名");
		jLabelUser.setBounds(new Rectangle(-1, 43, 89, 23));
		jSpnUser.setBounds(new Rectangle(92, 42, 142, 24));
		jLabelPASS.setBounds(new Rectangle(202, 43, 86, 23));
		jLabelPASS.setText("密码");
		jLabelPASS.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelPASS.setToolTipText("");
		jTxtPASS.setToolTipText("");
		jTxtPASS.setText("");
		jTxtPASS.setBounds(new Rectangle(298, 42, 100, 24));
		jButtonTest.setBounds(new Rectangle(200, 220, 80, 24));
		jButtonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doTest();
			}
		});

		this.add(jPanParam, BorderLayout.CENTER);
		jPanParam.add(jLabelIP, null);
		jPanParam.add(jSpnIP, null);
		jPanParam.add(jTxtPASS, null);
		jPanParam.add(jLabelUser, null);
		jPanParam.add(jSpnUser, null);
		jPanParam.add(jLabelPASS, null);
		jPanParam.add(jButtonTest, null);
		
		JLabel label = new JLabel();
		label.setToolTipText("");
		label.setText("用户级别");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBounds(new Rectangle(-1, 43, 89, 23));
		label.setBounds(-1, 84, 89, 23);
		jPanParam.add(label);
		
		txtLevel = new JTextField();
		txtLevel.setBounds(new Rectangle(92, 42, 100, 24));
		txtLevel.setBounds(92, 83, 142, 24);
		jPanParam.add(txtLevel);
	}

	/**
	 * 设置初始值
	 * 
	 * @param param
	 */
	public void setParameter(IPMIMonitorMethodOption param) {
		this.param = param;
		jSpnIP.setText(param.getHost());
		jTxtPASS.setText(param.getPassword());
		jSpnUser.setText(param.getUsername());
		txtLevel.setText(param.getLevel());
	}

	/**
	 * 设置Snmp信息
	 */
	protected void setSnmpInfo() {
		if (param == null) {
			return;
		}
		param.setHost(jSpnIP.getText().trim());
		param.setUsername(jSpnUser.getText().trim());
		param.setPassword(new String(jTxtPASS.getPassword()).trim());
		param.setLevel(txtLevel.getText().trim());
	}

	/**
	 * 获取编辑结果
	 * 
	 * @return
	 */
	public IPMIMonitorMethodOption getParameter() {
		setSnmpInfo();
		return param;
	}

	public boolean verify() {
		if (jSpnIP.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(IPMIConfigPanel.this, "BMC IP地址不能为空！", "信息", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		Pattern pattern = Pattern
				.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		if (!pattern.matcher(jSpnIP.getText().trim()).matches()) {
			JOptionPane.showMessageDialog(IPMIConfigPanel.this, "BMC IP地址格式不对！", "信息", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		if (jSpnUser.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(IPMIConfigPanel.this, "用户名不能为空！", "信息", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		if (new String(jTxtPASS.getPassword()).trim().length() == 0) {
			JOptionPane.showMessageDialog(IPMIConfigPanel.this, "密码不能为空！", "信息", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		return true;
	}

	public boolean doTest() {
		if (!verify())
			return false;
		try {			
			IPMIMonitorMethodOption option = getParameter();
			if (option == null)
				return false;
			IPMIParameter param = option.toParameter(getContext().getNode().getIp());
			getContext().getServerFactory().getProbeService()
					.executeMethod(getContext().getNode().getProbeId(), IPMIConfigTester.class.getName(), "doTest",
							param);			
			JOptionPane.showMessageDialog(IPMIConfigPanel.this, "IPMI监测参数配置正确", "成功", JOptionPane.INFORMATION_MESSAGE);
			return true;
		} catch (Throwable e) {
			ErrorDlg.createErrorDlg(IPMIConfigPanel.this, "错误信息", e).setVisible(true);
			return false;
		}
	}

	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		IPMIMonitorMethodOption method = getParameter();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setParameter(new IPMIMonitorMethodOption(method));		
	}  
}
