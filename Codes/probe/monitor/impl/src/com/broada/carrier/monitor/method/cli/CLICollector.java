/***********************************************************************************************************************
 * Module: CLICollector.java Author: Broada Purpose: Defines the Interface CLICollector
 **********************************************************************************************************************/

package com.broada.carrier.monitor.method.cli;

import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;

/**
 * CLI信息采集器
 * 
 * @author ZhouCY (zhoucy@broada.com.cn)
 * Create By 2006-10-13 14:06:18
 */
public interface CLICollector {
	
  /**
   * 关闭采集器，如果底层连接为长连接，则保留连接缓存，否则立即关闭连接
   */
  void close();
  
  /**
   * 销毁采集器
   */
  void destroy();

  /**
   * 执行命名并返回采集结果
   * @param comandName 命令集合的名称（并不是真正需要执行的命令），参看采集规范
   * @param args 命令可能需要的参数
   * @param isLogErr 是否需要Log错误日志信息(panghf Add 2011-10-12 为了方便控制调试而加入)
   * @return 采集结果(经过解析后的结果）
   * @throws CLIResultParseException 对结果解析错误时抛出
   * @throws CLIException 采集过程中如果出现异常抛出
   */
  CLIResult execute(String comandName, String[] args,boolean isLogErr) throws CLIResultParseException,CLIException;
  /**
   * 执行命名并返回采集结果
   * @param comandName 命令集合的名称（并不是真正需要执行的命令），参看采集规范
   * @param args 命令可能需要的参数
   * @param afterExecuteListener 采集过程监听器,执行完每个具体命令后执行
   * @param isLogErr 是否需要Log错误日志信息(panghf Add 2011-10-12 为了方便控制调试而加入)
   * @return 采集结果(经过解析后的结果）
   * @throws CLIResultParseException 对结果解析错误时抛出
   * @throws CLIException 采集过程中如果出现异常抛出
   */
  CLIResult execute(String comandName, String[] args, AfterExecuteListener afterExecuteListener,boolean isLogErr) throws CLIResultParseException,CLIException;
  
  /**
   * 初始化执行器,初始化时会提供必要的参数
   * @param options 初始话参数，一般为登录所需要的各种参数
   * @param isLogErr 是否需要Log错误日志信息(panghf Add 2011-10-12 为了方便控制调试而加入)
   * @throws CLILoginFailException 当登录失败时抛出
   * @throws CLIConnectException 当连接目标设备失败时抛出
   * @return 标识是否能成功初始化
   */
  boolean init(Properties options,boolean isLogErr) throws CLILoginFailException, CLIConnectException;
  /**
   * 标识是否需要重新建立
   * @return
   */
  boolean isWeak();
  
  /**
   * 标识此连接是长连接还是短连接
   */
  boolean isStanding();
  
  /**
   * 标识是否已经关闭
   * @return
   */
  boolean isClosed();
  
  /**
   * 仅供db2 agent监测时调用
   * @param comandName
   * @param args
   * @return
   * @throws CLIResultParseException
   * @throws CLIException
   */
  List<String> executeSQL(String comandName, String[] args) throws CLIException;
}