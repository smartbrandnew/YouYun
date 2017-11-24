package com.broada.carrier.monitor.client.impl.impexp.util;

import java.sql.Clob;
import java.sql.SQLException;

import com.broada.common.db.Table;

public class TableUtil {
	public static String get(Table table, int row, String col, String defaultValue) {
		Object obj = table.get(row, col);
		if (obj != null && obj instanceof Clob) {
			Clob clob = (Clob) obj;
			try {
				obj = clob.getSubString(1, (int) clob.length());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		return obj != null ? obj.toString() : "";
	}

	public static String checkString(Table table, int row, String col) {
		String result = get(table, row, col, "");
		if (result.isEmpty())
			throw new IllegalArgumentException("缺少属性：" + col);
		return result;
	}

	public static int checkInteger(Table table, int row, String col) {
		Object obj = table.get(row, col);
		if (obj == null)
			throw new IllegalArgumentException("缺少属性：" + col);
		if (!(obj instanceof Number))
			throw new IllegalArgumentException("属性值类型错误：" + col + "=" + obj);
		return ((Number)obj).intValue();
	}
	
	public static boolean checkBoolean(Table table, int row, String col) {
		Object obj = table.get(row, col);
		if (obj == null)
			throw new IllegalArgumentException("缺少属性：" + col);
		if (!(obj instanceof Boolean))
			throw new IllegalArgumentException("属性值类型错误：" + col + "=" + obj);
		return ((Boolean)obj).booleanValue();
	}
}
