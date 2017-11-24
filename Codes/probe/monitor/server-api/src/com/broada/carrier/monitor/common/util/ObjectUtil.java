package com.broada.carrier.monitor.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.Modifier;

import com.broada.component.utils.error.ErrorUtil;


public class ObjectUtil {

	public static Object executeMethod(String className, String methodName, Object... params) {
		try {
			Class<?> cls = Class.forName(className);
			Class<?>[] paramTypes = new Class<?>[params.length];
			for (int i = 0; i < paramTypes.length; i++)
				paramTypes[i] = params[i].getClass();
			Method method = cls.getMethod(methodName, paramTypes);
			int modifers = method.getModifiers();
			Object obj = null;
			if (!Modifier.isStatic(modifers))
				obj = cls.newInstance();
			return method.invoke(obj, params);
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				InvocationTargetException ie = (InvocationTargetException) e;
				if (ie.getCause() != null) {
					e = ie.getCause();
					if (e instanceof RuntimeException)
						throw (RuntimeException)e;
				}
			}
			throw ErrorUtil.createRuntimeException("调用对象方法失败", e);
		}
	}

	public static boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2)
			return true;
		else if (obj1 == null || obj2 == null)
			return false;
		else
			return obj1.equals(obj2);
	}

}
