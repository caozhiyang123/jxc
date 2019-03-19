package com.site.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.upload.UploadFile;
import com.site.core.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ExcelUtil {

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

    public static JSONArray readExcel(UploadFile file) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //检查文件
        checkFile(file);
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        JSONArray json = new JSONArray();
        if (workbook != null) {
            for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                List<String[]> list = new ArrayList<String[]>();
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
                        boolean isMerge = isMergedRegion(sheet, rowNum, cell.getColumnIndex());
                        //判断是否具有合并单元格
                        if (isMerge) {
                            String rs = getMergedRegionValue(sheet, sdf, row.getRowNum(), cell.getColumnIndex());
                            if (StringUtils.isNotBlank(rs)) {
                                cells[cellNum] = rs;
                            }
                        } else {
                            String value = getCellValue(cell, sdf);
                            if (StringUtils.isNotBlank(value)) {
                                cells[cellNum] = value;
                            }
                        }
                    }
                    if (cells.length > 0) {
                        list.add(cells);
                    }
                }
                json.add(list);
            }
            workbook.close();
        }
        return json;
    }

    public static JSONArray readExcel(UploadFile file, String[] fieldNames) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //检查文件
        checkFile(file);
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        JSONArray json = new JSONArray();
        if (workbook != null) {

            int sheetCount = workbook.getNumberOfSheets();

            //只取第一个sheet
            for (int sheetNum = 0; sheetNum < sheetCount; sheetNum++) {
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
                    int lastCellNum = row.getLastCellNum();
                    JSONObject cells = new JSONObject();
                    //循环当前行
                    for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        if (cell == null) {
                            continue;
                        }
                        boolean isMerge = isMergedRegion(sheet, rowNum, cell.getColumnIndex());
                        //判断是否具有合并单元格
                        if (isMerge) {
                            String rs = getMergedRegionValue(sheet, sdf, row.getRowNum(), cell.getColumnIndex());
                            if (StringUtils.isNotBlank(rs)) {
                                cells.put(fieldNames[cellNum], rs);
                            }
                        } else {
                            String value = getCellValue(cell, sdf);
                            if (StringUtils.isNotBlank(value)) {
                                cells.put(fieldNames[cellNum], value);
                            }
                        }
                    }
                    if (cells.size() > 0) {
                        json.add(cells);
                    }
                }
            }
            workbook.close();
        }
        return json;
    }

    public static JSONObject readMultipleExcel(UploadFile file, Map<String, String[]> fieldNameMap) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //检查文件
        checkFile(file);
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        JSONObject json = new JSONObject();
        if (workbook != null) {

            int sheetCount = workbook.getNumberOfSheets();

            //只取第一个sheet
            for (int sheetNum = 0; sheetNum < sheetCount; sheetNum++) {
                //获得当前sheet工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if (sheet == null) {
                    continue;
                }

                String name = sheet.getSheetName();

                String[] fieldNames = fieldNameMap.get(name);

                if (fieldNames == null || fieldNames.length == 0) {
                    continue;
                }

                JSONArray array = new JSONArray();

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
                    int lastCellNum = row.getLastCellNum();
                    JSONObject cells = new JSONObject();
                    //循环当前行
                    for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {

                        //当前列用户未设置列名
                        if (cellNum >= fieldNames.length || fieldNames[cellNum] == null || "".equals(fieldNames[cellNum].trim())) {
                            continue;
                        }

                        Cell cell = row.getCell(cellNum);
                        if (cell == null) {
                            continue;
                        }
                        boolean isMerge = isMergedRegion(sheet, rowNum, cell.getColumnIndex());
                        //判断是否具有合并单元格
                        if (isMerge) {
                            String rs = getMergedRegionValue(sheet, sdf, row.getRowNum(), cell.getColumnIndex());
                            if (StringUtils.isNotBlank(rs)) {
                                cells.put(fieldNames[cellNum], rs);
                            }
                        } else {
                            String value = getCellValue(cell, sdf);
                            if (StringUtils.isNotBlank(value)) {
                                cells.put(fieldNames[cellNum], value);
                            }
                        }
                    }
                    if (cells.size() > 0) {
                        array.add(cells);
                    }
                }

                json.put(name, array);
            }
            workbook.close();
        }
        return json;
    }

    public static JSONArray readExcel(String path) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        File file = new File(path);
        //检查文件
        checkFile(file);
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        JSONArray json = new JSONArray();
        if (workbook != null) {
            for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                List<String[]> list = new ArrayList<String[]>();
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
                        boolean isMerge = isMergedRegion(sheet, rowNum, cell.getColumnIndex());
                        //判断是否具有合并单元格
                        if (isMerge) {
                            String rs = getMergedRegionValue(sheet, sdf, row.getRowNum(), cell.getColumnIndex());
                            if (StringUtils.isNotBlank(rs)) {
                                cells[cellNum] = rs;
                            }
                        } else {
                            String value = getCellValue(cell, sdf);
                            if (StringUtils.isNotBlank(value)) {
                                cells[cellNum] = value;
                            }

                        }
                    }
                    if (cells.length > 0) {
                        list.add(cells);
                    }
                }
                json.add(list);
            }
            workbook.close();
        }
        return json;
    }

    /**
     * 导出数据并生成EXCEL，有表头
     * @param response
     * @param clazz
     * @param list
     * @param fileName
     * @param twoColNames
     * @param columnModelNames
     * @param firstColNames
     * @throws IOException
     */
    public static void exportData(HttpServletResponse response,Class<?> clazz,List<?> list,String fileName,String twoColNames,String columnModelNames,String firstColNames) throws IOException{
        String sheetName="sheet1";
        response.setContentType("application/x-msdownload;charset=UTF-8");//设置response内容的类型
        response.setHeader("Content-disposition","attachment;filename="+ new String(fileName.getBytes("gb2312"),"ISO8859-1"));//设置头部信息
        int rowIndex = 0;
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(0,sheetName);
        if(StringUtils.isNotBlank(firstColNames)){
            HSSFRow row1= sheet.createRow((short)rowIndex);
            String[] colArray = firstColNames.split(",");
            for(int i=1;i<=colArray.length;i++){
                HSSFCell cell = row1.createCell(i-1,HSSFCell.CELL_TYPE_STRING);
                cell.setCellValue(colArray[i-1]);
            }
            rowIndex = 1;
        }
        HSSFRow row1= sheet.createRow((short)rowIndex);
        String[] colArray = twoColNames.split(",");
        for(int i=1;i<=colArray.length;i++){
            HSSFCell cell = row1.createCell(i-1,HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(colArray[i-1]);
        }
        String[] columnModelArray = columnModelNames.split(",");
        generateHSSFSheetFromFieldValue(clazz,list,sheet,columnModelArray,2);
        ServletOutputStream fOut = response.getOutputStream();
        workbook.write(fOut);
        fOut.flush();
        fOut.close();
    }

    public static void generateHSSFSheetFromFieldValue(Class<?> clazz,List<?> list,HSSFSheet sheet,String[] cols,int rowNum) {
        for(Object object:list){
            HSSFRow row= sheet.createRow(rowNum);
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
                        if(temp==null){
                            value = "";
                        }else{
                            String fmtstr = "yyyy-MM-dd HH:mm:ss";
                            SimpleDateFormat sdf = new SimpleDateFormat(fmtstr, Locale.UK);
                            value = sdf.format(temp);
                        }
                    }
                    else{
                        if(fieldGetMet.invoke(bean)==null) value="";
                        else
                            value = String.valueOf(fieldGetMet.invoke(bean));
                    }
                    HSSFCell cellNew = row.createCell(colNum,HSSFCell.CELL_TYPE_STRING);
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
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static String parGetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return "get" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
    }

    public static void checkFile(File file) throws IOException {
        //判断文件是否存在
        if (null == file) {
            throw new FileNotFoundException("文件不存在！");
        }
        //获得文件名
        String fileName = file.getName();
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

    public static Workbook getWorkBook(File file) {
        //获得文件名
        String fileName = file.getName();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = new FileInputStream(file);
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

    public static String getCellValue(Cell cell, SimpleDateFormat sdf) {
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
            case Cell.CELL_TYPE_STRING: //字符串
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC: // 数字||日期
                boolean cellDateFormatted = DateUtil.isCellDateFormatted(cell);
                if (cellDateFormatted) {
                    Date dateCellValue = cell.getDateCellValue();
                    cellValue = sdf.format(dateCellValue);
                } else {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                }
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
     * 判断合并了行
     *
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    public static boolean isMergedRow(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row == firstRow && row == lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断指定的单元格是否是合并单元格
     *
     * @param sheet
     * @param row    行下标
     * @param column 列下标
     * @return
     */
    public static boolean isMergedRegion(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取合并单元格的值
     *
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    public static String getMergedRegionValue(Sheet sheet, SimpleDateFormat sdf, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();

        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();

            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    Row fRow = sheet.getRow(firstRow);
                    Cell cell = fRow.getCell(firstColumn);
                    return getCellValue(cell, sdf);
                }
            }
        }
        return null;
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

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("abc", "123");
        System.out.println(jsonObject.size());
    }

}
