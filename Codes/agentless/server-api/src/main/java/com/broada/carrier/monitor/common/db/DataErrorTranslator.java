package com.broada.carrier.monitor.common.db;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.PropertyValueException;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import com.broada.carrier.monitor.common.error.DataAccessException;

/**
 * 数据错误翻译
 * @author Jiangjw
 */
public class DataErrorTranslator implements ThrowsAdvice {
	private static DataErrorTranslator instance;
	private Map<String, String> constraints = new HashMap<String, String>();		

	/**
	 * 获取默认实例
	 * @return
	 */
	public static DataErrorTranslator getDefault() {
		if (instance == null) {
			synchronized (DataErrorTranslator.class) {
				if (instance == null)
					instance = new DataErrorTranslator();
			}
		}
		return instance;
	}
	
	/**
	 * 拦截与处理异常
	 * @param method
	 * @param args
	 * @param target
	 * @param err
	 * @throws Throwable
	 */
	public void afterThrowing(Method method, Object[] args, Object target, Exception err) throws Throwable {
		throw processException(err);
	}

	/**
	 * 添加约束
	 * @param name
	 * @param message
	 */
	public void addConstraint(String name, String message) {
		constraints.put(name.toUpperCase(), message);
		constraints.put(name.toLowerCase(), message);
	}

	private Throwable processException(Throwable err) {
		Throwable next = err;
		while (next != null) {
			if (next instanceof PropertyValueException) {
				PropertyValueException e = (PropertyValueException) next;
				String errMsg = e.getMessage();
				if (errMsg.contains("not-null"))
					errMsg = "不允许为null";
				return new DataAccessException(String.format("属性[%s]数据非法：%s", e.getPropertyName(), errMsg));
			} else if (next instanceof DataIntegrityViolationException || next instanceof SQLException) {
				String errMsg = next.getMessage();
				if (errMsg == null)
					return new DataAccessException(String.format("数据完整性错误：%s", next));

				for (Entry<String, String> entry : constraints.entrySet()) {
					if (errMsg.contains(entry.getKey()))
						return new DataAccessException(entry.getValue());
				}

				if (errMsg.contains("UK_") || errMsg.contains("PK_"))
					errMsg = "违反唯一约束";
				return new DataAccessException(String.format("数据完整性错误：%s", errMsg));
			}
			next = next.getCause();
		}
		return err;
	}
}
