package com.broada.carrier.monitor.impl.db.oracle.redolog;

import java.util.LinkedHashMap;

/**
 * Oracle Redo 日志信息监测参数
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-10-16 下午01:34:14
 */
public class OracleRedoLogParameter {
  public static final String FIELD_TOTAL_MIS = "total_mis";
  public static final String FIELD_TOTAL_GETS = "total_gets";
  public static final String FIELD_TOTAL_IMM_MIS = "total_imm_mis";
  public static final String FIELD_TOTAL_IMM_GETS = "total_imm_gets";
  public static final String FIELD_WILLING_TO_WAIT_RATIO = "willing_to_wait_ratio";
  public static final String FIELD_IMMIDIATE_RATIO = "immidiate_ratio";
  public static final String FIELD_UNALLOCS = "unAllocs";
  public static final String FIELD_ARCHCOUNT = "archCount";
  public static final String FIELD_AVESIZE = "aveSize";
  public static final LinkedHashMap<String,String> items = new LinkedHashMap<String,String>();
  static {
    //初始化顺序跟保存到数据库库性能项的次序一致
    items.put(FIELD_TOTAL_MIS, "初始以Willing-to-wait请求类型请求一个latch不成功的总次数");
    items.put(FIELD_TOTAL_GETS, "成功地以Willing-to-wait请求类型请求一个latch的总次数");
    items.put(FIELD_TOTAL_IMM_MIS, "以Immediate请求类型请求一个latch不成功的总次数");
    items.put(FIELD_TOTAL_IMM_GETS, "以Immediate请求类型成功地获得一个latch的总次数");
    items.put(FIELD_WILLING_TO_WAIT_RATIO, "Willing-to-wait请求类型的丢失量占其获得数的百分比");
    items.put(FIELD_IMMIDIATE_RATIO, "Immediate请求类型的丢失量占其获得数的百分比");
    items.put(FIELD_UNALLOCS, "重做日志缓冲中用户进程不能分配空间的次数");
    items.put(FIELD_ARCHCOUNT, "归档重做日志文件的数目");
    items.put(FIELD_AVESIZE, "重做条目的平均大小");

  }

}
