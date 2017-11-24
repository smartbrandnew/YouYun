package uyun.bat.web.api.resource.entity;

import java.util.List;

/**
 * Created by lilm on 17-5-24.
 */
public class SimpleResInfo {

    private String ip;
    private String hostname;
    private String os;
    private List<String> tags;
    private String type;

    public SimpleResInfo() {
    }

    public SimpleResInfo(String ip, String hostname, String os, List<String> tags, String type) {
        this.ip = ip;
        this.hostname = hostname;
        this.os = os;
        this.tags = tags;
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
