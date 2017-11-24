package com.broada.carrier.monitor.common.util;

import java.io.UnsupportedEncodingException;



public class Base64Util {
	public static final String PREFIX_BASE64 = "#--base64--\n";
	private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
			.toCharArray();

	private static int[] toInt = new int[128];

	static {
		for (int i = 0; i < ALPHABET.length; i++) {
			toInt[ALPHABET[i]] = i;
		}
	}

	public static String encode(byte[] buf) {
		if (buf == null)
			return null;
		
		int size = buf.length;
		char[] ar = new char[((size + 2) / 3) * 4];
		int a = 0;
		int i = 0;
		while (i < size) {
			byte b0 = buf[i++];
			byte b1 = (i < size) ? buf[i++] : 0;
			byte b2 = (i < size) ? buf[i++] : 0;

			int mask = 0x3F;
			ar[a++] = ALPHABET[(b0 >> 2) & mask];
			ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
			ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
			ar[a++] = ALPHABET[b2 & mask];
		}
		switch (size % 3) {
		case 1:
			ar[--a] = '=';
		case 2:
			ar[--a] = '=';
		}
		return new String(ar);
	}

	public static byte[] decode(String s) {
		if (s == null || s.isEmpty())
			return null;
		
		int delta = s.endsWith("==") ? 2 : s.endsWith("=") ? 1 : 0;
		byte[] buffer = new byte[s.length() * 3 / 4 - delta];
		int mask = 0xFF;
		int index = 0;
		for (int i = 0; i < s.length(); i += 4) {
			int c0 = toInt[s.charAt(i)];
			int c1 = toInt[s.charAt(i + 1)];
			buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);
			if (index >= buffer.length) {
				return buffer;
			}
			int c2 = toInt[s.charAt(i + 2)];
			buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);
			if (index >= buffer.length) {
				return buffer;
			}
			int c3 = toInt[s.charAt(i + 3)];
			buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
		}
		return buffer;
	}


	public static Object decodeObject(String resultText) {
		return SerializeUtil.decodeBytes(Base64Util.decode(resultText));		
	}
	
	public static String encodeObject(Object object) {		
		return Base64Util.encode(SerializeUtil.encodeBytes(object));
	}

	public static String encodeString(String text) {
		if (text == null)
			return null;
		try {
			return encode(text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("未知的字符集");
		}
	}
	
	public static String decodeString(String text) {
		if (text == null)
			return null;
		try {
			return new String(decode(text), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("未知的字符集");
		}
	}

	public static Object decodeComplex(Object value) {
		if (value == null)
			return null;
		else if (value instanceof Number)
			return value;
		else if (value instanceof Boolean)
			return value;
		else {
			String text = (String) value;
			if (text.startsWith(PREFIX_BASE64))
				return decodeObject(text.substring(PREFIX_BASE64.length()));
			else
				return text;
		}
	}

	public static Object encodeComplex(Object value) {
		if (value == null)
			return null;
		else if (value instanceof Number)
			return value;
		else if (value instanceof String)
			return value;
		else if (value instanceof Boolean)
			return value;
		else 
			return PREFIX_BASE64 + encodeObject(value);
	}
}