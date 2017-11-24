package com.broada.carrier.monitor.impl.db.informix.strategy.interceptor.ext;

import com.broada.carrier.monitor.impl.db.informix.strategy.interceptor.StrategyResultIntercetor;

/**
 * @author lixy Sep 5, 2008 11:20:28 AM
 */
public class StrategyResultMinuteIntercetor extends StrategyResultIntercetor {

  @Override
  protected Double doIntercetor(double currValue, double lastValue, double interval) {
    return (currValue - lastValue) / (interval / 60);
  }

}
