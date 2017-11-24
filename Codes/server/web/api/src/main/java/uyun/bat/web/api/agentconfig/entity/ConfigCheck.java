package uyun.bat.web.api.agentconfig.entity;

import java.util.List;

/**
 * Created by lilm on 17-5-23.
 */
public class ConfigCheck {

    private String id;
    private String pluginName;
    private String source;

    private List<String> ipList;

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

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }
}
