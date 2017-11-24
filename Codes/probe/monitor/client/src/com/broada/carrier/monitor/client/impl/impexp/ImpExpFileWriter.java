package com.broada.carrier.monitor.client.impl.impexp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpFile;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpNode;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpResource;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpTask;
import com.broada.carrier.monitor.client.impl.impexp.entity.Log;
import com.broada.carrier.monitor.client.impl.impexp.entity.LogLevel;
import com.broada.carrier.monitor.client.impl.impexp.entity.MapObject;
import com.broada.carrier.monitor.client.impl.impexp.util.ExcelUtil;
import com.broada.carrier.monitor.client.impl.impexp.util.Logger;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;

public class ImpExpFileWriter {
	private ImpExpFile file;

	public void write(ImpExpFile file, String filename) {
		this.file = file;
		WritableWorkbook workbook = null;
		try {
			workbook = Workbook.createWorkbook(new File(filename));
			int sheetIndex = 0;
			writeMethods(workbook, sheetIndex++);
			writePolicies(workbook, sheetIndex++);
			writeProbes(workbook, sheetIndex++);
			writeNodes(workbook, sheetIndex++);
			writeResources(workbook, sheetIndex++);
			writeTasks(workbook, sheetIndex++);
			Logger.log(new Log(LogLevel.INFO, "监测任务已全部写入到文件：" + filename));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (workbook != null) {
					workbook.write();
					workbook.close();
				}
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN, "关闭文件失败：" + filename, e));
			}
		}
	}

	private void writeTasks(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet(
				MapObject.EXCEL_TABLE_TASK.getId(), sheetIndex);

		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_START);
		ExcelUtil.writeCell(sheet, row, col++,
				MapObject.EXCEL_TABLE_TASK.getId());
		row++;

		col = Const.TABLE_COL_START;
		ExcelUtil
				.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_NODE_IP.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_RESOURCE_NAME.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_TYPE.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_NAME.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_METHOD_CODE.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_POLICY_CODE.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_INSTANCE.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_ENABLED.getId());
		row++;

		int no = 1;
		for (ImpExpTask task : file.getTasks()) {
			col = Const.TABLE_COL_START;
			ExcelUtil.writeCell(sheet, row, col++, no);
			ExcelUtil.writeCell(sheet, row, col++, task.getNodeIp());
			ExcelUtil.writeCell(sheet, row, col++, task.getResourceName());
			ExcelUtil.writeCell(sheet, row, col++, task.getTypeId());
			ExcelUtil.writeCell(sheet, row, col++, task.getName());
			ExcelUtil.writeCell(sheet, row, col++, task.getMethodCode());
			ExcelUtil.writeCell(sheet, row, col++, task.getPolicyCode());
			ExcelUtil.writeCell(sheet, row, col++, encode(task));
			ExcelUtil.writeCell(sheet, row, col++, task.isEnabled());
			row++;
			no++;
		}
	}

	private String encode(ImpExpTask task) {
		List<MonitorInstance> instances = task.getInstances();
		StringBuilder sb = new StringBuilder();
		for (MonitorInstance instance : instances) {
			if (sb.length() > 0)
				sb.append("\n");
			sb.append(instance.getCode()).append("=")
					.append(instance.getName());
			if (instance.getExtra() != null && !instance.getExtra().isEmpty())
				sb.append(Const.VALUE_INSTANCE_EXTRA).append(
						instance.getExtra());
		}
		String result = sb.toString();
		byte[] data = result.getBytes();
		if (data.length > Const.CELL_LENGTH_MAX)
			result = Const.VALUE_IGNORE;
		return result;
	}

	private void writeResources(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet(
				MapObject.EXCEL_TABLE_RESOURCE.getId(), sheetIndex);

		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_START);
		ExcelUtil.writeCell(sheet, row, col++,
				MapObject.EXCEL_TABLE_RESOURCE.getId());
		row++;

		col = Const.TABLE_COL_START;
		ExcelUtil
				.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_NODE_IP.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_NAME.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_TYPE.getId());
		row++;

		int no = 1;
		for (ImpExpNode node : file.getNodes().values()) {
			for (ImpExpResource resource : node.getResources().values()) {
				col = Const.TABLE_COL_START;
				ExcelUtil.writeCell(sheet, row, col++, no);
				ExcelUtil.writeCell(sheet, row, col++, node.getIp());
				ExcelUtil.writeCell(sheet, row, col++, resource.getName());
				ExcelUtil.writeCell(sheet, row, col++, resource.getTypeId());
				row++;
				no++;
			}
		}
	}

	private void writeNodes(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet(
				MapObject.EXCEL_TABLE_NODE.getId(), sheetIndex);

		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_START);
		ExcelUtil.writeCell(sheet, row, col++,
				MapObject.EXCEL_TABLE_NODE.getId());
		row++;

		col = Const.TABLE_COL_START;
		ExcelUtil
				.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_NAME.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_IP.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_TYPE.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_DOMAIN.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_PROBE_CODE.getId());
		row++;

		int no = 1;
		for (ImpExpNode node : file.getNodes().values()) {
			col = Const.TABLE_COL_START;
			ExcelUtil.writeCell(sheet, row, col++, no);
			ExcelUtil.writeCell(sheet, row, col++, node.getName());
			ExcelUtil.writeCell(sheet, row, col++, node.getIp());
			ExcelUtil.writeCell(sheet, row, col++, node.getTypeId());
			ExcelUtil.writeCell(sheet, row, col++, node.getDomainId());
			ExcelUtil.writeCell(sheet, row, col++, node.getProbeCode());
			row++;
			no++;
		}
	}

	private void writeProbes(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet(
				MapObject.EXCEL_TABLE_PROBE.getId(), sheetIndex);

		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_START);
		ExcelUtil.writeCell(sheet, row, col++,
				MapObject.EXCEL_TABLE_PROBE.getId());
		row++;

		col = Const.TABLE_COL_START;
		ExcelUtil
				.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_CODE.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_NAME.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_DESCR.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_IP.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_PORT.getId());
		row++;

		int no = 1;
		for (MonitorProbe probe : file.getProbes().values()) {
			col = Const.TABLE_COL_START;
			ExcelUtil.writeCell(sheet, row, col++, no);
			ExcelUtil.writeCell(sheet, row, col++, probe.getCode());
			ExcelUtil.writeCell(sheet, row, col++, probe.getName());
			ExcelUtil.writeCell(sheet, row, col++, probe.getDescr());
			ExcelUtil.writeCell(sheet, row, col++, probe.getHost());
			ExcelUtil.writeCell(sheet, row, col++, probe.getPort());
			row++;
			no++;
		}
	}

	private void writePolicies(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet(
				MapObject.EXCEL_TABLE_POLICY.getId(), sheetIndex);

		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_START);
		ExcelUtil.writeCell(sheet, row, col++,
				MapObject.EXCEL_TABLE_POLICY.getId());
		row++;

		col = Const.TABLE_COL_START;
		ExcelUtil
				.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_CODE.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_NAME.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_DESCR.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_INTERVAL.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_ERROR_INTERVAL.getId());
		row++;

		int no = 1;
		for (MonitorPolicy policy : file.getPolicies()) {
			col = Const.TABLE_COL_START;
			ExcelUtil.writeCell(sheet, row, col++, no);
			ExcelUtil.writeCell(sheet, row, col++, policy.getCode());
			ExcelUtil.writeCell(sheet, row, col++, policy.getName());
			ExcelUtil.writeCell(sheet, row, col++, policy.getDescr());
			ExcelUtil.writeCell(sheet, row, col++, policy.getInterval());
			ExcelUtil.writeCell(sheet, row, col++, policy.getErrorInterval());
			row++;
			no++;
		}
	}

	private void writeMethods(WritableWorkbook workbook, int sheetIndex) {
		if (file.getMethods().isEmpty())
			return;

		Map<String, List<MonitorMethod>> methods = new LinkedHashMap<String, List<MonitorMethod>>();
		for (MonitorMethod method : file.getMethods()) {
			List<MonitorMethod> list = methods.get(method.getTypeId());
			if (list == null) {
				list = new ArrayList<MonitorMethod>();
				methods.put(method.getTypeId(), list);
			}
			list.add(method);
		}

		WritableSheet sheet = workbook.createSheet(
				MapObject.EXCEL_TABLE_METHOD.getId(), sheetIndex);
		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		for (Entry<String, List<MonitorMethod>> entry : methods.entrySet()) {
			row = writeMethod(sheet, row, col, entry.getValue()) + 2;
		}
	}

	private int writeMethod(WritableSheet sheet, int tableRow, int tableCol,
			List<MonitorMethod> methods) {
		int row = tableRow;
		int col = tableCol;

		String typeId = methods.get(0).getTypeId();
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_START);
		ExcelUtil.writeCell(sheet, row, col++,
				MapObject.EXCEL_TABLE_METHOD.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_TYPE.getId());
		ExcelUtil.writeCell(sheet, row, col++, typeId);
		row++;

		col = tableCol;
		ExcelUtil
				.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_CODE.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_NAME.getId());
		ExcelUtil.writeHeader(sheet, row, col++,
				MapObject.EXCEL_FIELD_DESCR.getId());
		int headerRow = row;
		int headerCol = col;
		row++;

		int no = 1;
		Map<String, Integer> propertyIndexs = new HashMap<String, Integer>();
		for (MonitorMethod method : methods) {
			col = tableCol;
			ExcelUtil.writeCell(sheet, row, col++, no);
			ExcelUtil.writeCell(sheet, row, col++, method.getCode());
			ExcelUtil.writeCell(sheet, row, col++, method.getName());
			ExcelUtil.writeCell(sheet, row, col++, method.getDescr());
			for (Entry<String, Object> entry : method.getProperties()
					.entrySet()) {
				Integer index = propertyIndexs.get(entry.getKey());
				if (index == null) {
					index = headerCol++;
					propertyIndexs.put(entry.getKey(), index);
					ExcelUtil.writeHeader(sheet, headerRow, index,
							entry.getKey());
				}
				ExcelUtil.writeCell(sheet, row, index, entry.getValue());
			}
			row++;
			no++;
		}

		return row;
	}

}
