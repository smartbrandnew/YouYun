package com.broada.carrier.monitor.method.cli.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.base.utils.BDProperties;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.utils.StringUtil;

/**
 * 
 * 解析结果实现类
 * 
 * @author Administrator
 *
 */
public class DefaultCLIParser implements CLIParser {

  private static final Log logger = LogFactory.getLog(DefaultCLIParser.class);
  private static final BDProperties cliMessageZhProps = new BDProperties();
  private static final CLIParser cliParser = new DefaultCLIParser();
  private DefaultCLIParser() {
  	try {
			InputStream ins = new FileInputStream(CLIConstant.CLI_MESSAGES_ZH_CONFIG);
			cliMessageZhProps.setKeyValueSeparators("=");
			cliMessageZhProps.load(ins);
		} catch (IOException e) {
			logger.error("加载" + CLIConstant.CLI_MESSAGES_ZH_CONFIG + "文件出错.", e);
		}
  }

  /**
   * 获取单例
   * 
   * @return
   */
  public static CLIParser getInstance() {
    return cliParser;
  }
  
  /*
   * 经过本次改造后,解析异常是不会抛出去了,只有查看日志才能看到原始的异常信息
   * @see com.broada.carrier.monitor.method.cli.parser.CLIParser#parse(java.lang.String, com.broada.carrier.monitor.method.cli.parser.ParserRule)
   */
  public CLIResult parse(String collectData, ParserRule parserRule) throws CLIResultParseException {
  	try {
  		List<CLIErrorLine> errLines = new ArrayList<CLIErrorLine>();
	    if (parserRule == null) {
	    	if (logger.isDebugEnabled())
	    		printParse(collectData, parserRule, null);
	      return null;
	    }
	    /*
	     * 预处理
	     */
	    Object object = "";
	    try {
	      object = preparse(collectData, parserRule);
	    } catch (CLIResultParseException e) {	    	
	    	throw new CLIResultParseException(ErrorUtil.createMessage("解析CLI输出内容失败", e), e);
	    }
	    /*
	     * 如果是字符串
	     */
	    if (object instanceof String) {
	      collectData = (String) object;
	      /*
	       * 创建行解析器
	       */
	      CLILineParser cliLineParser = CLILineParserFactory.getCLILineParser(parserRule.getDatatype());
	      /*
	       * 按换行符将字符串分行
	       */
	      String[] lines = collectData.split(CLIConstant.LINE_SPILTTER);
	      /*
	       * 解析数据表格的标题
	       */
	      int title = 0;
	      if (parserRule.hasTitle()) {
	        title = parserRule.getTitleLineNo(lines);
	        if (title < 0)
	          throw new CLIResultParseException("未找到标题行。");
	        cliLineParser.parseLine(parserRule.getTitleLineNo(), lines[title], parserRule);
	        title++;
	      }
	
	      /*
	       * 逐行处理
	       */
	      int errLineCount = 0;
	      boolean hasSomeLineOk = false;
	      CLIResultParseException error = null;
	      for (int index = title + parserRule.getStart(); index < lines.length - parserRule.getEnd(); index++) {
	        String line = lines[index];
	        if (logger.isDebugEnabled()) {
	          logger.debug("处理的行数:" + (index - errLineCount) + "-" + line);
	        }
	         
	        if (line.endsWith(": not found") || line.endsWith(": command not found")) {
	        	logger.warn(String.format("输出文本“%s”应属于命令执行失败，将被过滤", line));
	        	errLineCount++;
	        	continue;
	        }
	
	        if (!StringUtil.isNullOrBlank(line)) {
	          try {
	            cliLineParser.parseLine(index - errLineCount, line, parserRule);
	            hasSomeLineOk = true;
	          } catch (Throwable e) {
	            // modify by huangjb 2008/11/28 记录解析出错的行号及内容(如果配置了预处理脚本，则记录的跟获取回来的原始数据可能有差别)
	            errLines.add(new CLIErrorLine(index, line));
	            //add panghf 2011-10-12 为了方便一些结果解析代码可以log节点、命令相关信息加入
	            String errorMsg;
	            if(e instanceof CLIResultParseException){
	            	errorMsg = e.getMessage();
	            }else if(e instanceof StringIndexOutOfBoundsException){
	            	errorMsg ="文本的长度过短，无法提取指定列的数据";
	            }else{
	            	errorMsg = e.toString();
	            }
	            error = new CLIResultParseException(String.format("解析CLI输出内容失败，解析文本[行号：%d 内容：%s]，解析错误：%s", index, line, errorMsg), e);
	            logger.warn(error.getMessage());
	            logger.debug("堆栈：", e);
	          }
	        }
	      }
		      
	      if (hasSomeLineOk)
	      	object = cliLineParser.getResult();
	      else if (error != null)
	      	throw error;
	    }
	
	    /*
	     * 将结果包装到CLIResult中
	     */
	    CLIResult result;
	    if (object instanceof Properties)
	    	result = new PropCLIResult((Properties) object,errLines.toArray(new CLIErrorLine[errLines.size()]));
	    else if (object instanceof List) 
	    	result = new TableCLIResult((List<Properties>)object,errLines.toArray(new CLIErrorLine[errLines.size()]));
      else
      	result = null;
	    if (logger.isDebugEnabled())
    		printParse(collectData, parserRule, result);
	    return result;
  	} catch (Throwable err) {  		
  		printParse(collectData, parserRule, err);
  		throw new CLIResultParseException(err);
  	}
  }

