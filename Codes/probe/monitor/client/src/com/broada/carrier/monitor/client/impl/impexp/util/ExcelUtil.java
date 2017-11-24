package com.broada.carrier.monitor.client.impl.impexp.util;

import jxl.Sheet;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.client.impl.impexp.entity.MapObject;
import com.broada.carrier.monitor.client.impl.impexp.entity.Row;
import com.broada.carrier.monitor.client.impl.impexp.entity.Table;
import com.broada.component.utils.error.ErrorUtil;

public class ExcelUtil {
	public static Table readTable(Sheet sheet, int tableStartCol, int tableStartRow) {
		Table table = new Table();
		
		int col = tableStartCol + 2;		
		int maxCol = sheet.getColumns();		
		while (col < maxCol - 1) {
			String value1 = sheet.getCell(col++, tableStartRow).getContents();
			String value2 = sheet.getCell(col++, tableStartRow).getContents();
			if (value1.isEmpty() || value2.isEmpty())
				break;
			
			MapObject field = MapObject.checkExcelField(value1);
			table.setProperty(field, value2);
		}
				
		int row = tableStartRow + 1;
		col = tableStartCol;
		while (col < maxCol) {
			String value = sheet.getCell(col++, row).getContents();
			if (value.isEmpty())
				break;			
			table.addColumn(value);
		}

		row++;
		int maxRow = sheet.getRows();
		for (; row < maxRow; row++) {
			Row newRow = table.createRow();
			for (int i = 0; i < table.getColumnCount(); i++) {
				String value = sheet.getCell(tableStartCol + i, row).getContents();
				if (i == 0 && value.isEmpty()) {
					newRow = null;
					break;
				}
				newRow.setValue(i, value);
			}
			if (newRow == null)
				break;
			table.addRow(newRow);
		}
			
		return table;
	}
	
	private static final Log logger = LogFactory.getLog(ExcelUtil.class);

	public static final WritableFont.FontName FONT_DEFAULT = WritableFont.createFont("宋体");
	public static final WritableCellFormat FORMAT_HEADER = new WritableCellFormat(new WritableFont(FONT_DEFAULT, 9,
			WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE));
	public static final WritableCellFormat FORMAT_HEADER_DATA = new WritableCellFormat(new WritableFont(FONT_DEFAULT,
			9, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE));
	public static final WritableCellFormat FORMAT_CELL = new WritableCellFormat(new WritableFont(FONT_DEFAULT, 9,
			WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE));

	static {
		try {
			FORMAT_HEADER.setAlignment(Alignment.CENTRE);
			FORMAT_HEADER.setBackground(Colour.GRAY_25);
			FORMAT_HEADER.setBorder(Border.ALL, BorderLineStyle.THIN);

			FORMAT_HEADER_DATA.setBackground(Colour.GRAY_25);
			FORMAT_HEADER_DATA.setBorder(Border.ALL, BorderLineStyle.THIN);

			FORMAT_CELL.setBorder(Border.ALL, BorderLineStyle.THIN);
		} catch (WriteException e) {
			logger.error("CoreDB导出工具初始化失败" + e.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
	}

	public static void writeHeader(WritableSheet sheet, int row, int col, String text) {
		try {
			sheet.addCell(new Label(col, row, text, FORMAT_HEADER));
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("写入excel数据失败", e);
		}
	}

	public static void writeHeaderData(WritableSheet sheet, int row, int col, String text) {
		try {
			sheet.addCell(new Label(col, row, text, FORMAT_HEADER_DATA));
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("写入excel数据失败", e);
		}
	}

	public static void writeCell(WritableSheet sheet, int row, int col, Object data) {
		if (data == null)
			data = "";
		
		try {
			sheet.addCell(new Label(col, row, data.toString(), FORMAT_CELL));
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("写入excel数据失败", e);
		}
	}

	public static void writeHeader(WritableSheet sheet, int startRow, int startCol, String[] headers) {
		for (int i = 0; i < headers.length; i++)
			writeHeader(sheet, startRow + 0, startCol + i, headers[i]);
	}

	public static void setColumnWidth(WritableSheet sheet, int startCol, Integer[] widths) {
		for (int i = 0; i < widths.length; i++)
			sheet.setColumnView(startCol + i, widths[i]);
	}
}
