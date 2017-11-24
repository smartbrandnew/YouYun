package com.broada.carrier.monitor.impl.mw.tomcat;

import com.broada.carrier.monitor.server.api.entity.PerfResult;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tomcat Monitor的管理类.
 */
public abstract class AbstractTomcatManager {
  
  /**
   * 监测的结果信息(用于监测结果详细描述).
   */
  private StringBuffer resultDesc = new StringBuffer();

  /**
   * 当前情况(用于发送告警信息).
   */
  private StringBuffer currentVal = new StringBuffer();
  
  /**
   * 通过URL和manager帐号去访问tomcat,并取得HTTPResponse.
   * @param url
   * @param user
   * @param password
   * @return
   * @throws IOException 
   * @throws HttpException 
   * @throws IOException
   * @throws ConnectException
   */
  @SuppressWarnings("deprecation")
  public GetMethod fetchResponse(String url, String user, String password) throws HttpException, IOException {
    HttpClient client = new HttpClient();

    client.getParams().setSoTimeout(30000);
    //设置一个GET请求
    GetMethod get = new GetMethod(url);

    NTCredentials upc = new NTCredentials();
    upc.setUserName(user == null ? "foo" : user);
    upc.setPassword(password == null ? "bar" : password);

    get.setDoAuthentication(true);
    client.getState().setCredentials(null, null, upc);
    client.setConnectionTimeout(30000);

    client.executeMethod(get);

    return get;
/*    URI uri = new URI(url);
    HTTPConnection hTTPConnection = new HTTPConnection(uri);
    hTTPConnection.addBasicAuthorization("Tomcat Manager Application", user, password);
    hTTPConnection.setAllowUserInteraction(false);

    return hTTPConnection.Get(uri.getPathAndQuery());*/
  }
  
  /**
   * 装配性能数据.
   * @param nameForIndex
   * @return
   */
  public PerfResult[] assemblePerf(List<PerfItemMap> nameForIndex) {
    List<PerfResult> perfResultList = new ArrayList<PerfResult>();
    for(Iterator<PerfItemMap> it = nameForIndex.iterator(); it.hasNext();){
      PerfItemMap perfItemMap = (PerfItemMap)it.next();
      PerfResult pr = new PerfResult(perfItemMap.getCode(), true);
      Object value = perfItemMap.getValue();
      
      if(value instanceof String){
        pr.setStrValue((String) value);
      }else if(value instanceof Double){
        pr.setValue(((Double)value).doubleValue());
      }      
      perfResultList.add(pr);
    }
    

    return (PerfResult[]) perfResultList.toArray(new PerfResult[0]);
  }
  
  
  /**
   * 获取Tomcat返回的监测数据.
   * @param is
   * @return
   * @throws Exception
   */
  public abstract Tomcat fetchInfo(InputStream is) throws Exception;
  
  public StringBuffer getCurrentVal() {
    return currentVal;
  }

  public StringBuffer getResultDesc() {
    return resultDesc;
  }


}
