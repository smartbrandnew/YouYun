package com.broada.carrier.monitor.impl.virtual.operationcenter.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class RequestEntity{
    private static List<String> headerDisplayKey = new ArrayList<String>();
    
    private HttpsNode node;
    
    private RequestType reqType;
    
    private Map<String, String> header;
    
    private Map<String, String> nameValuePairs;
    
    private String content;
    
    private String url;
    
    public HttpsNode getNode(){
        return node;
    }
    
    public void setNode(HttpsNode node){
        this.node = node;
    }
    
    public RequestType getReqType(){
        return reqType;
    }
    
    public void setReqType(RequestType reqType){
        this.reqType = reqType;
    }
    
    public Map<String, String> getHeader(){
        return header;
    }
    
    public void setHeader(Map<String, String> header){
        this.header = header;
    }
    
    public Map<String, String> getNameValuePairs(){
        return nameValuePairs;
    }
    
    public void setNameValuePairs(Map<String, String> nameValuePairs){
        this.nameValuePairs = nameValuePairs;
    }
    
    public String getContent(){
        return content;
    }
    
    public void setContent(String content){
        this.content = content;
    }
    
    public String getUrl(){
        return url;
    }
    
    public void setUrl(String url){
        this.url = url;
    }
    
    /**
     * 打印header中的内容
     * <br>如果在白名单中打印，否则打印*
     * @return header内容
     * @see [类、类#方法、类#成员]
     */
    public String headerToString(){
        StringBuffer strBuf = new StringBuffer();
        if (null != this.header){
            Iterator<Entry<String, String>> it = this.header.entrySet().iterator();
            Entry<String, String> entry = null;
            String value = null;
            String key = null;
            while (it.hasNext()){
                entry = it.next();
                value = entry.getValue();
                key = entry.getKey();
                if (headerDisplayKey.contains(key.toLowerCase(Locale.getDefault()))){
                    strBuf.append(key).append(":").append(value).append(',');
                } else{
                    if (null == value){
                        strBuf.append(key).append(": is null").append(',');
                    } else if ("".equals(value)){
                        strBuf.append(key).append(": is blank").append(',');
                    } else{
                        strBuf.append(key).append(":").append(hideString(value)).append(',');
                    }
                }
            }
        }
        return strBuf.toString();
    }
    
    /**
     * 隐藏字符串，将源字符串中的字符替换为‘*’
     * @return 替换后的字符串
     */
    private String hideString(String src){
        String target = "******";
        return target;
    }
    
    /**
     * 打印请求消息头中的字段，白名单，在其中的打印，不在其中的盘空打印。
     */
    static{
        headerDisplayKey.clear();
        headerDisplayKey.add("soapaction");
        headerDisplayKey.add("content-type");
        headerDisplayKey.add("accept");
        headerDisplayKey.add("x-auth-user");
        headerDisplayKey.add("x-auth-usertype");
        headerDisplayKey.add("accept-language");
        headerDisplayKey.add("userName");
        headerDisplayKey.add("domainType");
    }
}
