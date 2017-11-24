package com.broada.carrier.monitor.method.snmp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;
import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpTarget;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.StringUtil;

/**
 * Snmp协议监测的基本信息设置面板
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 * Modified by hesy at 2008-2-22 下午14:19:06 NUM-469（参数配置面板统一加入测试按钮）
 */

public class SnmpBaseConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BorderLayout borderLayout1 = new BorderLayout();
	private SpinnerNumberModel snm_port = new SpinnerNumberModel(1, 1, 65535, 1);
	JPanel jPanParam = new JPanel();
	JLabel jLabel1 = new JLabel();
	JSpinner jSpnPort = new JSpinner(snm_port);
	JLabel jLabel2 = new JLabel();
	JComboBox jCmbVersion = new JComboBox();
	JLabel jLabel3 = new JLabel();
	JPasswordField jTxtCommunity = new JPasswordField();
	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	private SpinnerNumberModel snm_timeout = new SpinnerNumberModel(3, 1,
			Integer.MAX_VALUE, 1);
	private JSpinner jSpnTimeout = new JSpinner(snm_timeout);
	JLabel jLabel7 = new JLabel();
	JComboBox jCmbSecLev = new JComboBox();
	JLabel jLabel8 = new JLabel();
	JTextField jTfSecName = new JTextField();
	JLabel jLabel9 = new JLabel();
	JComboBox jCmbAuthProt = new JComboBox();
	JLabel jLabel10 = new JLabel();
	JPasswordField jPfAuthPass = new JPasswordField();
	JLabel jLabel11 = new JLabel();
	JComboBox jCmbPrivProt = new JComboBox();
	JLabel jLabel12 = new JLabel();
	JPasswordField jPfPrivaPass = new JPasswordField();
	private JButton jButtonTest = new JButton(" 测 试 ");
	private MonitorMethodConfigContext context;

	//

	public SnmpBaseConfigPanel() {
		try {
			jbInit();
			initPanel();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jPanParam.setLayout(null);
		jLabel1.setToolTipText("");
		jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel1.setText("端口(Port)");
		jLabel1.setBounds(new Rectangle(-1, 4, 86, 23));
		jSpnPort.setOpaque(false);
		jSpnPort.setBounds(new Rectangle(92, 4, 77, 24));
		jPanParam.setMinimumSize(new Dimension(1, 1));
		jPanParam.setPreferredSize(new Dimension(1, 70));
		jPanParam.setRequestFocusEnabled(true);
		jPanParam.setToolTipText("");
		jLabel2.setBounds(new Rectangle(197, 4, 89, 23));
		jLabel2.setText("版本(Version)");
		jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel2.setToolTipText("");
		jCmbVersion.setBounds(new Rectangle(293, 4, 70, 24));
		jLabel3.setBounds(new Rectangle(-1, 43, 86, 23));
		jLabel3.setText("Community");
		jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel3.setToolTipText("");
		jTxtCommunity.setToolTipText("");
		jTxtCommunity.setText("");
		jTxtCommunity.setBounds(new Rectangle(92, 42, 102, 24));
		jLabel5.setToolTipText("");
		jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel5.setText("延时(Timeout)");
		jLabel5.setBounds(new Rectangle(197, 43, 89, 23));
		jLabel6.setBounds(new Rectangle(367, 42, 100, 23));
		jLabel6.setText("(秒)");
		jLabel6.setToolTipText("");
		jLabel6.setHorizontalAlignment(SwingConstants.LEFT);
		jSpnTimeout.setBounds(new Rectangle(292, 42, 72, 24));
		jButtonTest.setBounds(new Rectangle(92, 220, 80, 24)); //NUM-469
		jLabel7.setBounds(new Rectangle(-1, 82, 86, 23));
		jLabel7.setText("安全等级");
		jLabel7.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel7.setToolTipText("");
		jCmbSecLev.setBounds(new Rectangle(92, 82, 125, 24));
		jLabel8.setBounds(new Rectangle(197, 82, 86, 24));
		jLabel8.setText("安全名");
		jLabel8.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel8.setToolTipText("");
		jTfSecName.setBounds(new Rectangle(293, 82, 147, 25));
		jLabel9.setBounds(new Rectangle(-1, 120, 86, 23));
		jLabel9.setText("用户协议");
		jLabel9.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel9.setToolTipText("");
		jCmbAuthProt.setBounds(new Rectangle(92, 120, 70, 24));
		jLabel10.setBounds(new Rectangle(197, 120, 86, 23));
		jLabel10.setText("用户密码");
		jLabel10.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel10.setToolTipText("");
		jPfAuthPass.setBounds(new Rectangle(292, 120, 102, 24));
		jPfAuthPass.setToolTipText("");
		jPfAuthPass.setText("");
		jLabel11.setBounds(new Rectangle(-1, 158, 86, 23));
		jLabel11.setText("私有协议");
		jLabel11.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel11.setToolTipText("");
		jCmbPrivProt.setBounds(new Rectangle(92, 158, 70, 24));
		jLabel12.setBounds(new Rectangle(197, 158, 86, 23));
		jLabel12.setText("私有密码");
		jLabel12.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel12.setToolTipText("");
		jPfPrivaPass.setBounds(new Rectangle(292, 158, 102, 24));
		jPfPrivaPass.setToolTipText("");
		jPfPrivaPass.setText("");

		// NUM-469
		jButtonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//测试参数配置的有效性
				doTest();
			}
		});
		//

		this.add(jPanParam, BorderLayout.CENTER);
		jPanParam.add(jLabel1, null);
		jPanParam.add(jSpnPort, null);
		jPanParam.add(jCmbVersion, null);
		jPanParam.add(jLabel2, null);
		jPanParam.add(jTxtCommunity, null);
		jPanParam.add(jLabel5, null);
		jPanParam.add(jSpnTimeout, null);
		jPanParam.add(jLabel6, null);
		jPanParam.add(jLabel3, null);
		jPanParam.add(jButtonTest, null); //NUM-469
		jPanParam.add(jLabel7, null);
		jPanParam.add(jCmbSecLev, null);
		jPanParam.add(jLabel8, null);
		jPanParam.add(jTfSecName, null);
		jPanParam.add(jLabel9, null);
		jPanParam.add(jCmbAuthProt, null);
		jPanParam.add(jLabel10, null);
		jPanParam.add(jPfAuthPass, null);
		jPanParam.add(jLabel11, null);
		jPanParam.add(jCmbPrivProt, null);
		jPanParam.add(jLabel12, null);
		jPanParam.add(jPfPrivaPass, null);
		jLabel7.setVisible(false);
		jCmbSecLev.setVisible(false);
		jLabel8.setVisible(false);
		jTfSecName.setVisible(false);
		jLabel9.setVisible(false);
		jCmbAuthProt.setVisible(false);
		jLabel10.setVisible(false);
		jPfAuthPass.setVisible(false);
		jLabel11.setVisible(false);
		jCmbPrivProt.setVisible(false);
		jLabel12.setVisible(false);
		jPfPrivaPass.setVisible(false);

		jCmbVersion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//判断用户选择的是否是SnmpV3来重新配置面板
				if ("v3".equalsIgnoreCase(jCmbVersion.getSelectedItem().toString())) {
					jLabel3.setVisible(false);
					jTxtCommunity.setVisible(false);
					jLabel7.setVisible(true);
					jCmbSecLev.setVisible(true);
					jLabel8.setVisible(true);
					jTfSecName.setVisible(true);
					jLabel9.setVisible(true);
					jCmbAuthProt.setVisible(true);
					jLabel10.setVisible(true);
					jPfAuthPass.setVisible(true);
					jLabel11.setVisible(true);
					jCmbPrivProt.setVisible(true);
					jLabel12.setVisible(true);
					jPfPrivaPass.setVisible(true);
				} else {
					jLabel3.setVisible(true);
					jTxtCommunity.setVisible(true);
					jLabel7.setVisible(false);
					jCmbSecLev.setVisible(false);
					jLabel8.setVisible(false);
					jTfSecName.setVisible(false);
					jLabel9.setVisible(false);
					jCmbAuthProt.setVisible(false);
					jLabel10.setVisible(false);
					jPfAuthPass.setVisible(false);
					jLabel11.setVisible(false);
					jCmbPrivProt.setVisible(false);
					jLabel12.setVisible(false);
					jPfPrivaPass.setVisible(false);
				}
			}
		});

		jCmbSecLev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("AUTH_NOPRIV".equalsIgnoreCase(jCmbSecLev.getSelectedItem().toString())) {
					jLabel9.setVisible(true);
					jCmbAuthProt.setVisible(true);
					jLabel10.setVisible(true);
					jPfAuthPass.setVisible(true);
					jLabel11.setVisible(false);
					jCmbPrivProt.setVisible(false);
					jLabel12.setVisible(false);
					jPfPrivaPass.setVisible(false);
				} else if ("NOAUTH_NOPRIV".equalsIgnoreCase(jCmbSecLev.getSelectedItem().toString())) {
					jLabel9.setVisible(false);
					jCmbAuthProt.setVisible(false);
					jLabel10.setVisible(false);
					jPfAuthPass.setVisible(false);
					jLabel11.setVisible(false);
					jCmbPrivProt.setVisible(false);
					jLabel12.setVisible(false);
					jPfPrivaPass.setVisible(false);
				} else {
					jLabel9.setVisible(true);
					jCmbAuthProt.setVisible(true);
					jLabel10.setVisible(true);
					jPfAuthPass.setVisible(true);
					jLabel11.setVisible(true);
					jCmbPrivProt.setVisible(true);
					jLabel12.setVisible(true);
					jPfPrivaPass.setVisible(true);
				}
			}
		});
	}

	private void initPanel() {
		for (SnmpVersion ver : SnmpVersion.values())
			jCmbVersion.addItem(ver);
		jCmbVersion.setSelectedIndex(0);
		jCmbSecLev.addItem(Snmp.SAFELEVEL_AUTHPRIV);
		jCmbSecLev.addItem(Snmp.SAFELEVEL_AUTHNOPRIV);
		jCmbSecLev.addItem(Snmp.SAFELEVEL_NOAUTHNOPRIV);
		jCmbSecLev.setSelectedIndex(0);
		jCmbAuthProt.addItem(SnmpTarget.AuthProtocol.MD5.name());
		jCmbAuthProt.addItem(SnmpTarget.AuthProtocol.SHA.name());
		jCmbAuthProt.setSelectedIndex(0);
		jCmbPrivProt.addItem(SnmpTarget.PrivProtocol.DES.name());
		jCmbPrivProt.addItem(SnmpTarget.PrivProtocol.AES128.name());
		jCmbPrivProt.addItem(SnmpTarget.PrivProtocol.AES192.name());
		jCmbPrivProt.addItem(SnmpTarget.PrivProtocol.AES256.name());
		jCmbPrivProt.setSelectedIndex(0);
	}

	/**
	 * 设置初始值
	 * @param param
	 */
	public void setParameter(SnmpMethod param) {
		jSpnPort.setValue(new Integer(param.getPort()));
		jCmbVersion.setSelectedItem(param.getVersion());
		jTxtCommunity.setText(param.getCommunity());
		jSpnTimeout.setValue(new Integer((int) param.getTimeout() / 1000));
		if (param.getVersion() == SnmpVersion.V3) {
			jCmbSecLev.setSelectedItem(param.getSecurityLevel());
			jTfSecName.setText(param.getSecurityName());
			if (Snmp.SAFELEVEL_AUTHPRIV.equalsIgnoreCase(param.getSecurityLevel())) {
				jCmbAuthProt.setSelectedItem(param.getAuthProtocol());
				jPfAuthPass.setText(param.getAuthPassword());
				jCmbPrivProt.setSelectedItem(param.getPrivProtocol());
				jPfPrivaPass.setText(param.getPrivPassword());
			} else if (Snmp.SAFELEVEL_AUTHNOPRIV.equalsIgnoreCase(param.getSecurityLevel())) {
				jCmbAuthProt.setSelectedItem(param.getAuthProtocol());
				jPfAuthPass.setText(param.getAuthPassword());
			}
		}
	}

	/**
	 * 设置Snmp信息
	 */
	protected void setSnmpInfo(SnmpMethod param) {
		param.setPort(snm_port.getNumber().intValue());
		param.setTimeout(snm_timeout.getNumber().intValue() * 1000);
		param.setVersion((SnmpVersion) jCmbVersion.getSelectedItem());
		param.setCommunity(new String(jTxtCommunity.getPassword()).trim());
		if ("v3".equalsIgnoreCase(jCmbVersion.getSelectedItem().toString())) {
			param.setSecurityLevel(jCmbSecLev.getSelectedItem().toString());
			param.setSecurityName(jTfSecName.getText());
			if (Snmp.SAFELEVEL_AUTHPRIV.equalsIgnoreCase(jCmbSecLev.getSelectedItem().toString())) {
				param.setAuthProtocol(jCmbAuthProt.getSelectedItem().toString());
				param.setAuthPassword(new String(jPfAuthPass.getPassword()).trim());
				param.setPrivProtocol(jCmbPrivProt.getSelectedItem().toString());
				param.setPrivPassword(new String(jPfPrivaPass.getPassword()).trim());
			} else if (Snmp.SAFELEVEL_AUTHNOPRIV.equalsIgnoreCase(jCmbSecLev.getSelectedItem().toString())) {
				param.setAuthProtocol(jCmbAuthProt.getSelectedItem().toString());
				param.setAuthPassword(new String(jPfAuthPass.getPassword()).trim());
			}
		}
	}

	/**
	 * 获取编辑结果
	 * @return
	 */
	public SnmpMethod getParameter() {
		SnmpMethod param = new SnmpMethod();
		setSnmpInfo(param);
		return param;
	}

	public boolean verify() {
		if ("v3".equalsIgnoreCase(jCmbVersion.getSelectedItem().toString())) {
			if (new String(jTfSecName.getText()).trim().length() == 0) {
				JOptionPane.showMessageDialog(SnmpBaseConfigPanel.this, "安全名不能为空！", "信息", JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			if (Snmp.SAFELEVEL_AUTHPRIV.equalsIgnoreCase(jCmbSecLev.getSelectedItem().toString())) {
				if (new String(jPfAuthPass.getPassword()).trim().length() < 8) {
					JOptionPane.showMessageDialog(SnmpBaseConfigPanel.this, "用户密码不能小于8位！", "信息", JOptionPane.INFORMATION_MESSAGE);
					return false;
				} else if (new String(jPfPrivaPass.getPassword()).trim().length() < 8) {
					JOptionPane.showMessageDialog(SnmpBaseConfigPanel.this, "私有密码不能小于8位！", "信息", JOptionPane.INFORMATION_MESSAGE);
					return false;
				}
			} else if (Snmp.SAFELEVEL_AUTHNOPRIV.equalsIgnoreCase(jCmbSecLev.getSelectedItem().toString())) {
				if (new String(jPfAuthPass.getPassword()).trim().length() < 8) {
					JOptionPane.showMessageDialog(SnmpBaseConfigPanel.this, "用户密码不能小于8位！", "信息", JOptionPane.INFORMATION_MESSAGE);
					return false;
				}
			}
		} else {
			if (new String(jTxtCommunity.getPassword()).trim().length() == 0) {
				int opt = JOptionPane.showConfirmDialog(this, "Community真的为空吗？", "提问？",
						JOptionPane.YES_NO_OPTION);
				if (opt != JOptionPane.YES_OPTION) {
					jTxtCommunity.requestFocus();
					return false;
				}
			}
			return true;
		}
		return true;
	}

	/**
	 * 在测试SNMP的配置是否成功时，需要得到当前监测节点的IP地址，所以添加了该方法（必须调用）。
	 * 
	 * @param monitorNode 当前的监测节点
	 */
	public void setContext(MonitorMethodConfigContext context) {
		this.context = context;
	}

	private void doTest() {
		if (!verify()) {
			return;
		}

		//判断是否已经设置了监测目标的IP地址
		if (context == null || StringUtil.isNullOrBlank(context.getNode().getIp())) {
			JOptionPane.showMessageDialog(SnmpBaseConfigPanel.this, "获取当前监测节点的IP地址失败，可能没有调用setMonitorNode()方法。", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		int snmpVer = ((SnmpVersion) jCmbVersion.getSelectedItem()).getId();
		Object callResult = null;
		try {
			String hostIpAddr = context.getNode().getIp();
			int snmpPort = snm_port.getNumber().intValue();
			int timeout = snm_timeout.getNumber().intValue() * 1000;
			String community = new String(jTxtCommunity.getPassword());
			String secLev = jCmbSecLev.getSelectedItem().toString();
			String secName = jTfSecName.getText().trim();
			String authProt = jCmbAuthProt.getSelectedItem().toString();
			String authPass = new String(jPfAuthPass.getPassword());
			String privaProt = jCmbPrivProt.getSelectedItem().toString();
			String privaPass = new String(jPfPrivaPass.getPassword());
			callResult = (String) context.getServerFactory().getProbeService()
					.executeMethod(context.getNode().getProbeId(), SnmpTester.class.getName(), "doTest",
							hostIpAddr, snmpPort, snmpVer, timeout, community, secLev, secName, authProt, authPass,
							privaProt, privaPass);
		} catch (Exception e2) {
			ErrorDlg.show("远程探针执行方法失败,ProbeCode:" + context.getNode().getProbeId(), e2);
			return;
		}

		if (callResult == null) {
			JOptionPane.showMessageDialog(this, "SNMP配置测试成功！", "信息", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "SNMP配置测试失败！错误：" + callResult, "错误", JOptionPane.ERROR_MESSAGE);
		}
	}
}
