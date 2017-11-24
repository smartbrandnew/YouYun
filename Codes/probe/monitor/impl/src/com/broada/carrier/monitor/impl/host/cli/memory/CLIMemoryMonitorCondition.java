package com.broada.carrier.monitor.impl.host.cli.memory;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class CLIMemoryMonitorCondition extends MonitorCondition {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -4557897962311628138L;

  public final static String FIELD_MEMORYUSEDSIZE = "memoryusedsize";

  public final static String FIELD_MEMORYUTIL = "memoryutil";

  public final static String FIELD_VITURALYUTIL = "virtual";

  public CLIMemoryMonitorCondition() {
  }

  public CLIMemoryMonitorCondition(String field, int type, String value) {
    super(field, type, value);
  }

  public String getUnit() {
    return getField().equalsIgnoreCase(FIELD_MEMORYUSEDSIZE) ? "M" : "%";
  }

  public String getFieldDescription() {
    if (getField().equals(FIELD_MEMORYUSEDSIZE)) {
      return "内存使用量";
    } else if (getField().equals(FIELD_MEMORYUTIL)) {
      return "内存使用率";
    } else {
      return "虚拟内存使用率";
    }
  }

  public String getFieldCondition() {
    return "";
  }

  public String getFieldName() {
    return getFieldDescription();
  }
}
