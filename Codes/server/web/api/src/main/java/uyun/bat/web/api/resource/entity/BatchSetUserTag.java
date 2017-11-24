package uyun.bat.web.api.resource.entity;

import java.util.List;

/**
 * 用户批量覆盖自定义的标签
 */
public class BatchSetUserTag {
	private List<String> resId;
	private String tags;

	public List<String> getResId() {
		return resId;
	}

	public void setResId(List<String> resId) {
		this.resId = resId;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public BatchSetUserTag() {
	}

	public BatchSetUserTag(List<String> resId, String tags) {
		this.resId = resId;
		this.tags = tags;
	}
}
