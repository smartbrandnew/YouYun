package com.broada.carrier.monitor.method.cli.parser;


/**
 * CLI行结果解析器，主要实现对采集结果的每行进行解析，并返回最终解析后的结果
 * 
 * @author ZhouCY (zhoucy@broada.com.cn)
 * Create By 2006-9-16 14:27:01
 */
public interface CLILineParser {
  /**
   * 对采集结果进行逐行解析
   * @param lineno 行号
   * @param line 行内容
   * @param parserRule 解析规则
   */
  public void parseLine(int lineno, String line, ParserRule parserRule) throws Exception;
  /**
   * 返回最终解析结果
   * @return
   */
  public Object getResult();
}
