package com.broada.carrier.monitor.method.cli.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.broada.carrier.monitor.method.cli.parser.ParserRule;

/** 
 * @pdOid 5a0e1d1a-1b6f-4874-ac65-b459a22787cb
 * CLI 类别项，用于表达一个特定CLI命令或者脚本配置
 */
public class Category {

  /** @pdOid 49594185-0549-431c-8ef0-50278316c0c5 */
  private String name;

  /** @pdOid df0f9d22-2ae1-4732-ba88-eab37550d15e */
  private String sysversion;

  /** @pdOid 5b7e4018-8ba5-4240-a595-2246921e063d */
  private String description;

  /** @pdOid d284bdf2-3271-4b1c-8075-625447558eb4 */
  private List<Command> commands = new ArrayList<Command>();

  private String host = "";

  /** @pdRoleInfo migr=no name=ParserRule assc=association6 mult=0 type=Aggregation */
  public ParserRule parserRule;
  
  /** 该Category隶属的CLIConfiguration配置项 */
  private CLIConfiguration parentCLIConf;
  
  /** 该shellCmd放置推送的脚本 */
  private Command shellCmd = null;
  
  /** 远程脚本路径 */
  private String remoteFilePath;
  
  private String sequence ="1000";

  /**
   * 获取类别名称 
   */
  public String getName() {
    return name;
  }

  /**
   * 设置类别名称
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * 获取脚本或者命令内容
   */
  public List<Command> getCommands() {
    return commands;
  }

  /**
   * 设置脚本或者命令内容
   */
  public void addCommand(Command command) {
    commands.add(command);
    Collections.sort(commands);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * 获取脚本或者命令输出的解析规则配置
   */
  public ParserRule getParserRule() {
    return parserRule;
  }

  /**
   * 设置脚本或者命令的解析规则配置
   */
  public void setParserRule(ParserRule parserRule) {
    this.parserRule = parserRule;
  }

  /**
   * 得到CLI脚本或者命令的使用系统版本
   */
  public String getSysversion() {
    return sysversion;
  }

  /**
   * 设置CLI脚本或者命令的使用系统版本
   */
  public void setSysversion(String sysversion) {
    this.sysversion = sysversion;
  }

  String toPrintString() {
    return "Category name=" + name + "\t sysversion=" + sysversion + "\t desc=" + description;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public boolean equals(Object obj) {
    Category cate = (Category) obj;
    if (this.name.trim().equalsIgnoreCase(cate.getName().trim())
        && this.sysversion.trim().equalsIgnoreCase(cate.getSysversion().trim())
        && this.host.trim().equalsIgnoreCase(cate.getHost().trim()))
      return true;
    return false;
  }

	public CLIConfiguration getParentCLIConf() {
		return parentCLIConf;
	}

	public void setParentCLIConf(CLIConfiguration parentCLIConf) {
		this.parentCLIConf = parentCLIConf;
	}

	public Command getShellCmd() {
		return shellCmd;
	}

	public void setShellCmd(Command shellCmd) {
		this.shellCmd = shellCmd;
	}

	public String getRemoteFilePath() {
		return remoteFilePath;
	}

	public void setRemoteFilePath(String remoteFilePath) {
		this.remoteFilePath = remoteFilePath;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

}