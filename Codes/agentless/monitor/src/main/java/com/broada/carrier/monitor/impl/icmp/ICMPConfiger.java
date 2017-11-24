package com.broada.carrier.monitor.impl.icmp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.impl.common.BaseMonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

/**
 * ICMP 监测类型的监测参数配置界面
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Eric Liu
 * @version 1.0
 */

public class ICMPConfiger extends BaseMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private static SpinnerNumberModel snm_timeout = new SpinnerNumberModel(100, 10, 10000, 10);
	private static SpinnerNumberModel snm_count = new SpinnerNumberModel(3, 1, 5, 1);
	private static SpinnerNumberModel snm_interval = new SpinnerNumberModel(1, 0, 3, 1);
	private static SpinnerNumberModel snm_ttlavglimit = new SpinnerNumberModel(50, 5, 10000, 5);

	private BorderLayout borderLayout1 = new BorderLayout();
	private JTabbedPane jPanBody = new JTabbedPane();
	JPanel jPanParam = new JPanel();
	JLabel jLabel1 = new JLabel();
	JSpinner jSpnCount = new JSpinner(snm_count);
	TitledBorder titledBorder1;
	/*对应编辑的参数*/
	private ICMPParameter param = new ICMPParameter();
	private JLabel jLabUsername = new JLabel();
	JSpinner jSpnInterval = new JSpinner(snm_interval);
	JLabel jLabUsername1 = new JLabel();
	JSpinner jSpnTimeLimit = new JSpinner(snm_ttlavglimit);
	JLabel jLabel4 = new JLabel();
	JLabel jLabUsername2 = new JLabel();
	JLabel jLabUsername3 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JLabel jLabUsername4 = new JLabel();
	JLabel jLabUsername5 = new JLabel();
	JSpinner jSpnTimeout = new JSpinner(snm_timeout);
	JLabel jLabel5 = new JLabel();
	JLabel jLabUsername6 = new JLabel();
	JLabel jLabUsername7 = new JLabel();
	JLabel jLabUsername8 = new JLabel();
	JLabel jLabUsername9 = new JLabel();
	JPanel jPanExt = new JPanel();
	JPanel jPanRule = new JPanel();
	TitledBorder titledBorder2;
	JRadioButton jRabLoose = new JRadioButton();
	JRadioButton jRabStrict = new JRadioButton();
	ButtonGroup ruleGroup = new ButtonGroup();
	private AddressTablePanel addrPan = new AddressTablePanel();
	private BorderLayout borderLayout2 = new BorderLayout();

	public ICMPConfiger() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
				white, new Color(148, 145, 140)), "扩展监测地址表");
		titledBorder2 = new TitledBorder("");
		this.setLayout(borderLayout1);
		jPanParam.setLayout(null);
		jLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
		jLabel1.setText("请求次数");
		jLabel1.setBounds(new Rectangle(-14, 81, 93, 23));
		jSpnCount.setOpaque(false);
		jSpnCount.setBounds(new Rectangle(88, 80, 77, 24));
		jPanParam.setMinimumSize(new Dimension(1, 1));
		jPanParam.setPreferredSize(new Dimension(1, 200));
		jPanParam.setRequestFocusEnabled(true);
		jLabUsername.setOpaque(false);
		jLabUsername.setPreferredSize(new Dimension(33, 25));
		jLabUsername.setHorizontalAlignment(SwingConstants.TRAILING);
		jLabUsername.setText("请求间隔");
		jLabUsername.setBounds(new Rectangle(210, 83, 74, 16));
		jSpnInterval.setOpaque(false);
		jSpnInterval.setBounds(new Rectangle(290, 79, 77, 24));
		jLabUsername1.setBounds(new Rectangle(-1, 154, 84, 16));
		jLabUsername1.setText("平均响应小于");
		jLabUsername1.setPreferredSize(new Dimension(33, 25));
		jLabUsername1.setHorizontalAlignment(SwingConstants.TRAILING);
		jSpnTimeLimit.setOpaque(false);
	    jSpnTimeLimit.setBounds(new Rectangle(89, 152, 77, 24));
		jLabel4.setBounds(new Rectangle(86, 105, 354, 35));
		jLabel4.setText("<html>在较大的网络,发出多次ICMP请求可以减少单次造成的误差.");
		jLabel4.setHorizontalAlignment(SwingConstants.LEADING);
		jLabUsername2.setPreferredSize(new Dimension(33, 25));
		jLabUsername2.setText("毫秒");
		jLabUsername2.setBounds(new Rectangle(169, 158, 32, 16));
		jLabUsername3.setBounds(new Rectangle(374, 83, 32, 16));
		jLabUsername3.setText("秒");
		jLabUsername3.setOpaque(false);
		jLabUsername3.setPreferredSize(new Dimension(33, 25));
		jLabel6.setBounds(new Rectangle(90, 176, 329, 53));
		jLabel6.setText("<html>统计成功请求的平均延时,如果超出平均响应时间认为链路不稳定,系统也将进行告警.");
		jLabel6.setRequestFocusEnabled(true);
		jLabel6.setHorizontalAlignment(SwingConstants.LEADING);
		jLabUsername4.setBounds(new Rectangle(168, 12, 32, 16));
		jLabUsername4.setText("毫秒");
		jLabUsername4.setPreferredSize(new Dimension(33, 25));
		jLabUsername5.setHorizontalAlignment(SwingConstants.TRAILING);
		jLabUsername5.setPreferredSize(new Dimension(33, 25));
		jLabUsername5.setText("请求超时");
		jLabUsername5.setBounds(new Rectangle(-5, 12, 84, 16));
		jSpnTimeout.setBounds(new Rectangle(88, 9, 77, 24));
		jSpnTimeout.setOpaque(false);
		jLabel5.setHorizontalAlignment(SwingConstants.LEADING);
		jLabel5.setText("<html>如果ICMP请求没有在该超时时间内返回,则认为请求失败.");
		jLabel5.setBounds(new Rectangle(88, 32, 354, 35));
		jLabUsername6.setOpaque(false);
		jLabUsername6.setPreferredSize(new Dimension(33, 25));
		jLabUsername6.setText("（0-3 秒）");
		jLabUsername6.setBounds(new Rectangle(391, 83, 99, 16));
		jLabUsername7.setBounds(new Rectangle(171, 84, 102, 16));
		jLabUsername7.setText("（1-5次）");
		jLabUsername7.setPreferredSize(new Dimension(33, 25));
		jLabUsername7.setRequestFocusEnabled(true);
		jLabUsername8.setRequestFocusEnabled(true);
		jLabUsername8.setPreferredSize(new Dimension(33, 25));
		jLabUsername8.setText("（10-10000毫秒）");
		jLabUsername8.setBounds(new Rectangle(205, 12, 99, 16));
		jLabUsername9.setBounds(new Rectangle(204, 157, 211, 16));
		jLabUsername9.setText("（10-10000毫秒,并小于请求超时）");
		jLabUsername9.setPreferredSize(new Dimension(33, 25));
		jLabUsername9.setRequestFocusEnabled(true);
		jPanRule.setBorder(titledBorder2);
		jPanRule.setPreferredSize(new Dimension(20, 70));
		titledBorder2.setTitle("判断规则");
		titledBorder2.setBorder(BorderFactory.createEtchedBorder());
		jRabLoose.setSelected(true);
		jRabLoose.setOpaque(false);
		jRabLoose.setToolTipText("");
		jRabLoose.setText("只要主目标地址和扩展监测地址中有一个能测试通过就算目标连通");
		jRabLoose.setBounds(new Rectangle(18, 21, 425, 18));
		jRabStrict.setText("只有主目标地址和所有扩展监测地址都能测试通过才算目标连通");
		jRabStrict.setBounds(new Rectangle(18, 42, 425, 18));
		addrPan.setBorder(titledBorder1);
		jPanExt.setLayout(borderLayout2);
		jPanBody.add(jPanParam, "基本设置");
		jPanBody.add(jPanExt, "扩展配置");
		this.add(jPanBody, BorderLayout.CENTER);
		jPanParam.add(jSpnTimeout, null);
		jPanParam.add(jLabel5, null);
		jPanParam.add(jLabUsername8, null);
		jPanParam.add(jLabUsername5, null);
		jPanParam.add(jLabel1, null);
		jPanParam.add(jSpnCount, null);
		jPanParam.add(jLabUsername7, null);
		jPanParam.add(jLabel4, null);
		jPanParam.add(jLabel6, null);
		jPanParam.add(jLabUsername9, null);
		jPanParam.add(jLabUsername1, null);
		jPanParam.add(jSpnTimeLimit, null);
		jPanParam.add(jLabUsername, null);
		jPanParam.add(jSpnInterval, null);
		jPanParam.add(jLabUsername3, null);
		jPanParam.add(jLabUsername6, null);
		jPanParam.add(jLabUsername2, null);
		jPanParam.add(jLabUsername4, null);
		jPanExt.add(jPanRule, BorderLayout.NORTH);
		jPanRule.add(jRabLoose, null);
		jPanRule.add(jRabStrict, null);
		jPanExt.add(addrPan, BorderLayout.CENTER);
		jPanRule.setLayout(null);
		ruleGroup.add(jRabLoose);
		ruleGroup.add(jRabStrict);
	}
	
	@Override
	public void setData(MonitorConfigContext data) {
		super.setData(data);
		param = new ICMPParameter(getTask().getParameter());
		jSpnTimeout.setValue(new Integer(param.getTimeout()));
		jSpnCount.setValue(new Integer(param.getRequestCount()));
		jSpnInterval.setValue(new Integer(param.getRequestInterval()));
		jSpnTimeLimit.setValue(new Integer(param.getTTLAvgLimit()));
		if (param.getRuleType() == ICMPParameter.RULE_STRICT) {
			jRabStrict.setSelected(true);
		}
		addrPan.setAddressTable(param.getExtAddrs());
	}

	@Override
	public boolean getData() {		
		param.setTimeout(snm_timeout.getNumber().intValue());
		param.setRequestCount(snm_count.getNumber().intValue());
		param.setRequestInterval(snm_interval.getNumber().intValue());
		param.setTTLAvgLimit(snm_ttlavglimit.getNumber().intValue());
		if (jRabStrict.isSelected()) {
			param.setRuleType(ICMPParameter.RULE_STRICT);
		} else {
			param.setRuleType(ICMPParameter.RULE_LOOSE);
		}
		param.setExtAddrs(addrPan.getAddressTable());
		getTask().setParameter(param.encode());
		return true;
	}
}
