package com.broada.carrier.monitor.impl.stdsvc.tcp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import com.broada.carrier.monitor.impl.common.NumenMonitorConfiger;
import com.broada.carrier.monitor.impl.common.ui.SimpleNotePanel;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;

/**
 * TCP 监测类型的监测参数配置界面
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class TCPParamConfiger extends NumenMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private SimpleNotePanel notePanel = new SimpleNotePanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private ConditionPanel conditionPanel = new ConditionPanel();
  /*对应编辑的参数*/
  private TCPParameter param = new TCPParameter();

  public TCPParamConfiger() {
    try {
      jbInit();
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    
    this.setLayout(borderLayout1);
    notePanel.setNote("监测指定的TCP端口是否打开,可以设定超时时间、失败重试次数" +
                      "和正常判断条件.当正常判断条件选择\"端口Down\"表示如果端口" +
                      "打开则异常,反之则表示正常,主要用于监测某些危险端口是否打开.");
    
    this.add(notePanel, BorderLayout.SOUTH);
    this.add(conditionPanel,BorderLayout.CENTER);
  }

  /**
   * 设置初始值
   * @param xml
   */
  public void setParameters(String xml) {
    param = new TCPParameter(xml);
    conditionPanel.setTcpConditions(param.getConditions());
  }

  /**
   * 获取编辑结果
   * @return
   */
  public String getParameters() {
    param.removeAllCondition();
    List<TCPMonitorCondition> conditions = conditionPanel.getTcpConditions();
    for(int index = 0; index < conditions.size(); index++)
      param.setCondition((TCPMonitorCondition) conditions.get(index));
    return param.encode();
  }

  public boolean verify() {
    List<TCPMonitorCondition> conditions = conditionPanel.getTcpConditions();
    if(conditions.size()==0){
      JOptionPane.showMessageDialog(this,"请至少监测一个TCP端口,否则这个监测任务没有任何意义.");
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
    List<TCPMonitorCondition> conditions = conditionPanel.getTcpConditions();
    Iterator<TCPMonitorCondition> iter = conditions.iterator();
    while(iter.hasNext()) {
      TCPMonitorCondition condition = (TCPMonitorCondition) iter.next();
      MonitorInstance mi = new MonitorInstance();
      mi.setInstanceKey(condition.getField());
      mi.setInstanceName(condition.getField());
      insts.add(mi);
    }
    return (MonitorInstance[]) insts.toArray(new MonitorInstance[insts.size()]);
  }
  
}