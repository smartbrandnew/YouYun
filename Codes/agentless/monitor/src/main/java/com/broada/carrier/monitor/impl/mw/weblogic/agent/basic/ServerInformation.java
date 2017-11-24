package com.broada.carrier.monitor.impl.mw.weblogic.agent.basic;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.mw.weblogic.agent.Property;

public class ServerInformation {
  private static final Log logger = LogFactory.getLog(ServerInformation.class);

  private static final String PROPERTY = "\t<property name=\"{0}\" value=\"{1}\"/>\n";

  private static final String XML_START = "<weblogic>";

  private static final String XML_END = "</weblogic>";

  /** *********************BASIC INFO************************ */
  private boolean listenPortEnabled;

  private boolean sSLListenPortEnabled;

  private String currentDirectory;

  private String weblogicVersion;

  private String state;

  private int listenPort;

  private int sSLListenPort;

  private String listenAddress;

  private int openSocketsCurrentCount;

  private int restartsTotalCount;

  private String healthState;

  /** *********************JVM************************ */
  private long heapSizeCurrent;

  private long heapFreeCurrent;

  private String javaVendor;

  private String javaVersion;

  private String oSName;

  private String oSVersion;

  /** *************************异常************************ */
  private String message;

  private String detail;

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getCurrentDirectory() {
    return currentDirectory;
  }

  public void setCurrentDirectory(String currentDirectory) {
    this.currentDirectory = currentDirectory;
  }

  public String getHealthState() {
    return healthState;
  }

  public void setHealthState(String healthState) {
    this.healthState = healthState;
  }

  public long getHeapFreeCurrent() {
    return heapFreeCurrent;
  }

  public void setHeapFreeCurrent(long heapFreeCurrent) {
    this.heapFreeCurrent = heapFreeCurrent;
  }

  public long getHeapSizeCurrent() {
    return heapSizeCurrent;
  }

  public void setHeapSizeCurrent(long heapSizeCurrent) {
    this.heapSizeCurrent = heapSizeCurrent;
  }

  public String getListenAddress() {
    return listenAddress;
  }

  public void setListenAddress(String listenAddress) {
    this.listenAddress = listenAddress;
  }

  public int getListenPort() {
    return listenPort;
  }

  public void setListenPort(int listenPort) {
    this.listenPort = listenPort;
  }

  public boolean isListenPortEnabled() {
    return listenPortEnabled;
  }

  public void setListenPortEnabled(boolean listenPortEnabled) {
    this.listenPortEnabled = listenPortEnabled;
  }

  public int getOpenSocketsCurrentCount() {
    return openSocketsCurrentCount;
  }

  public void setOpenSocketsCurrentCount(int openSocketsCurrentCount) {
    this.openSocketsCurrentCount = openSocketsCurrentCount;
  }

  public String getJavaVendor() {
    return javaVendor;
  }

  public void setJavaVendor(String javaVendor) {
    this.javaVendor = javaVendor;
  }

  public String getJavaVersion() {
    return javaVersion;
  }

  public void setJavaVersion(String javaVersion) {
    this.javaVersion = javaVersion;
  }

  public String getOSName() {
    return oSName;
  }

  public void setOSName(String name) {
    oSName = name;
  }

  public String getOSVersion() {
    return oSVersion;
  }

  public void setOSVersion(String version) {
    oSVersion = version;
  }

  public int getRestartsTotalCount() {
    return restartsTotalCount;
  }

  public void setRestartsTotalCount(int restartsTotalCount) {
    this.restartsTotalCount = restartsTotalCount;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getWeblogicVersion() {
    return weblogicVersion;
  }

  public void setWeblogicVersion(String weblogicVersion) {
    this.weblogicVersion = weblogicVersion;
  }

  public int getSSLListenPort() {
    return sSLListenPort;
  }

  public void setSSLListenPort(int listenPort) {
    sSLListenPort = listenPort;
  }

  public boolean isSSLListenPortEnabled() {
    return sSLListenPortEnabled;
  }

  public void setSSLListenPortEnabled(boolean listenPortEnabled) {
    sSLListenPortEnabled = listenPortEnabled;
  }

  public String toXMLString() throws Exception {
    StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    buffer.append(XML_START);
    Map describe = BeanUtils.describe(this);

    Iterator iter = describe.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      String value = (String) describe.get(key);
      buffer.append(MessageFormat.format(PROPERTY, new Object[] { key, value }));
    }
    buffer.append(XML_END);
    return buffer.toString();
  }

  public void addProperty(Property ejbProperty) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("==>添加:" + ejbProperty.getName() + "==" + ejbProperty.getValue());
    }
    if (PropertyUtils.isWriteable(this, ejbProperty.getName()))
      BeanUtils.setProperty(this, ejbProperty.getName(), ejbProperty.getValue());
  }
}
