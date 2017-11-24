package com.broada.carrier.monitor.impl.db.oracle.secaccess;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.broada.utils.DateUtil;
import com.broada.utils.TextUtil;

/**
 * <p>
 * Title: OracleSecAccessRules
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author plx
 * @version 2.3
 */

public class OracleSecAccessRules implements Comparator<String>{

  private List conditions;

  private List records;

  private StringBuffer msg = new StringBuffer(); // 规则匹配信息

  private boolean wonted = true; // 规则匹配结果

//  private StringBuffer lawlessAdds = new StringBuffer(); // 保存非法连接的地址列表

  public OracleSecAccessRules(List conditions, List records, String hostName, String dbUser) {
    this.conditions = conditions;
    this.records = filterSel(records, hostName, dbUser);
  }

  public boolean matchAllRecords() {
    Set<String> hostUsers = null;
    if (records != null && records.size() > 0 && conditions != null && conditions.size() > 0) {
      hostUsers = new HashSet<String>();
      Object[][] caches = new Object[conditions.size()][];
      for (Iterator itr = records.iterator(); itr.hasNext();) {
        OracleAccess oa = (OracleAccess) itr.next();
        if (!matchRecord(oa, hostUsers, caches)) {
          wonted = false;
        }
      }
    }
    if (wonted) {
      msg.append("所有数据库连接均为合法连接!");
    } else {
      msg.append("非法连接:");
      String[] users = new String[hostUsers.size()];
      int i = 0;
      for (String hostUser : hostUsers)
        users[i++] = hostUser;
      Arrays.sort(users, this);
      for (String user : users)
        msg.append('\n').append(user);
      if (msg.charAt(msg.length() - 1) == ',')
        msg.setCharAt(msg.length() - 1, '.');
      else
        msg.append('.');
    }
    return wonted;
  }

  /**
   * 判断一个数据库连接记录是否合法,若不符合所有的规则,则将该数据库连接信息记录到msg中.
   * 
   * @param osa
   * @return
   */
  private boolean matchRecord(OracleAccess osa, Set<String> hostUsers, Object[][] caches) {
    int index = 0;
    for (Iterator itr = conditions.iterator(); itr.hasNext(); index++) {
      Object[] cache = caches[index];
      if (cache == null) {
        cache = new Object[6];
        caches[index] = cache;
      }
      OracleSecAccessMonitorCondition condition = (OracleSecAccessMonitorCondition) itr.next();
      if (!matchIp(osa.getIpAddr(), osa.getHostName(), condition.getIpAddr(), cache, 0)) {
        continue;
      }
      if (!matchDbUser(osa.getDbUser(), condition.getDbUser(), cache, 1)) {
        continue;
      }
      if (!matchWeek(condition.getWeek(), cache, 2)) {
        continue;
      }
      if (!matchTime(condition.getStartTime(), condition.getEndTime(), cache, 3)) {
        continue;
      }
      if (!matchOsUser(osa.getOsUser(), condition.getOsUser(), cache, 4)) {
        continue;
      }
      if (!matchProgram(osa.getProgram(), condition.getProgram(), cache, 5)) {
        continue;
      }
      return true;
    }
    String desc = osa.getDesc();
    // 避免重复信息
    if (!hostUsers.contains(desc))
      hostUsers.add(desc);
    return false;
  }

  /**
   * 匹配Ip(支持"*"、"."类型的正则表达式)
   * 
   * @param accessIp
   * @param accessHostName
   * @param condIp
   * @return
   */
  private boolean matchIp(String accessIp, String accessHostName, String condIp, Object[] cache, int cacheIndex) {
    if (StringUtils.isNotEmpty(condIp)) {
      Pattern pattern = (Pattern) cache[cacheIndex];
      if (pattern == null) {
        String regexIp = condIp.replaceAll(TextUtil.SEPARATOR, "|").replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*")
            .replaceAll("\\?", "\\\\.");
        pattern = Pattern.compile(regexIp);
        cache[cacheIndex] = pattern;
      }
      if (StringUtils.isNotEmpty(accessIp)) {
        if (pattern.matcher(accessIp).matches())
          return true;
      }
      if (StringUtils.isNotEmpty(accessHostName)) {
        accessHostName = accessHostName.substring(accessHostName.indexOf('\\') + 1);
        if (pattern.matcher(accessHostName).matches())
          return true;
      }
    }
    return false;
  }

  /**
   * 匹配数据库用户
   * 
   * @param accessDbUser
   * @param condDbUser
   * @return
   */
  private boolean matchDbUser(String accessDbUser, String condDbUser, Object[] cache, int cacheIndex) {
    if (StringUtils.isNotEmpty(condDbUser)) {
      Pattern pattern = (Pattern) cache[cacheIndex];
      if (pattern == null) {
        String regexDbUser = condDbUser.replaceAll(TextUtil.SEPARATOR, "|").replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*")
            .replaceAll("\\?", "\\\\.");
        pattern = Pattern.compile(regexDbUser, Pattern.CASE_INSENSITIVE);
        cache[cacheIndex] = pattern;
      }
      return pattern.matcher(accessDbUser).matches();
    }
    return true;
  }

