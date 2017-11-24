package uyun.bat.web.api.agentconfig.entity;

import java.util.List;
import java.util.Map;

/**
 * Created by lilm on 17-5-16.
 */
public class ConfigMethodUpdate {

    private String id;
    private String pluginName;
    private String source;

    private List<Map<String, Object>> newMethods;
    private List<String> removeNameList;
    private Map<String, Object> updateMethod;

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Map<String, Object>> getNewMethods() {
        return newMethods;
    }

    public void setNewMethods(List<Map<String, Object>> newMethods) {
        this.newMethods = newMethods;
    }

    public List<String> getRemoveNameList() {
        return removeNameList;
    }

    public void setRemoveNameList(List<String> removeNameList) {
        this.removeNameList = removeNameList;
    }

    public Map<String, Object> getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(Map<String, Object> updateMethod) {
        this.updateMethod = updateMethod;
    }
}
