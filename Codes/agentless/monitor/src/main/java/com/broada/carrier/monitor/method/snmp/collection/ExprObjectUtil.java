package com.broada.carrier.monitor.method.snmp.collection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.snmp.SnmpGet;
import com.broada.snmp.SnmpUtil;
import com.broada.snmputil.SnmpTarget;

/**
 * 获取ExprObject工具类o
 */
public class ExprObjectUtil {
	private static final Log logger = LogFactory.getLog(ExprObjectUtil.class);

	private static <T extends ExprObject> T getExprObject(String instKey, String instName, String expr, String instOid,
			SnmpGet get, double max, Class<T> objClass) throws Throwable {
		T obj = objClass.newInstance();
		obj.setMonitorInst(instKey);
		obj.setMonitorName(instName);
		obj.setExpression(expr);

		double val = SnmpUtil.getSnmpExpressionValue(get, obj.getExpression(), instOid);
		if (Double.isNaN(val))
			val = 0;
		val = new BigDecimal(val).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		if (val > max)
			val = max;
		obj.setValue(val);

		return obj;
	}

	/**
	 * 获取指定监测服务的表达式对象
	 * @param srv 服务
	 * @param param SNMP参数
	 * @param cond 条件
	 * @param insts 实例列表
	 * @param max 结果最大值
	 * @param objClass 结果类 
	 * @return
	 */
	public static <T extends ExprObject> List<T> getExprObjects(MonitorTask srv, SnmpTarget target,
			MonitorInstance[] insts, double max, Class<T> objClass) {		
		SnmpGet snmpGet = new SnmpGet(target);

		List<T> result = new ArrayList<T>();		
		for (int i = 0; i < insts.length; i++) {
			MonitorInstance inst = (MonitorInstance) insts[i];
			String exp = inst.getExtra();
			if (exp == null) {
				logger.warn(String.format("获取监测表达式对象失败，监测任务[%s]。错误：实例[%s]没有配置监测表达式", srv, inst.getCode()));
				continue;
			}

			try {
				int pos = inst.getCode().indexOf(".");
				String instOid = inst.getCode().substring(pos + 1, inst.getCode().length());
				result.add(getExprObject(inst.getCode(), inst.getName(), exp, instOid, snmpGet, max,
						objClass));
			} catch (Throwable e) {
				logger.warn(String.format("获取监测表达式对象失败，监测任务[%s]，实例[%s]。错误：%s", srv, inst.getCode(), e));
				logger.debug("堆栈：", e);
			}
		}		

		return result;
	}
}
