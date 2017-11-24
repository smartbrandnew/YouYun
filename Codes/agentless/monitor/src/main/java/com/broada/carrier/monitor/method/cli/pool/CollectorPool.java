package com.broada.carrier.monitor.method.cli.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.CLICollector;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.DefaultCLICollector;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.utils.cache.Cache;
import com.broada.utils.cache.EHCache;

/**
 * 
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Create By 2006-8-17
 * huangjb 2007/11/23  在缓存CacheKey中新添加serviceId标志
 */
public class CollectorPool {

  private final static Log logger = LogFactory.getLog(CollectorPool.class);

  /**
   * 线程睡眠时间.
   * 
   * 线程恢复运行的时候检查缓存是否有过期的Collector
   * 
   * 每当调用getObject的时候,EHCache会检查给定key的缓存是否过期
   * 
   * 关闭操作在监听器中.
   * 
   * 应该保证检查线程间隔的时间大于等于缓存失效的时间,否则将造成缓存对象永远有效
   * 
   * 缓存配置文件中的失效时间为1800秒(半个小时)
   */
  //private final static long MAX_TIME = 1900 * 1000;
  private final static Cache cache = EHCache.getInstance();

  private final static List<String> cacheKeys = new ArrayList<String>();

  private final static CollectorPool collectorPool = new CollectorPool();

  private CollectorPool() {

  }
  
  /**
   * 根据指定的参数和serviceId获取执行器
   * @param options
   * @param serviceId 采集ID,作为标识用,如果小于0则表示临时获取用，不进行缓存,外部使用后需要关闭
   * @param isLogErr 是否需要Log错误日志信息(panghf Add 2011-10-12 为了方便控制调试而加入)
   * @return 返回获取到的CLI采集器
   * @throws CLILoginFailException 登录失败时抛出
   * @throws CLIConnectException 网络连接失败时抛出
   */
  public static CLICollector getCLICollector(Properties options, String serviceId, boolean isLogErr) throws CLILoginFailException,
      CLIConnectException {
    if (serviceId==null||serviceId.trim().length()<=0) {
      //如果serviceId小于0则表示测试用，不进行缓存,外部使用后需要关闭
      return collectorPool.createCollector(options, false, isLogErr);
    }
    
    String key = getCacheKey(options, serviceId);
		if (logger.isDebugEnabled())
			logger.debug(String.format("获取CLI采集器[opts: %s srvId: %s]", options, serviceId));	
    
    synchronized (key) {//对同一个key进行锁定
			CLICollector cliCollector = (CLICollector) cache.getObject(key);
      if (cliCollector == null) {
        if (logger.isDebugEnabled())
          logger.debug("采集器不存在,创建一个连接并缓存,缓存key:" + key);
        cliCollector = collectorPool.createCollector(options, true, isLogErr);
        if(cliCollector.isStanding())//非长连接不进行缓存
        	cache.putObject(key, cliCollector);        
      } else {
        if (logger.isDebugEnabled())
          logger.debug("缓存中存在采集器,缓存key:" + key);
        if (cliCollector.isWeak()) {
        	if (logger.isDebugEnabled())
        		logger.debug("该采集器已经不可靠，重新连接主机:" + options);
        	if (!cliCollector.isClosed())	// 如果还未关闭，则明确要求关闭
          	cliCollector.destroy();
          cache.removeObject(key);
          cliCollector = collectorPool.createCollector(options, true, isLogErr);
          cache.putObject(key, cliCollector);
        } 
      }
      return cliCollector;
    }
  }

  private CLICollector createCollector(Properties options, boolean useCache, boolean isLogErr) throws CLILoginFailException, CLIConnectException {
    DefaultCLICollector dcc = new DefaultCLICollector(useCache);
    dcc.init(options, isLogErr);
    return dcc;
  }

  /**
   * 根据指定字符串获取缓存里的对应key，如果有同一个key则返回缓存里的key，否则往缓存插入一个key并返回该key
   * 因为key主要用于同步锁定用,所以必须获取缓存里的同一个key
   * @param cacheKey
   * @return
   */
  private static String searchCacheKey(String cacheKey) {
    synchronized (cacheKeys) {
      for (Iterator<String> iter = cacheKeys.iterator(); iter.hasNext();) {
        String key = (String) iter.next();
        if (key.equals(cacheKey)) {
          return key;
        }
      }
      cacheKeys.add(cacheKey);
      return cacheKey;
    }
  }

  /**
   * 根据属性生成缓存Key
   * @param options
   * @param serviceId
   * @return 保证有值返回，除非抛出异常
   */
  private static String getCacheKey(Properties options, String serviceId) {
    StringBuffer buffer = new StringBuffer("Collector:");
    /*
     * 主机
     */
    buffer.append(options.get(CLIConstant.OPTIONS_REMOTEHOST));
    buffer.append(":");
    /*
     * 端口
     */
    buffer.append(options.get(CLIConstant.OPTIONS_REMOTEPORT));
    buffer.append(":");
    /*
     * 用户
     */
    buffer.append(options.get(CLIConstant.OPTIONS_LOGINNAME));
    buffer.append(":");
    /*
     * 密码
     */
    buffer.append(options.get(CLIConstant.OPTIONS_PASSWORD));
    buffer.append(":");
    
    /*add by huangjb 2008/03/31 为了使缓存中的options属性跟数据库中保持一致,添加sysversion作为Key一部分
     * 这样配置面板中如果改动系统版本的话，就会重新生成一个CLICollector
     * OPTIONS_OSVERSION
     */
    buffer.append(options.get(CLIConstant.OPTIONS_OSVERSION));
    buffer.append(":");
    /*add by huangjb 2008/03/08 为了使缓存中的options属性跟数据库中保持一致,添加prompt作为Key一部分
     * 这样配置面板中如果改动命令提示符的话，就会重新生成一个CLICollector
     * OPTIONS_PROMPT
     */
    buffer.append(options.get(CLIConstant.OPTIONS_PROMPT));

    String key = buffer.toString();
    return searchCacheKey(key);
  }
}
