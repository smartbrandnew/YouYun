package uyun.bat.agent.api.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lilm on 17-5-8.
 */
public class YamlHost {
    private String collect_method;
    private String ip;
    private String id;
    private String host;
    private String os;
    private String tags;
    private String excludes;
    private Integer checkStatus;
    private YamlDynamicMap dynamic_properties;
    private String type;


    public YamlDynamicMap getDynamic_properties() {
        return dynamic_properties;
    }

    public void setDynamic_properties(YamlDynamicMap dynamic_properties) {
        this.dynamic_properties = dynamic_properties;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCollect_method() {
        return collect_method;
    }

    public void setCollect_method(String collect_method) {
        this.collect_method = collect_method;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public List<String> getExcludeList() {
        String str = getExcludes();
        List<String> list = new ArrayList<String>();
        if (str != null && str.trim().length() > 0) {
            String[] arrs = str.split(",");
            for (String s : arrs) {
                if (s != null && s.trim().length() > 0)
                    list.add(s);
            }
        }
        return list;
    }

    public Integer getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(Integer checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "YamlHost [ip=" + ip + ", collect_method=" + collect_method + ", tags=" + tags + ", excludes="
                + excludes + ", id=" + id + ", os=" + os + ", host=" + host + ", dynamic_properties="
                + dynamic_properties + "]";
    }

}
