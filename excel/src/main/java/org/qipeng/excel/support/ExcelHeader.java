package org.qipeng.excel.support;

import java.util.HashMap;
import java.util.Map;

public class ExcelHeader implements Comparable<ExcelHeader>{
	 
    private String title;   //标题
 
    private int order;     //标题排序
 
    private String fieldName;   //标题对应的类的字段的名称那个
 
    private String dateFormat;  //日期的格式
 
    private Map<String,String> map = new HashMap<String,String>();  //需要替换的值
    
    private static Map<String,ExcelDataConverter> converterMap = new HashMap<String,ExcelDataConverter>();  
    
    public ExcelHeader(String title, int order, String fieldName) {
    	this.title = title;
    	this.order = order;
    	this.fieldName = fieldName;
	}
    
	public ExcelHeader() {
		super();
	}

	public int compareTo(ExcelHeader o) {
        return order>o.order?1:(order<o.order?-1:0);
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public static void addConverters(String filedName,Class<?> clz) {
		try {
			converterMap.put(filedName, (ExcelDataConverter)clz.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static Map<String,ExcelDataConverter> getConverters() {
		return converterMap;
	}
	
	
}
