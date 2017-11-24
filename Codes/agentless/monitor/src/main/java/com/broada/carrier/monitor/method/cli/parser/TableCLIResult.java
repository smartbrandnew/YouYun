package com.broada.carrier.monitor.method.cli.parser;

import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;
import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;

/**
 * table形式的结果
 * 
 * @author 
 *
 */
public class TableCLIResult extends AbstractCLIResult {

  private List<Properties> tables = null;

  public TableCLIResult(List<Properties> tables,CLIErrorLine[] errLines) {
    this.tables = tables;
    this.errLines = errLines;
  }

  public List<Properties> getListTableResult() {
    return tables;
  }

  public Properties getPropResult() {
    throw new CLIRuntimeException("对表格的解析，不支持Property返回");
  }
  
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("类型：").append(getClass().getSimpleName()).append("\n");		
		sb.append("错误行：").append(getErrLines()).append("\n");
		sb.append("表格数据：").append(getListTableResult()).append("\n");	
		return sb.toString();
	}  
}
