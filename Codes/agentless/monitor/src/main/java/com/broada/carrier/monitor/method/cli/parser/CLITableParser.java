package com.broada.carrier.monitor.method.cli.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * 表格解析工具类
 * @author Jiangjw
 */
public class CLITableParser {
	/**
	 * 解析一个表格，第1行为标题行，分隔符为\s+
	 * @param text
	 * @return
	 */
	public static CLITable parse(String text) {
		return parse(text, 0);
	}

	/**
	 * 解析一个表格，第titleLine为标题行，分隔符为\s+
	 * @param text
	 * @param titleLine
	 * @return
	 */
	public static CLITable parse(String text, int titleLine) {
		return parse(text, titleLine, "\\s+");
	}

	/**
	 * 解析一个表格，第titleLine为标题行，分隔符为split
	 * @param text
	 * @param titleLine
	 * @param split
	 * @return
	 */
	public static CLITable parse(String text, int titleLine, String split) {
		String[] lines = text.split("\n");
		return parse(lines[titleLine], lines, titleLine + 1, lines.length - titleLine - 1, split);
	}

	/**
	 * 解析一个表格
	 * @param titleLine
	 * @param dataLines
	 * @param offset 从第几行开始解析数据
	 * @param length 共解析几行数据
	 * @param split
	 * @return
	 */
	public static CLITable parse(String titleLine, String[] dataLines, int offset, int length, String split) {
		String[] columns = titleLine.split(split);
		List<String[]> rows = new ArrayList<String[]>(length);
		List<String> beforeRow = new ArrayList<String>(columns.length);
		for (int i = 0; i + offset < dataLines.length && i < length; i++) {
			String[] row = dataLines[i + offset].split(split);
			int valueCount = 0;
			for (String value : row)
				if (!value.isEmpty())
					valueCount++;
			if (valueCount < columns.length) {
				for (String data : row) {
					if (data.isEmpty())
						continue;
					beforeRow.add(data);
				}
				if (beforeRow.size() < columns.length) 					
					continue;
				row = beforeRow.toArray(new String[beforeRow.size()]);
				beforeRow.clear();
			} else if (beforeRow.isEmpty())
				beforeRow.clear();
			rows.add(row);
		}
		return new CLITable(columns, rows);
	}
}
