package com.broada.carrier.monitor.method.cli.config;

import java.util.ArrayList;
import java.util.List;

/**
 * CLI命令的内容或者文件引用配置项目
 * 
 * @author Eric Liu (liudh@broada.com.cn)
 */
public class Command implements Comparable<Command> ,Cloneable{
  private int id = 0;

  private boolean output = true;

  private String cmd;

  private long delay = 0;
  
  private List<Object> params = new ArrayList<Object>();

  private String cliPrompt = null;//为null的话，执行命令的时候会取默认的 系统配置
  
  /**
   * 得到命令内容
   */
  public String getCmd() {
    return cmd;
  }

  /**
   * 设置命令内容
   */
  public void setCmd(String command) {
    this.cmd = command.trim();
    /*
     * 去掉每一行的多余空格(首尾空格),在Linux可能由于多余空格发生意想不到的问题
     */
    cmd = cmd.replaceAll("\n+\\s*", "\n");
   // cmd = cmd.replaceAll("\n", "").replaceAll("\\s+", " ");
   // parseParam();
  }

  public int compareTo(Command o) {
    if (o == null)
      return -1;
    Command c = (Command) o;
    return id - c.id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isOutput() {
    return output;
  }

  public void setOutput(boolean output) {
    this.output = output;
  }

  public List<Object> getParams() {
    return params;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }
  
	public String getCliPrompt() {
		return cliPrompt;
	}

	public void setCliPrompt(String cliPrompt) {
		this.cliPrompt = cliPrompt;
	}
	
	public Object clone(){
		Command cmd = null;
		try {
			cmd = (Command)super.clone();
		} catch (CloneNotSupportedException e) {
		  InternalError ie = new InternalError();
		  ie.initCause(e);
			throw ie;
		}
		return cmd;
	}
}
