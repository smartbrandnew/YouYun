package com.broada.carrier.monitor.common.util;

public class AnyObject {
	public static final String PREFIX_BASE64 = "#--base64--\n";
	
	public static String encode(Object obj) {
		if (obj == null)
			return null;
		else if (obj instanceof String
				|| obj instanceof Integer
				|| obj instanceof Double
				|| obj instanceof Boolean)
			return obj.toString();
		else 
			return PREFIX_BASE64 + Base64Util.encodeObject(obj); 
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T decode(String obj, Class<T> cls) {		
		if (obj == null)
			return null;
		else if (obj.startsWith(PREFIX_BASE64))
			return (T) Base64Util.decodeObject(obj.substring(PREFIX_BASE64.length()));
		else if (cls == String.class)
			return (T) obj;
		else if (cls == Integer.class)
			return (T)(new Integer(Integer.parseInt(obj)));
		else if (cls == Double.class)
			return (T)(new Double(Double.parseDouble(obj)));
		else if (cls == Boolean.class)
			return (T)(new Boolean(Boolean.parseBoolean(obj)));
		else
			return SerializeUtil.decodeJson(obj, cls);
	}
} 
