package com.broada.carrier.monitor.client.impl.impexp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.impexp.entity.Log;
import com.broada.carrier.monitor.client.impl.impexp.entity.LogLevel;
import com.broada.carrier.monitor.client.impl.impexp.util.ExcelUtil;
import com.broada.carrier.monitor.client.impl.impexp.util.Logger;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.component.utils.text.DateUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ExporterCarrierType {
	private String serverIp;
	private String username;
	private String password;
	private File file;		
	private MonitorType[] types;
	private MonitorItem[] items;
	private Map<String, List<String>> targetTypes = new LinkedHashMap<String, List<String>>();

	public ExporterCarrierType(String serverIp, String username, String password, File file) {
		this.serverIp = serverIp;
		this.username = username;
		this.password = password;
		this.file = file;
	}

	public void exp() {
		loadData();		
		writeFile();
	}

	private void writeFile() {		
		WritableWorkbook workbook = null;
		try {
			workbook = Workbook.createWorkbook(file);
			int sheetIndex = 0;			
			writeSummary(workbook, sheetIndex++);
			writeTypes(workbook, sheetIndex++);
			writeTargetTypes(workbook, sheetIndex++);
			writeItems(workbook, sheetIndex++);
			Logger.log(new Log(LogLevel.INFO, "监测类型定义已全部写入到文件：" + file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (workbook != null) {
					workbook.write();
					workbook.close();
				}
			} catch (Throwable e) {
				Logger.log(new Log(LogLevel.WARN, "关闭文件失败：" + file, e));
			}
		}
	}

	private void writeSummary(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet("汇总", sheetIndex);
		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;		
		ExcelUtil.setColumnWidth(sheet, col, new Integer[]{20, 20});
		writeKeyValue(sheet, row++, col, "导出时间", DateUtil.format(new Date()));
		writeKeyValue(sheet, row++, col, "监测任务数", types.length);
		writeKeyValue(sheet, row++, col, "监测指标数", items.length);
	}

	private void writeKeyValue(WritableSheet sheet, int row, int col, String key, Object value) {
		ExcelUtil.writeHeader(sheet, row, col++, key);
		ExcelUtil.writeCell(sheet, row, col++, value);
	}

	private void writeItems(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet("监测指标", sheetIndex);

		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		ExcelUtil.setColumnWidth(sheet, col, new Integer[]{10, 20, 20, 10, 10, 100});
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++, "编码");
		ExcelUtil.writeHeader(sheet, row, col++, "名称");
		ExcelUtil.writeHeader(sheet, row, col++, "类型");
		ExcelUtil.writeHeader(sheet, row, col++, "单位");
		ExcelUtil.writeHeader(sheet, row, col++, "说明");		
		row++;
		
		int no = 1;
		for (MonitorItem item : items) {			
			col = Const.TABLE_COL_START;
			ExcelUtil.writeCell(sheet, row, col++, no);
			ExcelUtil.writeCell(sheet, row, col++, item.getCode());
			ExcelUtil.writeCell(sheet, row, col++, item.getName());
			ExcelUtil.writeCell(sheet, row, col++, item.getType());
			ExcelUtil.writeCell(sheet, row, col++, item.getUnit());
			ExcelUtil.writeCell(sheet, row, col++, item.getDescr());
			row++;
			no++;
		}		
	}

	private void writeTargetTypes(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet("监测资源", sheetIndex);

		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		ExcelUtil.setColumnWidth(sheet, col, new Integer[]{10, 20, 150});
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++, "资源编码");
		ExcelUtil.writeHeader(sheet, row, col++, "适用监测任务");

		row++;
		
		int no = 1;
		for (Entry<String, List<String>> entry : targetTypes.entrySet()) {
			String text = entry.getValue().toString();
			col = Const.TABLE_COL_START;
			ExcelUtil.writeCell(sheet, row, col++, no);
			ExcelUtil.writeCell(sheet, row, col++, entry.getKey());
			ExcelUtil.writeCell(sheet, row, col++, TextUtil.between(text, "[", "]"));
			row++;
			no++;
		}		
	}

	private void writeTypes(WritableWorkbook workbook, int sheetIndex) {
		WritableSheet sheet = workbook.createSheet("监测任务", sheetIndex);

		int row = Const.TABLE_ROW_START;
		int col = Const.TABLE_COL_START;
		ExcelUtil.setColumnWidth(sheet, col, new Integer[]{10, 20, 40, 40, 100});
		ExcelUtil.writeHeader(sheet, row, col++, ImpExpFileReader.TABLE_HEAD_NO);
		ExcelUtil.writeHeader(sheet, row, col++, "编码");
		ExcelUtil.writeHeader(sheet, row, col++, "名称");
		ExcelUtil.writeHeader(sheet, row, col++, "适用资源");
		ExcelUtil.writeHeader(sheet, row, col++, "说明");		
		row++;
		
		int no = 1;
		for (MonitorType type : types) {
			String text = Arrays.toString(type.getTargetTypeIds());
			col = Const.TABLE_COL_START;
			ExcelUtil.writeCell(sheet, row, col++, no);
			ExcelUtil.writeCell(sheet, row, col++, type.getId());
			ExcelUtil.writeCell(sheet, row, col++, type.getName());
			ExcelUtil.writeCell(sheet, row, col++, TextUtil.between(text, "[", "]"));
			ExcelUtil.writeCell(sheet, row, col++, type.getDescription());
			row++;
			no++;
		}		
	}

	private void loadData() {
		ServerContext.connect(serverIp);
		ServerContext.login(username, password);
		try {
			loadTypes();
			loadItems();			
		} finally {
			ServerContext.logout();
		}		
	}

	private void loadItems() {
		items = ServerContext.getTypeService().getItems();
		Arrays.sort(items, new Comparator<MonitorItem>() {
			@Override
			public int compare(MonitorItem o1, MonitorItem o2) {
				return o1.getCode().compareTo(o2.getCode());
			}
		});
		Logger.log(new Log(LogLevel.INFO, String.format("共加载监测指标 %d 个。", items.length)));
	}

	private void loadTypes() {
		types = ServerContext.getTypeService().getTypes();
		Arrays.sort(types, new Comparator<MonitorType>() {
			@Override
			public int compare(MonitorType o1, MonitorType o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		Logger.log(new Log(LogLevel.INFO, String.format("共加载监测类型 %d 个。", types.length)));
		
		for (MonitorType type : types) {
			for (String targetTypeId : type.getTargetTypeIds()) {
				addTargetType(targetTypeId, type.getId());
			}
		}		
	}

	private void addTargetType(String targetTypeId, String typeId) {
		List<String> typeIds = targetTypes.get(targetTypeId);
		if (typeIds == null) {
			typeIds = new ArrayList<String>();
			targetTypes.put(targetTypeId, typeIds);
		}
		typeIds.add(typeId);				
	}
}
