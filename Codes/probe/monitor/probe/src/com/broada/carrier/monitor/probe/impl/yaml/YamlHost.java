package com.broada.carrier.monitor.probe.impl.yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import com.broada.carrier.monitor.probe.impl.util.StringUtils;

public class YamlHost {
	private String ip;
	private String collect_method;
	private String tags;
	private String excludes;
	private String id;
	private String os;
	private String host;
	private Integer checkStatus;
	private String type;
	private YamlDynamicMap dynamic_properties;


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
		if (StringUtils.isNotNullAndTrimBlank(str)) {
			String[] arrs = str.split(",");
			for (String s : arrs) {
				if (StringUtils.isNotNullAndTrimBlank(s))
					list.add(s);
			}
		}
		return list;
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

	public void setCheckStatus(Integer checkStatus) {
		this.checkStatus = checkStatus;
	}

	public Integer getCheckStatus() {
		return checkStatus;
	}

	@Override
	public String toString() {
		return "YamlHost [ip=" + ip + ", collect_method=" + collect_method + ", tags=" + tags + ", excludes="
		+ excludes + ", id=" + id + ", os=" + os + ", host=" + host + ", dynamic_properties="
		+ dynamic_properties + "]";
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}