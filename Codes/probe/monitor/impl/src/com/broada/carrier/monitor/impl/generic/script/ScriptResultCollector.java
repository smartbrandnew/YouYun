package com.broada.carrier.monitor.impl.generic.script;

import java.util.Properties;

public interface ScriptResultCollector {

	/**
   * 关闭并销毁采集器
   */
  void close();
  
  /**
   * 检查与Agent端连通情况
   * @return 采集结果
   * @throws ScriptException
   */
  boolean checkConnectWithAgent() throws ScriptException;
  
  /**
   * 初始化采集器
   * @param options 初始化所必要的参数
   * @throws ScriptConnectException 当链接目标设备失败的时候抛出
   */
  void init(Properties options);
}