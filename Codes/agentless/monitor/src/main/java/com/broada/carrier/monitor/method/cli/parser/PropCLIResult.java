package com.broada.carrier.monitor.method.cli.parser;

import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;
import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;

/**
 * Properties形式的结果
 * 
 * @author 
 *
 */
public class PropCLIResult extends AbstractCLIResult {

  private Properties properties = null;

  public PropCLIResult(Properties properties,CLIErrorLine[] errLines) {
    this.properties = properties;
    this.errLines = errLines;
  }

  public List<Properties> getListTableResult() {
    throw new CLIRuntimeException("对属性的解析，不支持Table返回");
  }

  public Properties getPropResult() {
    return properties;
  }
  
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("类型：").append(getClass().getSimpleName()).append("\n");		
		sb.append("错误行：").append(getErrLines()).append("\n");
		sb.append("属性数据：").append(getPropResult());		
		return sb.toString();
	}  
}
