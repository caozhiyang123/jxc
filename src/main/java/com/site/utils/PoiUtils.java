package com.site.utils;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.upload.UploadFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class PoiUtils {

    private final static String xls = "xls";
    private final static String xlsx = "xlsx";

    /**
     * CSV文件列分隔符
     */
    private static final String CSV_COLUMN_SEPARATOR = ",";

    /**
     * CSV文件列分隔符
     */
    private static final String CSV_STRING_MARK = "'";

    /**
     * CSV文件列分隔符
     */
    private static final String CSV_RN = "\r\n";

    public static PoiUtils me = new PoiUtils();

    public static List<String[]> readExcel(UploadFile file) throws IOException {
        //检查文件
        checkFile(file);
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        List<String[]> list = new ArrayList<String[]>();
        if (workbook != null) {
            for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                //获得当前sheet工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if (sheet == null) {
                    continue;
                }
                //获得当前sheet的开始行
                int firstRowNum = sheet.getFirstRowNum();
                //获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                //循环除了第一行的所有行
                for (int rowNum = firstRowNum + 1; rowNum <= lastRowNum; rowNum++) {
                    //获得当前行
                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        continue;
                    }
                    //获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    //获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    String[] cells = new String[row.getPhysicalNumberOfCells()];
                    //循环当前行
                    for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        cells[cellNum] = getCellValue(cell);
                    }
                    list.add(cells);
                }
            }
            workbook.close();
        }
        return list;
    }

    public static void checkFile(UploadFile file) throws IOException {
        //判断文件是否存在
        if (null == file) {
            throw new FileNotFoundException("文件不存在！");
        }
        //获得文件名
        String fileName = file.getFileName();
        //判断文件是否是excel文件
        if (!fileName.endsWith(xls) && !fileName.endsWith(xlsx)) {
            throw new IOException(fileName + "不是excel文件");
        }
    }

    public static Workbook getWorkBook(UploadFile file) {
        //获得文件名
        String fileName = file.getFileName();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = new FileInputStream(file.getFile());
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if (fileName.endsWith(xls)) {
                //2003
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(xlsx)) {
                //2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
        }
        return workbook;
    }

    public static String getCellValue(Cell cell) {
        String cellValue = "";
        if (cell == null) {
            return cellValue;
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC: //数字
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING: //字符串
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BOOLEAN: //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA: //公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case Cell.CELL_TYPE_BLANK: //空值
                cellValue = "";
                break;
            case Cell.CELL_TYPE_ERROR: //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    /**
     * 根据MAP数据导出成CSV格式文件
     *
     * @param dataList 集合数据
     * @param colNames 表头部数据
     * @param mapKey   查找的对应数据
     */
    public static boolean doExport(List<Map<String, Object>> dataList, String colNames, String mapKey, File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "GBK"));
            StringBuffer buf = new StringBuffer();

            if (org.apache.commons.lang3.StringUtils.isNotEmpty(colNames)) {
                String[] colNamesArr = null;
                colNamesArr = colNames.split(",");
                for (int i = 0; i < colNamesArr.length; i++) {
                    buf.append(colNamesArr[i]).append(CSV_COLUMN_SEPARATOR);
                }
                buf.append(CSV_RN);
            }

            String[] mapKeyArr = null;
            mapKeyArr = mapKey.split(",");

            if (null != dataList) { // 输出数据
                for (int i = 0; i < dataList.size(); i++) {
                    for (int j = 0; j < mapKeyArr.length; j++) {
                        buf.append(dataList.get(i).get(mapKeyArr[j])).append(CSV_COLUMN_SEPARATOR);
                    }
                    buf.append(CSV_RN);
                }
            }
            bw.write(buf.toString());
            bw.flush();
            bw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据类型反射导出成CSV格式文件
     *
     * @param dataList 集合数据
     * @param colNames 表头部数据
     * @param mapKey   查找的对应数据
     */
    public static boolean doExport(Class<?> clazz, List<?> dataList, String colNames, String mapKey, File file) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "GBK"));
            StringBuffer buf = new StringBuffer();

            if (StringUtils.isNotEmpty(colNames)) {
                String[] colNamesArr = null;
                colNamesArr = colNames.split(",");
                for (int i = 0; i < colNamesArr.length; i++) {
                    buf.append(colNamesArr[i]).append(CSV_COLUMN_SEPARATOR);
                }
                buf.deleteCharAt(buf.length() - 1);
                buf.append(CSV_RN);
            }

            String[] mapKeyArr = null;
            mapKeyArr = mapKey.split(",");
            DecimalFormat df = new DecimalFormat("#.00");
            if (null != dataList) { // 输出数据
                int totalSize = dataList.size();
                for (int i = 0; i < totalSize; i++) {
                    Object bean = dataList.get(i);
                    for (int j = 0; j < mapKeyArr.length; j++) {
                        Method fieldMethod = Model.class.getMethod("get", String.class);
                        Object value = fieldMethod.invoke(bean, mapKeyArr[j]);
                        if (value instanceof Date) {
                            Date temp = (Date) value;
                            if (temp == null) {
                                value = "";
                            } else {
                                value = sdf.format(temp);
                            }
                            buf.append(value).append(CSV_COLUMN_SEPARATOR);
                        } else {
                            buf.append(value).append(CSV_COLUMN_SEPARATOR);
                        }
                    }
                    buf.deleteCharAt(buf.length() - 1);
                    buf.append(CSV_RN);
                }
            }
            bw.write(buf.toString());
            bw.flush();
            bw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据类型反射导出成CSV格式文件，Session中携带进度条
     *
     * @param dataList 集合数据
     * @param colNames 表头部数据
     * @param mapKey   查找的对应数据
     */
    public static boolean doExport(Class<?> clazz, List<?> dataList, String colNames, String mapKey, File file, HttpSession session) {
        BigDecimal initPercent = new BigDecimal("50");
        session.setAttribute("productPercent", initPercent);
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "GBK"));
            StringBuffer buf = new StringBuffer();

            if (StringUtils.isNotEmpty(colNames)) {
                String[] colNamesArr = null;
                colNamesArr = colNames.split(",");
                for (int i = 0; i < colNamesArr.length; i++) {
                    buf.append(colNamesArr[i]).append(CSV_COLUMN_SEPARATOR);
                }
                buf.deleteCharAt(buf.length() - 1);
                buf.append(CSV_RN);
            }

            String[] mapKeyArr = null;
            mapKeyArr = mapKey.split(",");
            DecimalFormat df = new DecimalFormat("#.00");
            if (null != dataList) { // 输出数据
                int totalSize = dataList.size();
                for (int i = 0; i < totalSize; i++) {
                    Object bean = dataList.get(i);
                    session.setAttribute("productPercent", initPercent.add(new BigDecimal(df.format(((double) (i + 2) / totalSize) * 0.5 * 100))));
                    for (int j = 0; j < mapKeyArr.length; j++) {
                        Method fieldMethod = Model.class.getMethod("get", String.class);
                        buf.append(fieldMethod.invoke(bean, mapKeyArr[j])).append(CSV_COLUMN_SEPARATOR);
                    }
                    buf.deleteCharAt(buf.length() - 1);
                    buf.append(CSV_RN);
                }
            }
            bw.write(buf.toString());
            session.setAttribute("productPercent", new BigDecimal("100"));
            bw.flush();
            bw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String parseGetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return "get" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
    }

    /**
     * 导出数据并生成EXCEL，有表头
     *
     * @param response
     * @param clazz
     * @param list
     * @param fileName
     * @param colNames
     * @param columnModelNames
     * @throws IOException
     */
    public static void exportData(HttpServletResponse response, Class<?> clazz, List<?> list, String fileName, String colNames, String columnModelNames) throws IOException {

        String sheetName = "sheet1";
        response.setContentType("application/x-msdownload;charset=UTF-8");//设置response内容的类型
        response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));//设置头部信息

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(0, sheetName);
        HSSFRow row1 = sheet.createRow((short) 0);
        HSSFCellStyle headStyle = getHeadStyle(workbook);
        String[] colArray = colNames.split(",");
        for (int i = 1; i <= colArray.length; i++) {
            sheet.autoSizeColumn((short)i, true);
            HSSFCell cell = row1.createCell(i - 1, HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(colArray[i - 1]);
            //设置表头样式
            cell.setCellStyle(headStyle);
            //设置列宽
            sheet.setColumnWidth(i-1, 15*256);
        }
        String[] columnModelArray = columnModelNames.split(",");
        generateHSSFSheetFromFieldValue(clazz, list, sheet, workbook, columnModelArray);
        ServletOutputStream fOut = response.getOutputStream();
        workbook.write(fOut);
        fOut.flush();
        fOut.close();
    }

    /**
     * 合并单元格，生成单个sheet，有表头
     * 生成的excel每个sheet的标题，列均相同，只合并行，不合并列，合并依据：单元格内容一致时合并
     * @param response
     * @param clazz
     * @param dataList 数据集
     * @param mergeMap key：需要合并的列，value：是否需要在excel中显示
     * @param fileName
     * @param colNames
     * @param columnModelNames
     * @throws IOException
     */
    public static<T> void exportMergeData(HttpServletResponse response, Class<?> clazz,  List<?> dataList, Map<String, String> mergeMap, String fileName, String colNames, String columnModelNames, String[] colorFields) throws IOException {
        String sheetName = "sheet1";
        response.setContentType("application/x-msdownload;charset=UTF-8");//设置response内容的类型
        response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));//设置头部信息

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(0, sheetName);
        HSSFRow row1 = sheet.createRow((short) 0);
        HSSFCellStyle headStyle = getHeadStyle(workbook);
        String[] colArray = colNames.split(",");
        for (int i = 1; i <= colArray.length; i++) {
            sheet.autoSizeColumn((short)i, true);
            HSSFCell cell = row1.createCell(i - 1, HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(colArray[i - 1]);
            //设置表头样式
            cell.setCellStyle(headStyle);
            //设置列宽
            sheet.setColumnWidth(i-1, 23*256);
        }
        String[] columnModelArray = columnModelNames.split(",");
        generateHSSFSheetFromFieldValue(clazz, dataList, sheet, workbook, columnModelArray, mergeMap, colorFields);
        ServletOutputStream fOut = response.getOutputStream();
        workbook.write(fOut);
        fOut.flush();
        fOut.close();
    }

    /**
     * 合并单元格，生成多个sheet，有表头
     * 生成的excel每个sheet的标题，列均相同，只合并行，不合并列，合并依据：单元格内容一致时合并
     * @param response
     * @param clazz
     * @param dataMap key:sheet名称 value:数据集
     * @param mergeMap key：需要合并的列，value：是否需要在excel中显示
     * @param fileName
     * @param colNames
     * @param columnModelNames
     * @param colorFields 需要标注颜色的字段(需要有backgroundColor属性)，如果为空并且有backgroundColor属性则全部标颜色
     * @throws IOException
     */
    public static<T> void exportMergeData(HttpServletResponse response, Class<?> clazz, Map<String, List<T>> dataMap, Map<String, String> mergeMap, String fileName, String colNames, String columnModelNames, String[] colorFields) throws IOException {

        response.setContentType("application/x-msdownload;charset=UTF-8");//设置response内容的类型
        response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));//设置头部信息

        HSSFWorkbook workbook = new HSSFWorkbook();

        Set<String> sheetNames = dataMap.keySet();

        int sheetIndex = 0;
        for (String sheetName:sheetNames) {
            List<T> list = dataMap.get(sheetName);

            HSSFSheet sheet = workbook.createSheet();
            workbook.setSheetName(sheetIndex, sheetName);
            HSSFRow row1 = sheet.createRow((short) 0);
            HSSFCellStyle headStyle = getHeadStyle(workbook);
            String[] colArray = colNames.split(",");
            for (int i = 1; i <= colArray.length; i++) {
                sheet.autoSizeColumn((short)i, true);
                HSSFCell cell = row1.createCell(i - 1, HSSFCell.CELL_TYPE_STRING);
                String title = colArray[i - 1];
                cell.setCellValue(title);
                //设置表头样式
                cell.setCellStyle(headStyle);
                headStyle.setWrapText(true);
                //设置列宽
//                sheet.setColumnWidth(i-1, 23*256);
                //居中
                headStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                if ("商业公司名称".equals(title)) {
                    //设置列宽
                    sheet.setColumnWidth(i-1, 25*256);
                }
                if ("上游商业".equals(title)) {
                    //设置列宽
                    sheet.setColumnWidth(i-1, 25*256);
                }
                if ("类型".equals(title)) {
                    //设置列宽
                    sheet.setColumnWidth(i-1, 20*256);
                }
            }
            String[] columnModelArray = columnModelNames.split(",");
            generateHSSFSheetFromFieldValue(clazz, list, sheet, workbook, columnModelArray, mergeMap, colorFields);
            sheetIndex++;
        }

        ServletOutputStream fOut = response.getOutputStream();
        workbook.write(fOut);
        fOut.flush();
        fOut.close();
    }


    /**
     * 合并单元格，生成多个sheet，有表头，表头可不相同
     * 生成的excel每个sheet的标题，列均相同，只合并行，不合并列，合并依据：单元格内容一致时合并
     * @param response
     * @param clazz
     * @param dataMap key:sheet名称 value:数据集
     * @param mergeMap key：需要合并的列，value：是否需要在excel中显示
     * @param fileName
     * @param colNameMap key：sheet名称，value：该sheet下的表头名称
     * @param columnModelMap key：sheet名称，value：该sheet下的表头映射的属性名称
     * @param colorFields 需要标注颜色的字段(需要有backgroundColor属性)，如果为空并且有backgroundColor属性则全部标颜色
     * @throws IOException
     */
    public static<T> void exportMergeData(HttpServletResponse response, Class<?> clazz, Map<String, List<T>> dataMap, Map<String, String> mergeMap, String fileName, Map<String, String> colNameMap, Map<String, String> columnModelMap, String[] colorFields) throws IOException {

        response.setContentType("application/x-msdownload;charset=UTF-8");//设置response内容的类型
        response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));//设置头部信息

        HSSFWorkbook workbook = new HSSFWorkbook();

        Set<String> sheetNames = dataMap.keySet();

        int sheetIndex = 0;
        for (String sheetName:sheetNames) {
            List<T> list = dataMap.get(sheetName);

            HSSFSheet sheet = workbook.createSheet();
            workbook.setSheetName(sheetIndex, sheetName);
            HSSFRow row1 = sheet.createRow((short) 0);
            HSSFCellStyle headStyle = getHeadStyle(workbook);
            String colNames = colNameMap.get(sheetName);
            String[] colArray = colNames.split(",");
            for (int i = 1; i <= colArray.length; i++) {
                sheet.autoSizeColumn((short)i, true);
                HSSFCell cell = row1.createCell(i - 1, HSSFCell.CELL_TYPE_STRING);
                String title = colArray[i - 1];
                cell.setCellValue(title);
                //设置表头样式
                cell.setCellStyle(headStyle);
                headStyle.setWrapText(true);
                //设置列宽
//                sheet.setColumnWidth(i-1, 15*256);
                //居中
                headStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                if ("商业公司名称".equals(title)) {
                    //设置列宽
                    sheet.setColumnWidth(i-1, 25*256);
                }
                if ("上游商业".equals(title)) {
                    //设置列宽
                    sheet.setColumnWidth(i-1, 25*256);
                }
                if ("类型".equals(title)) {
                    //设置列宽
                    sheet.setColumnWidth(i-1, 20*256);
                }
            }
            String columnModelNames = columnModelMap.get(sheetName);
            String[] columnModelArray = columnModelNames.split(",");
            generateHSSFSheetFromFieldValue(clazz, list, sheet, workbook, columnModelArray, mergeMap, colorFields);
            sheetIndex++;
        }

        ServletOutputStream fOut = response.getOutputStream();
        workbook.write(fOut);
        fOut.flush();
        fOut.close();
    }

    /**
     * 导出数据并生成EXCEL，有表头，session中提供进度
     *
     * @param response
     * @param clazz
     * @param list
     * @param fileName
     * @param colNames
     * @param columnModelNames
     * @throws IOException
     */
    public static void exportData(HttpServletResponse response, Class<?> clazz, List<?> list, String fileName, String colNames, String columnModelNames, HttpSession session, String sessionKey) throws IOException {

        String sheetName = "sheet1";
        response.setContentType("application/x-msdownload;charset=UTF-8");//设置response内容的类型
        response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));//设置头部信息

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(0, sheetName);
        HSSFRow row1 = sheet.createRow((short) 0);
        HSSFCellStyle headStyle = getHeadStyle(workbook);
        String[] colArray = colNames.split(",");
        for (int i = 1; i <= colArray.length; i++) {
            HSSFCell cell = row1.createCell(i - 1, HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(colArray[i - 1]);
            //设置表头样式
            cell.setCellStyle(headStyle);
            //设置列宽
            sheet.setColumnWidth(i-1, 15*256);
        }
        String[] columnModelArray = columnModelNames.split(",");
        generateHSSFSheetFromFieldValue(clazz, list, sheet, workbook, columnModelArray, session, sessionKey);
        ServletOutputStream fOut = response.getOutputStream();
        workbook.write(fOut);
        fOut.flush();
        fOut.close();
    }

    public static void generateHSSFSheetFromFieldValue(Class<?> clazz, List<?> list, HSSFSheet sheet, HSSFWorkbook workbook, String[] cols, Map<String, String> mergeMap) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        Set<String> mergeSet = mergeMap.keySet();
        Map<String, InnerRowOffset> map = new HashMap<>();
        for (String mergeName:mergeSet) {
            InnerRowOffset rowOffset = new InnerRowOffset(0,0,"", mergeMap.get(mergeName));
            map.put(mergeName, rowOffset);
        }

        int rowNum = 1;
        for (Object object : list) {
            HSSFRow row = sheet.createRow(rowNum);
            Object bean = null;
            try {
                bean = object;
                // 取出bean里的所有方法
                int colNum = 0;
                boolean fillBackgroundColor = false;
                for (String fieldName : cols) {
                    Method fieldMethod = Model.class.getMethod("get", String.class);
                    Object value = fieldMethod.invoke(bean, fieldName);
                    if (fieldName.equals("backgroundColor")) {
                        if (value != null) {
                            fillBackgroundColor = true;
                        }
                        continue;
                    }
                    HSSFCell cellNew = row.createCell(colNum, HSSFCell.CELL_TYPE_STRING);
                    if (fillBackgroundColor) {
                        //生成单元格样式
                        HSSFCellStyle style = workbook.createCellStyle();
                        //设置背景颜色
                        style.setFillForegroundColor(HSSFColor.LIME.index);
                        //solid 填充  foreground  前景色
                        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);// 设置前景色
                        cellNew.setCellStyle(style);
                    }
                    if (value instanceof Date) {
                        Date temp = (Date) value;
                        if (temp == null) {
                            value = "";
                        } else {
                            value = sdf.format(temp);
                        }
                        cellNew.setCellValue(value.toString());
                        sheet.setColumnWidth(colNum, 252 * 30 + 323);
                    } else {
                        if (value == null) {
                            value = "";
                            cellNew.setCellValue(value.toString());
                        } else {
                            String pattern = "(\\s|\\S)+\\.(jpg|gif|bmp|png)";
                            boolean isMatch = Pattern.matches(pattern, value.toString());

                            if (isMatch) {
                                sheet.setColumnWidth(colNum, 252 * 15 + 323);
                                row.setHeight((short) (252 * 6.5));
                                //anchor主要用于设置图片的属性
                                HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 255, 255, (short) colNum, rowNum - 1, (short) (colNum + 2), rowNum - 1);
                                //插入图片
                                patriarch.createPicture(anchor, workbook.addPicture(getOutPutStream(value.toString()).toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
                            } else {
                                String val = value.toString();
                                if (NumberUtil.isNumber(val)) {
                                    cellNew.setCellValue(Double.parseDouble(val));
                                } else {
                                    cellNew.setCellValue(val);
                                }
                            }
                        }
                    }

                    //需要合并的单元格
                    if (mergeSet.contains(fieldName)) {
                        InnerRowOffset innerRowOffset = map.get(fieldName);
                        String content = innerRowOffset.getContent();

                        if (content.equals(value.toString())) {
                            innerRowOffset.setEndRow(rowNum);
                        } else {
                            if (innerRowOffset.getEndRow()-innerRowOffset.getStartRow() > 0) {
                                sheet.addMergedRegion(new CellRangeAddress(innerRowOffset.getStartRow(), innerRowOffset.getEndRow(), colNum, colNum));
                            }
                            innerRowOffset.setStartRow(rowNum);
                            innerRowOffset.setEndRow(rowNum);
                            String fetchName = innerRowOffset.getFetchName();
                            if (StringUtils.isNotBlank(fetchName)) {
                                Object obj = fieldMethod.invoke(bean, fetchName);
                                String fetchValue = obj == null ? "" : obj.toString();
                                innerRowOffset.setContent(fetchValue);
                            } else {
                                innerRowOffset.setContent(value.toString());
                            }
                        }
                    }

                    colNum++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rowNum++;
        }
    }

    public static void generateHSSFSheetFromFieldValue(Class<?> clazz, List<?> list, HSSFSheet sheet, HSSFWorkbook workbook, String[] cols, Map<String, String> mergeMap, String[] colorFields) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        //哪些字段需要背景颜色
        List<String> colorFieldList = null;
        //为空时所有列均无颜色
        if (colorFields == null) {
            colorFieldList = Arrays.asList();
        }
        //构建存储 excel 颜色的 map集合
        Map<String, HSSFCellStyle> excelColorMap = new HashMap<>();
        if (colorFields != null) {
            colorFieldList = Arrays.asList(colorFields);
        }

        //设置居中样式
        HSSFCellStyle alignCenter = workbook.createCellStyle();
        alignCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
        alignCenter.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直

        Set<String> mergeSet = mergeMap.keySet();
        Map<String, InnerRowOffset> map = new HashMap<>();
        for (String mergeName:mergeSet) {
            InnerRowOffset rowOffset = new InnerRowOffset(1,1,"", mergeMap.get(mergeName));
            map.put(mergeName, rowOffset);
        }

        int rowNum = 1;
        for (Object object : list) {
            HSSFRow row = sheet.createRow(rowNum);
            Object bean = null;
            try {
                bean = object;
                // 取出bean里的所有方法
                int colNum = 0;
                for (String fieldName : cols) {
                    Method fieldMethod = Model.class.getMethod("get", String.class);
                    Object value = fieldMethod.invoke(bean, fieldName);
                    HSSFCell cellNew = row.createCell(colNum, HSSFCell.CELL_TYPE_STRING);
                    if (colorFieldList.contains(fieldName)) {
                        //取出背景颜色字段
                        Object backgroundColorValue = fieldMethod.invoke(bean, "backgroundColor");
                        HSSFCellStyle style = excelColorMap.get(backgroundColorValue.toString());
                        if (style == null) {
                            //生成单元格样式
                            style = getCustomCellStyle(workbook);
                            //设置背景颜色
                            style.setFillForegroundColor((short)backgroundColorValue);
                            excelColorMap.put(backgroundColorValue.toString(), style);
                        }
                        cellNew.setCellStyle(style);
                    }
                    if (value instanceof Date) {
                        Date temp = (Date) value;
                        if (temp == null) {
                            value = "";
                        } else {
                            value = sdf.format(temp);
                        }
                        cellNew.setCellValue(value.toString());
                        sheet.setColumnWidth(colNum, 252 * 30 + 323);
                    } else {
                        if (value == null) {
                            value = "";
                            cellNew.setCellValue(value.toString());
                        } else {
                            String pattern = "(\\s|\\S)+\\.(jpg|gif|bmp|png)";
                            boolean isMatch = Pattern.matches(pattern, value.toString());

                            if (isMatch) {
                                sheet.setColumnWidth(colNum, 252 * 15 + 323);
                                row.setHeight((short) (252 * 6.5));
                                //anchor主要用于设置图片的属性
                                HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 255, 255, (short) colNum, rowNum - 1, (short) (colNum + 2), rowNum - 1);
                                //插入图片
                                patriarch.createPicture(anchor, workbook.addPicture(getOutPutStream(value.toString()).toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
                            } else {
                                String val = value.toString();
                                if (NumberUtil.isNumber(val)) {
                                    cellNew.setCellValue(Double.parseDouble(val));
                                } else {
                                    cellNew.setCellValue(val);
                                }
                            }
                        }
                    }

                    //需要合并的单元格
                    if (mergeSet.contains(fieldName)) {
                        InnerRowOffset innerRowOffset = map.get(fieldName);
                        String content = innerRowOffset.getContent();

                        String fetchValue = null;
                        String fetchName = innerRowOffset.getFetchName();
                        if (StringUtils.isNotBlank(fetchName)) {
                            Object obj = fieldMethod.invoke(bean, fetchName);
                            fetchValue = obj == null ? "" : obj.toString();
                        } else {
                            fetchValue = value.toString();
                        }

                        if (content.equals(fetchValue)) {
                            innerRowOffset.setEndRow(rowNum);
                        } else {
                            if (innerRowOffset.getEndRow()-innerRowOffset.getStartRow() > 0) {
                                sheet.addMergedRegion(new CellRangeAddress(innerRowOffset.getStartRow(), innerRowOffset.getEndRow(), colNum, colNum));
                            }
                            innerRowOffset.setStartRow(rowNum);
                            innerRowOffset.setEndRow(rowNum);
                            innerRowOffset.setContent(fetchValue);
                        }

                        //加载到最后一行时还需要判断一次
                        if (rowNum == list.size() && innerRowOffset.getEndRow()-innerRowOffset.getStartRow() > 0) {
                            sheet.addMergedRegion(new CellRangeAddress(innerRowOffset.getStartRow(), innerRowOffset.getEndRow(), colNum, colNum));
                        }

                        HSSFCell hssfCell = sheet.getRow(innerRowOffset.getStartRow()).getCell(colNum);
                        hssfCell.setCellStyle(alignCenter);
                    }

                    colNum++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rowNum++;
        }
    }

    public static void generateHSSFSheetFromFieldValue(Class<?> clazz, List<?> list, HSSFSheet sheet, HSSFWorkbook workbook, String[] cols) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        BigDecimal initPercent = new BigDecimal("50");
        DecimalFormat df = new DecimalFormat("#.00");
        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        int rowNum = 1;
        for (Object object : list) {
            HSSFRow row = sheet.createRow(rowNum);
            rowNum++;
            Object bean = null;
            try {
                bean = object;
                // 取出bean里的所有方法
                int colNum = 0;
                boolean fillBackgroundColor = false;
                for (String fieldName : cols) {
                    Method fieldMethod = Model.class.getMethod("get", String.class);
                    Object value = fieldMethod.invoke(bean, fieldName);
                    if (fieldName.equals("backgroundColor")) {
                        if (value != null) {
                            fillBackgroundColor = true;
                        }
                        continue;
                    }
                    HSSFCell cellNew = row.createCell(colNum, HSSFCell.CELL_TYPE_STRING);
                    if (fillBackgroundColor) {
                        //生成单元格样式
                        HSSFCellStyle style = workbook.createCellStyle();
                        //设置背景颜色
                        style.setFillForegroundColor(HSSFColor.LIME.index);
                        //solid 填充  foreground  前景色
                        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);// 设置前景色
                        cellNew.setCellStyle(style);
                    }
                    if (value instanceof Date) {
                        Date temp = (Date) value;
                        if (temp == null) {
                            value = "";
                        } else {
                            value = sdf.format(temp);
                        }
                        cellNew.setCellValue(value.toString());
                        sheet.setColumnWidth(colNum, 252 * 30 + 323);
                    } else {
                        if (value == null) {
                            value = "";
                            cellNew.setCellValue(value.toString());
                        } else {
                            String pattern = "(\\s|\\S)+\\.(jpg|gif|bmp|png)";
                            boolean isMatch = Pattern.matches(pattern, value.toString());

                            if (isMatch) {
                                sheet.setColumnWidth(colNum, 252 * 15 + 323);
                                row.setHeight((short) (252 * 6.5));
                                //anchor主要用于设置图片的属性
                                HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 255, 255, (short) colNum, rowNum - 1, (short) (colNum + 2), rowNum - 1);
                                //插入图片
                                patriarch.createPicture(anchor, workbook.addPicture(getOutPutStream(value.toString()).toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
                            } else {
                                String val = value.toString();
                                if (NumberUtil.isNumber(val)) {
                                    cellNew.setCellValue(Double.parseDouble(val));
                                } else {
                                    cellNew.setCellValue(val);
                                }
                            }
                        }
                    }

                    colNum++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void generateHSSFSheetFromFieldValue(Class<?> clazz, List<?> list, HSSFSheet sheet, HSSFWorkbook workbook, String[] cols, HttpSession session, String sessionKey) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        BigDecimal initPercent = new BigDecimal("50");
        session.setAttribute(sessionKey, initPercent);
        DecimalFormat df = new DecimalFormat("#.00");
        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        int rowNum = 1;
        for (Object object : list) {
            HSSFRow row = sheet.createRow(rowNum);
            rowNum++;
            Object bean = null;
            try {
                bean = object;
                // 取出bean里的所有方法
                int colNum = 0;
                for (String fieldName : cols) {
                    Method fieldMethod = Model.class.getMethod("get", String.class);
                    Object value = fieldMethod.invoke(bean, fieldName);
                    HSSFCell cellNew = row.createCell(colNum, HSSFCell.CELL_TYPE_STRING);
                    if (value instanceof Date) {
                        Date temp = (Date) value;
                        if (temp == null) {
                            value = "";
                        } else {
                            value = sdf.format(temp);
                        }
                        cellNew.setCellValue(value.toString());
                        sheet.setColumnWidth(colNum, 252 * 30 + 323);
                    } else {
                        if (value == null) {
                            value = "";
                            cellNew.setCellValue(value.toString());
                        } else {
                            String pattern = "(\\s|\\S)+\\.(jpg|gif|bmp|png)";
                            boolean isMatch = Pattern.matches(pattern, value.toString());

                            if (isMatch) {
                                sheet.setColumnWidth(colNum, 252 * 15 + 323);
                                row.setHeight((short) (252 * 6.5));
                                //anchor主要用于设置图片的属性
                                HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 255, 255, (short) colNum, rowNum - 1, (short) (colNum + 2), rowNum - 1);
                                //插入图片
                                patriarch.createPicture(anchor, workbook.addPicture(getOutPutStream(value.toString()).toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
                            } else {
                                String val = value.toString();
                                if (NumberUtil.isNumber(val)) {
                                    cellNew.setCellValue(Double.parseDouble(val));
                                } else {
                                    cellNew.setCellValue(val);
                                }
                            }
                        }
                    }

                    colNum++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            session.setAttribute(sessionKey, initPercent.add(new BigDecimal(df.format(((double) (rowNum + 2) / list.size()) * 0.5 * 100))));
        }
        session.setAttribute(sessionKey, new BigDecimal("100"));
    }

    public static void generateHSSFSheetFromFieldValue(Class<?> clazz, List<?> list, HSSFSheet sheet, String[] cols, int rowNum) {
        for (Object object : list) {
            HSSFRow row = sheet.createRow(rowNum);
            rowNum++;
            Object bean = null;
            try {
                bean = object;
                // 取出bean里的所有方法
                int colNum = 0;
                for (String fieldName : cols) {
                    String fieldGetName = parGetName(fieldName);
                    Method fieldGetMet = clazz.getDeclaredMethod(fieldGetName);
                    Field field = clazz.getDeclaredField(fieldName);
                    String fieldType = field.getType().getSimpleName();
                    String value = "";
                    if ("Date".equals(fieldType)) {
                        Date temp = (Date) fieldGetMet.invoke(bean);
                        if (temp == null) {
                            value = "";
                        } else {
                            String fmtstr = "yyyy-MM-dd HH:mm:ss";
                            SimpleDateFormat sdf = new SimpleDateFormat(fmtstr, Locale.UK);
                            value = sdf.format(temp);
                        }
                    } else {
                        if (fieldGetMet.invoke(bean) == null) value = "";
                        else
                            value = String.valueOf(fieldGetMet.invoke(bean));
                    }
                    HSSFCell cellNew = row.createCell(colNum, HSSFCell.CELL_TYPE_STRING);
                    cellNew.setCellValue(value);
                    colNum++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 设置样式
     */
    private static HSSFCellStyle getHeadStyle(HSSFWorkbook workbook) {
        HSSFFont headFont = workbook.createFont();
        headFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headFont.setFontHeightInPoints((short) 11);
        HSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setFont(headFont);
        headStyle.setBorderTop((short)1);
        headStyle.setBorderRight((short)1);
        headStyle.setBorderBottom((short)1);
        headStyle.setBorderLeft((short)1);
        headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        return headStyle;
    }

    /**
     * 设置样式
     */
    private static HSSFCellStyle getCustomCellStyle(HSSFWorkbook workbook) {
        //生成单元格样式
        HSSFCellStyle style = workbook.createCellStyle();
        //solid 填充  foreground  前景色
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);// 设置前景色
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        return style;
    }

    private static String parGetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return "get" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
    }

    public static ByteArrayOutputStream getOutPutStream(String picUrl) {
        try {
            URL url = new URL(picUrl + "-200X200");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            return outStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class InnerRowOffset {
        int startRow;
        int endRow;
        int startCol;
        int endCol;
        String content;
        String fetchName;

        public InnerRowOffset() {
        }

        public InnerRowOffset(int startRow, int endRow, String content, String fetchName) {
            this.startRow = startRow;
            this.endRow = endRow;
            this.content = content;
            this.fetchName = fetchName;
        }

        public String getFetchName() {
            return fetchName;
        }

        public void setFetchName(String fetchName) {
            this.fetchName = fetchName;
        }

        public int getStartRow() {
            return startRow;
        }

        public void setStartRow(int startRow) {
            this.startRow = startRow;
        }

        public int getEndRow() {
            return endRow;
        }

        public void setEndRow(int endRow) {
            this.endRow = endRow;
        }

        public int getStartCol() {
            return startCol;
        }

        public void setStartCol(int startCol) {
            this.startCol = startCol;
        }

        public int getEndCol() {
            return endCol;
        }

        public void setEndCol(int endCol) {
            this.endCol = endCol;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static void main(String[] args) {
        String sb = "http://oy4908855.bkt.clouddn.com/2017102413453071_Capture001.png";
        String pattern = "(\\s|\\S)+\\.(jpg|gif|bmp|png)";
        boolean isMatch = Pattern.matches(pattern, sb);
        System.out.println(isMatch);
    }
}
