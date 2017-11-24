package uyun.bat.agent.api.entity;

import java.util.List;
import java.util.Map;

/**
 * Created by lilm on 17-5-8.
 */
public class YamlBean {

    private List<Map<String, Object>> collect_methods;

    private List<YamlHost> hosts;

    public List<Map<String, Object>> getCollect_methods() {
        return collect_methods;
    }

    public void setCollect_methods(List<Map<String, Object>> collect_methods) {
        this.collect_methods = collect_methods;
    }

    public List<YamlHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<YamlHost> hosts) {
        this.hosts = hosts;
    }

}
