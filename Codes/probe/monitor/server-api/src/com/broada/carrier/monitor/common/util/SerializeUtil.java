package com.broada.carrier.monitor.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.broada.carrier.monitor.common.restful.CustomObjectMapper;
import com.broada.component.utils.error.ErrorUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializeUtil {
	private static ObjectMapper om;
	
	public static Object decodeBytes(byte[] data) {
		if (data == null || data.length == 0)
			return null;
		
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(is);
			return ois.readObject();
		} catch (Throwable e) {
			throw ErrorUtil.createIllegalArgumentException("反序列化对象失败：" + data, e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static byte[] encodeBytes(Object object) {
		if (object == null)
			return null;
		if (!(object instanceof Serializable))
			throw new IllegalArgumentException("需要序列化的对象必须实现Serializable接口，类：" + object.getClass());		
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(object);
		} catch (Throwable e) {
			throw ErrorUtil.createIllegalArgumentException("序列化对象失败：" + object, e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return os.toByteArray();
	}

	public static String encodeJson(Object obj) {
		if (obj == null)
			return null;
		try {
			return getObjectMapper().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw ErrorUtil.createIllegalArgumentException("无法序列化对象为json格式：" + obj, e);
		}		
	}

	public static  <T> T decodeJson(String json, Class<T> cls) {
		if (json == null)
			return null;
		try {
			return getObjectMapper().readValue(json, cls);
		} catch (Throwable e) {
			throw ErrorUtil.createIllegalArgumentException(String.format("无法反序列化json数据对目标对象类型[类型：%s 数据：%s]", cls, json), e);
		}
	}
	
	private static ObjectMapper getObjectMapper() {
		if (om == null) {
			synchronized (SerializeUtil.class) {
				if (om == null) {
					om = new CustomObjectMapper();
				}
			}
		}
		return om;		
	}
}
