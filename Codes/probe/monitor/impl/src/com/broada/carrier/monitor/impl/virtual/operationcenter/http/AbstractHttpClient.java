package com.broada.carrier.monitor.impl.virtual.operationcenter.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpClient{
    /**
     * HTTP请求连接超时时间
     */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpClient.class);
    
    private static final int CONN_TIMEOUT = 300 * 1000;
    
    /**
     * HTTP请求读取字节流超时时间
     */
    private static final int READ_TIMEOUT = 300 * 1000;
    
    /**
     * HTTP请求内容类型
     */
    private static final String CONTENT_TYPE = "Content-Type";
    
    /**
     * HTTP请求字符集设置
     */
    private static final String CHARSET = "charset";
    
    /**
     * HTTP PUT 请求
     * @param entity 请求数据实体
     * @return 请求响应
     */
    public abstract ResponseEntity put(RequestEntity entity);
    
    /**
     * HTTP DELETE 请求
     * @param entity 请求数据实体
     * @return 请求响应
     */
    public abstract ResponseEntity delete(RequestEntity entity);
    
    /**
     * HTTP POST 请求
     * @param entity 请求数据实体
     * @return 请求响应
     */
    public abstract ResponseEntity post(RequestEntity entity);
    
    /**
     * HTTP GET 请求
     * @param entity 请求数据实体
     * @return 请求响应
     */
    public abstract ResponseEntity get(RequestEntity entity);
    
    /**
     * {@inheritDoc}
     */
    public ResponseEntity sendRequest(RequestEntity entity){
        ResponseEntity response = null;
        RequestType reqType = entity.getReqType();
        switch (reqType)
        {
            case GET:
            {
                response = this.get(entity);
                break;
            }
            case POST:
            {
                response = this.post(entity);
                break;
            }
            case PUT:
            {
                response = this.put(entity);
                break;
            }
            case DELETE:
            {
                response = this.delete(entity);
                break;
            }
            default:
            {
                LOGGER.error("method type of request is error");
            }
        }
        return response;
    }
    
    /**
     * 给POST和PUT请求设置请求内容
     * @param reqType 方法类型
     * @param content 请求内容
     * @param connection 连接
     */
    public void setContent(RequestType reqType, String content, URLConnection connection){
        OutputStream outputStream = null;
        try {
            if (reqType.equals(RequestType.PUT) || reqType.equals(RequestType.POST)) {
                if (null != content) {
                    outputStream = connection.getOutputStream();
                    outputStream.write(content.getBytes("UTF-8"));
                    outputStream.flush();
                }
            }
        } catch (Exception e){
            LOGGER.error("io Exception:" + e);
        } finally {
            if (null != outputStream){
                try{
                    outputStream.close();
                } catch (IOException e){
                    LOGGER.error("io Exception");
                }
            }
        }
        
    }
    
    /**
     * 设置连接的基本参数
     * @param connection 连接
     */
    public void setConnection(URLConnection connection){
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(CONN_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
    }
    
    /**
     * 获取响应消息
     * @param connection 连接
     * @param charset 编码格式
     * @return 响应消息
     */
    public String getResponseContent(URLConnection connection, String charset){
        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try{
            inputStream = connection.getInputStream();
            if (null != charset) {
                reader = new BufferedReader(new InputStreamReader(inputStream, charset));
            } else {
                reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            }
            String currentLine = "";
            while ((currentLine = reader.readLine()) != null){
                if (currentLine.length() > 0){
                    sb.append(currentLine.trim());
                }
            }
        } catch (IOException e) {
            
            LOGGER.error("io Exception");
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                LOGGER.error("Exception");
            }
        }
        return sb.toString();
    }
    
    /**
     * 设置HTTP请求的Header
     * @param header HTTP请求的header信息
     * @param connection 连接
     */
    public void setHeader(Map<String, String> header, URLConnection connection){
        if (null != header) {
            Set<Entry<String, String>> entrySet = header.entrySet();
            Iterator<Entry<String, String>> it = entrySet.iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * 将 HTTP请求中的键值对转换为HTTP请求标准字符串
     * @param nameValuePairs 键值对
     * @return 标准的HTTP请求字符串
     */
    public String convertNameValuePair(Map<String, String> nameValuePairs) {
        StringBuffer sb = new StringBuffer();
        if (null != nameValuePairs) {
            Iterator<Entry<String, String>> it = nameValuePairs.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entity = it.next();
                sb.append(entity.getKey());
                sb.append('=');
                sb.append(entity.getValue());
                if (it.hasNext()) {
                    sb.append('&');
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 获取响应Header
     * @param connection Http连接
     * @return Header解析后的map
     */
    public Map<String, String> getResponseHeader(URLConnection connection) {
        Map<String, String> responseHeader = new HashMap<String, String>();
        Set<String> fieldNames = connection.getHeaderFields().keySet();
        if (null != fieldNames) {
            for (String fieldName : fieldNames){
                responseHeader.put(fieldName, connection.getHeaderField(fieldName));
            }
        }
        return responseHeader;
    }
    
    /**
     * 获取HTTP请求的字符集
     * @param header HTTP请求的消息头
     * @return HTTP请求字符集
     */
    public String getCharset(Map<String, String> header) {
        String charset = null;
        if (null == header) {
            LOGGER.error("header is null, maybe template's header is null or not configed");
            return charset;
        }
        String contentType = header.get(CONTENT_TYPE);
        if (null != contentType) {
            String[] temp = contentType.split(CHARSET);
            if (null != temp && temp.length > 1) {
                charset = temp[1].substring(1);
            }
        }
        return charset;
    }
}
