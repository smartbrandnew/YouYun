package uyun.bat.monitor.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uyun.bat.monitor.core.entity.TagEntry;

public abstract class TagUtil {
	public static final String SEPARATOR = ":";

	/**
	 * 由于设置的tagk可能重复，且tagv还不一定有 故需要对tag列表做去重以及相同tagk的tagv逗号拼接合并
	 */
	public static void generateTags(List<TagEntry> tags) {
		if (tags == null || tags.size() == 0)
			return;
		// 标签去重
		Map<String, Set<String>> tagMap = new HashMap<String, Set<String>>();
		for (TagEntry te : tags) {
			Set<String> tagSet = tagMap.get(te.getKey());
			if (tagSet == null) {
				tagSet = new HashSet<String>();
				tagMap.put(te.getKey(), tagSet);
			}
			tagSet.add(te.getValue());
		}

		tags.clear();
		List<String> keys = new ArrayList<String>(tagMap.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			List<String> tagvs = new ArrayList<String>(tagMap.get(key));
			// 去null
			for (int i = 0; i < tagvs.size(); i++) {
				if (tagvs.get(i) == null) {
					tagvs.remove(i);
					i--;
				}
			}

			if (tagvs.size() > 0) {
				Collections.sort(tagvs);
				// TODO 应datastore的需要，同key的标签对应值合并，用逗号联结
				StringBuilder sb = new StringBuilder();
				for (String v : tagvs) {
					sb.append(v);
					sb.append(',');
				}
				sb.deleteCharAt(sb.length() - 1);
				tags.add(new TagEntry(key, sb.toString()));
			} else {
				// TODO 应datastore的要求，tagv不能为null，改成""
				tags.add(new TagEntry(key, ""));
			}
		}

	}

	public static void main(String[] args) {
		List<TagEntry> tags = new ArrayList<TagEntry>();
		tags.add(new TagEntry("host", "jianglf"));
		tags.add(new TagEntry("host", null));
		tags.add(new TagEntry("host", "jianglf"));
		tags.add(new TagEntry("host", "jianglf"));
		tags.add(new TagEntry("host", null));
		tags.add(new TagEntry("host", "jianglf1"));
		tags.add(new TagEntry("host", "jianglf2"));
		generateTags(tags);
	}
}
