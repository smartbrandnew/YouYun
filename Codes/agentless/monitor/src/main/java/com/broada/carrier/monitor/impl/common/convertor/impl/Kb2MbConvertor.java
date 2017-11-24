package com.broada.carrier.monitor.impl.common.convertor.impl;

import com.broada.carrier.monitor.impl.common.convertor.MonResultConvertor;

/**
 * 千字节到兆字节转换器
 */
public class Kb2MbConvertor implements MonResultConvertor {
  public double doConvert(double srcValue) {
    return srcValue / 1024;
  }
}
