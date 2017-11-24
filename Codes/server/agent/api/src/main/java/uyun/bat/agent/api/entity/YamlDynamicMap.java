package uyun.bat.agent.api.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lilm on 17-5-8.
 */
public class YamlDynamicMap {
    private String scriptPath;
    private Map<String, Object> properties = new HashMap<>();

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "DynamicMap [scriptPath=" + scriptPath + ", properties=" + properties + "]";
    }
}
