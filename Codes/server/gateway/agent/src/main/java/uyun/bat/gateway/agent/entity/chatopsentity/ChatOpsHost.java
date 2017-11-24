package uyun.bat.gateway.agent.entity.chatopsentity;

import uyun.bat.gateway.agent.entity.ResourceInfo;

import java.util.List;

public class ChatOpsHost {
    private String id;
    private String name;
    private String ip;
    private List<ResourceInfo> info;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<ResourceInfo> getInfo() {
        return info;
    }

    public void setInfo(List<ResourceInfo> info) {
        this.info = info;
    }

    public ChatOpsHost() {
    }

    public ChatOpsHost(String id, String name, String ip, List<ResourceInfo> info) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.info = info;
    }
}
