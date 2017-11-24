package com.broada.carrier.monitor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CheckStorageMonitorType {
	private static Set<String> set=new HashSet<String>();
	private static Set<String> notContainSet=new HashSet<String>();
	public static List<MonitorPackage> getAllMonitorPackages(String packageName, Class<?> clss) {
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
						List<MonitorPackage> clsList = generate(f.getPath(),
								clss, new ArrayList<MonitorPackage>());
						list.addAll(clsList);
					}
				} else {
					if (file.getName().endsWith(".class")) {
						String pathName = file.toString();
						pathName = pathName.replace("\\", ".");
						pathName = pathName.replace("/", ".");
						String className = pathName.substring(pathName
								.indexOf("classes."));
						className = className.replace("classes.", "").trim();
						if (className.endsWith("Package")
								|| className.endsWith("package")) {
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

	private static List<MonitorPackage> generate(String path, Class<?> clss,
			List<MonitorPackage> list) {
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
				String className = pathName.substring(pathName
						.indexOf("classes."));
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
	
	public static void check(){
		loadMonitorTypes();
		List<MonitorPackage> list=getAllMonitorPackages("com.broada.carrier.monitor.impl.storage", MonitorPackage.class);
		for(MonitorPackage pack:list){
			MonitorType[] types=pack.getTypes();
			for(MonitorType type:types){
				String typeId=type.getId();
				if(!set.contains(typeId.toUpperCase()))
					notContainSet.add(typeId.toUpperCase());
			}
		}
		List<String> typeIdList=new ArrayList<String>();
		typeIdList.addAll(set);
		Collections.sort(typeIdList);
		List<String> notContainsList=new ArrayList<String>();
		notContainsList.addAll(notContainSet);
		Collections.sort(notContainsList);
		ObjectMapper mapper=new ObjectMapper();
		try {
			String contains=mapper.writeValueAsString(typeIdList);
			String notContains=mapper.writeValueAsString(notContainsList);
			System.out.println("包含： "+contains);
			System.out.println("不包含: "+notContains);
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadMonitorTypes(){
		try {
			Workbook wb=new XSSFWorkbook(System.getProperty("user.dir")+"/conf/storage.xlsx");
			Sheet sheet=wb.getSheetAt(0);
			Iterator<Row> iterator=sheet.rowIterator();
			while(iterator.hasNext()){
				Row row=iterator.next();
				for(int i=0;i<4;i++){
					if(row.getCell(i)==null)
						row.createCell(i);
					String val=row.getCell(1).getStringCellValue();
					if(val!=null)
						set.add(val.toUpperCase());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		check();
	}

}
