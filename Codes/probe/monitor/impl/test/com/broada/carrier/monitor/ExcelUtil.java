package com.broada.carrier.monitor;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {
	private static String workDir = System.getProperty("user.dir");

	private static Map<String, String> typeMap=TypeUtil.loadTypesFromPackage();

	public void generate() {
		XSSFWorkbook wb = null;
		FileInputStream fs = null;
		XSSFWorkbook writer=new XSSFWorkbook();
		XSSFSheet writerSheet=writer.createSheet();
		
		
		try {
			//设置要读取的文件路径
			fs = new FileInputStream(workDir + "/conf/mapper.xlsx");
			//HSSFWorkbook相当于一个excel文件，HSSFWorkbook是解析excel2007之前的版本（xls）
			//之后版本使用XSSFWorkbook（xlsx）
			wb = new XSSFWorkbook(fs);

			//获得sheet工作簿
			XSSFSheet sheet = wb.getSheetAt(0);
			//获得行
			Iterator<Row> iterator = sheet.iterator();
			int count=0;
			while (iterator.hasNext()) {
				Row row = iterator.next();
				if (row.getCell(0) == null)
					break;
				for (int i = 0; i < 11; i++) {
					if (row.getCell(i) == null)
						row.createCell(i);
					row.getCell(i).setCellType(Cell.CELL_TYPE_STRING);
				}
				if ("code".equals(row.getCell(0).getStringCellValue()))
					continue;
				boolean sign=writeRow(row, writerSheet,count);
				if(sign)
					count++;
			}
			
			File file=new File(workDir + "/conf/metric.xlsx");
			OutputStream stream=new FileOutputStream(file);
			writer.write(stream);
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	private boolean writeRow(Row row,XSSFSheet sheet,int count){
		Row r=sheet.createRow(count);
		String sign=row.getCell(0).getStringCellValue().toUpperCase();
		String name=typeMap.get(sign);
		System.out.println("name: "+name);
		if(name!=null){
			Cell c=r.createCell(0);
			c.setCellValue(name);
		for(int i=1;i<12;i++){
			String value=row.getCell(i-1).getStringCellValue();
			Cell cell=r.createCell(i);
			cell.setCellValue(value);
		}
		return true;
		}
		return false;
	}
	
	public static void main(String[] args){
		ExcelUtil util=new ExcelUtil();
		util.generate();
	}
	

}
