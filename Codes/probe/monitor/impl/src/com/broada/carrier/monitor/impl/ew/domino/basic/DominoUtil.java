/**
 * 
 */
package com.broada.carrier.monitor.impl.ew.domino.basic;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

/**
 * @author xxb
 *
 */
public class DominoUtil {

	/**
	 * 
	 */
	private DominoUtil() {
		super();
		// TODO Auto-generated constructor stub
	}
	
  /**
   * 获取IOR
   * @param ipAddr
   * @param port
   * @return
   * @throws IOException 当网络不通，获取错误时抛出
   */
  @SuppressWarnings("deprecation")
	public static String getIOR(String ipAddr,int port) throws IOException{
    HttpClient client = new HttpClient();
    client.getParams().setSoTimeout(30000);
    GetMethod get = new GetMethod("http://"+ipAddr+":"+port+"/diiop_ior.txt");
    //设置连接超时    
    client.setConnectionTimeout(30000);
    //通过客户端执行GET方法
    try {
      client.executeMethod(get);
      String ior=get.getResponseBodyAsString();
      return ior;
    }finally{
      try {
        get.releaseConnection();
      } catch (RuntimeException e) {
      }
    }
  }
}
