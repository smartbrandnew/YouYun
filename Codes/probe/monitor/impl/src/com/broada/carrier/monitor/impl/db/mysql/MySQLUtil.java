package com.broada.carrier.monitor.impl.db.mysql;

import java.text.DecimalFormat;

public class MySQLUtil {
  /**
   * 字节转换成兆，保留2位小数。输入是字节
   * 
   * @param b
   * @return
   */
  public static String b2M(long b) {
    float m = (float)b / 1048576L;
    
    DecimalFormat format = new DecimalFormat("#.");
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(2);
    return format.format(m);
  }
}
