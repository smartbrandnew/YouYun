package uyun.bat.datastore.dao;

import uyun.bat.datastore.entity.Tag;

public interface TagDao {
	Tag getByKeyValue(String key, String value);

	void create(Tag item);

	void delete(String tagId);

	Tag get(String tagId);
}
