package com.broada.carrier.monitor.client.impl.impexp;

import java.io.File;

import jxl.Sheet;
import jxl.Workbook;

import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpFile;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpNode;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpResource;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpTask;
import com.broada.carrier.monitor.client.impl.impexp.entity.Log;
import com.broada.carrier.monitor.client.impl.impexp.entity.LogLevel;
import com.broada.carrier.monitor.client.impl.impexp.entity.MapLibrary;
import com.broada.carrier.monitor.client.impl.impexp.entity.MapObject;
import com.broada.carrier.monitor.client.impl.impexp.entity.Table;
import com.broada.carrier.monitor.client.impl.impexp.util.ExcelUtil;
import com.broada.carrier.monitor.client.impl.impexp.util.Logger;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.carrier.monitor.method.common.BaseMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.component.utils.error.ErrorUtil;

public class ImpExpFileReader {
	static final String TABLE_START = "表";
	static final String TABLE_HEAD_NO = "序号";
	static final String VALUE_EXTRA = "__extra__";
	private static final int COL_START = 1;
	private ImpExpFile ieFile = new ImpExpFile();

	public ImpExpFile read(String filename) throws IllegalArgumentException {
		return read(new File(filename));
	}

	public ImpExpFile read(File file) throws IllegalArgumentException {
		if (!file.exists() || !file.isFile())
			throw new IllegalArgumentException("文件不存在：" + file);

		Workbook workbook;
		try {
			workbook = Workbook.getWorkbook(file);
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("文件无法打开：" + file, e);
		}

		try {
			Sheet[] sheets = workbook.getSheets();
			for (Sheet sheet : sheets) {
				read(sheet);
			}
		} finally {
			workbook.close();
		}

		Logger.log(new Log(LogLevel.INFO, "读取文件：" + file + "完成"));
		return ieFile;
	}

	private void read(Sheet sheet) throws IllegalArgumentException {
		int row = 0;
		while (true) {
			row = findNextTable(sheet, COL_START, row);
			if (row < 0)
				break;

			row = readTable(sheet, COL_START, row);
		}
	}

	private int readTable(Sheet sheet, int col, int row) throws IllegalArgumentException {
		String tableType = sheet.getCell(col + 1, row).getContents();
		MapObject mo = MapObject.checkExcelTable(tableType);
		Table table = ExcelUtil.readTable(sheet, col, row);
		switch (mo) {
		case EXCEL_TABLE_METHOD:
			readTableMethod(table);
			break;
		case EXCEL_TABLE_POLICY:
			readTablePolicy(table);
			break;
		case EXCEL_TABLE_PROBE:
			readTableProbe(table);
			break;
		case EXCEL_TABLE_NODE:
			readTableNode(table);
			break;
		case EXCEL_TABLE_RESOURCE:
			readTableResource(table);
			break;
		case EXCEL_TABLE_TASK:
			readTableTask(table);
			break;
		default:
			throw new IllegalArgumentException(mo.toString());
		}
		return row + 2 + table.getRowCount();
	}