  private void printParse(String collectData, ParserRule parserRule, Object result) {
  	StringBuffer sb = new StringBuffer();
  	sb.append("解析输入：\n").append(collectData).append("\n==============\n");
  	sb.append("解析结果：\n");
  	if (result == null)
  		sb.append("null");  		
  	else if (result instanceof Throwable)  		
  		sb.append(result);  		
  	else
  		sb.append(result);
  	sb.append("\n==============");
  	logger.debug(sb);
	}

	/**
   * 脚本解析预处理
   * @param collectData
   * @param parserRule
   * @return
   * @throws CLIResultParseException 脚本解析失败时抛出
   */
  protected Object preparse(String collectData, ParserRule parserRule) throws CLIResultParseException {
    Bsh bsh = parserRule.getBsh();
    /*
     * 如果没有配置beanshell预处理，直接返回
     */
    if (bsh == null) {
      return collectData;
    }
    ScriptManager manager = ScriptManager.newInstance("beanshell");
    manager.put(CLIConstant.BSH_COLLECT_DATA, collectData);
    manager.put(CLIConstant.BSH_PARSERRULE, parserRule);

    /*
     * 如果配置了file则优先采用脚本文件
     */
    try {
      if (!StringUtil.isNullOrBlank(bsh.getFile()))
        return manager.evalScriptFile(bsh.getFile());
      else if (!StringUtil.isNullOrBlank(bsh.getScript()))
        return manager.eval(bsh.getScript());
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        //add panghf 2011-10-12 为了方便一些结果解析代码可以log节点、命令相关信息加入
        ThreadLocal<String> nodeInfo=new ThreadLocal<String>();
        String node=nodeInfo.get();
        logger.debug((node==null?"":node+",")+"运行脚本解析时发生异常，采集结果为:" + collectData, e);
      }
      throw new CLIResultParseException("运行脚本解析时发生异常，错误：" + e, e);
    }
    return collectData;
  }

	public void messageLocalized(String message, StringBuffer buffer) {
		buffer.append(message);
		Enumeration<Object> enu =cliMessageZhProps.keys();
		while(enu.hasMoreElements()){
			String key = enu.nextElement().toString();
			if(message != null && message.contains(key.trim())){
				buffer.append("\n出错提示:"+cliMessageZhProps.getProperty(key));
			}
		}
	}
}
