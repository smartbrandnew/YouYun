package com.broada.carrier.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class GenerateStorageExcel {
	private static Map<String, String> typeMap = new HashMap<String, String>();
	private static Map<String, List<MonitorItem>> metricMap = new HashMap<String, List<MonitorItem>>();

	private static List<MonitorPackage> getAllMonitorPackages(String packageName, Class<?> clss) {
		List<MonitorPackage> list = new ArrayList<MonitorPackage>();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		packageName = packageName.replace(".", "/");
		try {
			Enumeration<URL> enumera = loader.getResources(packageName);
			while (enumera.hasMoreElements()) {
				URL url = enumera.nextElement();
				File file = new File(url.getFile());
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (File f : files) {
						List<MonitorPackage> clsList = generate(f.getPath(), clss, new ArrayList<MonitorPackage>());
						list.addAll(clsList);
					}
				} else {
					if (file.getName().endsWith(".class")) {
						String pathName = file.toString();
						pathName = pathName.replace("\\", ".");
						pathName = pathName.replace("/", ".");
						String className = pathName.substring(pathName.indexOf("classes."));
						className = className.replace("classes.", "").trim();
						if (className.endsWith("Package") || className.endsWith("package")) {
							Class<?> cls = Class.forName(className);
							list.add((MonitorPackage) cls.newInstance());
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return list;
	}

	private static List<MonitorPackage> generate(String path, Class<?> clss, List<MonitorPackage> list) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				generate(f.getPath(), clss, list);
			}
		} else {
			if (file.getName().endsWith(".class")) {
				String pathName = file.toString();
				pathName = pathName.replace("\\", ".");
				pathName = pathName.replace("/", ".");
				String className = pathName.substring(pathName.indexOf("classes."));
				className = className.replace("classes.", "");
				className = className.replace(".class", "").trim();
				if (true) {
					Class<?> cls;
					try {
						cls = Class.forName(className);
						if (MonitorPackage.class.isAssignableFrom(cls))
							list.add((MonitorPackage) cls.newInstance());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}

			}
		}
		return list;
	}

	private static void loadPackages() {
		List<MonitorPackage> list = getAllMonitorPackages("com.broada.carrier.monitor.impl.virtual",
				MonitorPackage.class);
		for (MonitorPackage pack : list) {
			MonitorType[] types = pack.getTypes();
			if (types != null) {
				for (MonitorType type : types) {
					String app = type.getGroupId();
					String typeId = type.getId();
					typeMap.put(typeId, app);
				}
			}
			if (pack.getItems() != null) {
				for (MonitorItem item : pack.getItems()) {
					String typeId = item.getId();
					List<MonitorItem> items=metricMap.get(typeId);
					if(items==null)
						items=new ArrayList<MonitorItem>();
					items.add(item);
					metricMap.put(typeId, items);
				}
			}

		}
	}

	public static void generate() {
		loadPackages();
		generateExcel();
	}

	private static void generateExcel() {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet();
		int count = 0;
		for (Entry<String, List<MonitorItem>> entry : metricMap.entrySet()) {
			String key = entry.getKey();
			String app = typeMap.get(key);
			List<MonitorItem> items=entry.getValue();
			for(MonitorItem item:items){
			generateExcelData(sheet, app, item, count);
			count++;
			}
		}
		try {
			FileOutputStream outPut = new FileOutputStream("d:/vm.xlsx");
			wb.write(outPut);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void generateExcelData(Sheet sheet, String app, MonitorItem item, int count) {
		Row row = sheet.createRow(count);
		String typeId = item.getId();
		String metricName = item.getCode();
		String cn = item.getName();
		String desc = item.getDescr();
		String unit = item.getUnit();
		String valueType = item.getType().ordinal() == 0 ? "0" : "1";
		row.createCell(0).setCellValue(app);
		row.createCell(1).setCellValue(typeId);
		row.createCell(2).setCellValue(metricName);
		row.createCell(3).setCellValue("");
		row.createCell(4).setCellValue(cn);
		row.createCell(5).setCellValue(desc);
		row.createCell(6).setCellValue(unit);
		row.createCell(7).setCellValue(valueType);
	}

	public static void main(String[] args) {
		generate();
	}

}
