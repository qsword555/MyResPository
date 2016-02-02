package org.qipeng.excel.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {
	 
    /**
     * 获得excel中的所有的sheet
     *
     * @param workbook
     * @return List<Sheet>
     */
    public static List<Sheet> getAllSheet(Workbook workbook) {
        List<Sheet> list = new ArrayList<Sheet>();
        for (int i = 0; i < getNumberOfSheet(workbook); i++) {
            list.add(workbook.getSheetAt(i));
        }
        return list;
    }
 
    /**
     * 获得excel中sheet的数量
     *
     * @param workbook
     * @return workbook为空时返回0，否则返回sheet的数量
     */
    public static int getNumberOfSheet(Workbook workbook) {
        return workbook == null ? 0 : workbook.getNumberOfSheets();
    }
 
 
    /**
     * 根据对象的annotation或得的信息构建excel标题对象的list
     * @param clz
     * @return
     */
    private static List<ExcelHeader> getHeaderList(Class<?> clz) {
        List<ExcelHeader> headers = new ArrayList<ExcelHeader>();
 
        Field[] fields = clz.getDeclaredFields();
 
        for (Field field : fields) {
            ExcelHeader header = null;
            if (field.isAnnotationPresent(ExcelResource.class)) {
                //获得字段的annotation
                ExcelResource er = field.getAnnotation(ExcelResource.class);
                String title = isNullOrEmpty(er.title())?field.getName():er.title();
                header = new ExcelHeader(title, er.order(), field
                        .getName());
                
                if(er.converter()!=ExcelDataConverter.Void.class){
                	ExcelHeader.addConverters(field.getName(),er.converter());
                }
 
                if(!isNullOrEmpty(er.keyValue()[0])){
                    Map<String,String> map = new HashMap<String,String>();
                    for (String str : er.keyValue()) {
                        String tmp[] = str.split("=");
                        map.put(tmp[0],tmp[1]);
                    }
                    header.setMap(map);
                }
                
                if(!isNullOrEmpty(er.dateFormat())){
                    header.setDateFormat(er.dateFormat());
                }
                
                headers.add(header);
            }
 
        }
        Collections.sort(headers);
        return headers;
    }
 
 
    /**
     * 将excel的 标题按照列号-标题的形式映射到一个map中去
     * @param clz
     * @param titleRow
     * @return
     */
    private static Map<Integer,ExcelHeader> getHeaderMap(Class<?> clz,Row titleRow){
        List<ExcelHeader> headers = getHeaderList(clz);
        Map<Integer,ExcelHeader> map = new HashMap<Integer,ExcelHeader>();
 
        for (Cell cell : titleRow) {
            String value = getCellValue(cell).trim();
 
            for (ExcelHeader header : headers) {
                if(value.equals(header.getTitle())){
                    map.put(cell.getColumnIndex(),header);
                }
            }
        }
 
        return map;
    }
 
 
 
 
    /**
     * 直接输出到excel,不需要模板
     * @param list 要输出的对象集合
     * @param sheetName 输出的sheet的名称
     * @param stream  输出到流
     * @param is2007  是否生成2007版本的
     * @param serName 如果serName为null或者""，则标识不需要序号，否则则将输出serName序号列
     */
    public static void export2Excel(List<?> list, String sheetName,OutputStream stream,boolean is2007,
             String serName) {
        Workbook wb = null;
        Sheet sheet = null;
 
        if(is2007){
            wb = new XSSFWorkbook();
        } else {
            wb = new HSSFWorkbook();
        }
 
        if (list != null && list.size() > 0) {
            // 获得要输出的excel的标题头
            List<ExcelHeader> headers = getHeaderList(list.get(0).getClass());
 
            if (headers != null && headers.size() > 0) {
                // 创建sheet
                sheet = isNullOrEmpty(sheetName)?wb.createSheet():wb.createSheet(sheetName);
 
                // 写标题
                writeHeader(sheet, headers, serName);
 
                // 写数据
                writeData(sheet, headers, list, serName);
 
            }
 
        }
        try {
            wb.write(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 将excel数据导入为对象集合
     * @param is Excel文件输入流
     * @param sheetName 要导入的数据的sheet的名字,设置为空，默认取第一个sheet
     * @param clz 数据映射的类
     * @param titleLine 开始行
     * @return
     */
    public static <T> List<T> importFromExcel(InputStream is,String sheetName,Class<T> clz,int titleLine){
        Workbook wb = null;
        Sheet sheet = null;
 
        try {
            wb = WorkbookFactory.create(is);
 
            if(isNullOrEmpty(sheetName)){
                sheet = wb.getSheetAt(0);
            }else{
                sheet = wb.getSheet(sheetName);
            }
 
            Row titleRow = sheet.getRow(titleLine);
            Map<Integer,ExcelHeader> map = getHeaderMap(clz,titleRow);//获得excel中的标题行信息
 
            if(map!=null && map.size()>0){
                //映射excel中的数据为对象
                return setProperty(clz, titleLine, sheet, map);
 
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return null;
    }
 
    /**
     * Excel中的数据映射至对象
     * @param clz
     * @param titleLine
     * @param sheet
     * @param map
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ParseException
     */
    private static <T> List<T> setProperty(Class<T> clz, int titleLine,
            Sheet sheet, Map<Integer, ExcelHeader> map)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, ParseException {
        List<T> list = new ArrayList<T>();
        Row row = null;
        for(int i=titleLine+1;i<=sheet.getLastRowNum();i++) {
            row = sheet.getRow(i);
            T obj = clz.newInstance();
            for(Cell cell:row) {
                int ci = cell.getColumnIndex();
                ExcelHeader header = map.get(ci);
                if(header!=null){
                    if(!isNullOrEmpty(header.getDateFormat())){//设置日期
                    	reverseStringToDate(obj,header.getFieldName(),header.getDateFormat(),getCellValue(cell));
                    }else if(header.getMap().size()>0){ //设置替代值
                        Map<String,String> reverseMap = reverse(header.getMap());
                        String value = reverseMap.get(getCellValue(cell));
                        BeanUtils.setProperty(obj,header.getFieldName(),value);
                    }else if(ExcelHeader.getConverters().containsKey(header.getFieldName())){
                    	ExcelDataConverter converter = ExcelHeader.getConverters().get(header.getFieldName());
                    	BeanUtils.setProperty(obj,header.getFieldName(),converter.convertFromString(getCellValue(cell)));
                    }else{
                        BeanUtils.setProperty(obj,header.getFieldName(),getCellValue(cell));
                    }
                }
            }
            list.add(obj);
        }
        return list;
    }
 
    /**
     * 获取单元格的值
     * @param c
     * @return
     */
    private static String getCellValue(Cell c) {
        String o = null;
        switch (c.getCellType()) {
        case Cell.CELL_TYPE_BLANK:
            o = ""; break;
        case Cell.CELL_TYPE_BOOLEAN:
            o = String.valueOf(c.getBooleanCellValue()); break;
        case Cell.CELL_TYPE_FORMULA:
            o = String.valueOf(c.getCellFormula()); break;
        case Cell.CELL_TYPE_NUMERIC:
            o = String.valueOf(c.getNumericCellValue()); break;
        case Cell.CELL_TYPE_STRING:
            o = c.getStringCellValue(); break;
        default:
            o = null;
            break;
        }
        return o;
    }
 
 
 
 
    /**
     * 写入excel标题
     * @param sheet
     * @param headers
     * @param serName
     */
    private static void writeHeader(Sheet sheet, List<ExcelHeader> headers,
            String serName) {
        int cellIndex = 0; // 列号
        Row row = sheet.createRow(0); // 默认将标题写入到第1行
        // 判断序号是否是null和空串，如果不是的话，先写入序号的字符串名称
        if (!isNullOrEmpty(serName)) {
            row.createCell(cellIndex).setCellValue(serName);
            cellIndex++;
        }
 
        // 开始写入标题，用@ExcelResource标识的标题
        for (ExcelHeader header : headers) {
            row.createCell(cellIndex++).setCellValue(header.getTitle());
        }
    }
 
    /**
     * 写入数据
     * @param sheet
     * @param headers
     * @param list
     * @param serName
     */
    private static void writeData(Sheet sheet, List<ExcelHeader> headers,
            List<?> list, String serName) {
 
            int rowIndex = 1; // 默认将数据从第二行开始写
            for (Object obj : list) {
                int cellIndex = 0;
                Row row = sheet.createRow(rowIndex);
 
                //输出序号
                if (!isNullOrEmpty(serName)) {
                    row.createCell(cellIndex).setCellValue(
                            String.valueOf(rowIndex));
                    cellIndex++;
                }
 
                //输出数据
                for (ExcelHeader header : headers) {
                    row.createCell(cellIndex++).setCellValue(
                            convert(obj,header));
                }
                rowIndex++;
            }
 
    }
 
 
    /**
     * 对象中的属性的值转化为excel的值
     * @param obj
     * @param header
     * @return
     */
    private static String convert(Object obj,ExcelHeader header){
        try {
        	//ExcelDataConverter处理
        	if(ExcelHeader.getConverters().containsKey(header.getFieldName())){
        		ExcelDataConverter converter = ExcelHeader.getConverters().get(header.getFieldName());
        		return converter.convertToString(PropertyUtils.getProperty(obj, header.getFieldName()));
        	}
        	
            //替代值转化
            if(header.getMap().size()!=0){
                return header.getMap().get(BeanUtils.getProperty(obj, header.getFieldName()));
            }
 
            //日期转化
            if(!isNullOrEmpty(header.getDateFormat())){
            	return convertDateToString(PropertyUtils.getProperty(obj, header.getFieldName()),header.getDateFormat());
            }
 
            return BeanUtils.getProperty(obj, header.getFieldName());
        } catch (Exception e) {
        } 
        
        return "";
    }
 
 
 
 
    /**
     * 判断字符串是否是null或者""
     *
     * @param str
     * @return
     */
    private static boolean isNullOrEmpty(String str) {
        if (str == null || str.trim().length() == 0) {
            return true;
        }
        return false;
    }
    
    /**
     * 将一个日期转换为一个字符串
     * 支持java.util.Date,java.sql.Date,java.sql.TimeStamp,java.util.Calendar,Long(long)类型
     * @param object
     * @return
     */
    private static String convertDateToString(Object obj,String format){
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	String result = null;
    	switch(obj.getClass().getName()){
    		case "java.util.Date":
    			result = sdf.format((java.util.Date)obj);
    			break;
    		case "java.sql.Date":
    			result = sdf.format(new java.util.Date(((java.sql.Date)obj).getTime()));
    			break;
    		case "java.sql.Timestamp":
    			result = sdf.format(new java.util.Date(((java.sql.Timestamp)obj).getTime()));
    			break;
    		case "java.util.GregorianCalendar":
    			result = sdf.format(new java.util.Date(((java.util.Calendar)obj).getTimeInMillis()));
    			break;
    		case "java.lang.Long":
    			result = sdf.format(new java.util.Date((Long)obj));
    			break;
    		case "long":
    			result = sdf.format(new java.util.Date((long)obj));
    			break;
    	}
    	return result;
    }
    
    private static void reverseStringToDate(Object obj,String fieldName,String dateFormat,String cellValue){
    	try {
			Field field = obj.getClass().getDeclaredField(fieldName);
			Object result = null;
			switch(field.getType().getName()){
			case "java.util.Date":
				result = new SimpleDateFormat(dateFormat).parse(cellValue);
				break;
			case "java.sql.Date":
				result = new java.sql.Date(new SimpleDateFormat(dateFormat).parse(cellValue).getTime());
				break;
			case "java.sql.Timestamp":
				result = new java.sql.Timestamp(new SimpleDateFormat(dateFormat).parse(cellValue).getTime());
				break;
			case "java.util.Calendar":
				Calendar cal = Calendar.getInstance();
				cal.setTime(new SimpleDateFormat(dateFormat).parse(cellValue));
				result = cal;
				break;
			case "java.lang.Long":
				result = Long.valueOf(new SimpleDateFormat(dateFormat).parse(cellValue).getTime());
				break;
			case "long":
				result = new SimpleDateFormat(dateFormat).parse(cellValue).getTime();
				break;
			}
			BeanUtils.setProperty(obj,fieldName,result);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
    }
    
 
    /**
     * 将一个Map的key value反转
     * @param map
     * @return
     */
    public static Map<String,String> reverse(Map<String,String> map){
 
        Map<String,String> newMap = new HashMap<String,String>();
 
        Set<String> set = map.keySet();
        for (String key : set) {
            newMap.put(map.get(key),key);
        }
 
        return newMap;
    }
 
 
}
