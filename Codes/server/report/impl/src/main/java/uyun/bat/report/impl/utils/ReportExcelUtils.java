package uyun.bat.report.impl.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import uyun.bat.common.config.Config;
import uyun.bat.common.utils.StringUtils;
import uyun.bat.common.utils.UnitUtil;
import uyun.bat.datastore.api.entity.ResourceMetrics;
import uyun.bat.report.api.entity.Report;
import uyun.bat.report.api.entity.web.TReportData;
import uyun.bat.report.api.entity.web.TReportDataAll;
import uyun.bat.report.api.entity.web.TReportMetricData;
import uyun.bat.report.api.entity.web.TReportResourceMetrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Excel工具类
 */
public class ReportExcelUtils {
    private static boolean isZH = Config.getInstance().isChinese();

    private static final String[] reportHeaders = {" 序号 ", " 资源名称 ", " 设备IP "};
    private static final String[] reportHeadersEN = {" Serial number ", " Name ", " Equipment IP "};

    private static List<String> generateHeaders(Report r) {
        List<String> headers = new ArrayList<>();
        if (isZH) {
            for (String reportHeader : reportHeaders) {
                headers.add(reportHeader);
            }
        } else {
            for (String reportHeader : reportHeadersEN) {
                headers.add(reportHeader);
            }
        }
        List<String> metrics = r.getMetrics();
        if (metrics != null && metrics.size() > 0) {
            for (String metric : metrics) {
                headers.add(metric);
            }
        }
        return headers;
    }

    private static List<String> generateHeaders(List<ResourceMetrics> list) {
        List<String> headers = new ArrayList<>();
        for (String reportHeader : reportHeaders) {
            headers.add(reportHeader);
        }
        ResourceMetrics rm = list.get(0);
        List<ResourceMetrics.MetricVal> metrics = rm.getMetrics();
        if (metrics != null && metrics.size() > 0) {
            for (ResourceMetrics.MetricVal metric : metrics) {
                headers.add(metric.getMetricName());
            }
        }
        return headers;
    }

    /**
     * 导出实时数据
     * @param list
     * @param file
     */
    public static byte[] expDataExsNow(List<ResourceMetrics> list, String file, String start, String end) {
        if (list != null && list.size() > 0) {
            List<String> headers = generateHeaders(list);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet");
            //前三列为固定中文设置固定长度
            sheet.setColumnWidth(0, 512);
            sheet.setColumnWidth(1, 512);
            sheet.setColumnWidth(2, 512);

            HSSFRow row;
            HSSFCell cell;
            //设置空行放置图片
            int blankRow = 12;
            HSSFCellStyle cellStyle = commonExlFunc(file, headers, workbook, sheet, blankRow, start, end);
            int i = 1;
            for (ResourceMetrics resource : list) {
                row = sheet.createRow(blankRow + i);
                row.setHeightInPoints(30);
                List<ResourceMetrics.MetricVal> metrics = resource.getMetrics();
                for (int j = 0; j < headers.size(); j++) {
                    cell = row.createCell(j);
                    cell.setCellStyle(cellStyle);
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    switch (j) {
                        case 0:
                            //第一格为序号
                            cell.setCellValue(i);
                            break;
                        case 1:
                            //第二格为资源名称
                            cell.setCellValue(resource.getHostname());
                            break;
                        case 2:
                            //第三格为设备IP
                            cell.setCellValue(resource.getIpaddr());
                            break;
                        default:
                            //其余列自行匹配header
                            String metricHeader = headers.get(j);
                            if (metrics != null && metrics.size() >= 1) {
                                for (ResourceMetrics.MetricVal metricData : metrics) {
                                    if (metricHeader.equals(metricData.getMetricName())) {
                                        Double val = metricData.getValAvg();
                                        String metricVal = null;
                                        if (val != null) {
                                            metricVal = UnitUtil.transformUnit(val, metricData.getUnit());
                                        }
                                        cell.setCellValue(metricVal == null ? "-" : metricVal);
                                    }
                                }
                            }
                            break;
                    }
                }
                i++;
            }

            for (int j = 0; j < headers.size(); j++) {
                sheet.autoSizeColumn((short)j);
            }
            return workbook2Bytes(workbook);
        }
        return null;
    }

