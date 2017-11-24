package uyun.bat.datastore.entity;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryMetric;
import uyun.bat.datastore.api.util.StringUtils;

public class MetricTranslate {
	private static final String BASE64_PREFIX = "-b64-";
	private static final String EMPTY = "EMPTY";
	private static final String BASE64_EQUAL_REPACLE = ">";
	private static final String DEFAULT_CHARSET = "UTF-8";

	//tag、name非法字符的转换
	public static String translateStr(String value) {

		if (value == null) {
			throw new IllegalArgumentException("metric name or tagk、tagv can't be null");
		} else if (StringUtils.isBlank(value)) {
			return EMPTY;
		}//非法字符目前已知":"、"="、空格
		else if (value.contains(":") || value.contains("=") || isContainsChinese(value) || value.contains(" ")) {
			byte[] bytes = Base64.getEncoder().encode(value.getBytes(Charset.forName(DEFAULT_CHARSET)));
			value = new String(bytes, Charset.forName(DEFAULT_CHARSET));
			value = value.replace("=", BASE64_EQUAL_REPACLE);
			value = BASE64_PREFIX + value;
			return value;
		}
		return value;
	}

	public static String deTranslateStr(String value) {
		if (value == null) {
			return null;
		} else if (EMPTY.equals(value)) {
			return "";
		}//非法字符目前已知":"、"="
		else if (value.contains(BASE64_PREFIX)) {
			String[] arrs = value.split(",");
			StringBuilder builder = new StringBuilder();
			for (String str : arrs) {
				if (str.startsWith(BASE64_PREFIX)) {
					str = str.replace(BASE64_PREFIX, "");
					str = str.replace(BASE64_EQUAL_REPACLE, "=");
					byte[] bytes = Base64.getDecoder().decode(str.getBytes(Charset.forName(DEFAULT_CHARSET)));
					str = new String(bytes, Charset.forName(DEFAULT_CHARSET));

				}
				builder.append(str);
				builder.append(",");
			}
			value = builder.toString();
			value = value.substring(0, value.lastIndexOf(","));
			return value;
		}
		return value;
	}

	public static List<String> deTranslateList(List<String> list) {
		if (list == null)
			return new ArrayList<String>();
		List<String> strs = new ArrayList<String>();
		for (String str : list) {
			strs.add(deTranslateStr(str));
		}
		return strs;
	}

	//base64编码tag、name(含有非法字符)
	public static void translate(PerfMetric metric) {
		metric.setName(translateStr(metric.getName().trim()));
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (Entry<String, List<String>> entry : metric.getTags().entrySet()) {
			String key = entry.getKey();
			List<String> values = entry.getValue();
			map.put(translateStr(key), values);
			for (int i = 0; i < values.size(); i++) {
				values.set(i, translateStr(values.get(i)));
			}
		}
		metric.setTags(map);
	}

	//base64解码tag、name
	public static void deTranslate(PerfMetric metric) {
		metric.setName(deTranslateStr(metric.getName()));
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (Entry<String, List<String>> entry : metric.getTags().entrySet()) {
			String key = entry.getKey();
			List<String> values = entry.getValue();
			map.put(deTranslateStr(key), values);
			for (int i = 0; i < values.size(); i++) {
				values.set(i, deTranslateStr(values.get(i)));
			}
		}
		metric.setTags(map);
	}

	// 根据Unicode编码完美的判断中文汉字和符号
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	// 完整的判断中文汉字和符号
	private static boolean isContainsChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

	public static void translate(QueryMetric metric) {
		metric.setName(translateStr(metric.getName().trim()));
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (Entry<String, List<String>> entry : metric.getTags().entrySet()) {
			String key = entry.getKey();
			List<String> values = entry.getValue();
			List<String> list = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			map.put(translateStr(key), list);
			for (int i = 0; i < values.size(); i++) {
				sb.append(translateStr(values.get(i)));
				sb.append(",");
			}
			String s = sb.substring(0, sb.lastIndexOf(","));
			list.add(s);
		}
		List<String> groupers = metric.getGroupers();
		for (int i = 0; i < groupers.size(); i++) {
			groupers.set(i, translateStr(groupers.get(i)));
		}
		metric.setTags(map);
	}

	public static void deTranslate(QueryMetric metric) {
		metric.setName(deTranslateStr(metric.getName()));
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (Entry<String, List<String>> entry : metric.getTags().entrySet()) {
			String key = entry.getKey();
			List<String> values = entry.getValue();
			map.put(deTranslateStr(key), values);
			for (int i = 0; i < values.size(); i++) {
				values.set(i, deTranslateStr(values.get(i)));
			}
		}
		metric.setTags(map);
	}

	public static void main(String[] args) {
		String str = MetricTranslate.deTranslateStr("-b64-QWRvYmUgTE0gU2VydmljZQ>>");
		System.out.println("str: " + str);
		System.out.println("length: " + str.length());
		System.out.println("-b64-QWRvYmUgTE0gU2VydmljZQ>>".equals(MetricTranslate.translateStr("Adobe LM Service")));
		System.out.println(MetricTranslate.deTranslateStr("-b64-6LW15Lqa5Y2XbGludXg>"));
	}
}