  /**
   * 匹配星期
   * 
   * @param condWeek
   * @return
   */
  private boolean matchWeek(String condWeek, Object[] cache, int cacheIndex) {
    String todayWeek = (String) cache[cacheIndex];
    if (todayWeek == null) {
      Calendar calendar = Calendar.getInstance();
      todayWeek = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));
      cache[cacheIndex] = todayWeek;
    }
    return condWeek.indexOf(todayWeek) >= 0;
  }

  /**
   * 匹配时间
   * 
   * @param condStartTime
   * @param condEndTime
   * @return
   */
  private boolean matchTime(String condStartTime, String condEndTime, Object[] cache, int cacheIndex) {
    Boolean result = (Boolean) cache[cacheIndex];
    if (result == null) {
      result = Boolean.TRUE;
      Calendar calendar = Calendar.getInstance();
      calendar.clear(Calendar.YEAR);
      calendar.clear(Calendar.MONTH);
      calendar.clear(Calendar.WEEK_OF_YEAR);
      calendar.clear(Calendar.WEEK_OF_MONTH);
      calendar.clear(Calendar.DATE);
      calendar.clear(Calendar.DAY_OF_YEAR);
      calendar.clear(Calendar.DAY_OF_WEEK);
      calendar.clear(Calendar.DAY_OF_WEEK_IN_MONTH);
      Date time = calendar.getTime();
      if (StringUtils.isNotBlank(condStartTime)) {
        try {
          Date startTime = DateUtil.TIME_FORMAT.parse(condStartTime);
          if (time.before(startTime))
            result = Boolean.FALSE;
        } catch (ParseException e) {
          result = Boolean.FALSE;
        }
      }
      if (StringUtils.isNotBlank(condEndTime)) {
        try {
          Date endTime = DateUtil.TIME_FORMAT.parse(condEndTime);
          if (time.after(endTime))
            result = Boolean.FALSE;
        } catch (ParseException e) {
          result = Boolean.FALSE;
        }
      }
      cache[cacheIndex] = result;
    }
    return result;
  }

  /**
   * 匹配操作系统用户
   * 
   * @param accessOsUser
   * @param condOsUser
   * @return
   */
  private boolean matchOsUser(String accessOsUser, String condOsUser, Object[] cache, int cacheIndex) {
    if (StringUtils.isNotEmpty(condOsUser)) {
      Pattern pattern = (Pattern) cache[cacheIndex];
      if (pattern == null) {
        String regexOsUser = condOsUser.replaceAll(TextUtil.SEPARATOR, "|").replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*")
            .replaceAll("\\?", "\\\\.");
        pattern = Pattern.compile(regexOsUser, Pattern.CASE_INSENSITIVE);
        cache[cacheIndex] = pattern;
      }
      return pattern.matcher(accessOsUser).matches();
    }
    return true;
  }

  /**
   * 匹配连接数据库的程序(支持"*"、"."类型的正则表达式)
   * 
   * @param accessProgram
   * @param condProgram
   * @return
   */
  private boolean matchProgram(String accessProgram, String condProgram, Object[] cache, int cacheIndex) {
    if (StringUtils.isNotEmpty(condProgram)) {
      Pattern pattern = (Pattern) cache[cacheIndex];
      if (pattern == null) {
        String regexProgram = condProgram.replaceAll(TextUtil.SEPARATOR, "|").replaceAll("\\.", "\\\\.")
            .replaceAll("\\*", ".*").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\[", "\\\\[")
            .replaceAll("\\]", "\\\\]").replaceAll("\\?", "\\.");
        pattern = Pattern.compile(regexProgram);
        cache[cacheIndex] = pattern;
      }
      return pattern.matcher(accessProgram == null ? "" : accessProgram).matches();
    }
    return true;
  }

  /**
   * 获取Oracle安全访问信息； 请在执行matchAllRecords()方法之后再调用该方法.
   * 
   * @return
   */
  public StringBuffer getMsg() {
    return msg;
  }

  /**
   * 获得非法连接地址列表
   * 
   * @return
   */
//  public StringBuffer getLawlessAdds() {
//    return lawlessAdds;
//  }

  private List filterSel(List connections, String hostName, String dbUser) {
    List _list = new ArrayList();
    if (connections == null)
      return _list;
    Pattern pattern = Pattern.compile(".*thin.*", Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
    for (Iterator itr = connections.iterator(); itr.hasNext();) {
      OracleAccess oa = (OracleAccess) itr.next();

      if (StringUtils.isEmpty(oa.getHostName()) || !oa.getHostName().equalsIgnoreCase(hostName)) {
        _list.add(oa);
        continue;
      }

      if (StringUtils.isEmpty(oa.getDbUser()) || !oa.getDbUser().equalsIgnoreCase(dbUser)) {
        _list.add(oa);
        continue;
      }

      if (StringUtils.isEmpty(oa.getProgram()) || !pattern.matcher(oa.getProgram()).matches()) {
        _list.add(oa);
        continue;
      }

    }
    return _list;
  }

  public int compare(String left, String right) {
    return left.compareToIgnoreCase(right);
  }
}
