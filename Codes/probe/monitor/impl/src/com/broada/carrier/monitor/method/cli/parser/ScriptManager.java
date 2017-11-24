package com.broada.carrier.monitor.method.cli.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import bsh.engine.BshScriptEngine;
import bsh.engine.BshScriptEngineFactory;

/** 
 * @pdOid a70fe695-1324-42e7-9904-fe0d58b01178
 * JAVA相关脚本语言管理器</p>
 * attention:目前eval*,exec*,call方法仅仅在beanshell上进行了测试
 */
public class ScriptManager {
	public static final String LANG_BSH = "bsh";

  private static ScriptEngineManager manager = new ScriptEngineManager();
  private ScriptEngine engine;  

  private static final Map<String, ScriptManager> langMap = new HashMap<String, ScriptManager>();
  private static Map<String, ScriptManager> managers = new HashMap<String, ScriptManager>();
  
  static {  	
  	ScriptEngineFactory factory = new BshScriptEngineFactory();
  	manager.registerEngineName(LANG_BSH, factory);
  	manager.registerEngineName("beanshell", factory);
  }

  private ScriptManager(String lang) {  	
    this.engine = manager.getEngineByName(lang);
  }

  /**
   * 获取ScriptManager,系统针对每个Script类型，都提供一个共享的ScriptManager
   * @param lang 可以接受语言的参数，目前只支持beanshell.
   * @return 管理器，如果不支持lang，返回Null
   */
  public static ScriptManager getInstance(String lang) {
    if (langMap.containsKey(lang)) {
      return (ScriptManager) langMap.get(lang);
    } else {
      ScriptManager manager = new ScriptManager(lang);
      langMap.put(lang, manager);
      return manager;
    }
  }

  /**
   * 根据语言类型，单独创建一个独享的ScriptManager
   * @param lang 可以接受语言的参数，目前只支持beanshell
   * @return 管理器，如果不支持lang，返回Null
   */
  public static ScriptManager newInstance(String lang) {
  	return new ScriptManager(lang);
  }
  
  /**
   * 执行一段Script，如果此Script之前执行过，则使用预先编译的方式执行，以大幅提高性能
   * 注意仅满足以下几点时，才会使用预先编译：
   * 1. 目前是bsh语言
   * 2. 脚本必须已经执行过一次
   * @param lang 语言
   * @param script 脚本 
   * @param paramName 参数名称
   * @param paramValue 参数值
   * @throws Throwable 
   */
  public static Object evalByCompiled(String lang, String script, String paramName, Object paramValue) throws Throwable {
  	if (!LANG_BSH.equalsIgnoreCase(lang)) {
  		ScriptManager manager = newInstance(lang);
  		manager.put(paramName, paramValue);
  		return manager.eval(script);
  	}
  	
  	ScriptManager manager = managers.get(script);
  	if (manager == null)  {
  		manager = ScriptManager.newInstance(lang);
  		manager.eval("fun(" + paramName + "){" + script + ";}");
  		managers.put(script, manager);
  	}
  	
  	synchronized (manager) {
      manager.put(paramName, paramValue);
      return manager.eval("return fun(" + paramName + ")");					
		}
  }

  /** 
   * 将环境变量对象设置到脚本环境中
   * @param paramName 对象名称
   * @param paramValue 对象
   * @return 是否设置成功
   */
  public boolean put(String paramName, Object paramValue) {
  	engine.put(paramName, paramValue);
  	return true;
  }

  /** 
   * 将环境变量对象从脚本环境中移出
   * @param paramName 对象名称
   * @param paramValue 对象
   * @return 是否移出成功
   */
  public boolean remove(String paramName) {
  	engine.put(paramName, null);
  	return true;
  }

  /**
   * 根据参数获取参数对象
   * @param paramName 参数名
   * @return 如果没有，返回null
   */
  public Object get(String paramName) {
  	return engine.get(paramName);    
  }

  /**
   * 执行脚本,并返回结果
   */
  public Object eval(String script) throws Exception {
    return engine.eval(script);
  }

  /**
   * 根据文件名执行脚本，并返回结果
   */
  public Object evalScriptFile(String scriptFile) throws Exception {
    String script;
    try {
      script = readFileContent(scriptFile);
    } catch (FileNotFoundException ex) {
      throw new Exception("文件" + scriptFile + "没有找到!", ex);
    } catch (IOException ex) {
      throw new Exception("读取文件" + scriptFile + "过程中发生IO错误!", ex);
    }
    return engine.eval(script);
  }

  /**
   * 执行脚本
   */
  public void exec(String script) throws Exception {
  	engine.eval(script);
  }

  /**
   * 根据文件名执行脚本
   */
  public void execScriptFile(String scriptFile) throws Exception {
    String script;
    try {
      script = readFileContent(scriptFile);
    } catch (FileNotFoundException ex) {
      throw new Exception("文件" + scriptFile + "没有找到!", ex);
    } catch (IOException ex) {
      throw new Exception("读取文件" + scriptFile + "过程中发生IO错误!", ex);
    }  
    engine.eval(script);
  }

  /**
   * 执行脚本的某个函数
   * @param object 对象，可以为null
   * @param name 方法名
   * @param args 参数
   * @return 返回的值
   */
  public Object call(Object object, String name, Object[] args) throws Exception {
  	if (engine instanceof BshScriptEngine) {
  		BshScriptEngine bshEngine = (BshScriptEngine)engine;
  		if (object == null)
  			return bshEngine.invoke(name, args); 
  		else
  			return bshEngine.invoke(object, name, args);
  	} else
  		throw new RuntimeException("非BSH引擎[" + engine + "]，未实现call方法");
  }

  private String readFileContent(String scriptFile) throws FileNotFoundException, IOException {
    BufferedReader r = new BufferedReader(new FileReader(scriptFile));
    StringWriter w = new StringWriter();
    String line = null;
    while ((line = r.readLine()) != null) {
      w.write(line + "\n");
    }
    r.close();
    return w.toString();
  }
}
