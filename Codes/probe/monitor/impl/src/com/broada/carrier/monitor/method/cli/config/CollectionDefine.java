package com.broada.carrier.monitor.method.cli.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * CLI collection-define节点对应类
 * @author Eric Liu (liudh@broada.com.cn)
 */
public class CollectionDefine {
  
  private List<CLIConfiguration> cliConfigurations = new ArrayList<CLIConfiguration>();

  /**获取所有的CLI配置项*/
  public List<CLIConfiguration> getCliConfigurations() {
    return cliConfigurations;
  }

  /**添加一个CLI配置项*/
  public void addCliConfiguration(CLIConfiguration cliConfiguration) {
    cliConfigurations.add(cliConfiguration);
  }
  
  String toPrintString() {
    StringBuffer sb = new StringBuffer();
    sb.append("------------ CollectionDefine begin --------------").append("\n");
    for (Iterator<CLIConfiguration> iter = cliConfigurations.iterator(); iter.hasNext();) {
      CLIConfiguration cliconf = (CLIConfiguration)iter.next();
      sb.append(cliconf.toPrintString());
    }
    sb.append("------------ CollectionDefine end   --------------").append("\n");
    return  sb.toString();
  }
}
