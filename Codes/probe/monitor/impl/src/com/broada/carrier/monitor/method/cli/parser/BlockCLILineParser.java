package com.broada.carrier.monitor.method.cli.parser;

import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;

/**
 * Block方式解析数据块
 * （根据配置脚本中对返回的数据块同时提取多行数据）
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-4-30 下午12:56:01
 */
public class BlockCLILineParser extends AbstractCLILineParser {

  private StringBuffer blockBuf = new StringBuffer();

  private Properties properties = new Properties();
  private String contentKey ="";
  
  public Properties getResult() {
    properties.put(contentKey, blockBuf.toString());
    return properties;
  }

  public void parseLine(int lineno, String line, ParserRule parserRule) {
    blockBuf.append(line).append("\n");
    List<ParserItem> parserItems = parserRule.getParserItems();
    for (int index = 0; index < parserItems.size(); index++) {
      ParserItem parserItem = (ParserItem) parserItems.get(index);
      if(parserItem.getLine() == -1)
        contentKey = parserItem.getName();
      if (parserItem.getLine() != lineno)
        continue;
      int start = parserItem.getStart();
      int end = parserItem.getEnd();
      if ( end != 0) {
        String name = parserItem.getName();
        int endPos = getEnd(line,parserItem);
        String value = line.substring(start, endPos + 1).trim();
        Object obj = afterParse(parserItem.getBsh(), name, value);
        properties.put(name, obj);
      } else {
        /*
         * 将行按分隔符规则处理
         */
        String[] units = dealLine(line, parserRule.getDelimeter());
        Object obj = parseProperties(lineno, units, parserItem);
        if(obj != null){
          properties.put(parserItem.getName(), obj);
        }
      }
    }
  }

  private int getEnd(String line, ParserItem parserItem) {
    /*
     * 如果end为-1,那么计算字符串的长度，将字符串的长度返回
     */
    if (parserItem.getEnd() == -1)
      return line.length() - 1;
    // 如果配置了start但没有配置end或者end小于start将抛出异常
    if (parserItem.getEnd() == 0 || parserItem.getEnd() < parserItem.getStart()) {
      throw new CLIRuntimeException("block方式解析[" + parserItem.getName() + "]，end属性配置错误！");
    }
    return parserItem.getEnd();
  }
}
