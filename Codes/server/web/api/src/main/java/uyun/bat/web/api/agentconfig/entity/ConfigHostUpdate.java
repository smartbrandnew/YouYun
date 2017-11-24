package uyun.bat.web.api.agentconfig.entity;

import uyun.bat.agent.api.entity.AgentConfigDetailHost;

import java.util.List;

/**
 * Created by lilm on 17-5-16.
 */
public class ConfigHostUpdate {
    private String id;
    private String pluginName;
    private Boolean checkNow = false;
    private String source;

    private List<AgentConfigDetailHost> newHosts;
    private List<AgentConfigDetailHost> removeHosts;
    private List<AgentConfigDetailHost> updateHosts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public Boolean getCheckNow() {
        return checkNow;
    }

    public void setCheckNow(Boolean checkNow) {
        this.checkNow = checkNow;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<AgentConfigDetailHost> getNewHosts() {
        return newHosts;
    }

    public void setNewHosts(List<AgentConfigDetailHost> newHosts) {
        this.newHosts = newHosts;
    }

    public List<AgentConfigDetailHost> getRemoveHosts() {
        return removeHosts;
    }

    public void setRemoveHosts(List<AgentConfigDetailHost> removeHosts) {
        this.removeHosts = removeHosts;
    }

    public List<AgentConfigDetailHost> getUpdateHosts() {
        return updateHosts;
    }

    public void setUpdateHosts(List<AgentConfigDetailHost> updateHosts) {
        this.updateHosts = updateHosts;
    }
}
