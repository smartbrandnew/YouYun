package com.broada.carrier.monitor.method.script;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;

public class ScriptMethodConfiger extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;
	private JComboBox cbxExecuteSide = new JComboBox();
	private JLabel lblAgentPort = new JLabel("代理端口：");
	private JSpinner txtAgentPort = new JSpinner();
	private JButton btnTest = new JButton("测试");

	public ScriptMethodConfiger() {
		setLayout(null);

		JLabel lblNewLabel = new JLabel("执行位置：");
		lblNewLabel.setBounds(10, 10, 77, 15);
		add(lblNewLabel);

		cbxExecuteSide.addItemListener(new CbxExecuteSideItemListener());
		cbxExecuteSide.setModel(new DefaultComboBoxModel(ScriptExecuteSide.values()));
		cbxExecuteSide.setBounds(74, 7, 132, 21);
		add(cbxExecuteSide);

		lblAgentPort.setBounds(10, 45, 65, 15);
		add(lblAgentPort);

		txtAgentPort.setBounds(74, 42, 109, 22);
		add(txtAgentPort);
				
		btnTest.addActionListener(new BtnTestActionListener());
		btnTest.setBounds(10, 84, 93, 23);
		add(btnTest);
		
		refreshExecuteSide();
	}	
	
	private void refreshExecuteSide() {
		ScriptExecuteSide side = (ScriptExecuteSide) cbxExecuteSide.getSelectedItem();
		boolean isAgent = side == ScriptExecuteSide.AGENT;
		lblAgentPort.setVisible(isAgent);
		txtAgentPort.setVisible(isAgent);
		btnTest.setVisible(isAgent);
	}
	
	private ScriptMethod getMethod() {
		ScriptMethod sm = new ScriptMethod();
		sm.setExecuteSide((ScriptExecuteSide) cbxExecuteSide.getSelectedItem());
		sm.setAgentPort((Integer) txtAgentPort.getValue());
		sm.verify();
		return sm;
	}

	@Override
	public boolean getData() {		
		getContext().getMethod().set(getMethod());
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		ScriptMethod sm;
		if (method == null)
			sm = new ScriptMethod();
		else if (method instanceof ScriptMethod)
			sm = (ScriptMethod) method;
		else
			sm = new ScriptMethod(method);
		cbxExecuteSide.setSelectedItem(sm.getExecuteSide());
		txtAgentPort.setValue(sm.getAgentPort());
	}

	private class CbxExecuteSideItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			refreshExecuteSide();
		}
	}
	private class BtnTestActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				ScriptMethod method = getMethod();
				if (method == null)
					return;			 
				getContext().getServerFactory().getProbeService().executeMethod(getContext().getNode().getProbeId(), ScriptTest.class.getName(), "test",
						getContext().getNode().getIp(), method);
				JOptionPane.showMessageDialog(ScriptMethodConfiger.this, "测试正确，可以正常连接到Agent");
 			} catch (Exception e2) {
 				ErrorDlg.createErrorDlg(ScriptMethodConfiger.this, "远程探针执行方法失败,ProbeCode:" + getContext().getNode().getProbeId(), e2).setVisible(true);
 			  return;
 			}
		}
	}
}
