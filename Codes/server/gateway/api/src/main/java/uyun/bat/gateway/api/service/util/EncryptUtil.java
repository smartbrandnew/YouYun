package uyun.bat.gateway.api.service.util;

import java.security.MessageDigest;

public abstract class EncryptUtil {
	/**** FNV32 **/
	private static final long OFFSET_BASIS = 2166136261L;// 32位offset basis
	private static final long PRIME = 16777619; // 32位prime

	/**** FNV32 **/

	/**
	 * FNV32Hash
	 * 
	 * @param src
	 * @return
	 */
	public static long hash(byte[] src) {
		long hash = OFFSET_BASIS;
		for (byte b : src) {
			hash ^= b;
			hash *= PRIME;
		}
		return hash;
	}

	/***
	 * MD5加码 生成32位md5码
	 */
	public static String string2MD5(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();

	}

	/**
	 * 加密解密算法 执行一次加密，两次解密
	 */
	public static String convertMD5(String inStr) {

		char[] a = inStr.toCharArray();
		for (int i = 0; i < a.length; i++) {
			a[i] = (char) (a[i] ^ 't');
		}
		String s = new String(a);
		return s;

	}

	public static void main(String[] args) {
		String s = new String("tangfuqiang:[asdasd],[asdsadsadsad]m[][]");
		System.out.println("原始：" + s);
		System.out.println("MD5后：" + string2MD5(s));
		System.out.println("加密的：" + convertMD5(s));
		System.out.println("解密的：" + convertMD5(convertMD5(s)));
		System.out.println(hash("j".getBytes()));

	}

}
