/***********************************************************************************************************************
 * Module: CLISession.java Author: Broada Purpose: Defines the Interface CLISession
 **********************************************************************************************************************/

package com.broada.carrier.monitor.method.cli.session;

import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;

/**
 * CLI会话，主要是维护与目标设备的一个会话，并提供命令执行功能
 * 
 * @author ZhouCY (zhoucy@broada.com.cn)
 * Create By 2006-10-13 14:06:18
 */
public interface CLISession {
  /**
   * 执行指定的命令，并返回执行结果
   * @param cmd 具体的命令行
   * @param args 命令行需要的参数
   * @param prompt 命令执行的提示符 null或者default:采用系统默认配置，否则以输入为准
   * @param localBuf 存储中文化后的命令执行结果输出
   * @param isLogErr 是否需要Log错误日志信息(panghf Add 2011-10-12 为了方便控制调试而加入)
   * @return  返回命令行执行的结果字符串
   * @throws CLIException 执行过程中如果出现异常抛出
   */
  String execCmd(String cmd, String[] args,String prompt,StringBuffer localBuf,boolean isLogErr) throws CLIException;
  /**
   * 仅供db2 agent执行sql时调用
   * @param cmd 切换到db2用户控制台等命令
   * @param args 包含数据库名称，用户，sql语句
   * @param localBuf
   * @return
   * @throws CLIException
   */
  List<String> runSQL(String cmd, String[] args,StringBuffer localBuf) throws CLIException;
  /**
   * 执行指定的脚本，并返回执行结果
   * @param scriptFile 具体的脚本文件名
   * @param args 脚本需要的参数
   * @return  返回脚本执行的结果字符串
   * @throws CLIException 执行过程中如果出现异常抛出
   */
  String execScript(String scriptFile, String[] args) throws CLIException;

  /**
   * 标识该会话是否具有上下文关系保持的能力
   * <p>
   * 如果为true则在执行命令集合时执行器会一次执行一个命令，并进行相应的处理
   * 如果为false则在执行命令集合时执行器会把所有命令组合起来一次执行完毕
   * @return
   */
  boolean hasContext();

  /**
  * 打开一个CLI会话，调用该方法后会与目标服务器建立一个连接并维持
  * @param options 为登录所需要的各种参数，参数请参考CLIConstant类的"OPTIONS_"开头的常量
  * @param isLogErr 是否需要Log错误日志信息(panghf Add 2011-10-12 为了方便控制调试而加入)
  * @throws CLILoginFailException 当登录失败时抛出，一般是用户名、密码等访问参数错误引起
  * @throws CLIConnectException 当连接目标设备失败时抛出，一般是网络连接错误，可能是目标地址和端口错误或目标设备没有开启
  */
  void open(Properties options,boolean isLogErr) throws CLILoginFailException, CLIConnectException;
  
  /**
   * 标识此连接是长连接还是短连接，一个短连接在命令执行完成后将被关闭，
   */
  boolean isStanding();

  /**
   * 关闭并销毁会话
   */
  void close();
}