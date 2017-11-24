package uyun.bat.datastore.api.service;

import uyun.bat.datastore.api.entity.Checkperiod;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.State;

import java.util.List;

/**
 * 状态服务
 * 注意：状态标签查询要符合以下要求：
 * 1. 精确匹配：数据的标签集合与查询的标签集合完全匹配（个数也一致），顺序不作要求
 * 2. 模糊匹配：数据的标签集合包含了查询的标签集合（即查询的标签集合是数据标签集合的子集），顺序不作要求
 */
public interface StateService {
	/**
	 * 保存状态定义
	 * @param tenantId
	 * @param state
	 * @return
	 */
	String saveState(String tenantId, State state);

	/**
	 * 删除状态定义
	 * @param tenantId
	 * @param name
	 */
	void deleteState(String tenantId, String name);

	/**
	 * 获取所有状态定义
	 * @param tenantId
	 * @return
	 */
	State[] getStates(String tenantId);

	/**
	 * 保存一个状态点
	 * @param cp
	 */
	void saveCheckpoint(Checkpoint cp);

	/**
	 * 查询一个状态，标签为精确匹配
	 * @param state
	 * @param tags
	 * @return
	 */
	Checkpoint getCheckpoint(String state, String tags[]);

	/**
	 * 删除一个状态，标签为模糊匹配<br>
	 * 请谨慎调用，一旦模糊匹配的tag影响范围过大，可能删除较多数据
	 * @param state
	 * @param tags
	 */
	void deleteCheckpoints(String state, String tags[]);

	/**
	 * 删除一个租户的一个状态数据
	 * @param state
	 * @param tenantId
	 */
	//objectId没删除
	@Deprecated
	void deleteCheckpoints(String state, String tenantId);

	/**
	 * 查询状态与值匹配的个数，标签为模糊匹配
	 * @param state
	 * @param tags
	 * @param value
	 * @return
	 */
	int getCheckpointsCount(String state, String tags[], String value);

	/**
	 * 查询状态，标签为模糊匹配
	 * @param state
	 * @param tags
	 * @return
	 */
	Checkpoint[] getCheckpoints(String state, String tags[]);

	/**
	 * 查询状态变更区间，标签为模糊匹配
	 * @param state
	 * @param tags
	 * @return
	 * @throws IllegalArgumentException 如果使用标签模糊匹配到多个对象时，会弹出异常
	 */
	Checkperiod[] getCheckperiods(String state, String tags[], long firstTime, long lastTime);

	/**
	 * 查询指定对象的指定状态的状态变更区间
	 * @param tenantId
	 * @param state
	 * @param objectId
	 * @return
	 */
	Checkperiod[] getCheckperiods(String tenantId, String state, String objectId, long firstTime, long lastTime);


	/**
	 * 查询指定对象的指定状态的最后状态变更
	 * 没有返回null
	 * @param tenantId
	 * @param state
	 * @param objectId
     * @return
     */
	Checkperiod getLastCheckperiod(String tenantId, String state, String objectId);

	/**
	 *查询指定状态的标签集合
	 * 不包含tenantId和resourceId
	 * @param tenantId
	 * @param state
	 * @return
	 */
	List<String> getTagsByState(String tenantId, String state);

	/**
	 * 查询指定状态的状态变更区间最后一条记录
	 * 标签是模糊查询
	 * 支持使用标签模糊匹配到多个对象
	 * @param state
	 * @param tags
	 * @param firstTime
	 * @param lastTime
	 * @return
	 */
	Checkperiod[] getLastCheckperiods(String state,String[] tags,long firstTime,long lastTime);
}
