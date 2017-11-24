package com.broada.carrier.monitor.method.cli.entity;

import java.util.List;
import java.util.Properties;

public interface CLIResult {
  List<Properties> getListTableResult();

  /**
   * 获取properties类型的结果
   * 
   * @return
   */
  Properties getPropResult();
  
  /**
   * 获取解析出错的行号(key)及内容(value)
   * 如果存在key为-1的情况,则表明解析预处理脚本出错
   * @return
   */
  CLIErrorLine[] getErrLines();
}
