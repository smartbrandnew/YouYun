package com.broada.carrier.monitor.impl.db.oracle.pga;

import java.util.LinkedHashMap;

/**
 * Oracle PGA 监测参数类
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-10-7 下午04:28:58
 */
public class OraclePGAParameter {

  public static final String FIELD_PGA_TARGET_PARA = "aggregate PGA target parameter";
  public static final String FIELD_PGA_AUTO_TARGET = "aggregate PGA auto target";
  public static final String FIELD_GLOB_MEM_BOUND = "global memory bound";
  public static final String FIELD_TOTAL_PGA_INUSE = "total PGA inuse";
  public static final String FIELD_TOTAL_PGA_ALLOC = "total PGA allocated";
  public static final String FIELD_MAX_PGA_ALLOC = "maximum PGA allocated";
  public static final String FIELD_TOTAL_FREE_PGA_MEM = "total freeable PGA memory";
  public static final String FIELD_FREEBACK_TO_OS = "PGA memory freed back to OS";
  public static final String FIELD_TOTAL_USED4AUTO_WRKAREAS = "total PGA used for auto workareas";
  public static final String FIELD_MAX_USED4AUTO_WRKAREAS = "maximum PGA used for auto workareas";
  public static final String FIELD_TOTAL_USED4MAN_WRKAREAS = "total PGA used for manual workareas";
  public static final String FIELD_MAX_USED4MAN_WRKAREAS = "maximum PGA used for manual workareas";
  public static final String FIELD_OVER_ALLOC_ACCOUNT = "over allocation count";
  public static final String FIELD_BYTES_PROCESSED = "bytes processed";
  public static final String FIELD_EXTRA_BYTES_RW = "extra bytes read/written";
  public static final String FIELD_CACHE_HITRATO = "cache hit percentage";
  public static final LinkedHashMap<String,String> items = new LinkedHashMap<String,String>();
  static {
    items.put(FIELD_PGA_TARGET_PARA, "PGA内存总大小");
    items.put(FIELD_PGA_AUTO_TARGET, "当前可用于自动分配了的PGA大小");
    items.put(FIELD_GLOB_MEM_BOUND, " 自动模式下工作区域的最大大小");
    items.put(FIELD_TOTAL_PGA_INUSE, "使用的PGA大小");
    items.put(FIELD_TOTAL_PGA_ALLOC, "分配的PGA大小");
    items.put(FIELD_MAX_PGA_ALLOC, "PGA的最大分配大小（历史最大值）");
    items.put(FIELD_TOTAL_FREE_PGA_MEM, "空闲的PGA大小");
    items.put(FIELD_FREEBACK_TO_OS, "释放的PGA大小");
    items.put(FIELD_TOTAL_USED4AUTO_WRKAREAS, "自动工作区PGA使用大小");
    items.put(FIELD_MAX_USED4AUTO_WRKAREAS, "自动工作区PGA最大使用量（历史最大值）");
    items.put(FIELD_TOTAL_USED4MAN_WRKAREAS, "手动工作区PGA使用大小");
    items.put(FIELD_MAX_USED4MAN_WRKAREAS, "手动工作区PGA最大使用量（历史最大值）");
    items.put(FIELD_OVER_ALLOC_ACCOUNT, "实例启动后PGA分配次数");
    items.put(FIELD_BYTES_PROCESSED, "实例启动后处理的字节数");
    items.put(FIELD_EXTRA_BYTES_RW, "实例启动后额外处理（读/写）的字节数");
    items.put(FIELD_CACHE_HITRATO, "Cache命中率");
  }
}
