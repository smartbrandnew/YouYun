package com.broada.carrier.monitor.common.restful;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 自定义序列化类的Bean选项
 */
public class CustomBeanOption {
	private CustomBeanField[] fields;
	private String[] fieldNames;
	
	/**
	 * 构建所有属性
	 * @param cls
	 */
	public CustomBeanOption(Class<?> cls) {
		this(cls, null);			
	}
	
	/**
	 * 构建指定属性
	 * @param cls
	 * @param fieldNames
	 */
	public CustomBeanOption(Class<?> cls, String[] fieldNames) {
		Set<CustomBeanField> fields = new LinkedHashSet<CustomBeanField>(fieldNames == null ? 5 : fieldNames.length);
		
		for (Method method : cls.getMethods()) {	
			int pos;
			if (method.getParameterTypes().length > 0)
				continue;
			else if (method.getName().equals("getClass"))
				continue;
			else if (method.getName().startsWith("get"))
				pos = 3;
			else if (method.getName().startsWith("is"))
				pos = 2;			
			else 
				continue;
			
			//method.getAnnotation(annotationClass);
			
			CustomBeanField field = null;
			String fieldName = getFieldName(method.getName().substring(pos));
			if (fieldNames == null)
				field = new CustomBeanField(fieldName, method);					
			else {
				for (String item : fieldNames) {
					if (fieldName.equals(item)) {
						field = new CustomBeanField(fieldName, method);
						break;
					}
				}
			}
			
			if (field != null) 
				fields.add(field);				
		}
		
		if (fieldNames != null) {
			if (fields.size() != fieldNames.length)
				throw new IllegalArgumentException(String.format("Bean类[%s]解析到到属性个数[%d]与需要的属性个数[%d]不同", cls.getName(), fields.size(), fieldNames.length));

			for (String fieldName : fieldNames) {
				boolean isFound = false;
				for (CustomBeanField field : fields)
					if (field.getName().equals(fieldName)) {
						isFound = true;
						break;
					}
				if (!isFound)
					throw new IllegalArgumentException(String.format("无法在Bean类[%s]上属性[%s]", cls.getName(), fieldName));
			}
		}
		
		if (fields.size() <= 0)
			throw new IllegalArgumentException(String.format("Bean类[%s]没有任何属性", cls.getName()));
		
		this.fields = fields.toArray(new CustomBeanField[fields.size()]);
		this.fieldNames = fieldNames;
	}

	private static String getFieldName(String name) {
		int pos = 0;
		for (; pos < name.length(); pos++) {
			char c = name.charAt(pos);
			if (!Character.isUpperCase(c))
				break;
		}
		if (pos == name.length())
			name = name.toLowerCase();
		else if (pos > 0)
			name = name.substring(0, pos).toLowerCase() + name.substring(pos);
		return name;
	}

	/**
	 * 获取属性
	 * @return
	 */
	public CustomBeanField[] getFields() {
		return fields;
	}

	public String[] getFieldNames() {
		return fieldNames;
	}
}
