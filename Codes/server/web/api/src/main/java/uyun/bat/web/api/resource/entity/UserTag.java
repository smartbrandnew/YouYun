package uyun.bat.web.api.resource.entity;

/**
 * 用户返回用户自定义的标签
 */
public class UserTag {
	private String resourceId;
	private String userTags;

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getUserTags() {
		return userTags;
	}

	public void setUserTags(String userTags) {
		this.userTags = userTags;
	}

	public UserTag() {
	}

	public UserTag(String resourceId, String userTags) {
		this.resourceId = resourceId;
		this.userTags = userTags;
	}
}
