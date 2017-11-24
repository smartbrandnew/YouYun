package uyun.bat.datastore.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import uyun.bat.datastore.api.service.TagService;
import uyun.bat.datastore.dao.TagDao;
import uyun.bat.datastore.dao.TagObjectDao;
import uyun.bat.datastore.entity.ListWrapper;
import uyun.bat.datastore.entity.Tag;
import uyun.bat.datastore.entity.TagObject;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.util.ArrayList;
import java.util.List;

public class TagServiceImpl implements TagService {
	@Autowired
	private TagDao tagDao;
	@Autowired
	private TagObjectDao tagObjectDao;

	@Override
	public String checkObjectId(String[] tags) {
		List<String> tagIds = checkTagIds(tags);
		ListWrapper wrapper = new ListWrapper(tagIds);
		String objectId = tagObjectDao.getObjectIdByTagIds(wrapper);
		if (objectId == null) {
			TagObject to = new TagObject(tagIds);
			try {
				to.setId(UUIDTypeHandler.createUUID());
				tagObjectDao.createObject(to);
				objectId = to.getId();
				for (String tagId : tagIds)
					tagObjectDao.createMap(tagId, objectId);
			} catch (DuplicateKeyException e) {
				objectId = tagObjectDao.getObjectIdByTagIdsColumn(to.getTagIds());
			}
		}
		return objectId;
	}

	@Override
	public String getObjectId(String[] tags) {
		List<String> tagIds = getTagIds(tags);
		if (tagIds != null)
			return tagObjectDao.getObjectIdByTagIds(new ListWrapper(tagIds));
		return null;
	}

	@Override
	public String[] queryObjectIds(String[] tags) {
		List<String> tagIds = getTagIds(tags);
		if (tagIds != null)
			return tagObjectDao.queryObjectIdsByTagIds(tagIds);
		return new String[0];
	}

	@Override
	public List<String> getTagIds(String[] tags) {
		List<String> tagIds = new ArrayList<>(tags.length);
		for (String tag : tags) {
			String tagId = getTagId(tag);
			if (tagId == null)
				return null;
			tagIds.add(tagId);
		}
		return tagIds;
	}

	@Override
	public String[] getObjectTags(String objectId) {
		List<String> tagIds = tagObjectDao.getTagIdsByObjectId(objectId);
		String[] tags;
		if (tagIds == null)
			tags = new String[0];
		else {
			tags = new String[tagIds.size()];
			int i = 0;
			for (String tagId : tagIds) {
				tags[i] = getTag(tagId);
				i++;
			}
		}
		return tags;
	}

	private String getTag(String tagId) {
		Tag tag = tagDao.get(tagId);
		if (tag == null)
			return null;
		else
			return tag.encode();
	}

	private String getTagId(String tag) {
		Tag item = Tag.decode(tag);
		item = tagDao.getByKeyValue(item.getKey(), item.getValue());
		if (item == null)
			return null;
		return item.getId();
	}

	@Override
	public void deleteTagsAndObjects(String[] tags) {
		List<String> tagIds = getTagIds(tags);
		if (tagIds != null) {
			for (String tagId : tagIds)
				deleteTagAndObjects(tagId);
		}
	}

	private void deleteTagAndObjects(String tagId) {
		List<String> tagIds = new ArrayList<>();
		tagIds.add(tagId);
		String[] objectIds = tagObjectDao.queryObjectIdsByTagIds(tagIds);
		for (String objectId : objectIds) {
			tagObjectDao.deleteMapByObjectId(objectId);
			tagObjectDao.deleteObject(objectId);
		}
		tagDao.delete(tagId);
	}

	// TODO 可以使用cache加速
	@Override
	public List<String> checkTagIds(String[] tags) {
		List<String> tagIds = new ArrayList<>(tags.length);
		for (String tag : tags) {
			String tagId = checkTagId(tag);
			tagIds.add(tagId);
		}
		return tagIds;
	}

	private String checkTagId(String tag) {
		Tag item = Tag.decode(tag);
		Tag exists = tagDao.getByKeyValue(item.getKey(), item.getValue());
		String tagId;
		if (exists == null) {
			try {
				item.setId(UUIDTypeHandler.createUUID());
				tagDao.create(item);
				tagId = item.getId();
			} catch(DuplicateKeyException error) {
				exists = tagDao.getByKeyValue(item.getKey(), item.getValue());
				tagId = exists.getId();
			}
		} else
			tagId = exists.getId();
		return tagId;
	}
}
