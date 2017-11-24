package uyun.bat.web.api.overview.service;

import java.util.List;

import uyun.bat.event.api.entity.PageUnrecoveredEvent;
import uyun.bat.web.api.overview.entity.Statistic;
import uyun.bat.web.api.overview.entity.TagKey;
import uyun.bat.web.api.overview.entity.TagNode;

public interface OverviewWebService {
	/**
	 * 总览树
	 */
	List<TagNode> getAllStatistics(String tenantId);

	/**
	 * 总览树,根据标签key，或者完整标签获取数据统计
	 */
	Statistic getStatisticByTag(String tenantId, String tag);

	/**
	 * 总览堆图标签key列表
	 */
	List<TagKey> getTagKeyList(String tenantId);

	/**
	 * 总览堆图统计数据
	 */
	List<Statistic> getStatisticsByTagKey(String tenantId, String key);


	/**
	 * 总览事件列表
	 */
	PageUnrecoveredEvent getUnrecoveredEvents(String tenantId, int currentPage, int pageSize, String key,
			String searchValue, String sort);
}
