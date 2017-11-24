package com.broada.carrier.monitor.impl.generic.script.session.agent;

import java.net.ConnectException;
import java.util.Properties;

import com.broada.agent.config.HostAgentClient;
import com.broada.carrier.monitor.impl.generic.script.ScriptConstants;
import com.broada.carrier.monitor.impl.generic.script.ScriptException;
import com.broada.carrier.monitor.impl.generic.script.session.ScriptSession;
import com.broada.numen.agent.original.service.OriginalAgent;

public class AgentScriptSession implements ScriptSession{
	
	private Properties options;
	
  public boolean connectAgentSuccessed() throws ScriptException {
    OriginalAgent agent = null;
    String ip=options.getProperty(ScriptConstants.OPTION_REMOTEHOST);
    int port=((Integer)options.get(ScriptConstants.OPTION_REMOTEPORT)).intValue();
    String rmiName = ScriptConstants.RMI_AGENTNAME;
    try {
      agent = HostAgentClient.getHostAgent(ip,port, rmiName);
      //直接通过能否获取OriginalAgent对象来判断连通性
      if(agent != null){
      	return true;
      }
    } catch (Throwable e1) {
    	Throwable cause = e1;
    	while (cause != null) {
    		if (cause instanceof ConnectException)
    			throw new ScriptException("获取远程代理失败,可能是代理没有开启或者网络无法连通,代理="+ip+":"+port+":"+rmiName, e1);
    		cause = cause.getCause();
    	}
      throw new ScriptException("获取远程代理失败,未知异常,代理="+ip+":"+port+":"+rmiName, e1);
    }
    return false;
  }
  
  public void open(Properties options) {
    this.options = options;
  }

  public void close() {
    HostAgentClient.removeAgent(options.getProperty(ScriptConstants.OPTION_REMOTEHOST), ((Integer) (options
        .get(ScriptConstants.OPTION_REMOTEPORT))).intValue(), ScriptConstants.RMI_AGENTNAME);
  }
}
