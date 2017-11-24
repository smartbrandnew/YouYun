package com.broada.carrier.monitor.impl.db.oracle.lock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import com.broada.carrier.monitor.impl.common.ui.EditAddrDialog;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

/**
 * <p>Title: OracleLockParamConfiger</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class OracleLockParamConfiger implements MonitorConfiger {  
	private LockParamPanel panel = new LockParamPanel();
	private OracleLockParameter parameter;
	private MonitorConfigContext data;
	
  public class LockParamPanel extends JPanel {
    private static final long serialVersionUID = -127229876458510508L;

    /*
     * @see com.broada.srvmonitor.impl.host.ConditionConfig#getConditions()
     */
    public List getConditions() {

      return new ArrayList();
    }
    
    public void setLockMonitorParam(){
    	parameter.setCTime(ora_ctime.getNumber().intValue());
      if (jChkResource.isSelected()) {
      	parameter.setMatchType(jCmbMatchType.getSelectedIndex());
      	parameter.setResourceName(jTxtResource.getText());
      } else {
      	parameter.removeMatchType();
      	parameter.removeResourceName();
      }
    }

    /*
     * @see com.broada.srvmonitor.impl.host.ConditionConfig#setConditions(java.util.List)
     */
    public void setConditions(List conditions) {
      return;
    }

    /*
     * @see com.broada.srvmonitor.impl.host.ConditionConfig#verify()
     */
    public boolean verify() {
      return true;
    }
    
    public void resetLockInfo() {
      OracleLockParameter param = (OracleLockParameter)parameter;
      if (param.getCTime() >= 0) {
        jSpnCTime.setValue(new Integer(param.getCTime()));
      }
      if (param.getResourceName() != null) {
        jChkResource.setSelected(true);
        int matchType = param.getMatchType();
        if (matchType >= 0 && matchType < jCmbMatchType.getItemCount()) {
          jCmbMatchType.setSelectedIndex(matchType);
        }
        jTxtResource.setText(param.getResourceName());
      } else {
        jChkResource.setSelected(false);
      }
    }
    

    BorderLayout borderLayout1 = new BorderLayout();
    Border border1;
    TitledBorder titledBorder1;
    Border border2;
    JPanel jPanel1 = new JPanel();
    Border border3;
    TitledBorder titledBorder3;
    Border border4;
    Border border5;
    TitledBorder titledBorder4;
    Border border6;
    Border border7;
    Border border8;
    TitledBorder titledBorder5;
    JCheckBox jChkResource = new JCheckBox();
    JComboBox jCmbMatchType = new JComboBox();
    JLabel jLabel4 = new JLabel();
    JTextField jTxtResource = new JTextField();
    JCheckBox jChkCTime = new JCheckBox();
    JLabel jLabel5 = new JLabel();
    JLabel jLabel6 = new JLabel();
    private SpinnerNumberModel ora_ctime = new SpinnerNumberModel(10, 1, 65535, 1);
    JSpinner jSpnCTime = new JSpinner(ora_ctime);
    JLabel jLabel8 = new JLabel();

    public LockParamPanel() {
      try {
        jbInit();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    private void jbInit() throws Exception {
      border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
      titledBorder1 = new TitledBorder(border1,"基本配置");
      border2 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
      border3 = BorderFactory.createLineBorder(Color.white,1);
      titledBorder3 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(148, 145, 140)),"正确判断条件");
      border4 = BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"正确判断条件"),BorderFactory.createEmptyBorder(6,6,6,6));
      border5 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
      titledBorder4 = new TitledBorder(border5,"正确判断条件");
      border6 = BorderFactory.createCompoundBorder(titledBorder4,BorderFactory.createEmptyBorder(6,6,6,6));
      border7 = new TitledBorder(null,"正确判断条件");
      border8 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
      titledBorder5 = new TitledBorder(border8,"正确判断条件");
      this.setLayout(borderLayout1);
      this.setBorder(null);
      
      jPanel1.setBorder(titledBorder5);
      jPanel1.setLayout(null);
      jChkResource.addChangeListener(new javax.swing.event.ChangeListener(){
        public void stateChanged(ChangeEvent e) {
          jChkCondition_stateChanged(e);
        }
      });
      jChkResource.setBounds(new Rectangle(40, 34, 100, 25));
      jChkResource.setVerifyInputWhenFocusTarget(true);
      jChkResource.setActionCommand("监测资源范围");
      jChkResource.setText("监测资源范围");
      jCmbMatchType.setEnabled(false);
      jCmbMatchType.setBounds(new Rectangle(205, 34, 83, 25));
      jLabel4.setIconTextGap(4);
      jLabel4.setText("匹配模式");
      jLabel4.setBounds(new Rectangle(150, 34, 55, 25));
      jTxtResource.setText("");
      jTxtResource.setBackground(Color.white);
      jTxtResource.setDisabledTextColor(Color.black);
      jTxtResource.setEditable(false);
      jTxtResource.setEnabled(false);
      jTxtResource.setBounds(new Rectangle(115, 74, 263, 20));
      jTxtResource.addMouseListener(new MouseAdapter() {
        //点击弹出编辑菜单
        public void mouseClicked(MouseEvent e) {
          if(jChkResource.isSelected()){
            EditAddrDialog ead = new EditAddrDialog(jTxtResource);
            ead.show(jTxtResource, 0,22);
          }
        }
      });
      jChkCTime.setText("锁定时间");
      jChkCTime.setVerticalAlignment(SwingConstants.CENTER);
      jChkCTime.setActionCommand("锁定时间");
      jChkCTime.setSelected(true);
      jChkCTime.setEnabled(false);
      jChkCTime.setVerifyInputWhenFocusTarget(true);
      jChkCTime.setBounds(new Rectangle(40, 109, 79, 25));
      jLabel5.setDebugGraphicsOptions(0);
      jLabel5.setText("小于");
      jLabel5.setBounds(new Rectangle(173, 109, 32, 25));
      jLabel6.setBounds(new Rectangle(288, 109, 25, 23));
      jLabel6.setText("秒");
      jSpnCTime.setBounds(new Rectangle(205, 109, 79, 23));
      jSpnCTime.setOpaque(false);
      jLabel8.setBounds(new Rectangle(57, 71, 55, 25));
      jLabel8.setText("资源名称");
      jLabel8.setIconTextGap(4);
      this.add(jPanel1,  BorderLayout.CENTER);
      jPanel1.add(jChkResource, null);
      jPanel1.add(jLabel4, null);
      jPanel1.add(jCmbMatchType, null);
      jPanel1.add(jLabel5, null);
      jPanel1.add(jSpnCTime, null);
      jPanel1.add(jLabel6, null);
      jPanel1.add(jChkCTime, null);
      jPanel1.add(jLabel8, null);
      jPanel1.add(jTxtResource, null);
      for (int i = 0; i < OracleLockParameter.MATCH_NMAE.length; i++) {
        jCmbMatchType.addItem(OracleLockParameter.MATCH_NMAE[i]);
      }
    }
    
    private void jChkCondition_stateChanged(ChangeEvent e){
      Object obj = e.getSource();
      if (obj == jChkResource) {
        boolean sel = jChkResource.isSelected();
        jCmbMatchType.setEnabled(sel);
        jTxtResource.setEnabled(sel);
      }
    }
  }

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public boolean getData() {
		if (!panel.verify())
			return false;

		panel.setLockMonitorParam();
		data.getTask().setParameterObject(parameter);
		return true;
	}

	@Override
	public void setData(MonitorConfigContext data) {
		this.data = data;
		parameter = data.getTask().getParameterObject(OracleLockParameter.class);
		if (parameter == null)
			parameter = new OracleLockParameter();
		panel.resetLockInfo();
	}

	@Override
	public void setMethod(MonitorMethod method) {		
	}
}
