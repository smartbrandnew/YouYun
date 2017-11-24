package com.broada.carrier.monitor.method.cli.parser;

import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;

public abstract class AbstractCLIResult implements CLIResult {

  protected CLIErrorLine[] errLines;

  public CLIErrorLine[] getErrLines() {
    return errLines;
  }
  
  /**
   * 获取table类型的结果
   * 
   */
  public abstract List<Properties> getListTableResult();
  
  /**
   * 获取properties类型的结果
   * 
   * @return
   */
  public abstract Properties getPropResult();
}
