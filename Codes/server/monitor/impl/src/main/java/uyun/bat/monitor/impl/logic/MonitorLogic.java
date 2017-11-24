package uyun.bat.monitor.impl.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import uyun.bat.common.config.Config;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorCount;
import uyun.bat.monitor.api.entity.MonitorCountVO;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.api.entity.PageMonitor;
import uyun.bat.monitor.api.entity.SimpleMonitorQuery;
import uyun.bat.monitor.impl.common.RedisConnectionPool;
import uyun.bat.monitor.impl.dao.MonitorDao;
import uyun.bat.monitor.impl.util.JsonUtil;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

public class MonitorLogic {
	private static final Logger logger = LoggerFactory.getLogger(MonitorLogic.class);

	@Resource
	private MonitorDao monitorDao;
	// 租户监测器授权数量暂写死为10
	private int authNum = Config.getInstance().get("tenant.authority.monitor.num", 10);

	// monitor count redis key前缀
	private static final String monitor_count_prefix = "bat-monitor-count";

	// 初始化时把所有租户的监测器数量都加载到redis
	@SuppressWarnings("unused")
	private void init() {
		Jedis jedis = null;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			List<MonitorCountVO> list = getMonitorCount();
			Pipeline pipeline = jedis.pipelined();
			for (MonitorCountVO monitorCount : list) {
				pipeline.setnx(encodeMonitorKey(monitorCount.getTenantId()), monitorCount.getCount());
			}
			pipeline.sync();
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}
	}

	/**
	 * 生成monitor count redis key
	 * 
	 * @param key
	 * @return
	 */
	private String encodeMonitorKey(String key) {
		if (key == null)
			return null;
		return monitor_count_prefix + key;
	}

	private long getCountByTenantId(String tenantId, Jedis jedis) {
		if (jedis != null) {
			// 如果redis下线，则仅使用mysql并不进行个数限制
			String val = jedis.get(encodeMonitorKey(tenantId));
			if (val != null && val.length() > 0) {
				return Long.parseLong(val);
			}
		}
		return monitorDao.getCountByTenantId(tenantId);
	}

	public PageMonitor getMonitorsByFilter(String tenantId, int currentPage, int pageSize, String name,
			List<MonitorState> state, Boolean enable) {
		PageHelper.startPage(currentPage, pageSize);
		SimpleMonitorQuery m = new SimpleMonitorQuery();
		m.setTenantId(tenantId);
		m.setMonitorState(state);
		m.setName(name);
		m.setEnable(enable);
		Page<Monitor> ms = (Page<Monitor>) monitorDao.getMonitorsByFilter(m);
		PageMonitor pageMonitor = new PageMonitor();
		pageMonitor.setCount((int) ms.getTotal());
		pageMonitor.setMonitors(ms.getResult());
		return pageMonitor;
	}

	public List<Monitor> getMonitorList(String tenantId) {
		return monitorDao.getMonitorList(tenantId);
	}

	public Monitor getMonitorById(String tenantId, String monitorId) {
		return monitorDao.getMonitorById(tenantId, monitorId);
	}

	public Monitor createMonitor(Monitor monitor) {
		Jedis jedis = null;
		int i = 0;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			long count = getCountByTenantId(monitor.getTenantId(), jedis);
			if (count < authNum) {
				i = monitorDao.createMonitor(monitor);
				if (i == 1) {
					jedis.set(encodeMonitorKey(monitor.getTenantId()), Long.toString(count + 1));
				}
				//主键冲突，该主键已有对应数据
				return monitor;
			}
			throw new IllegalArgumentException("Failure to create and the number of monitors authorized is capped:" + authNum);
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
			if (i == 0) {
				// 未插入成功就该报错
				throw e;
			} else {
				// 插入成功，但是redis操作可能异常，暂时允许继续使用
				return monitor;
			}
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}
	}

	public Monitor updateMonitor(Monitor monitor) {
		monitorDao.updateMonitor(monitor);
		return monitor;
	}

	public boolean deleteMonitor(Monitor monitor) {
		Jedis jedis = null;
		int i = 0;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			long count = getCountByTenantId(monitor.getTenantId(), jedis);
			i = monitorDao.deleteMonitor(monitor.getTenantId(), monitor.getId());
			if (i > 0) {
				jedis.set(encodeMonitorKey(monitor.getTenantId()), Long.toString(count - 1));
				return true;
			}
			return false;
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
			if (i == 0) {
				// 未删除成功就该报错
				throw e;
			} else {
				// 删除成功，但是redis操作可能异常，暂时允许继续使用
				return true;
			}
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}
	}

	public List<MonitorCount> getCount(String tenantId) {
		return (List<MonitorCount>) monitorDao.getCount(tenantId);
	}

	/******************** 以下方法监测器触发调用 *******************************************/

	/**
	 * 
	 * @param tenantId 租户id
	 * @param type 监测器类型
	 * @return
	 */
	public List<Monitor> getMonitors(String tenantId, MonitorType type) {
		List<Monitor> results = getMonitorsCache(tenantId, type);
		if (results == null) {
			results = monitorDao.getMonitors(tenantId, type);
			generateRedisCache(tenantId, type, results);
		}
		return results;
	}

	private String GET_MONITORS_FROM_REDIS = "if redis.call('EXISTS',KEYS[1]) ~= 0 then\n" + "local array={}\n"
			+ "local result = redis.call('SMEMBERS',KEYS[1])\n" + "if(#result > 1) then\n" + "for i=1,#result do\n"
			+ "if result[i] ~= 'nil' then\n" + "array[i] = redis.call('HGETALL',result[i])\n" + "end\n" + "end\n" + "end\n"
			+ "return array\n" + "end\n" + "return nil";

	private List<Monitor> getMonitorsCache(String tenantId, MonitorType type) {
		Jedis jedis = null;
		List<Monitor> results = null;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			Object temp = jedis.eval(GET_MONITORS_FROM_REDIS, 1,
					String.format("bat-monitor:%s:set:%s", type.getCode(), tenantId));
			if (temp != null) {
				List<List<String>> datas = (List<List<String>>) temp;
				results = new ArrayList<Monitor>();
				for (List<String> list : datas) {
					Monitor m = new Monitor();
					String field = null;
					for (int i = 0; i < list.size(); i++) {
						if (i % 2 == 0) {
							field = list.get(i);
						} else {
							if ("id".equals(field))
								m.setId(list.get(i));
							else if ("tenantId".equals(field))
								m.setTenantId(list.get(i));
							else if ("query".equals(field))
								m.setQuery(list.get(i));
							else if ("options".equals(field))
								m.setOptions(JsonUtil.decode(list.get(i), Options.class));
						}
					}

					results.add(m);
				}
			}
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}
		return results;
	}

	private void generateRedisCache(String tenantId, MonitorType type, List<Monitor> results) {
		Jedis jedis = null;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			Pipeline pipeline = jedis.pipelined();
			String key = String.format("bat-monitor:%s:set:%s", type.getCode(), tenantId);
			// redis 空的集合不存在，故要塞个空数据
			pipeline.sadd(key, "nil");
			if (results != null && !results.isEmpty()) {
				for (Monitor monitor : results) {
					Map<String, String> r = new HashMap<String, String>();
					// redis缓存暂时只保存这么点数据。
					r.put("id", monitor.getId());
					r.put("tenantId", monitor.getTenantId());
					r.put("query", monitor.getQuery());
					r.put("options", monitor.getOptions() != null ? JsonUtil.encode(monitor.getOptions()) : "{}");

					String k = String.format("bat-monitor:%s", monitor.getId());
					pipeline.sadd(key, k);
					pipeline.hmset(k, r);
				}
			}
			pipeline.sync();
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}
	}

	private String DEL_MONITORS_FROM_REDIS = "if redis.call('EXISTS',KEYS[1]) ~= 0 then\n"
			+ "local result = redis.call('SMEMBERS',KEYS[1])\n" + "if(#result > 0) then\n" + "for i=1,#result do\n"
			+ "if result[i] ~= 'nil' then\n" + "redis.call('DEL',result[i])\n" + "redis.call('SREM',KEYS[1],result[i])\n"
			+ "end\n" + "end\n" + "end\n" + "redis.call('DEL',KEYS[1])\n" + "end\n" + "return 1";

	public void _onMonitorListChange(String tenantId, MonitorType type) {
		Jedis jedis = null;
		try {
			jedis = RedisConnectionPool.getInstance().getResource();
			Object temp = jedis.eval(DEL_MONITORS_FROM_REDIS, 1,
					String.format("bat-monitor:%s:set:%s", type.getCode(), tenantId));
			if (temp == null || !"1".equals(temp.toString())) {
				logger.warn("Monitor modification，failed to delete Redis cache!");
			}
		} catch (Throwable e) {
			RedisConnectionPool.getInstance().treatException(e, jedis);
		} finally {
			RedisConnectionPool.getInstance().returnResource(jedis);
		}
	}

	/**
	 * 更新监测器状态
	 * 
	 * @param monitor
	 * @return
	 */
	public boolean updateMonitorState(Monitor monitor) {
		return monitorDao.updateMonitorStatus(monitor) > 0;
	}

	/**
	 * 若分成多个数据库，则该方法需要改进
	 */
	public List<Monitor> getCheckEventMonitors(MonitorType type, MonitorState status, String option) {
		return monitorDao.getCheckEventMonitors(type, status, option);
	}

	public List<MonitorCountVO> getMonitorCountByDate(Date startTime, Date endTime) {
		return monitorDao.getMonitorCountByDate(startTime, endTime);
	}

	public List<MonitorCountVO> getMonitorCount() {
		return monitorDao.getMonitorCount();
	}

	public List<String> getIdListByTenantId(String tenantId) {
		return monitorDao.getIdListByTenantId(tenantId);
	}

}