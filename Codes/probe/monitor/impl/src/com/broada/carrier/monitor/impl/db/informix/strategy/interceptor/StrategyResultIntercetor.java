package com.broada.carrier.monitor.impl.db.informix.strategy.interceptor;

import com.broada.carrier.monitor.impl.db.informix.strategy.entity.StrategyLastResult;

import java.util.Date;


/**
 * 策略结果后拦截器
 * 
 * @author lixy Sep 5, 2008 9:58:38 AM
 */
public abstract class StrategyResultIntercetor {

  public static final Double REPEAT_MONIOTR_VALUE = new Double(-1);

  /**
   * 如果缓存内没有上一次监测值或监测值状态反转的时候，将返回REPEAT_MONIOTR_VALUE
   * 
   * @param srvId
   * @param itemCode
   * @param value
   * @return
   */
  public Double intercetor(String srvId, String itemCode, Double value) {
    Date occurTime = StrategyLastResult.getOccurTime(srvId, itemCode);
    Double lastValue = StrategyLastResult.getValue(srvId, itemCode);
    if (occurTime == null || lastValue == null) {
      return REPEAT_MONIOTR_VALUE;
    }

    // 状态反转的时候，返回-1
    if (value.doubleValue() < lastValue.doubleValue()) {
      return REPEAT_MONIOTR_VALUE;
    }

    double interval = (new Date().getTime() - occurTime.getTime()) / 1000;
    if (interval < 1)
      interval = 1;
    return this.doIntercetor(value.doubleValue(), lastValue.doubleValue(), interval);
  }

  /**
   * 上次监测与当前时间的间隔的秒数
   * @param interval
   */
  protected abstract Double doIntercetor(double currValue, double lastValue, double interval);
}
