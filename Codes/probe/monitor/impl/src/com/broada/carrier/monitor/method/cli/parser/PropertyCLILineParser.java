package com.broada.carrier.monitor.method.cli.parser;

import java.util.List;
import java.util.Properties;

/**
 * 行解析器，解析属性行
 * 
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Email : zhoucy@broada.com
 * @Create By 2006-8-11 上午09:25:18
 */
public class PropertyCLILineParser extends AbstractCLILineParser {
  private Properties properties = new Properties();
  
  public void parseLine(int lineno, String line, ParserRule parserRule) throws Exception {
    List<ParserItem> parserItems = parserRule.getParserItems();
    /*
     * 将行按分隔符规则处理
     */
    String[] units = dealLine(line, parserRule.getDelimeter());
    /*
     * 遍历定义的规则，如果为当前行定义了规则，那么解析这一行
     */
    for (int index = 0; index < parserItems.size(); index++) {
      ParserItem parserItem = (ParserItem) parserItems.get(index);
      if (parserItem.getLine() == lineno) {
        Object obj=parseProperties(lineno, units, parserItem);
        if(obj != null){
          if(parserItem.getName() == null || "".equals(parserItem.getName())){
        	  throw new NullPointerException("parserItem配置的name不能为空");
          }	
          properties.put(parserItem.getName(),obj);
        }
      }
    }
  }

  public Properties getResult() {
    return properties;
  }
}
