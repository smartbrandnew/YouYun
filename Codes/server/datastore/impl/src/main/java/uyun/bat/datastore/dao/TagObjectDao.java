package uyun.bat.datastore.dao;

import uyun.bat.datastore.entity.Binary;
import uyun.bat.datastore.entity.ListWrapper;
import uyun.bat.datastore.entity.TagObject;

import java.util.List;

public interface TagObjectDao {
	void createObject(TagObject object);

	void deleteObject(String objectId);

	void deleteMapByTagId(String tagId);

	void deleteMapByObjectId(String objectId);

	void createMap(String tagId, String objectId);

	String getObjectIdByTagIds(ListWrapper tagIds);

	String[] queryObjectIdsByTagIds(List<String> tagIds);

	List<String> getTagIdsByObjectId(String objectId);

	String getObjectIdByTagIdsColumn(Binary tagIds);
}
