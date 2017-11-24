package uyun.bat.web.api.report.entity;

import java.io.Serializable;

/**
 * Created by lilm on 17-3-17.
 */
public class SimpleReportGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    public SimpleReportGroup() {
    }

    public SimpleReportGroup(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    private String groupId;
    private String groupName;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
