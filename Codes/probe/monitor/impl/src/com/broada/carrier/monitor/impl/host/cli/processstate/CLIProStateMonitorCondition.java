package com.broada.carrier.monitor.impl.host.cli.processstate;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class CLIProStateMonitorCondition extends MonitorCondition {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -3468797979078050045L;

  private Boolean select = Boolean.FALSE;

  private boolean validate = true;


  /*
   * 虚拟内存占用
   */
  private String vsize;
  private String currentVsize;
  /*
   * 启动时间，持续时间
   */
  private String currentLstart;
  private String currentEtime;

	public CLIProStateMonitorCondition() {    
  }

  public Boolean getSelect() {
    return select;
  }

  public void setSelect(Boolean select) {
    this.select = select;
  }

  public void setSelect(String select) {
    this.select = select.equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE;
  }

  public boolean isValidate() {
    return validate;
  }

  public void setValidate(boolean validate) {
    this.validate = validate;
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof CLIProStateMonitorCondition)) {
      return false;
    }
    return getField().equals(((CLIProStateMonitorCondition) obj).getField());
  }

  public String getUnit() {
    return "KB";
  }

  public void setField(String field) {
    super.setField(field);
  }

 public String getFieldCondition() {
    return "的运行情况。";
  }
  
  /**
   * gets and sets
   * 
   */
	public String getCurrentVsize() {
		return currentVsize;
	}

	public void setCurrentVsize(String currentVsize) {
		this.currentVsize = currentVsize;
	}
	
  public String getVsize() {
		return vsize;
	}

	public void setVsize(String vsize) {
		this.vsize = vsize;
	}

  public String getCurrentLstart() {
		return currentLstart;
	}

	public void setCurrentLstart(String currentLstart) {
		this.currentLstart = currentLstart;
	}

	public String getCurrentEtime() {
		return currentEtime;
	}

	public void setCurrentEtime(String currentEtime) {
		this.currentEtime = currentEtime;
	}
}
