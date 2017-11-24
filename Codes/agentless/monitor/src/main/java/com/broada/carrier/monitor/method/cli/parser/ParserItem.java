package com.broada.carrier.monitor.method.cli.parser;

import java.util.regex.Pattern;

public class ParserItem implements Cloneable {
  /**
   * 行号，从0开始
   */
  private int line;

  /**
   * 对table方式有效，表示行的起始字符
   */
  private int start;

  /**
   * 对table方式有效，表示行的结束字符，如果是-1则表示到行尾
   */
  private int end;

  /**
   * 按分隔符分割只后第几个元素，从0开始
   */
  private int token;

  /**
   * 列名
   */
  private String name;

  /**
   * beanshell表达式计算
   */
  private String bsh;
  
  /**
   * 在数据表格的标题中，该数据项的名字。
   */
  private String titleName;
  private Pattern titleNamePattern;
  
  public ParserItem() {
		super();
	}
  
	public ParserItem(String name, int token) {
		super();
		this.name = name;
		this.token = token;
	}

	public ParserItem(int start, int end, int token) {
		super();
		this.start = start;
		this.end = end;
		this.token = token;
	}

	public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public String getBsh() {
    return bsh;
  }

  public void setBsh(String end) {
    this.bsh = end;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    titleNamePattern = null;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getToken() {
    return token;
  }

  public void setToken(int token) {
    this.token = token;
  }

  /**
   * 在数据表格的标题中，该数据项的名字
   */
  public Pattern getTitleNamePattern() {
    if (titleNamePattern == null)
      titleNamePattern = Pattern.compile(titleName == null ? name : titleName);
    return titleNamePattern;
  }

  /**
   * 在数据表格的标题中，该数据项的名字
   */
  public void setTitleName(String titleName) {
    this.titleName = titleName;
    titleNamePattern = null;
  }

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}