	private void readTableTask(Table table) throws IllegalArgumentException {
		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				ImpExpTask task = new ImpExpTask();
				String nodeIp = table.checkCell(MapObject.EXCEL_FIELD_NODE_IP, i);
				String resourceName = table.getCell(MapObject.EXCEL_FIELD_RESOURCE_NAME, i);
				String typeId = table.checkCell(MapObject.EXCEL_FIELD_TYPE, i);
				String name = table.checkCell(MapObject.EXCEL_FIELD_NAME, i);
				String methodCode = table.getCell(MapObject.EXCEL_FIELD_METHOD_CODE, i);
				String policyCode = table.getCell(MapObject.EXCEL_FIELD_POLICY_CODE, i);
				String instances = table.getCell(MapObject.EXCEL_FIELD_INSTANCE, i);
				boolean enabled = table.getCell(MapObject.EXCEL_FIELD_ENABLED, i, "true").equalsIgnoreCase("true");

				task.setNodeIp(nodeIp);
				task.setResourceName(resourceName);
				task.setTypeId(typeId);
				task.setMethodCode(methodCode);
				task.setPolicyCode(policyCode);
				task.setName(name);
				task.setEnabled(enabled);
				if (!instances.isEmpty() && !instances.equalsIgnoreCase(Const.VALUE_IGNORE)) {
					String[] lines = TextUtil.splitLines(instances);
					for (String line : lines) {
						int pos = line.indexOf("=");
						if (pos <= 0)
							continue;

						String instCode = line.substring(0, pos);
						String instName = instCode;
						String extra = null;
						if (pos + 2 < line.length()) {
							instName = line.substring(pos + 1);
							pos = instName.indexOf(Const.VALUE_INSTANCE_EXTRA);
							if (pos > 0) {
								String text = instName;
								instName = text.substring(0, pos);
								extra = text.substring(pos + Const.VALUE_INSTANCE_EXTRA.length());
							}
						}

						MonitorInstance mi = new MonitorInstance(instCode, instName);
						mi.setExtra(extra);
						task.add(mi);
					}
				}

				ieFile.add(task);
			}
		} catch (IllegalArgumentException e) {
			String s = e.getMessage();
			throw new IllegalArgumentException(String.format("监测任务表中第 %s 行， %s 列缺少值。", s.substring(s.indexOf(",") + 1),
					s.substring(0, s.indexOf(","))));
		}
	}

	private void readTableResource(Table table) throws IllegalArgumentException {
		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				ImpExpResource resource = new ImpExpResource();
				String ip = table.checkCell(MapObject.EXCEL_FIELD_NODE_IP, i);
				String name = table.getCell(MapObject.EXCEL_FIELD_NAME, i, ip);
				String typeId = table.checkCell(MapObject.EXCEL_FIELD_TYPE, i);

				resource.setName(name);
				resource.setTypeId(typeId);

				ieFile.add(ip, resource);
			}
		} catch (IllegalArgumentException e) {
			String s = e.getMessage();
			throw new IllegalArgumentException(String.format("监测资源表中第 %s 行， %s 列缺少值。", s.substring(s.indexOf(",") + 1),
					s.substring(0, s.indexOf(","))));
		}
	}

	private void readTableNode(Table table) throws IllegalArgumentException {
		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				ImpExpNode node = new ImpExpNode();
				String ip = table.checkCell(MapObject.EXCEL_FIELD_IP, i);
				String name = table.getCell(MapObject.EXCEL_FIELD_NAME, i, ip);
				String typeId = table.checkCell(MapObject.EXCEL_FIELD_TYPE, i);
				String domainId = table.getCell(MapObject.EXCEL_FIELD_DOMAIN, i, "rootDomain");
				String probeCode = table.getCell(MapObject.EXCEL_FIELD_PROBE_CODE, i, null);
				node.setIp(ip);
				node.setName(name);
				node.setDomainId(domainId);
				node.setTypeId(typeId);
				node.setProbeCode(probeCode);

				ieFile.add(node);
			}
		} catch (IllegalArgumentException e) {
			String s = e.getMessage();
			throw new IllegalArgumentException(String.format("监测节点表中第 %s 行 ，%s 列缺少值。", s.substring(s.indexOf(",") + 1),
					s.substring(0, s.indexOf(","))));
		}
	}

	private void readTableProbe(Table table) throws IllegalArgumentException {
		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				MonitorProbe probe = new MonitorProbe();
				String code = table.checkCell(MapObject.EXCEL_FIELD_CODE, i);
				String name = table.getCell(MapObject.EXCEL_FIELD_NAME, i, code);
				String descr = table.getCell(MapObject.EXCEL_FIELD_DESCR, i, "");
				String ip = table.checkCell(MapObject.EXCEL_FIELD_IP, i);
				int port = table.getCell(MapObject.EXCEL_FIELD_PORT, i, 9145);

				probe.setCode(code);
				probe.setName(name);
				probe.setDescr(descr);
				probe.setHost(ip);
				probe.setPort(port);

				ieFile.add(probe);
			}
		} catch (IllegalArgumentException e) {
			String s = e.getMessage();
			throw new IllegalArgumentException(String.format("监测探针表中第 %s 行， %s 列缺少值。", s.substring(s.indexOf(",") + 1),
					s.substring(0, s.indexOf(","))));
		}
	}

	private void readTablePolicy(Table table) throws IllegalArgumentException {
		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				MonitorPolicy policy = new MonitorPolicy();
				String code = table.checkCell(MapObject.EXCEL_FIELD_CODE, i);
				String name = table.getCell(MapObject.EXCEL_FIELD_NAME, i, code);
				String descr = table.getCell(MapObject.EXCEL_FIELD_DESCR, i, "");
				int interval = table.getCell(MapObject.EXCEL_FIELD_INTERVAL, i, 600);
				int errorInterval = table.getCell(MapObject.EXCEL_FIELD_ERROR_INTERVAL, i, interval);

				policy.setCode(code);
				policy.setName(name);
				policy.setDescr(descr);
				policy.setInterval(interval);
				policy.setErrorInterval(errorInterval);

				ieFile.add(policy);
			}
		} catch (IllegalArgumentException e) {
			String s = e.getMessage();
			throw new IllegalArgumentException(String.format("监测策略表中第 %s 行， %s 列缺少值。", s.substring(s.indexOf(",") + 1),
					s.substring(0, s.indexOf(","))));
		}
	}

	private void readTableMethod(Table table) throws IllegalArgumentException {
		try {
			MapObject[] fixedFields = new MapObject[] {
					MapObject.EXCEL_FIELD_INDEX,
					MapObject.EXCEL_FIELD_CODE,
					MapObject.EXCEL_FIELD_NAME,
					MapObject.EXCEL_FIELD_DESCR,
			};

			String type = table.checkProperty(MapObject.EXCEL_FIELD_TYPE);

			for (int i = 0; i < table.getRowCount(); i++) {
				BaseMethod method = new BaseMethod();
				String code = table.checkCell(MapObject.EXCEL_FIELD_CODE, i);
				String name = table.getCell(MapObject.EXCEL_FIELD_NAME, i, code);
				String descr = table.getCell(MapObject.EXCEL_FIELD_DESCR, i, "");

				method.setTypeId(type);
				method.setCode(code);
				method.setName(name);
				method.setDescr(descr);

				for (int j = 0; j < table.getColumnCount(); j++) {
					String colName = table.getColumn(j);
					MapObject mo = MapObject.get(fixedFields, colName);
					if (mo != null)
						continue;

					String value = table.getCell(j, i);
					if (value != null) {
						String valueType = MapLibrary.getDefault().get("carrier.method." + type + "." + colName);
						if (valueType != null && valueType.equalsIgnoreCase(VALUE_EXTRA))
							method.getExtraProperties().set(colName, value);
						else
							method.getProperties().set(colName, value);
					}
				}

				ieFile.add(method);
			}
		} catch (IllegalArgumentException e) {
			String s = e.getMessage();
			throw new IllegalArgumentException(String.format("监测方法表中第 %s 行， %s 列缺少值。", s.substring(s.indexOf(",") + 1),
					s.substring(0, s.indexOf(","))));
		}
	}

	private static int findNextTable(Sheet sheet, int col, int row) {
		int ret = -1;
		int maxRow = sheet.getRows() - 1;
		while (row < maxRow) {
			String value1 = sheet.getCell(col, row).getContents();
			String value2 = sheet.getCell(col, row + 1).getContents();
			if (TABLE_START.equals(value1) && TABLE_HEAD_NO.equals(value2)) {
				ret = row;
				break;
			}
			row++;
		}
		return ret;
	}
}
