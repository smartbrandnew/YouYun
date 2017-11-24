package com.broada.carrier.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class GenerateExcelData {
	private static String workDir=System.getProperty("user.dir");
	private static Set<String> set=new HashSet<String>();
	private static Map<String, String> map=new HashMap<String, String>();
	private static int count=0;
	public void generate(){
		initPackage();
		Workbook wb=new XSSFWorkbook();
		Sheet sheet=wb.createSheet();
		ServiceLoader<MonitorPackage> loader=ServiceLoader.load(MonitorPackage.class);
		for(MonitorPackage pack:loader){
			generateExcel(pack, sheet);
		}
		File file=new File(workDir+"/conf/add.xlsx");
		try {
			wb.write(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean generateExcel(MonitorPackage pack,Sheet sheet){
		if(pack.getTypes()==null)
			return false;
		for(MonitorType type:pack.getTypes()){
			String app=type.getGroupId().toUpperCase();
			if(set.contains(app.toUpperCase()))
				continue;
			String typeId=type.getId();
			map.put(typeId, type.getGroupId());
		}
		if(pack.getItems()==null)
			return false;
		for(MonitorItem item:pack.getItems()){
			String typeId=item.getId();
			String code=item.getCode();
			if(code==null)
				continue;
			String desc=item.getDescr();
			String cn=item.getName();
			String dataType=MonitorItemType.NUMBER.equals(item.getType())?"0":"1";
			String unit=item.getUnit();
			for(Entry<String, String> entry:map.entrySet()){
				String key=entry.getKey();
				String val=entry.getValue();
					if(typeId.equalsIgnoreCase(key)){
						Row row=sheet.createRow(count);	
						row.createCell(0).setCellValue(val);
						row.createCell(1).setCellValue(key);
						row.createCell(2).setCellValue(code);
						row.createCell(3).setCellValue(cn);
						row.createCell(4).setCellValue(desc);
						row.createCell(5).setCellValue(unit);
						row.createCell(6).setCellValue(dataType);
						count++;
					
				}
			}
		}
		return true;
	}
	
	
	private void initPackage(){
		try{
		File file=new File("d:/l.xlsx");
		Workbook wb=new XSSFWorkbook(file);
		Sheet sheet=wb.getSheetAt(0);
		Iterator<Row> iterator=sheet.rowIterator();
		while(iterator.hasNext()){
			Row row=iterator.next();
			row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
			String name=row.getCell(0).getStringCellValue().toUpperCase();
			if(name!=null)
				set.add(name);
		}
		System.out.println("set: "+set);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		GenerateExcelData genetate=new GenerateExcelData();
		genetate.generate();
	}
}
