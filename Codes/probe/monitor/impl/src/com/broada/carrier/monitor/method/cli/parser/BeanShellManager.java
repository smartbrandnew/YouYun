package com.broada.carrier.monitor.method.cli.parser;

import java.io.FileReader;
import java.io.Reader;

import bsh.EvalError;
import bsh.Interpreter;

import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;

public class BeanShellManager {

  private Interpreter interpreter = new Interpreter();

  public void put(String paraName, Object paramValue) {
    try {
      interpreter.set(paraName, paramValue);
    } catch (EvalError e) {
      throw new CLIRuntimeException("设置BeanShell参数发生错误", e);
    }
  }

  public Object eval(String script) {
    try {
      return interpreter.eval(script);
    } catch (EvalError e) {
      throw new CLIRuntimeException("BeanShell解析发生错误", e);
    }
  }

  public Object evalScriptFile(String scriptFile) {
    try {
      Reader reader = new FileReader(scriptFile);
      return interpreter.eval(reader);
    } catch (Exception e) {
      throw new CLIRuntimeException("BeanShell解析发生错误", e);
    }
  }

}