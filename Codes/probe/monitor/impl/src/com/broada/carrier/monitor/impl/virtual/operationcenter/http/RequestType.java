package com.broada.carrier.monitor.impl.virtual.operationcenter.http;

public enum RequestType
{
    
    /**
     * HTTP GET 请求, SNMP 请求
     */
    GET,
    
    /**
     * HTTP POST 请求
     */
    POST,
    
    /**
     * HTTP PUT 请求
     */
    PUT,
    
    /**
     * HTTP DELETE 请求
     */
    DELETE,
    
    /**
     * SNMP SET操作
     */
    SET,
    
    /**
     * SNMP TRAP操作
     */
    TRAP,
    
    /**
     * FTP UPLOAD
     */
    UPLOAD,
    
    /**
     * FTP DOWNLOAD
     */
    DOWNLOAD;
    
}
