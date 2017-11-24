package uyun.bat.datastore.entity;

import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class TagObject {
	private String id;
	private Integer tagCount;
	private Binary tagIds;

	public TagObject() {
	}

	public TagObject(List<String> tagIds) {
		this.tagCount = tagIds.size();
		tagIds.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		if (this.tagCount > 0) {
			ByteArrayOutputStream os = new ByteArrayOutputStream(this.tagCount * 16);
			for (String tagId : tagIds) {
				try {
					os.write(UUIDTypeHandler.convert(tagId));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			this.tagIds = new Binary(os.toByteArray());
		}
	}

	public Binary getTagIds() {
		return tagIds;
	}

	public void setTagIds(Binary tagIds) {
		this.tagIds = tagIds;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getTagCount() {
		return tagCount;
	}

	public void setTagCount(Integer tagCount) {
		this.tagCount = tagCount;
	}
}
