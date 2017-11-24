package com.broada.carrier.monitor.method.cli;

import java.util.Properties;

import com.broada.carrier.monitor.method.cli.config.Category;
import com.broada.carrier.monitor.method.cli.config.Command;
import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;

/**
 * 每一步执行后的监听器
 * 
 * @author <a href="mailto:zhoucy@broada.com.cn">zhoucy</a>
 * @Create By 2006-12-14 上午10:12:06
 */
public interface AfterExecuteListener {

  /**
   * 对于一项监测内容如果分多步执行,怎每一步都会调用实现的监听器
   * 
   * @param options - 主机参数
   * @param category
   * @param command - 命令
   * @param agrs - 参数
   * @param result - 执行命令的原始结果
   */
  public void handle(Properties options, Category category, Command command,String[] args, String result);
  /**
   * 处理每个Category的所有出错行
   * @param category
   * @param errlines 解析出错的行号及其内容(-1表示预处理脚本出错)
   */
  public void handle(Category category, CLIErrorLine[] errlines);
}
