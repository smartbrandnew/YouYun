package com.broada.carrier.monitor.impl.virtual.operationcenter.http;

import java.util.Map;

public class ResponseEntity {
    private String retCode;
    
    private String errorMsg;
    
    private int httpCode;
    
    private Map<String, String> header;
    
    private Object content;
    
    public String getRetCode(){
        return retCode;
    }
    
    public void setRetCode(String retCode){
        this.retCode = retCode;
    }
    
    public int getHttpCode(){
        return httpCode;
    }
    
    public void setHttpCode(int httpCode){
        this.httpCode = httpCode;
    }
    
    public Map<String, String> getHeader(){
        return header;
    }
    
    public void setHeader(Map<String, String> header){
        this.header = header;
    }
    
    public String getErrorMsg(){
        return errorMsg;
    }
    
    public void setErrorMsg(String errorMsg){
        this.errorMsg = errorMsg;
    }
    
    public Object getContent(){
        return content;
    }
    
    public void setContent(Object content){
        this.content = content;
    }
    
}
