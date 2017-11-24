package uyun.bat.web.api.resource.entity;

import java.util.List;

/**
 * 用户批量增加自定义的标签
 */
public class BatchAddUserTag {
	private String resId;
	private List<String> userTags;
	private String addTags;

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

	public List<String> getUserTags() {
		return userTags;
	}

	public void setUserTags(List<String> userTags) {
		this.userTags = userTags;
	}

	public String getAddTags() {
		return addTags;
	}

	public void setAddTags(String addTags) {
		this.addTags = addTags;
	}

	public BatchAddUserTag() {
	}

	public BatchAddUserTag(String resId, List<String> userTags, String addTags) {
		this.resId = resId;
		this.userTags = userTags;
		this.addTags = addTags;
	}
}
