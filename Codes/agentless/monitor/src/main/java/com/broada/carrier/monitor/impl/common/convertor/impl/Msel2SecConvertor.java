package com.broada.carrier.monitor.impl.common.convertor.impl;

import com.broada.carrier.monitor.impl.common.convertor.MonResultConvertor;

/**
 * 毫秒转换为秒
 * 
 * @author lixy Oct 13, 2008 11:21:33 AM
 */
public class Msel2SecConvertor implements MonResultConvertor {

  public double doConvert(double srcValue) {
    return srcValue / 1000;
  }

}
