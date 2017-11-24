package uyun.bat.datastore.api.util;

import uyun.bat.datastore.api.exception.Illegalargumentexception;

/**
 * tenantId+agentId或者 tenantId+hostname MD5后生成UUID工具类
 */
public class UUIDUtils {

	public static String generateResId(String tenantId, String str) {
		String symbol = "[" + tenantId + "],[" + str + "]";
		return EncryptUtil.string2MD5(symbol);
	}

	//解码获取mongodb id
	public static String decodeMongodbId(String str) {
		if (str == null)
			return null;
		int length = str.length();
		if (length != 32)
			throw new Illegalargumentexception("can not decode resId, resId lenth must be 32");
		if(str.endsWith("00000000"))
		str = str.substring(0, 24);
		return str;
	}

	// mongodb id固定长度24位，转化为32位长度的binary id
	public static String encodeMongodbId(String str) {
		if (str == null)
			return null;
		int length = str.length();
		if (length != 32) {
			StringBuilder sb = new StringBuilder(str);
			for (int i = length; i < 32; i++) {
				sb.append("0");
			}
			return sb.toString();
		}
		return str;
	}

	public static void main(String[] args) {
		String str = "123456789012345678901234";
		String s = encodeMongodbId(str);
		System.out.println("encode: " + s);
		s = decodeMongodbId(s);
		System.out.println("decode: " + s);
	}
}
