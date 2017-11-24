/***********************************************************************
 * Module:  CLIParser.java
 * Author:  Broada
 * Purpose: Defines the Interface CLIParser
 ***********************************************************************/

package com.broada.carrier.monitor.method.cli.parser;

import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;

/** 
 * @pdOid b31dbfd1-432f-4f7d-9dda-f3660d64913b 
 * 
 * 解析结果接口
 */
public interface CLIParser {
   /**
    * 对采集结果进行解析,返回解析后的数据
    * @param collectData 采集到的数据
    * @param paserRule 解析规则
    * @return
    * @throws CLIResultParseException 当解析出错时抛出
    */
   CLIResult parse(String collectData, ParserRule paserRule) throws CLIResultParseException;
   /**
    * 根据配置文件将返回的错误结果中文化
    * @param message 返回的结果
    * @param buffer 存放原始结果和可能中文化的错误提示
    */
   void messageLocalized(String message,StringBuffer buffer);
}