package com.broada.carrier.monitor.impl.mw.tomcat;

import java.util.ArrayList;
import java.util.List;

/**
 * Tomcat连接器信息
 */
public class TomcatConnector implements Tomcat{
  private String name;

  private TomcatThreadInfo threadInfo;

  private TomcatRequestInfo requestInfo;

  private List<TomcatWorker> workerList = new ArrayList<TomcatWorker>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TomcatRequestInfo getRequestInfo() {
    return requestInfo;
  }

  public void setRequestInfo(TomcatRequestInfo requestInfo) {
    this.requestInfo = requestInfo;
  }

  public TomcatThreadInfo getThreadInfo() {
    return threadInfo;
  }

  public void setThreadInfo(TomcatThreadInfo threadInfo) {
    this.threadInfo = threadInfo;
  }

  public List<TomcatWorker> getWorkerList() {
    return workerList;
  }

  public void addWorker(TomcatWorker tomcatWorker) {
    workerList.add(tomcatWorker);
  }
}
