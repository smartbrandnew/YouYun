package com.broada.carrier.monitor.impl.db.oracle.lock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.broada.utils.TextUtil;

/**
 * <p>Title: OracleLockTextUtil</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class OracleLockRules {

  private List locks;
  
  private List lockedRes; //被锁定的资源列表

  private String resource;

  private int matchType;

  private StringBuffer msg = new StringBuffer(); //规则匹配信息

  private StringBuffer val = new StringBuffer(); //规则匹配告警信息

  private boolean wonted = true; //规则匹配结果

  public OracleLockRules(List locks, String resource, int matchType) {
    this.locks = locks;
    this.resource = resource;
    this.matchType = matchType;
  }

  public OracleLockRules(List locks) {
    this.locks = locks;
    this.matchType = -1;
  }

  public boolean matchAllRecords() {
    if (locks != null && locks.size() > 0) {
      for (Iterator itr = locks.iterator(); itr.hasNext();) {
        OracleLock ol = (OracleLock) itr.next();
        if (matchRecord(ol)) {
          if (lockedRes == null)
            lockedRes = new ArrayList();
          lockedRes.add(lockedRes.size(), ol);
          //          msg.append("被锁对象:" + ol.getObjName() + ";类型:" + ol.getObjType() + ";所属用户:" + ol.getOwner() + ";锁定时间:"
          //              + ol.getCtime() + "秒.\n");
          msg.append("对象:" + ol.getObjName() + "被锁.");
          val.append("对象:" + ol.getObjName() + "被锁.");
          wonted = false;
        }
      }
    }
    if (wonted) {
      msg.append("数据库运行正常,未发现非法的资源锁定!");
    }
    return wonted;
  }

  private boolean matchRecord(OracleLock lock) {
    switch (matchType) {
    case OracleLockParameter.MATCH_NORMAL:
      return matchNormal(lock.getObjName(), resource);
    case OracleLockParameter.MATCH_REGEX:
      return matchRegex(lock.getObjName(), resource);
    default:
      return true;
    }
  }

  private boolean matchNormal(String lockResource, String condResource) {
    if (condResource != null && condResource.length() > 0) {
      String[] condResourceSplit = condResource.split(TextUtil.SEPARATOR);
      for (int i = 0; i < condResourceSplit.length; i++) {
        if (condResourceSplit[i].length() > 0) {
          String regexResource = condResourceSplit[i].replaceAll("\\*", ".*");
          regexResource = regexResource.replaceAll("\\(", "\\\\(");
          regexResource = regexResource.replaceAll("\\)", "\\\\)");
          regexResource = regexResource.replaceAll("\\?", "\\.");
          boolean result = lockResource.toLowerCase().matches(regexResource.toLowerCase());
          if (result) {
            return result;
          }
        }
      }
      return false;
    }
    return true;
  }

  private boolean matchRegex(String lockResource, String condResource) {
    if (condResource != null && condResource.length() > 0) {
      String[] condResourceSplit = condResource.split(TextUtil.SEPARATOR);
      for (int i = 0; i < condResourceSplit.length; i++) {
        if (condResourceSplit[i].length() > 0) {
          boolean result = lockResource.toLowerCase().matches(condResourceSplit[i].toLowerCase());
          if (result) {
            return result;
          }
        }
      }
      return false;
    }
    return true;
  }

  /**
   * 获取Oracle锁定信息；
   * 请在执行matchAllRecords()方法之后再调用该方法.
   * @return
   */
  public StringBuffer getMsg() {
    return msg;
  }

  public StringBuffer getVal() {
    return val;
  }
  
  public List getLockedRes() {
    return lockedRes;
  }

}
