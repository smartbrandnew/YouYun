/***********************************************************************************************************************
 * Module: CLIConfiguration.java Author: Broada Purpose: Defines the Class CLIConfiguration
 **********************************************************************************************************************/

package com.broada.carrier.monitor.method.cli.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** 
 * @pdOid b76ff376-080b-43bb-b39f-2dd06cd809d2
 * 
 * CLI的配置对象,对应一个系统System
 */
public class CLIConfiguration {

  /** 
   * @pdOid 82006c40-7279-4bb3-b810-25a38cf04ebe 
   * 系统名称
   */
  private String sysname;

  /** 
   * @pdOid c424888f-7d93-4e57-a977-693d67c36924
   * 命令配置项 
   */
  private List<Category> categories = new ArrayList<Category>();
  
  /**
   * 执行推送脚本的命令配置项(开始和结束)
   */
  private ShellInteract shellInteract;
  /**
   * 得到一个系统配置下面的所有命令配置项列表
   */
  public List<Category> getCategories() {
    return categories;
  }

  /**
   * 在系统配置下面添加一个命令配置项
   */
  public void addCategory(Category category) {
    categories.add(category);
    category.setParentCLIConf(this);
  }

  /**
   * 得到系统配置项的名称
   */
  public String getSysname() {
    return sysname;
  }

  /**
   * 设置系统配置项目的名称
   * @param system
   */
  public void setSysname(String system) {
    this.sysname = system;
  }
  
  public void addShellInteract(ShellInteract shellInteract){
  	this.shellInteract = shellInteract;
  }
  
	public ShellInteract getShellInteract() {
		return shellInteract;
	}
	
  String toPrintString() {
    StringBuffer sb = new StringBuffer();
    sb.append(sysname).append(" cli configuration begin ......").append("\n");
    for (Iterator<Category> iter = categories.iterator(); iter.hasNext();) {
      Category cate = (Category) iter.next();
      sb.append(cate.toPrintString()).append("\n");
    }
    sb.append(sysname).append(" cli configuration end   ......").append("\n");
    return sb.toString();
  }
}