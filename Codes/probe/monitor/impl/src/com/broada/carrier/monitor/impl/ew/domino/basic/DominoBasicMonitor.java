package com.broada.carrier.monitor.impl.ew.domino.basic;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import java.io.Serializable;

/**
 * 因为原来的DominoBasicMonitor不是面向单例编写的,而监测器是单例的,所以把原来的
 * DominoBasicMonitor修改为DominoBasicMonitorExecuter,每次实例化一个出来
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-10-20 下午06:39:13
 */
public class DominoBasicMonitor extends BaseMonitor {

  @Override public Serializable collect(CollectContext context) {
    DominoBasicMonitorExecuter executer=new DominoBasicMonitorExecuter();
    try{
      return executer.collect(context);
    }finally{
      executer.recycleAll();
    }
  }
}
