package com.broada.carrier.monitor.method.cli.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;
import com.broada.utils.StringUtil;

public abstract class AbstractCLILineParser implements CLILineParser {
  private static final Log logger = LogFactory.getLog(AbstractCLILineParser.class);

  /**
   * 处理一行输入，结果为字符串数组
   * 
   * 由于split方法会将匹配在首位的多加一个空字符串，例如：<br>
   * 
   * s = "abcd, ";<br>
   * 
   * s.split(",");<br>
   * 
   * 将会返回{"abcd",""}<br>
   * 
   * 可以判断如果分解的字符串首位是空字符串可以直接去掉
   * 
   * @param line
   * @param delimeter
   * @return
   */
  protected String[] dealLine(String line, String delimeter) {
    // 如果分隔符中包含多个空格符号，首先合并空格符
    line = mergeSpace(line);

    String[] results = line.split(delimeter);

    /*
     * 处理分解后的字符串，去掉多余的空格
     */
    List<String> s = new ArrayList<String>();
    /*
     * 第一个如果是空字符串，删！
     */
    if (results.length > 0 && !StringUtil.isNullOrBlank(results[0])) {
      s.add(results[0].trim());
    }

    /*
     * 去掉前后空格
     */
    for (int index = 1; index < results.length - 1; index++) {
      s.add(results[index].trim());
    }

    /*
     * 最后一个如果是空字符串，删！
     * modify by huangjb 2007/12/12 修改判断数组长度大于1,上面已经处理过第一个的情况了
     */
    if (results.length > 1 && !StringUtil.isNullOrBlank(results[results.length - 1])) {
      s.add(results[results.length - 1].trim());
    }

    return (String[]) s.toArray(new String[s.size()]);
  }

  /*
   * 多个空格合并成一个空格
   */
  private String mergeSpace(String line) {
    return line.trim().replaceAll("\\s+", " ");
  }
  
  /**
   * 提供每一行的属性解析功能
   * @param lineno
   * @param units
   * @param parserItem
   * @return 返回解析后的属性对象
   */
  protected Object parseProperties(int lineno, String[] units, ParserItem parserItem) {
    String name = parserItem.getName();
    // modify by huangjb 2007/12/04 添加数组越界判断
    String value = "";
    if(units.length > parserItem.getToken()){
      value = units[parserItem.getToken()].trim();
      //do not deal deadProcess
      if(units[0].indexOf("defunct") > 0 || units[0].indexOf("idle") > 0
          || units[0].indexOf("exiting") > 0){
        return value;
      }
    }else
      return value;

    if (logger.isDebugEnabled()) {
      logger.debug("当前处理的属性[" + name + "," + value + "]");
    }
    return afterParse(parserItem.getBsh(), name, value);
  }
  
  /**
   * 解析的最后一步就是解析BeanShell，如果配置了BeanShell脚本，那么执行脚本
   * @param script
   * @param name
   * @param value 当前获取得到的结果
   * @return 返回解析后的结果对象
   * @throws CLIRuntimeException 如果执行脚本解析错误时抛出
   */
  protected Object afterParse(String script, String name, String value){
    if (logger.isDebugEnabled()) {
      logger.debug("对单个属性后处理[" + name + "," + value + "]");
    }
    Object obj = null;
    /*
     * 如果配置了BeanShell脚本
     */
    if (!StringUtil.isNullOrBlank(script)) {
      try {
      	obj = ScriptManager.evalByCompiled(ScriptManager.LANG_BSH, script, name, value);
      } catch (Throwable e) {
        throw new CLIRuntimeException(String.format("进行["+name+"]的脚本解析时发生异常。错误：%s", e), e);
      }
    }
    if (obj != null){
      return obj;
    }else{
      return value;
    }
  }
}
