package com.broada.carrier.monitor.method.cli.session;

import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.error.CLIException;

public abstract class AbstractCLISession implements CLISession { 
  protected Properties options;

  public boolean hasContext() {
    return true;
  }
  
  public List<String> runSQL(String cmd, String[] args,StringBuffer localBuf) throws CLIException{
    throw new CLIException("本方法仅供db2 agent调用");
  }
}