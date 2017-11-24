package com.broada.carrier.monitor.method.cli.parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 封装CLI解析表格
 * @author Jiangjw
 */
public class CLITable {	
	private Map<String, Integer> columnsIndex;
	private List<String[]> rows;
	
	/**
	 * 构造一个表格
	 * @param columns
	 * @param rows
	 */
	public CLITable(String[] columns, List<String[]> rows) {
		columnsIndex = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < columns.length; i++)
			columnsIndex.put(columns[i].toLowerCase(), i);
		this.rows = rows;
	}

	/**
	 * 获取表格总行数
	 * @return
	 */
	public int getRowCount() {
		return rows.size();
	}
	
	/**
	 * 获取指定的单元格值
	 * @param row 第1行为0
	 * @param col 第1列为0
	 * @return 如果不存在返回null
	 */
	public String get(int row, int col) {
		return get(row, col, null);
	}
	
	/**
	 * 获取指定的单元格值
	 * @param row 第1行为0
	 * @param col 第1列为0
	 * @param defValue 默认值
	 * @return 如果单元格不存在或为null则返回defValue
	 */	
	public String get(int row, int col, String defValue) {
		if (row < 0 || row >= rows.size())			
			return defValue;
		if (col < 0)
			return defValue;
		String[] datas = rows.get(row);
		if (col >= datas.length)
			return defValue;
		return datas[col] == null ? defValue : datas[col];
	}

	/**
	 * 获取指定的单元格值
	 * @param row 第1行为0
	 * @param column 大小写不敏感
	 * @return 如果不存在返回null
	 */
	public String get(int row, String column) {						
		return get(row, getColumnIndex(column));
	}	
	
	/**
	 * 获取指定的单元格值
	 * @param row 第1行为0
	 * @param column 大小写不敏感
	 * @param defValue 默认值
	 * @return 如果单元格不存在或为null则返回defValue
	 */
	public String get(int row, String column, String defValue) {						
		return get(row, getColumnIndex(column), defValue);
	}

	/**
	 * 查找指定的列索引，如不存在返回-1
	 * @param column 大小写不敏感
	 * @return
	 */
	public int getColumnIndex(String column) {
		Integer ret = columnsIndex.get(column.toLowerCase());
		return ret == null ? -1 : ret;
	}

	/**
	 * 检查是否存在指定的列，如果不存在弹出异常
	 * @param column
	 * @return
	 */
	public int checkColumnIndex(String column) {
		int ret = getColumnIndex(column);
		if (ret == -1)
			throw new IllegalArgumentException("不存在的表格列：" + column);
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("no.");
		for (String column : columnsIndex.keySet()) {
			sb.append('\t').append(column);
		}
		for (int i = 0; i < rows.size(); i++) {
			sb.append('\n');
			sb.append(i).append('.');
			for (int j = 0; j < columnsIndex.size(); j++) {
				sb.append('\t').append(get(i, j));
			}
		}
		return sb.toString();
	}
}
