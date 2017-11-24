package com.broada.carrier.monitor.impl.common.convertor.impl;

import java.text.DecimalFormat;

import com.broada.carrier.monitor.impl.common.convertor.MonResultConvertor;

/**
 * 两位小数格式化器
 */
public class DoubleDecimalFormattor implements MonResultConvertor {
  private static final DecimalFormat formatter = new DecimalFormat("########.##");

  public double doConvert(double srcValue) {
    String tmp = formatter.format(srcValue);
    return new Double(tmp).doubleValue();
  }
}
