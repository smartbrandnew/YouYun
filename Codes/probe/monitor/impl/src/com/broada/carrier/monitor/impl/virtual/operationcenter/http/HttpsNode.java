package com.broada.carrier.monitor.impl.virtual.operationcenter.http;

public class HttpsNode {
    private String nodeType;
    
    private String name;
    
    private String ip;
    
    private String port;
    
    public String getNodeType(){
        return nodeType;
    }
    
    public void setNodeType(String nodeType){
        this.nodeType = nodeType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip){
        this.ip = ip;
    }
    
    public String getPort(){
        return port;
    }
    
    public void setPort(String port){
        this.port = port;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Node");
        sb.append("[ nodeType : " + this.nodeType);
        sb.append(", name : " + this.name);
        sb.append(", ip : " + this.ip);
        sb.append(", port : " + this.port);
        sb.append(']');
        return sb.toString();
    }
    
    public HttpsNode() {
		super();
	}
    
    public HttpsNode(String name, String ip, String port, String nodeType) {
		super();
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.nodeType = nodeType;
	}
}
