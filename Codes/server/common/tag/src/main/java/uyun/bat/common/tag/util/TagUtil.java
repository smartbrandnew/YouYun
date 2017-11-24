package uyun.bat.common.tag.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uyun.bat.common.tag.entity.Tag;

public abstract class TagUtil {
	public static final String TAG_SEPARATOR = ":";
	public static final String TAGS_SEPARATOR = ";";

	/**
	 * 字符串转标签对象
	 * 
	 * @param tag
	 * @return
	 */
	public static Tag string2Tag(String tag) {
		if (tag == null || tag.trim().length() == 0)
			return null;
		int index = tag.indexOf(TAG_SEPARATOR);
		if (index != -1) {
			return new Tag(tag.substring(0, index), tag.substring(index + 1));
		} else {
			return new Tag(tag);
		}
	}

	public static List<String> listTag2String(List<Tag> tags) {
		if (tags != null && tags.size() > 0) {
			Set<String> set = new HashSet<String>();
			for (Tag tag : tags) {
				set.add(tag.toString());
			}
			return new ArrayList<String>(set);
		}
		return new ArrayList<String>();
	}

	public static String array2String(String[] tagArray) {
		if (tagArray == null || tagArray.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (String tag : tagArray) {
			sb.append(tag);
			sb.append(TAGS_SEPARATOR);
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static String list2String(List<String> tagList) {
		if (tagList == null || tagList.isEmpty())
			return "";
		tagList = rmDuplicateTag(tagList);
		return array2String(tagList.toArray(new String[tagList.size()]));
	}

	public static List<String> string2List(String tags) {
		List<String> tagList = new ArrayList<String>();
		if (tags != null && tags.trim().length() > 0) {
			for (String tag : tags.split(TAGS_SEPARATOR)) {
				tagList.add(tag);
			}
		}
		return rmDuplicateTag(tagList);
	}

	/**
	 * tags 去重
	 * @param tagList
	 * @return
     */
	public static List<String> rmDuplicateTag(List<String> tagList) {
		if (tagList != null && tagList.size() > 0) {
			Set<String> tags = tagList.stream()
					.filter(s -> s != null && s.trim().length() > 0)
					.collect(Collectors.toSet());
			return new ArrayList<String>(tags);
		}
		return tagList;
	}

	/**
	 * 用户自定义标签检查
	 * @param tags
	 * @return
	 */
	public static void checkUserTag(List<String> tags) {
		// 加上字长验证，mysql表有长度限制
		List<String> list = tags;
		for (String str : list) {
			int index = str.indexOf(":");
			if (index == -1) {
				if (str.length() >= 65) {
					throw new IllegalArgumentException("tagk \"" + str + "\" length cannot larger than 65");
				}
			} else {
				String key = str.substring(0, index);
				String val = str.substring(index + 1);
				if (key.length() >= 65)
					throw new IllegalArgumentException("tagk \"" + key + "\" length cannot larger than 65");
				if (val.length() >= 65)
					throw new IllegalArgumentException("tagv \"" + val + "\" length cannot larger than 65");
			}
		}

	}

}
