package com.broada.carrier.monitor.probe.impl.sync.entity;

import java.util.Set;



/**
 * Server 端返回的文件列表信息
 */
public class FileList {
    private String fileName;
    private String md5;
    private boolean enabled; // TODO: 2016/12/8 提供app的启用和关闭功能
    private boolean deleted;
    private Set<String> ipList; // 应用需要验证采集验证的IP列表

    public FileList() {
    }

    public FileList(String fileName, String md5, boolean enabled, boolean deleted) {
        this.fileName = fileName;
        this.md5 = md5;
        this.enabled = enabled;
        this.deleted = deleted;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FileList{");
        sb.append("fileName='").append(fileName).append('\'');
        sb.append(", md5='").append(md5).append('\'');
        sb.append(", enabled=").append(enabled).append('\'');
        sb.append(", deleted=").append(deleted);
        sb.append('}');
        return sb.toString();
    }

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setIpList(Set<String> ipList) {
		this.ipList = ipList;
	}

	public Set<String> getIpList() {
		return ipList;
	}
}
