package com.broada.carrier.monitor.probe.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenerateStorageMetadata {
	public void startup() {

	}

	private static List<File> generate(String path) {
		List<File> list = new ArrayList<File>();
		File folder = new File(path);
		if (folder.exists()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				listFiles(list, file);
			}
			ObjectMapper mapper = new ObjectMapper();
			try {
				String json = mapper.writeValueAsString(list);
				System.out.println("json: " + json);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	private static void listFiles(List<File> list, File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isFile() && f.getName().endsWith(".groovy"))
					list.add(f);
				else if (f.isDirectory())
					listFiles(list, f);
			}
		} else if (file.isFile()) {
			if (file.isFile() && file.getName().endsWith(".groovy"))
				list.add(file);
		}

	}

	public static void generateExcel() {
		try {
			Workbook wb = new XSSFWorkbook();
			Sheet sheet = wb.createSheet();
			String path = Config.getConfDir();
			List<File> files = generate(path + "/script");
			int count = 0;
			for (File file : files) {
				BufferedReader buffReader = new BufferedReader(new FileReader(file));
				String typeId = file.getName().replace(".groovy", "");
				String line = buffReader.readLine();
				while (line != null) {
					if (line.contains("perf.")) {
						String name = line.substring(line.indexOf("perf."), line.lastIndexOf("'"))
								.trim();
						String remoteName=typeId.replace("_", ".").replace("-", ".") + ".";
						remoteName=remoteName.replace(".info", "").replace("info", "")
						+name.substring(name.lastIndexOf("perf.")+"perf.".length()).toLowerCase();
						generateExcelData(sheet, typeId, name,remoteName,"0", count);
						count++;

					} else if (line.contains("state.")) {
						String name = line.substring(line.indexOf("state."), line.lastIndexOf("'"))
								.trim();
						String remoteName=typeId.replace("_", ".").replace("-", ".") + ".";
						remoteName=remoteName.replace(".info", "").replace("info", "")+
						name.substring(name.lastIndexOf("state.")+"state.".length()).toLowerCase();
						generateExcelData(sheet, typeId, name,remoteName,"1", count);
						count++;
					}
					line = buffReader.readLine();
				}
			}
			generateCidExcel(sheet, count);
			File file = new File(Config.getConfDir(), "storage.xlsx");
			wb.write(new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateCidExcel(Sheet sheet, int count) {
		try {
			List<File> list = generate(System.getProperty("user.dir") + "/" + "scripts");
			for (File file : list) {
				BufferedReader buffReader = new BufferedReader(new FileReader(file));
				String line = buffReader.readLine();
				String typeId = null;
				while (line != null) {
					if (line.contains("monitor.output="))
						typeId = line.substring(line.indexOf("monitor.output=") + "monitor.output=".length())
								.toLowerCase();
					else if (line.contains("result.perf.")) {
						String name = line
								.substring(line.indexOf("result.perf.")+"result.".length(), line.indexOf("=")).trim();
						String remoteName=typeId.replace("_", ".").replace("-", ".") + ".";
							remoteName=remoteName.replace(".info", "").replace("info", "")+
							name.substring(name.lastIndexOf("perf.")+"perf.".length()).toLowerCase();
							remoteName=remoteName.replace("\"", "");
						generateExcelData(sheet, typeId, name,remoteName,"0", count);
						count++;
					} else if (line.contains("result.state.")) {
						String name = line
								.substring(line.indexOf("result.state.") + "result.".length(), line.indexOf("="))
								.trim();
						String remoteName=typeId.replace("_", ".").replace("-", ".") + ".";
						remoteName=remoteName.replace(".info", "").replace("info", "")+
						name.substring(name.lastIndexOf("state.")+"state.".length()).toLowerCase();
						remoteName=remoteName.replace("\"", "");
						generateExcelData(sheet, typeId, name,remoteName,"1", count);
						count++;
					}
					line = buffReader.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateExcelData(Sheet sheet, String typeId, String name, String remoteName, String valueType,
			int count) {
		Row row = sheet.createRow(count);
		row.createCell(0).setCellValue(typeId.toUpperCase());
		row.createCell(1).setCellValue(name);
		row.createCell(2).setCellValue(remoteName);
		row.createCell(3).setCellValue(valueType);
	}

	public static void main(String[] args) {
		generateExcel();
	}
}
