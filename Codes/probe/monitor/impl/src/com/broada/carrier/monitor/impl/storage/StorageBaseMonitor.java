package com.broada.carrier.monitor.impl.storage;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.storage.action.MonitorActionClient;
import com.broada.carrier.monitor.impl.storage.action.MonitorActionMetadata;
import com.broada.carrier.monitor.impl.storage.action.MonitorActionResult;
import com.broada.carrier.monitor.impl.storage.action.MonitorCapabilityLogic;
import com.broada.carrier.monitor.impl.storage.action.MonitorTaskHelper;
import com.broada.carrier.monitor.impl.storage.action.Result;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.cid.action.api.entity.ActionRequest;
import com.broada.cid.action.api.entity.ActionResponse;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.api.error.ActionCompileException;
import com.broada.cid.action.api.error.ActionNotSupportedException;
import com.broada.cid.action.api.error.ActionTimeoutException;
/**
 * 存储监测通用实现
 * @author ly
 *
 */
public class StorageBaseMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(StorageBaseMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorActionResult actionResult = doExecute(context);
		return convertActionResult(actionResult);
	}

	public MonitorResult convertActionResult(MonitorActionResult actionResult) {
		MonitorResult monitorResult = new MonitorResult();
		List<Result> results = actionResult.getResults();
		
		if (results == null)
			throw new CollectException("执行脚本后没有采集到任何数据。");
		
		for (Result result : results) {
			String rowKey = result.getKey();
			
			//遍历所有属性
			for (Entry<String, Object> entry : result.getAllMonitorItem().entrySet()) {
				String key = entry.getKey().toString();
				Object value = entry.getValue();
				if (value == null) {
					logger.debug(String.format("过滤值为空的监测项，实例：%s, key=%s", rowKey, key));
					continue;
				}
				createItem(monitorResult, key, value);
				monitorResult.addPerfResult(new PerfResult(rowKey, key, value));
			}
		}
		
		logger.debug("存储设备监测结果：{}", monitorResult);
		return monitorResult;
	}
	
	/**
	 * 执行动作
	 * @param context
	 * @return
	 */
	public MonitorActionResult doExecute(CollectContext context) {
		MonitorNode node = context.getNode();
		MonitorMethod method = context.getMethod();
		logger.debug("尝试在节点 [{}] 上发现配置项：{} ...", node.getIp(), node.getName());
		
		List<MonitorActionMetadata> actionMetas = MonitorCapabilityLogic.getDefault().getActionMetadatas(context.getTypeId());
		if (actionMetas == null || actionMetas.isEmpty()) {
			logger.warn("配置项类型[ " + method.getTypeId() + " ]缺少发现动作。");
			return new MonitorActionResult();
		}
		
		Protocol protocol = MonitorTaskHelper.getProtocol(context);
		Map<String, Object> requestParam = MonitorTaskHelper.createRequestParam(context);
		// 组装发现脚本请求
		// key: identifier，value：对应的脚本执行请求列表
		List<ActionRequest> requests = MonitorTaskHelper.assembleActionRequests(node, actionMetas, protocol, requestParam);
		if (requests.isEmpty()) {
			logger.warn("节点 [{}] 的发现请求为空，跳过。", node.getIp());
			return new MonitorActionResult();
		}
			
		// 执行当前分组内的脚本
		int responsePriority = -100; // 初始优先级值设置
		ActionResponse response = null;
		for (ActionRequest request : requests) {

			ActionResponse tempResponse = null;
			try {
				tempResponse = MonitorActionClient.execute(request);
			} catch (Exception e) {
				logger.debug("尝试在节点[ {} ]执行发现脚本[ {} ]失败", node.getIp(), request.getActionCode());
				processActionException(e);
				continue;
			}
			if (tempResponse.isError()) {
				logger.debug("尝试在节点[ {} ]执行发现脚本[ {} ]失败", node.getIp(), request.getActionCode());
				processActionException(tempResponse.getError());
				continue;
			}
			Object resultObject = tempResponse.getResult();
			if (resultObject == null) {
				logger.debug("在节点[ {} ]执行发现脚本[ {} ]，返回结果为空", node.getIp(), request.getActionCode());
				continue;
			}
			
			// 返回结果对象类型不正确
			if (!(resultObject instanceof MonitorActionResult)) {
				logger.debug("脚本[{}]执行返回结果对象不是[DiscoveryActionResult]类的实例，丢弃。",  request.getActionCode());
				continue;
			}
			MonitorActionResult actionResult = (MonitorActionResult) resultObject;
			logger.debug("尝试在节点[ {} ]执行发现脚本[ {} ]成功", node.getIp(), request.getActionCode());
			
			// 如果指明不再继续发现，则不再继续执行后续的脚本
			if (!actionResult.isContinue()) {
				logger.debug("在节点[ {} ]上发现脚本[{}]，返回结果指明不再继续发现，结束当前发现组[{}]的发现操作。", 
					new String[] {node.getIp(), request.getActionCode(), (String) request.getParam("typeId")});
				response = tempResponse;
				break;
			}
			
			// 取优先级高的（Priority值越小表示优先级越高），如果优先级一样，取第一个
			if (responsePriority == -100 || responsePriority > actionResult.getPriority()) {
				responsePriority = actionResult.getPriority();
				response = tempResponse;
			}
		}
			
		// 执行完毕一组脚本
		return (MonitorActionResult) (response != null ? response.getResult() : new MonitorActionResult());
	}
		
	private MonitorItem createItem(MonitorResult mr, String key, Object value) {
		if (mr.getItems() != null) {
			for (MonitorItem item : mr.getItems()) {
				if (item.getCode().equalsIgnoreCase(key))
					return item;
			}
		}

		MonitorItemType type;
		if (value instanceof Number)
			type = MonitorItemType.NUMBER;
		else
			type = MonitorItemType.TEXT;
		
		MonitorItem item = new MonitorItem(null,key, null, null, null, type);
		mr.addItem(item);
		return item;
	}
	
	/**
	 * 处理执行发现脚本过程中所遇到的错误。
	 */
	protected void processActionException(Exception e) {
		if (e instanceof ActionCompileException) {
			ActionCompileException ace = (ActionCompileException) e;
			logger.error("脚本编译错误：{}", ace.getError());
			logger.debug("堆栈：", ace);
			
		} else if (e instanceof ActionNotSupportedException) {
			ActionNotSupportedException anse = (ActionNotSupportedException) e;
			logger.debug("不被支持的脚本，{}", anse.getError());
			logger.trace("堆栈：", anse);
			
		} else if (e instanceof ActionTimeoutException) {
			ActionTimeoutException ate = (ActionTimeoutException) e;
			logger.debug("脚本执行超时：{}", ate.getError());
			logger.debug("堆栈：", ate);

		} else {
			String msg = e.getMessage();
			// TODO 只能根据消息内容来判断是否是线程被打断执行的异常
			if ("等待执行结果被中断".equals(msg)) {
				logger.debug("执行脚本时发生未知错误：{}",  msg);
			} else {
				logger.error("执行脚本时发生未知错误：{}",  msg);
			}
			logger.error("堆栈：", e);
		}
	}

}
