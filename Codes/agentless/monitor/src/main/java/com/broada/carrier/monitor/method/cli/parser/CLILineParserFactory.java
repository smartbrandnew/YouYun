package com.broada.carrier.monitor.method.cli.parser;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;

public class CLILineParserFactory {

  public static CLILineParser getCLILineParser(String style) {
    if (style != null && style.equalsIgnoreCase(CLIConstant.DATATYPE_TABLE)) {
      return new TableCLILineParser();
    } else if (style != null && style.equalsIgnoreCase(CLIConstant.DATATYPE_PROPERTY)) {
      return new PropertyCLILineParser();
    } else if (style != null && style.equalsIgnoreCase(CLIConstant.DATATYPE_BLOCK)) {
      return new BlockCLILineParser();
    } else {
      throw new CLIRuntimeException("不支持的解析方式：" + style);
    }
  }
}
