package com.broada.carrier.monitor.impl.db.db2;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于保存DB2执行结果解析后内容
 */
public class DB2CmdTable {
	private String server = "unknown";
	private String sqlAuthId = "unknown";
	private String dbAlias = "unknown";
	private String[] columns = new String[0];
	private List<List<String>> rows = new ArrayList<List<String>>();
	
	/**
	 * 从命令行输出中构建一个DB2CmdTable，表格类似为以下格式
	 *
	 *    Database Connection Information
	 * 
	 *  Database server        = DB2/AIX64 8.2.9
	 *  SQL authorization ID   = ZZQZ
	 *  Local database alias   = ZZQZ
	 * 
	 * 
	 * POOL_DATA_L_READS    POOL_INDEX_L_READS   POOL_DATA_P_READS    POOL_INDEX_P_READS   POOL_DATA_RATIO                   POOL_INDEX_RATIO                  DIRECT_READS         DIRECT_WRITES       
	 * -------------------- -------------------- -------------------- -------------------- --------------------------------- --------------------------------- -------------------- --------------------
	 *                    0                    0                    0                    0                      1.0000000000                      1.0000000000                    0                    0
	 *                    0                    0                    0                    0                      1.0000000000                      1.0000000000                    0                    0
	 * 
	 *   2 record(s) selected.
	 * 
	 * @param cmdOutput
	 */
	public DB2CmdTable(String cmdOutput) {
		super();
		
		String lines[] = cmdOutput.split("\n");
		
		// 分析首部信息
		for (String line : lines) {
			int index = line.indexOf("=");
			if (index < 0)
				continue;
			
			if (line.contains("Database server"))
				server = line.substring(index + 2).trim();
			else if (line.contains("SQL authorization ID"))
				sqlAuthId = line.substring(index + 2).trim();
			else if (line.contains("Local database alias")) {
				dbAlias = line.substring(index + 2).trim();
				break;
			}
		}
		
		// 分析列位置与标头
		int tableStubLine = -1;
		List<Integer> columnsStart = new ArrayList<Integer>();
		List<Integer> columnsEnd = new ArrayList<Integer>();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (!line.startsWith("---"))
				continue;
			
			tableStubLine = i;
			int index = 0;
			columnsStart.add(0);
			while (true) {
				index = line.indexOf("- -", index);
				if (index < 0)
					break;
				
				columnsEnd.add(index + 1);
				columnsStart.add(index + 2);
				index = index + 1;
			}
			columnsEnd.add(line.length());
		}
		
		if (tableStubLine < 0)
			return;
		
		String columnsLine = lines[tableStubLine - 1];
		columns = new String[columnsStart.size()];
		for (int i = 0; i < columnsStart.size(); i++) {
			int end = Math.min(columnsEnd.get(i), columnsLine.length());
			columns[i] = columnsLine.substring(columnsStart.get(i), end).trim();
		}
		
		rows.clear();
		for (int i = tableStubLine + 1; i < lines.length; i++) {
			String line = lines[i];
			if (line.trim().length() == 0)
				break;
			
			List<String> row = new ArrayList<String>(columns.length);
			for (int j = 0; j < columnsStart.size(); j++) {
				int end = Math.min(columnsEnd.get(j), line.length());
				String data = line.substring(columnsStart.get(j), end).trim();
				row.add(data);
			}
			rows.add(row);
		}
	}

	/**
	 * 获取DB2服务名称，如DB2/AIX64 8.2.9
	 * @return
	 */
	public String getServer() {
		return server;
	}

	/**
	 * 获取SQL许可用户ID
	 * @return
	 */
	public String getSqlAuthId() {
		return sqlAuthId;
	}

	/**
	 * 获取DB2数据库别名
	 * @return
	 */
	public String getDbAlias() {
		return dbAlias;
	}

	/**
	 * 获取所有列标题
	 * @return
	 */
	public String[] getColumns() {
		return columns;
	}
	
	/**
	 * 获取指定列名的索引号，大小写不敏感
	 * @param column
	 * @return 如果指定列名不存在，返回-1
	 */
	public int getColumnIndex(String column) {
		for (int i = 0; i < getColumnsSize(); i++)
			if (columns[i].equalsIgnoreCase(column))
				return i;
		return -1;
	}

	/**
	 * 获取行数
	 * @return
	 */
	public int getRowsSize() {
		return rows.size();
	}
	
	/**
	 * 获取列数
	 * @return
	 */
	public int getColumnsSize() {
		return columns.length;
	}
	
	
	/**
	 * 获取指定行指定列的数据
	 * @param row
	 * @param col
	 * @return
	 */
	public String getData(int row, int col) {
		List<String> tempRow = rows.get(row);
		return tempRow.get(col);
	}

	/**
	 * 获取列头
	 * @param i
	 * @return
	 */
	public String getColumn(int i) {
		return columns[i];
	}
}
