package uyun.bat.web.api.resource.entity;

import java.util.List;

public class ResourceVO1 {
    private String id;
    private String resourceType;
    private List<Apps> apps;
    private String hostName;
    private String sysIp;
    private boolean state;
    private int alertState;
    private List<ResourceVO1> children;
    private String os;
    private List<String> userTags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public List<Apps> getApps() {
        return apps;
    }

    public void setApps(List<Apps> apps) {
        this.apps = apps;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getSysIp() {
        return sysIp;
    }

    public void setSysIp(String sysIp) {
        this.sysIp = sysIp;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getAlertState() {
        return alertState;
    }

    public void setAlertState(int alertState) {
        this.alertState = alertState;
    }

    public List<ResourceVO1> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceVO1> children) {
        this.children = children;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public List<String> getUserTags() {
        return userTags;
    }

    public void setUserTags(List<String> userTags) {
        this.userTags = userTags;
    }

    public ResourceVO1() {
        super();
    }

    public ResourceVO1(String id, String resourceType, List<Apps> apps, String hostName, String sysIp,
                       boolean state, int alertState, String os, List<ResourceVO1> children, List<String> userTags) {
        super();
        this.id = id;
        this.resourceType = resourceType;
        this.apps = apps;
        this.hostName = hostName;
        this.sysIp = sysIp;
        this.state = state;
        this.alertState = alertState;
        this.os = os;
        this.children = children;
        this.userTags = userTags;
    }

}
