package com.broada.carrier.monitor.impl.mw.weblogic.snmp.servlet;

import java.util.Comparator;

public class ServletInfoComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    if (o1 == null || !(o1 instanceof WLSServletInfo))
      return 1;
    if (o2 == null || !(o1 instanceof WLSServletInfo))
      return -1;

    return Integer.parseInt(((WLSServletInfo) o2).getAvgTime()) - Integer.parseInt(((WLSServletInfo) o1).getAvgTime());
  }

}
