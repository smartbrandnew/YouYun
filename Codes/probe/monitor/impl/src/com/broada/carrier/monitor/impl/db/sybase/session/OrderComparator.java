package com.broada.carrier.monitor.impl.db.sybase.session;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.utils.TextUtil;

import java.util.Comparator;

public class OrderComparator implements Comparator {
  private int orderField = FIELD_NAMELEN;
    public static final int FIELD_KEY = 1;
    public static final int FIELD_NAME = 2;
    public static final int FIELD_NAMELEN = 3;

    public OrderComparator(int orderField) {
      this.orderField = orderField;
    }

    /**
     * 根据设定的字段进行比较
     * @param obj1
     * @param obj2
     * @return
     */
    public int compare(Object obj1, Object obj2) {
      MonitorInstance mi1 = (MonitorInstance) obj1;
      MonitorInstance mi2 = (MonitorInstance) obj2;

      if (FIELD_KEY==orderField) {
        int l1 = TextUtil.getRealLen(mi1.getCode());
        int l2 = TextUtil.getRealLen(mi2.getCode());
        if (l1 != l2) {
          return l1 - l2;
        } else {
          return mi1.getCode().compareToIgnoreCase(mi2.getCode());
        }
      } else if (FIELD_NAMELEN==orderField) {
        int l1 = TextUtil.getRealLen(mi1.toString());
        int l2 = TextUtil.getRealLen(mi2.toString());
        if (l1 != l2) {
          return l1 - l2;
        } else {
          return mi1.toString().compareToIgnoreCase(mi2.toString());
        }
      } else {
        return mi1.toString().compareToIgnoreCase(mi2.toString());
      }
    }

  }