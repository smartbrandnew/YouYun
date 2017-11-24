package com.broada.carrier.monitor.method.cli.session;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;
import com.broada.carrier.monitor.method.cli.session.agent.AgentCLISession;
import com.broada.carrier.monitor.method.cli.session.ssh.SSHSession;
import com.broada.carrier.monitor.method.cli.session.telnet.TelnetSession;
import com.broada.carrier.monitor.method.cli.session.wmi.WmiCLISession;

public class CLISessionFactory {
  /**
   * @param sessionName
   * @pdOid d1361b6e-5d3e-48db-8297-badf1a51f9bb
   */
  public static CLISession getCLISession(String sessionName) {
    if (sessionName == null) {
      throw new CLIRuntimeException("没有指定连接方式");
    }
    if (sessionName.equals(CLIConstant.SESSION_TELNET)) {
      return new CLISessionProxy(new TelnetSession());
    } else if (sessionName.equals(CLIConstant.SESSION_SSH)) {
      return new CLISessionProxy(new SSHSession());
    } else if (sessionName.equals(CLIConstant.SESSION_AGENT)) {
      return new CLISessionProxy(new AgentCLISession());
    } else if(sessionName.equals(CLIConstant.SESSION_WMI)){
      return new CLISessionProxy(new WmiCLISession());
    } else {
      throw new CLIRuntimeException("不支持的连接方式：" + sessionName);
    }
  }
}