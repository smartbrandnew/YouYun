package uyun.bat.agent.api.entity;

import java.util.Set;

/**
 * Server 端返回的文件列表信息
 */
public class FileList {
    private String fileName;
    private String md5;
    private boolean enabled; // 提供app的启用和关闭功能
    private boolean deleted;

    private Set<String> ipList; // 应用需要验证采集验证的IP列表

    public FileList() {
    }
    
    public FileList(String fileName, String md5, boolean enabled, boolean deleted, Set<String> ipList) {
        this.fileName = fileName;
        this.md5 = md5;
        this.enabled = enabled;
        this.deleted = deleted;
        this.ipList = ipList;
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

    public boolean isDeleted() {
			return deleted;
    }

	public void setDeleted(boolean deleted) {
			this.deleted = deleted;
    }

    public Set<String> getIpList() {
        return ipList;
    }

    public void setIpList(Set<String> ipList) {
        this.ipList = ipList;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FileList{");
        sb.append("fileName='").append(fileName).append('\'');
        sb.append(", md5='").append(md5).append('\'');
        sb.append(", enabled=").append(enabled);
        sb.append('}');
        return sb.toString();
    }
}
