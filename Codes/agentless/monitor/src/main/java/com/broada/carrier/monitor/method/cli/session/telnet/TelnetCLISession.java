package com.broada.carrier.monitor.method.cli.session.telnet;

import com.adventnet.cli.transport.TelnetProtocolOptionsImpl;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.session.TSCLISession;

@Deprecated
public class TelnetCLISession extends TSCLISession {

  public TelnetCLISession(){
    cpo = new TelnetProtocolOptionsImpl();
  }
  
  protected CLIException parserException(Throwable e){
    if(e==null){
  		return null;
  	}
  	if(e.getMessage()==null){
  		if(e instanceof CLIException){
      	return (CLIException)e;
  		}else{
  			return new CLIException(e);
  		}
  	}
    if (e.getMessage().indexOf("Session not established") != -1) {
      return new CLIConnectException("没有建立连接", e);
    }
    //如果已经是CLIException，则直接返回
    if(e instanceof CLIException){
    	return (CLIException)e;
    }else{
    	return new CLIException(e.getMessage(),e);
    }
  }

  protected String getTransportClassName() {
    return "com.adventnet.cli.transport.TelnetTransportImpl";
  }
}