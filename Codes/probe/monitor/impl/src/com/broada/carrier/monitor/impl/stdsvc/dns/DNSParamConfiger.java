package com.broada.carrier.monitor.impl.stdsvc.dns;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.broada.carrier.monitor.impl.common.NumenMonitorConfiger;
import com.broada.carrier.monitor.impl.common.ui.SimpleNotePanel;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.swing.util.ErrorDlg;

/**
 * DNS 监测类型的监测参数配置界面
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class DNSParamConfiger extends NumenMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private SimpleNotePanel notePanel = new SimpleNotePanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private ConditionPanel conditionPanel = new ConditionPanel();

  /*对应编辑的参数*/
  private DNSParameter param = new DNSParameter();
  JCheckBox jChkRun = new JCheckBox();
  JCheckBox jChkParse = new JCheckBox();
  JTextField jTxtName = new JTextField();

  public DNSParamConfiger() {
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    /*titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
        white, new Color(148, 145, 140)), "正常判断条件");*/
    this.setLayout(borderLayout1);
    notePanel.setNote("监测指定的DNS服务是否正常，可以设定端口、超时时间" +
                      "和正常判断条件.默认情况只监测服务是否运行并能回应正确的" +
                      "消息包，如果要精确测试能否正确解析某个域名请指定域名.");
    this.add(notePanel, BorderLayout.SOUTH);
    this.add(conditionPanel,BorderLayout.CENTER);
  }

  /**
   * 设置初始值
   * @param xml
   */
  public void setParameters(String xml) {
    param = new DNSParameter(xml);
    conditionPanel.setDnsConditions(param.getConditions());
  }

  /**
   * 获取编辑结果
   * @return
   */
  
  public String getParameters() {
		param.removeAllCondition();
		List<DNSMonitorCondition> conditions = conditionPanel.getDnsConditions();
		for (int index = 0; index < conditions.size(); index++){
			param.setCondition((DNSMonitorCondition) conditions.get(index));
		}
		//return param.getParameters();
		try { return param.encode(); }
		 catch (Exception ex) { ErrorDlg.createErrorDlg(this, "错误",
		 "DNS服务监测参数转换成字符串版本错误.", true, ex).setVisible(true); return ""; }
	}
  
  public boolean verify() {
	  List<DNSMonitorCondition> conditions = conditionPanel.getDnsConditions();
	    if(conditions.size()==0){
	      JOptionPane.showMessageDialog(this,"请至少监测一个要解析的域名,否则这个监测任务没有任何意义.");
	      return false;
	    }
	    return true;
  }

  public Component getConfigUI() {
    return this;
  }
  
  /*
   * 
   * @see com.broada.carrier.monitor.impl.stdsvc.DefaultParamConfigPanel#getMonitorInstances()
   */
  public MonitorInstance[] getMonitorInstances() {
    List<MonitorInstance> insts=new ArrayList<MonitorInstance>();
    List<DNSMonitorCondition> conditions = conditionPanel.getDnsConditions();
    Iterator<DNSMonitorCondition> iter = conditions.iterator();
    while(iter.hasNext()) {
      DNSMonitorCondition condition = (DNSMonitorCondition) iter.next();
      MonitorInstance mi = new MonitorInstance();
      mi.setInstanceKey(condition.getField());
      mi.setInstanceName(condition.getField());
      insts.add(mi);
    }
    return (MonitorInstance[]) insts.toArray(new MonitorInstance[insts.size()]);
  }

  void jChkParse_itemStateChanged(ItemEvent e) {
    jTxtName.setEnabled(jChkParse.isSelected());
  }
}