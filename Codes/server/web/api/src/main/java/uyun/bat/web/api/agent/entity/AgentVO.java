package uyun.bat.web.api.agent.entity;

import java.util.ArrayList;
import java.util.List;

public class AgentVO {

    private String id;
    private String hostname;
    private String ip;
    private List<String> tags=new ArrayList<>();
    private List<String> apps;
    private String source;

    public AgentVO() {
    }

    public AgentVO(String id, String hostname, String ip, List<String> tags, List<String> apps, String source) {
        this.id = id;
        this.hostname = hostname;
        this.ip = ip;
        this.tags = tags;
        this.apps = apps;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getApps() {
        return apps;
    }

    public void setApps(List<String> apps) {
        this.apps = apps;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
