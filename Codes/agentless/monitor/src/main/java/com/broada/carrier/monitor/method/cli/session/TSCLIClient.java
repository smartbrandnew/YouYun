package com.broada.carrier.monitor.method.cli.session;

import java.util.Observable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adventnet.cli.CLIClient;
import com.adventnet.cli.CLIMessage;
import com.adventnet.cli.CLISession;

@Deprecated
public class TSCLIClient extends Observable implements CLIClient {

  private static final Log logger = LogFactory.getLog(TSCLIClient.class);

  public boolean callback(CLISession session, CLIMessage message, int msgId) {
    if (message != null) {
      setChanged();
      notifyObservers(message.getData());
    } else {
      logger.info(" No Response received ");
      setChanged();
      notifyObservers(null);
    }
    return true;
  }
}
