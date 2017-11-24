package com.broada.carrier.monitor.common.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TextUtil {
	public static String[] splitLines(String text) {
		if (text == null)
			return new String[0];
		return text.split("\r?\n");
	}
	
	private static final int SPLIT_SUFFIX_LENGTH = sizeof("(9/9)");
	private static final int SPLIT_PAGE_MAX = 9;
	private static final int TRUNCATE_SUFFIX_LENGTH = sizeof("(省字)");
	
	public static boolean isLegalCode(String text) {
		if (isEmpty(text))
			return false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c >= 'a' && c <= 'z'
					|| c >= 'A' && c <= 'Z'
					|| c >= '0' && c <= '9'
					|| c == '-'
					|| c == '_')
				continue;
			return false;
		}
		return true;
	}
	
	public static boolean isEmpty(String text) {
		return text == null || text.trim().isEmpty();
	}
	
	/**
	 * <pre>
	 * 根据maxBytes指定的字节长度，将text分隔为多个文本，使每个文本的字节长度不能超于length。
	 * 并有以下要求：
	 * 1. 支持中文，由于一个中文占多个字节，此函数分隔不会导致切断了中文出现乱码
	 * 2. 如返回值超过1个字符串，则每个字符串结尾有分页信息“（当前页/总页数）”
	 * </pre>
	 * 
	 * @param text
	 * @param length
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String[] split(String text, int maxBytes) {
		char[] cs = text.toCharArray();
		int bytesLen = sizeof(cs);
		if (bytesLen <= maxBytes)
			return new String[] {text};
		
		int pageMaxBytes = maxBytes - SPLIT_SUFFIX_LENGTH;
		
		int pageBytes = 0;		
		List<String> pages = new ArrayList<String>(bytesLen / maxBytes + 1);				
		StringBuilder page = new StringBuilder(maxBytes);
		for (int i = 0; i < cs.length; i++) {
			char c = cs[i];
			if (pageBytes + sizeof(c) > pageMaxBytes) {				
				if (pageBytes == 0)
					throw new IllegalArgumentException("页最大字节数过小，无法进行分割");
				pages.add(page.toString());
				page.setLength(0);
				pageBytes = 0;
			} 
			page.append(c);
			pageBytes += sizeof(c);
		}
		if (page.length() > 0)
			pages.add(page.toString());
		
		String[] result = new String[Math.min(pages.size(), SPLIT_PAGE_MAX)];
		for (int i = 0; i < result.length; i++) 
			result[i] = pages.get(i) + "(" + (i + 1) + "/" + result.length + ")";
		return result;
	}

	/**
	 * <pre>
	 * 根据maxBytes指定的字节长度，将text进行截断。
	 * 并有以下要求：
	 * 1. 支持中文，由于一个中文占多个字节，此函数分隔不会导致切断了中文出现乱码
	 * 2. 如确实被截断了，则字符串的结尾有截断说明“（省23字）”
	 * </pre>
	 * 
	 * @param text
	 * @param maxBytes
	 * @return
	 */
	public static String truncate(String text, int maxBytes) {
		if (text == null || text.length() < maxBytes / 2)
			return text;
		
		char[] cs = text.toCharArray();
		int bytesLen = sizeof(cs);
		if (bytesLen <= maxBytes)
			return text;
		
		int pageBytes = 0;	
		int remainWordNum = 0;
		StringBuilder page = new StringBuilder(maxBytes);		
		for (int i = 0; i < cs.length; i++) {
			remainWordNum = cs.length - i;
			int pageMaxBytes = maxBytes - (TRUNCATE_SUFFIX_LENGTH + Integer.toString(remainWordNum).length());
			char c = cs[i];
			if (pageBytes + sizeof(c) > pageMaxBytes) {
				if (pageBytes == 0)
					throw new IllegalArgumentException("页最大字节数过小，无法进行截断");
				break;		
			}
			page.append(c);
			pageBytes += sizeof(c);
		}
		
		return page.toString() + "(省" + remainWordNum + "字)";
	}
	
	private static int sizeof(char c) {
		if (c < 128)
			return 1;
		else
			return 2;		
	}
	
	private static int sizeof(String str) {
		return sizeof(str.toCharArray());
	}
	
	private static int sizeof(char[] cs) {
		int sum = 0; 
		for (char c : cs)
			sum += sizeof(c);
		return sum;
	}

	/**
	 * 从text中截取prefix与suffix之间的字符串
	 * @param text
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	public static String between(String text, String prefix, String suffix) {
		String result = null;
		int prefixPos = text.indexOf(prefix);
		if (prefixPos >= 0) {
			int startPos = prefixPos + prefix.length();
			if (prefixPos < text.length()) {
				int suffixPos;
				if (suffix == null)
					suffixPos = text.length();
				else
					suffixPos = text.indexOf(suffix, startPos);
				if (suffixPos > 0)
					result = text.substring(startPos, suffixPos);
			}
		}
		return result;
	}	
}
