package com.broada.carrier.monitor.impl.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.broada.numen.agent.script.entity.DataColumn;
import com.broada.numen.agent.script.entity.DataRow;
import com.broada.utils.Condition;


public class ExtendedRow{
  private Boolean select = Boolean.FALSE;
  private DataRow dataRow = null;
  private DataColumn[] columns = null;
  private boolean hasInstance = false;
  private String instanceName;
  private Map<Integer,MonitorConditionHolder> conditions = new HashMap<Integer,MonitorConditionHolder>();
  public ExtendedRow(DataRow dataRow, Set<DataColumn> columns) {
    this.dataRow = dataRow;
    this.columns = (DataColumn[]) columns.toArray(new DataColumn[columns.size()]);
    if (dataRow.getKey() != null && dataRow.getKey().trim().length() > 0) {
      instanceName = dataRow.getKey().trim();
      hasInstance = true;
    }
  }

  public Boolean getSelect() {
    return select;
  }

  public void setSelect(Boolean select) {
    this.select = select;
  }

  public DataRow getDataRow() {
    return dataRow;
  }

  public DataColumn[] getColumns() {
    return columns;
  }
  
  public boolean isHasInstance() {
    return hasInstance;
  }
  /**
   * 获取表格动态column字段的名称
   * @return 
   */
  public String[] getColumnNames() {
    List<String> colsList = new ArrayList<String>();
    if (hasInstance) {
      colsList.add("选择");
      colsList.add("实例名");
    }
    for (int i = 0; i < columns.length; i++) {
      colsList.add(columns[i].getName());
    }
    return (String[]) colsList.toArray(new String[colsList.size()]);
  }
  /**
   * 获取每行column列对应的值
   * @param columnIndex
   * @return
   */
  public Object getPropValue(int columnIndex) {
    if (hasInstance) {
      columnIndex -= 2;
    }
    String code =columns[columnIndex].getCode() ;
    Object obj = dataRow.getProperty(code);
    return obj;
  }

  public MonitorConditionHolder getCondition(int columnIndex) {
    return conditions.get(columnIndex);
  }
  
  public MonitorConditionHolder createTmpConditon(int columnIndex){
    int oriIndex = columnIndex;
    if (hasInstance) {
      columnIndex -= 2;
    }
    DataColumn column = this.columns[columnIndex];
    MonitorConditionHolder holder = new MonitorConditionHolder(column.getCode(), Condition.LESSTHAN, "");
    holder.setInstance(dataRow.getKey());
    holder.setColumnName(column.getName());
    holder.setUnit(column.getUnit());
    holder.setCurrValue(dataRow.getProperty(column.getCode()));
    holder.setHasInstance(hasInstance);
    holder.setIndex(oriIndex);
    conditions.put(oriIndex, holder);
    return holder;
  }
  
  public String getCurrValue(int columnIndex){
    if (hasInstance) {
      columnIndex -= 2;
    }
    DataColumn column = this.columns[columnIndex];
    if (dataRow == null)
    	return "";
    Object result = dataRow.getProperty(column.getCode());
    if (result == null)
    	return "";
    return result.toString();
  }
  
  public void removeTmpConditon(int columnIndex){
    conditions.remove(columnIndex);
  }
  public MonitorConditionHolder[] getSubConditions() {
    return conditions.values().toArray(new MonitorConditionHolder[conditions.values().size()]);
  }

  public String getInstanceName() {
    return instanceName;
  }
}