    /**
     * 导出报表历史数据
     * @param r
     * @param file 图片base64编码
     */
    public static byte[] expReportExs(Report r, String file) {
        if (r == null) {
            return null;
        }

        List<String> headers = generateHeaders(r);
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(r.getReportName() == null ? "sheet" : r.getReportName());
        //前三列为固定中文设置固定长度
        sheet.setColumnWidth(0, 512);
        sheet.setColumnWidth(1, 512);
        sheet.setColumnWidth(2, 512);

        HSSFRow row;
        HSSFCell cell;
        //设置空行放置图片
        int blankRow = 12;

        TReportDataAll dataAll = r.getData();
        TReportData reportData = null;
        List<TReportData> calendar = r.getCalendar();
        for (TReportData tReportData : calendar) {
            String reportDataId = tReportData.getReportDataId();
            if (reportDataId.equals(dataAll.getReportDataId())) {
                reportData = tReportData;
            }
        }
        HSSFCellStyle cellStyle = commonExlFunc(file, headers, workbook, sheet, blankRow,
                reportData == null ? null : reportData.getStartDate(),
                reportData == null ? null : reportData.getEndDate());
        if (dataAll != null) {
            List<TReportResourceMetrics> resources = dataAll.getResources();
            if (resources != null && resources.size() > 0) {
                int i = 1;
                for (TReportResourceMetrics resource : resources) {
                    row = sheet.createRow(blankRow + i);
                    row.setHeightInPoints(30);
                    List<TReportMetricData> metrics = resource.getMetrics();
                    for (int j = 0; j < headers.size(); j++) {
                        cell = row.createCell(j);
                        cell.setCellStyle(cellStyle);
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                        switch (j) {
                            case 0:
                                //第一格为序号
                                cell.setCellValue(i);
                                break;
                            case 1:
                                //第二格为资源名称
                                cell.setCellValue(resource.getHostname());
                                break;
                            case 2:
                                //第三格为设备IP
                                cell.setCellValue(resource.getIpaddr());
                                break;
                            default:
                                //其余列自行匹配header
                                String metricHeader = headers.get(j);
                                if (metrics != null && metrics.size() >= 1) {
                                    for (TReportMetricData metricData : metrics) {
                                        if (metricHeader.equals(metricData.getMetricName())) {
                                            Double val = metricData.getValAvg();
                                            String metricVal = null;
                                            if (val != null) {
                                                metricVal = UnitUtil.transformUnit(val, metricData.getUnit());
                                            }
                                            cell.setCellValue(metricVal == null ? "-" : metricVal);
                                        }
                                    }
                                }
                                break;
                        }
                    }
                    i++;
                }
            }
        }

        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn((short)i);
        }
        return workbook2Bytes(workbook);
    }

    //生成表头 插入图片 设置字体以及样式
    private static HSSFCellStyle commonExlFunc(String file, List<String> headers, HSSFWorkbook workbook,
                                               HSSFSheet sheet, int blankRow, String start, String end) {
        HSSFRow row;
        HSSFCell cell;

        // 设置这些样式
        HSSFFont font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);//字体
        font.setFontHeightInPoints((short) 15);//字号

        // 生成第一行日期行
        HSSFRichTextString calendar = new HSSFRichTextString(start + " ~ " + end);
        row = sheet.createRow(0);
        row.setHeightInPoints(20);
        cell = row.createCell(0);
        cell.setCellValue(calendar);

        try {
            if (StringUtils.isNotBlank(file)) {
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] bytes = decoder.decode(file);
                HSSFClientAnchor anchor = new HSSFClientAnchor(0, 1, 800, 200, (short) 0, 2, (short) 7, 9);
                // 插入图片
                HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
                patriarch.createPicture(anchor, workbook.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_PNG));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        HSSFCellStyle cellStyle = workbook.createCellStyle(); //设置单元格样式
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER );
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_NONE);
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_NONE);
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyle.setFont(font);

        for (int i = 1; i < blankRow; i++) {
            row = sheet.createRow(i);
            row.setHeightInPoints(20);
        }
        //产生表格标题行
        row = sheet.createRow(blankRow);
        row.setHeightInPoints(30);
        for (int i = 0; i < headers.size(); i++) {
            HSSFRichTextString text = new HSSFRichTextString(headers.get(i));
            cell = row.createCell(i);
            cell.setCellValue(text);
            cell.setCellStyle(cellStyle);
        }

        cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_NONE);
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_NONE);
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyle.setDataFormat((short)0x31);//设置显示格式，避免点击后变成科学计数法了
        return cellStyle;
    }

    private static byte[] workbook2Bytes(HSSFWorkbook workBook) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            workBook.write(os);
            return os.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}