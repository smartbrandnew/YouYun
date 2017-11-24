package com.broada.carrier.monitor.method.cli.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;
import com.broada.utils.StringUtil;

public class TableCLILineParser extends AbstractCLILineParser {
  private static final Log logger = LogFactory.getLog(TableCLILineParser.class);

  private List<Properties> tables = new ArrayList<Properties>();
  private String[] titleNames;
  private ParserItem[] parserItems;

  public void parseLine(int lineno, String line, ParserRule parserRule) {
    if (lineno == parserRule.getTitleLineNo()) {
      // 解析数据表格的标题行
      Pattern titleIgnorePattern = parserRule.getTitleIgnorePattern();
      if (titleIgnorePattern != null)
        line = titleIgnorePattern.matcher(line).replaceAll("");
      titleNames = line.trim().split("\\s+");
      List<ParserItem> parserItems = parserRule.getParserItems();
      ParserItem[] items = new ParserItem[titleNames.length];
      int index = 0;
      for (int i = 0; i < titleNames.length; i++) {
        int j = index;
        while(true) {
          ParserItem item = (ParserItem) parserItems.get(j);
          if (++j >= parserItems.size())
            j = 0;
          if (item.getTitleNamePattern().matcher((titleNames[i])).matches()) {
            items[i] = item;
            if (items[i].getToken() == 0 && i > 0) {
            	items[i] = (ParserItem) items[i].clone();
            	items[i].setToken(i);
            }
            index = j;
            break;
          }
          if (j == index)
            break;
        }
      }
      this.parserItems = items;
      return;
    }
    Properties properties = new Properties();
    /*
     * 提前处理start和end
     */
    line = preParseLine(line, parserRule,properties);
    /*
     * 将行按分隔符规则处理
     */
    String[] units = dealLine(line, parserRule.getDelimeterPattern(), parserItems);

    /*
     * 遍历定义的规则
     */
    if (titleNames == null) { // 无标题行
      List<ParserItem> parserItems = parserRule.getParserItems();

      for (int index = 0; index < parserItems.size(); index++) {
        ParserItem parserItem = (ParserItem) parserItems.get(index);
        if (!isNeedParseStartAndEnd(parserItem)) {
          Object obj=parseProperties(lineno, units, parserItem);
          if(obj != null){
            properties.put(parserItem.getName(),obj);
          }
        }
      }
    } else { // 有标题行
      for (int index = 0; index < parserItems.length; index++) {
        ParserItem parserItem = parserItems[index];
        if (parserItem != null && !isNeedParseStartAndEnd(parserItem)) {
          Object obj = parseTitledProperties(lineno, units, parserItem, index);
          if(obj != null){
            properties.put(parserItem.getName(),obj);
          }
        }
      }
    }

    if (!properties.isEmpty())    	
    	tables.add(properties);
  }

  private String preParseLine(String line, ParserRule parserRule,Properties properties ) {
    List<ParserItem> parserItems = parserRule.getParserItems();

    /*
     * 检查配置了start和end的parserItem
     */
    for (int index = 0; index < parserItems.size(); index++) {
      ParserItem parserItem = (ParserItem) parserItems.get(index);

      int start = parserItem.getStart();
      if (isNeedParseStartAndEnd(parserItem)) {
        int end = getEnd(line, parserItem);

        if (logger.isDebugEnabled()) {
          logger.debug("处理表格的end和start指定的属性[start=" + start + ",end=" + end + "]");
        }
        /*
         * 取得从start-end的字符串，包括start处和end处的字符
         */
        String name = parserItem.getName();        
        String value = substr(line, start, end + 1);
        Object obj=afterParse(parserItem.getBsh(), name, value);
        properties.put(name,obj);
        if (start <= end && value.length() > 0)
          line = StringUtil.replace(line, getSpaces(end - start + 1), start, start + value.length());
      }
    }
    return line;
  }

  private static String substr(String text, int start, int end) {
  	if (text == null)
  		return "";
  	start = Math.min(text.length(), start);
  	end = Math.min(text.length(), end);
  	if (start >= end)
  		return "";
  	return text.substring(start,  end);  	
	}

	private boolean isNeedParseStartAndEnd(ParserItem parserItem) {
    return parserItem.getStart() != 0;
  }

  private String getSpaces(int m) {
    StringBuffer buffer = new StringBuffer();
    for (int index = 0; index < m; index++)
      buffer.append(" ");
    return buffer.toString();
  }

  private int getEnd(String line, ParserItem parserItem) {
    /*
     * 如果end为-1,那么计算字符串的长度，将字符串的长度返回
     */
    if (parserItem.getEnd() == -1)
      return line.length() - 1;
    // 如果配置了start但没有配置end或者end小于start将抛出异常
    if (parserItem.getEnd() == 0 || parserItem.getEnd() < parserItem.getStart()) {
      throw new CLIRuntimeException("Table方式解析["+parserItem.getName()+"]，end属性配置错误！");
    }
    return parserItem.getEnd();
  }

  public List<Properties> getResult() {
    return tables;
  }
  
  /**
   * 提供每一行的属性解析功能
   * @param lineno
   * @param units
   * @param parserItem
   * @param token 
   * @return 返回解析后的属性对象
   */
  protected Object parseTitledProperties(int lineno, String[] units, ParserItem parserItem, int token) {
    String name = parserItem.getName();
    String value = "";
    if(units.length > token){
      value = units[token].trim();
      //do not deal deadProcess
      if(units[0].indexOf("defunct") > 0 || units[0].indexOf("idle") > 0
          || units[0].indexOf("exiting") > 0){
        return null;
      }
    }else
      return null;

    if (logger.isDebugEnabled()) {
      logger.debug("当前处理的属性[" + name + "," + value + "]");
    }
    return afterParse(parserItem.getBsh(), name, value);
  }
  
  static String[] dealLine(String line, Pattern delimeter, ParserItem[] items) {
  	List<String> words = new ArrayList<String>(items == null ? 0 : items.length);
  	
  	int currStart = 0;
  	Matcher matcher = delimeter.matcher(line);
  	while (matcher.find(currStart)) {  	
  		if (currStart < matcher.start()) {
  			String word = null;
  			ParserItem item = getItemByToken(items, words.size());
  			if (item != null && item.getStart() == 0 && item.getEnd() > 0) 
  				word = line.substring(currStart, Math.min(currStart + item.getEnd(), line.length()));
  			else
  				word = line.substring(currStart, matcher.start());
  			currStart += word.length();
  			words.add(word.trim());  			   			
  		} else  			
  			currStart = matcher.end();
  	}
  	if (currStart < line.length()) {
			String word = line.substring(currStart).trim();
			words.add(word.trim());  			   		
  	}

  	return words.toArray(new String[0]);
  }

	private static ParserItem getItemByToken(ParserItem[] items, int token) {
		if (items == null)
			return null;
		for (ParserItem item : items)
			if (item != null && item.getToken() == token)
				return item;
		return null;
	}
}
