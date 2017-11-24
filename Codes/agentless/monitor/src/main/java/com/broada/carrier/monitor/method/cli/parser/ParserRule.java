package com.broada.carrier.monitor.method.cli.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/** 
 * @pdOid 1be855af-2742-4a8b-955e-541abc1b803a 
 * 解析规则类
 * 
 */
public class ParserRule {
  /** @pdOid f3b4f01d-a352-46c0-913a-6c6562f68eb7 */
  private int start;

  /** @pdOid 7331f77d-5359-4347-8e63-d389ff2ea8ab */
  private int end;

  /** @pdOid d6cfd030-444b-4517-88e9-ff7ffbf00753 */
  private String delimeter;
  private Pattern delimeterPattern;

  /** @pdOid 39cadadc-50db-476a-bbf9-5135921bbc96 */
  private List<ParserItem> parserItems = new ArrayList<ParserItem>();

  public Bsh bsh;

  private String datatype;
  
  /**
   * 数据表格的标题行的行号。标题行表明表格中数据的含义。
   * 它可以用来决定哪些数据是可获取的。
   * 由于操作系统的版本问题，可能导致某些监测数据在某些版本上不可获取，如linux的io监测器。
   * 若为-1，表示未指定标题行号。
   */
  private int titleLineNo = -1;
  /**
   * 数据表格的标题行的关键字。标题行表明表格中数据的含义。
   * 它可以用来决定哪些数据是可获取的。
   * 由于操作系统的版本问题，可能导致某些监测数据在某些版本上不可获取，如linux的io监测器。
   * 若为null，表示未指定标题行关键字。
   */
  private String titleKeyword = null;
  /**
   * 数据表格的标题行的被忽略的字符串的正则表达式。匹配的字符串部分将被忽略。
   */
  private String titleIgnore;
  /**
   * 数据表格的标题行的被忽略的字符串的正则表达式。匹配的字符串部分将被忽略。
   */
  private Pattern titleIgnorePattern;

  public String getDatatype() {
    return datatype;
  }

  public void setDatatype(String datatype) {
    this.datatype = datatype;
  }

  public Bsh getBsh() {
    return bsh;
  }

  public void setBsh(Bsh bsh) {
    this.bsh = bsh;
  }

  public String getDelimeter() {
    return delimeter;
  }

  public void setDelimeter(String delimeter) {
    this.delimeter = delimeter;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public List<ParserItem> getParserItems() {
    return parserItems;
  }

  public void addParserItem(ParserItem parserItem) {
    parserItems.add(parserItem);
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  /**
   * 数据表格的标题行的行号。标题行表明表格中数据的含义。
   * 它可以用来决定哪些数据是可获取的。
   * 由于操作系统的版本问题，可能导致某些监测数据在某些版本上不可获取，如linux的io监测器。
   * 若为-1，表示未指定标题行号。
   */
  public int getTitleLineNo() {
    return titleLineNo;
  }
  
  /**
   * 数据表格的标题行号。标题行表明表格中数据的含义。
   * 它可以用来决定哪些数据是可获取的。
   * 由于操作系统的版本问题，可能导致某些监测数据在某些版本上不可获取，如linux的io监测器。
   * 若返回-1，表示标题行未找到。
   */
  public int getTitleLineNo(String[] lines) {
    if (titleLineNo >= 0)
      return titleLineNo < lines.length ? titleLineNo : -1;
    if (titleKeyword != null) {
      for (int i = 0; i<lines.length; i++) {
        if (StringUtils.contains(lines[i], titleKeyword))
          return i;
      }
    }
    return -1;
  }

  /**
   * 数据表格的标题行的行号或关键字。标题行表明表格中数据的含义。
   * 它可以用来决定哪些数据是可获取的。
   * 由于操作系统的版本问题，可能导致某些监测数据在某些版本上不可获取，如linux的io监测器。
   */
  public void setTitle(String title) {
    titleLineNo = -1;
    titleKeyword = null;
    try {
      this.titleLineNo = Integer.parseInt(title);
    } catch (NumberFormatException e) {
      titleKeyword = title;
    }
  }

  /**
   * 是否有数据表格的标题行。
   */
  public boolean hasTitle() {
    return titleLineNo >= 0 || titleKeyword != null;
  }

  /**
   * 数据表格的标题行的被忽略的字符串的正则表达式。匹配的字符串部分将被忽略。
   */
  public String getTitleIgnore() {
    return titleIgnore;
  }

  /**
   * 数据表格的标题行的被忽略的字符串的正则表达式。匹配的字符串部分将被忽略。
   */
  public void setTitleIgnore(String titleIgnore) {
    if (StringUtils.isBlank(titleIgnore)) {
      this.titleIgnore = null;
      titleIgnorePattern = null;
    } else {
      this.titleIgnore = titleIgnore.trim();
      titleIgnorePattern = Pattern.compile(this.titleIgnore);
    }
  }

  /**
   * 数据表格的标题行的被忽略的字符串的正则表达式。匹配的字符串部分将被忽略。
   */
  Pattern getTitleIgnorePattern() {
    return titleIgnorePattern;
  }

	public Pattern getDelimeterPattern() {
		if (delimeterPattern == null) {
			if (delimeter.endsWith("+"))
				delimeterPattern = Pattern.compile(delimeter);
			else
				delimeterPattern = Pattern.compile(delimeter + "+");
		}
		return delimeterPattern;		
	}
}