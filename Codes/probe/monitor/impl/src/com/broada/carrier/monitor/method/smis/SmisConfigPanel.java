package com.broada.carrier.monitor.method.smis;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.sblim.wbem.cim.CIMInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;
import com.broada.cid.action.protocol.common.HttpProtocolType;
import com.broada.cid.action.protocol.impl.smis.SmisProtocol;
import com.broada.cid.action.protocol.impl.smis.SmisSession;
import com.broada.utils.FieldValidator;
import com.broada.utils.StringUtil;


/**
 * smis协议配置面板
 * @author ly
 *
 */
public class SmisConfigPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(SmisConfigPanel.class);
	private static final long serialVersionUID = 1L;
	private static final String[] PROTOCOL_VERSIONS = {"SSL", "SSLv1", "SSLv3", "TLS",  "TLSv1", "TLSv1.1", "TLSv1.2", "SSL_TLS", "SSL_TLSv2"};
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JLabel jLabel7 = new JLabel();
	JTextField jTxtDeviceIp = new JTextField("");
	JTextField jTxtUserName = new JTextField("administrator");
	JPasswordField jPwdPassword = new JPasswordField();
	JTextField jTxtNamespace = new JTextField("root/cimv2");
	JTextField jTxtPort = new JTextField("5988");
	JComboBox jComboProtocol = new JComboBox(new Object[] {HttpProtocolType.http, HttpProtocolType.https});
	JComboBox jProtocolVersion = new JComboBox(PROTOCOL_VERSIONS);
	JButton jButtonTest = new JButton(" 测 试 ");
	private MonitorMethodConfigContext context;

	public SmisConfigPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		this.setLayout(null); 

		jLabel1.setText("用户名");
		jLabel1.setBounds(new Rectangle(43, 30, 50, 22));
		jTxtUserName.setBounds(new Rectangle(149, 30, 115, 22));

		jLabel2.setText("密码");
		jLabel2.setBounds(new Rectangle(43, 60, 50, 22));
		jPwdPassword.setBounds(new Rectangle(149, 60, 115, 22));
		
		jLabel3.setText("命名空间");
		jLabel3.setBounds(new Rectangle(43, 90, 50, 22));
		jTxtNamespace.setBounds(new Rectangle(149, 90, 115, 22));
		
		jLabel4.setText("端口");
		jLabel4.setBounds(new Rectangle(43, 120, 50, 22));
		jTxtPort.setBounds(new Rectangle(149, 120, 115, 22));
		
		jLabel5.setText("连接协议");
		jLabel5.setBounds(new Rectangle(43, 150, 50, 22));
		jComboProtocol.setBounds(new Rectangle(149, 150, 115, 22));
		
		jLabel6.setText("协议版本");
		jLabel6.setBounds(new Rectangle(43, 180, 50, 22));
		jProtocolVersion.setBounds(new Rectangle(149, 180, 115, 22));
		
		jLabel7.setText("Provider IP");
		jLabel7.setBounds(new Rectangle(43, 210, 50, 22));
		jTxtDeviceIp.setBounds(new Rectangle(149, 210, 115, 22));

		jButtonTest.setBounds(new Rectangle(43, 240, 80, 24));
		
		this.add(jLabel1);
		this.add(jLabel2);
		this.add(jLabel3);
		this.add(jLabel4);
		this.add(jLabel5);
		this.add(jLabel6);
		this.add(jLabel7);
		this.add(jTxtDeviceIp);
		this.add(jTxtUserName);
		this.add(jPwdPassword);
		this.add(jTxtNamespace);
		this.add(jTxtPort);
		this.add(jComboProtocol);
		this.add(jButtonTest);
		this.add(jProtocolVersion);
		this.setVisible(true);
		this.setBorder(new TitledBorder("登录配置"));
		
		jButtonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doTest();
			}
		});
	}

	public boolean verify() {
		return FieldValidator.textRequired(this, jTxtUserName, "用户名") && 
				FieldValidator.textRequired(this, jPwdPassword, "密码") &&
				FieldValidator.textRequired(this, jTxtNamespace, "命名空间") &&
				FieldValidator.textRequired(this, jTxtPort, "端口") &&
				FieldValidator.textRequired(this, jTxtDeviceIp, "Provider IP");
	}

	/**
	 * 在测试SMI-S的配置是否成功时，需要得到当前监测节点的IP地址，所以添加了该方法（必须调用）。
	 * 
	 * @param monitorNode 当前的监测节点
	 */
	public void setContext(MonitorMethodConfigContext context) {
		this.context = context;
	}
	
	public void setOption(SmisMethod smisMethod) {
		if (StringUtils.isNotBlank(smisMethod.getUsername())) {
			this.jTxtUserName.setText(smisMethod.getUsername());
		}
		
		if (StringUtils.isNotBlank(smisMethod.getPassword())) {
			this.jPwdPassword.setText(smisMethod.getPassword());
		}
		
		if (StringUtils.isNotBlank(smisMethod.getNamespace())) {
			this.jTxtNamespace.setText(smisMethod.getNamespace());
		}

		this.jTxtPort.setText(String.valueOf(smisMethod.getPort()));
		
		if (StringUtils.isNotBlank(smisMethod.getProtocol())) {
			this.jComboProtocol.setSelectedItem(smisMethod.getProtocol());
		}
		
		if (StringUtils.isNotBlank(smisMethod.getProtocol())) {
			this.jComboProtocol.setSelectedItem(HttpProtocolType.valueOf(smisMethod.getProtocol()));
		}
		
		if (StringUtils.isNotBlank(smisMethod.getPrivProtocol())) {
			this.jProtocolVersion.setSelectedItem(smisMethod.getPrivProtocol());
		}
		
		if (StringUtils.isNotBlank(smisMethod.getPrivIp())) {
			this.jTxtDeviceIp.setText(smisMethod.getPrivIp());
		}

	}
	
	public SmisMethod getOptions() {
		SmisMethod option = new SmisMethod();
		option.setUsername(this.jTxtUserName.getText());
		option.setPassword(String.valueOf(this.jPwdPassword.getPassword()));
		option.setPort(NumberUtils.toInt(this.jTxtPort.getText()));
		option.setNamespace(this.jTxtNamespace.getText());
		option.setProtocol(this.jComboProtocol.getSelectedItem().toString());
		option.setPrivProtocol(this.jProtocolVersion.getSelectedItem().toString());
		option.setPrivIp(this.jTxtDeviceIp.getText().toString());
	    return option;
	  }


	private void doTest() {
		if (!verify()) {
			return;
		}

		//判断是否已经设置了监测目标的IP地址
		if (context == null || StringUtil.isNullOrBlank(this.jTxtDeviceIp.getText().toString())) {
			JOptionPane.showMessageDialog(SmisConfigPanel.this, "获取当前监测节点的IP地址失败，可能没有调用setMonitorNode()方法。", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String proctocol = this.jComboProtocol.getSelectedItem().toString();
		String protocolVersion = this.jProtocolVersion.getSelectedItem().toString();
		
		if (HttpProtocolType.valueOf(proctocol) == HttpProtocolType.http) {
			try {
				connect(proctocol, null);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "SMI-S配置测试失败！错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			
			try {
				connect(proctocol, protocolVersion);
				this.jProtocolVersion.setSelectedItem(protocolVersion);
			} catch (Exception e) {
				String message = e.getMessage();
				logger.debug("smis协议测试失败，协议版本：{}，错误消息:{}", protocolVersion, message);
				JOptionPane.showMessageDialog(this, "SMI-S配置测试失败！错误：" + message, "错误", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void connect(String proctocol, String proVersion) {
		String ipaddr = this.jTxtDeviceIp.getText();
		String userName = this.jTxtUserName.getText();
		String password = new String(this.jPwdPassword.getPassword());
		String namespace = this.jTxtNamespace.getText();
		int port = NumberUtils.toInt(this.jTxtPort.getText(), 5988);
		SmisProtocol smisProtocol = new SmisProtocol(HttpProtocolType.valueOf(proctocol) == HttpProtocolType.http ? false : true, ipaddr, port, userName, password, namespace, proVersion);
		SmisSession smisSession = new SmisSession(smisProtocol);
		smisSession.connect();
		List<CIMInstance> cims =smisSession.getInstancesByClass("CIM_System");
		JOptionPane.showMessageDialog(this, "SMI-S配置测试成功!", "信息", JOptionPane.INFORMATION_MESSAGE);
	}
}
