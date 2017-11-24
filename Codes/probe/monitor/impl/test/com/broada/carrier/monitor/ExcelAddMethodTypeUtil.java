package com.broada.carrier.monitor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class ExcelAddMethodTypeUtil {
	private static Map<String, String[]> typeMap = new HashMap<String, String[]>();
	static {
		ServiceLoader<MonitorPackage> loader = ServiceLoader.load(MonitorPackage.class);
		for (MonitorPackage pack : loader) {
			MonitorType[] types = pack.getTypes();
			if (types != null) {
				for (MonitorType type : types) {
					String typeId = type.getId();
					typeMap.put(typeId.toUpperCase(), type.getMethodTypeIds());
				}
			}
		}
	}

	public static Map<String, String[]> getTypeMap() {
		return typeMap;
	}

	public static void addMethodType() {
		try {
			String filePath = System.getProperty("user.dir") + "/conf/metric.xlsx";
			System.out.println("path: "+filePath);
			Workbook wb = new XSSFWorkbook(new File(filePath));
			Sheet xs = wb.getSheetAt(0);
			Iterator<Row> iterator = xs.rowIterator();
			while (iterator.hasNext()) {
				Row row = iterator.next();
				String type = row.getCell(1).getStringCellValue();
				String[] typeIds = typeMap.get(type.toUpperCase());
				if (typeIds != null) {
					StringBuilder sb = new StringBuilder();
					for (String str : typeIds) {
						sb.append(str);
						sb.append(",");
					}
					String method = sb.substring(0, sb.lastIndexOf(","));
					short index = 12;
					row.createCell(index).setCellValue(method);
				}
			}
			wb.write(new FileOutputStream(System.getProperty("user.dir") + "/metric1.xlsx"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	};

	public static void main(String[] args) {
		addMethodType();
	}

}
