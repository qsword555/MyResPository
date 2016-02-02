本工具类可以将一个List<Bean>输出成2003或者2007的Excel，使用方式仅仅需要在Bean中将需要输出的属性名称添加上`@ExcelResource`这个Annotation即可。

代码位于：[github](https://github.com/qsword555/MyRespository)上的excel工程下

需要的依赖：
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  	<poi.version>3.9</poi.version>
    <jdk.version>1.7</jdk.version>
  </properties>

  <dependencies>
   		<!-- apache poi -->
		<dependency>
			<groupId>org.apache.poi</groupId>
			<artifactId>poi</artifactId>
			<version>${poi.version}</version>
		</dependency>
		
		<dependency>
			<groupId>org.apache.poi</groupId>
			<artifactId>poi-ooxml</artifactId>
			<version>${poi.version}</version>
		</dependency>
		
		<!-- apache commons -->
		<dependency>
			<groupId>commons-beanutils</groupId>
			<artifactId>commons-beanutils</artifactId>
			<version>1.9.2</version>
		</dependency>
		
		<!-- 测试依赖 -->
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.12</version>
			<scope>test</scope>
		</dependency>
		
  </dependencies>
```

`@ExcelResource`这个Annotation的定义如下：
```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义一个用于标识Excel字段资源的annotation
 * @author Administrator
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelResource {
 
	/**
	 * 对应Excel的标题，如果不设置，默认为属性名称
	 * @return
	 */
    String title() default "";          
    
    /**
	 * 对应Excel输出时的column顺序
	 * @return
	 */
    int order() default 9999;           
 
    /**
     * 对应需要转化的值
     * 例如：支持String,char,boolean等等很多类型
     * @ExcelResource(keyValue={"M=男","F=女"})
     * @ExcelResource(keyValue={"true=是","false=否"})
     * 更复杂的转换请使用converter()
     * @return
     */
    String[] keyValue() default {""};  
 
    /**
     * 快速处理日期转换，支持如下类型日期的转换
     * java.sql.Date
     * java.util.Date
     * java.sql.Timestamp
     * java.util.Canendar
     * java.lang.Long(long)
     * 例如：
     * @ExcelResource(dateFormat="yyyy-MM-dd HH:mm:ss")
     * @return
     */
    String dateFormat() default "";    
    
    /**
     * 当遇到复杂转换的时候，可以写个转换器，转换器需要继承自ExcelDataConverter
     * 来进行复杂的转换
     * @return
     */
    Class<? extends ExcelDataConverter> converter() default ExcelDataConverter.Void.class;
 
}
```

然后定义一个用于保存Excel的header的实体bean，这个实体bean实现了`Comparable` 接口，用于对定义Excel输出的顺序
`ExcelHeader`代码如下：
```java
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
```
如果需要复杂的转换则需要继承转换器`ExcelDataConverter`
```java
public abstract class ExcelDataConverter {

	public abstract Object convertFromString(String str);
	
	public abstract String convertToString(Object t);
	
	public static class Void extends ExcelDataConverter{

		@Override
		public Object convertFromString(String str) {
			return null;
		}

		@Override
		public String convertToString(Object t) {
			return null;
		}
		
	}
}
```

最后是我们的`ExcelUtil`这个类，可以调用export2Excel来将对象集合输出到Excel，或者调用importFromExcel将Excel反序列化到实体对象集合
```java
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
```
###测试###
测试实体Vo
```java
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

import org.qipeng.excel.support.ExcelDataConverter;
import org.qipeng.excel.support.ExcelResource;

public class UserVo {

	//不需要输出，不用加注解即可
	private int id;           
	
	@ExcelResource(title="姓名",order=1)
	private String name;
	
	@ExcelResource(title="是否结婚",order=4,keyValue={"true=是","false=否"})
	private boolean married;
	
	@ExcelResource(title="性别",order=2,keyValue={"F=女","M=男"})
	private char sex;
	
	@ExcelResource(title="财富",order=5,converter=MoneyConverter.class)
	private BigDecimal money;
	
	@ExcelResource(title="年龄",order=3)
	private int age;
	
	@ExcelResource(title="生日",order=6,dateFormat="yyyy-MM-dd")
	private java.util.Date utilDate;
	
	@ExcelResource(order=7,dateFormat="yyyyMMdd")
	private java.sql.Date sqlDate;
	
	@ExcelResource(order=8,dateFormat="yyyy/MM/dd")
	private java.util.Calendar calendar;
	
	@ExcelResource(order=9,dateFormat="yyyy-MM-dd HH:mm:ss.SSS")
	private long baseLongDate;
	
	@ExcelResource(order=10,dateFormat="yyyy年MM月dd日 HH时mm分ss秒")
	private Long longDate;
	
	@ExcelResource(order=11,dateFormat="yyyyMMddHHmmssSSS")
	private java.sql.Timestamp timeStamp;
	
	//转换器
	public static class MoneyConverter extends ExcelDataConverter{

		@Override
		public Object convertFromString(String str) {
			if(str!=null && str.trim().length()>0){
				NumberFormat nf = new DecimalFormat("#,###.####");
				try {
					return new BigDecimal(nf.parse(str).toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public String convertToString(Object t) {
			if(t!=null){
				if(t instanceof BigDecimal){
					BigDecimal num = (BigDecimal) t;
					NumberFormat nf = new DecimalFormat("#,###.####");
					return nf.format(num);
				}
				return t.toString();
			}
			return null;
		}
		
	}
	
	public UserVo(int id, String name, boolean married, char sex, BigDecimal money, int age, Date utilDate) {
		super();
		this.id = id;
		this.name = name;
		this.married = married;
		this.sex = sex;
		this.money = money;
		this.age = age;
		this.utilDate = utilDate;
	}
	
	public UserVo() {
		super();
	}

    //setter和getter还有toString()略
	
}
```
测试代码：
```java
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.qipeng.excel.support.ExcelUtil;

public class TestExcel {

	@Test
	public void testExport() throws FileNotFoundException{
		UserVo vo1 = new UserVo(1,"张三",false,'M',new BigDecimal("200000"),21,new Date());
		vo1.setSqlDate(new java.sql.Date(new Date().getTime()));
		vo1.setCalendar(Calendar.getInstance());
		vo1.setLongDate(new Date().getTime());
		vo1.setBaseLongDate(new Date().getTime());
		vo1.setTimeStamp(new Timestamp(new Date().getTime()));
		
		UserVo vo2 = new UserVo(2,"大花",true,'F',new BigDecimal("80000"),22,new Date());
		vo2.setSqlDate(new java.sql.Date(new Date().getTime()));
		vo2.setCalendar(Calendar.getInstance());
		vo2.setLongDate(new Date().getTime());
		vo2.setBaseLongDate(new Date().getTime());
		vo2.setTimeStamp(new Timestamp(new Date().getTime()));
		
		List<UserVo> list = new ArrayList<UserVo>();
		list.add(vo1);
		list.add(vo2);
		
		OutputStream os = new FileOutputStream("d:\\1.xls");
		ExcelUtil.export2Excel(list,"测试", os, false,"序号");
		
		os = new FileOutputStream("d:\\2.xls");
		ExcelUtil.export2Excel(list,"测试", os, false,null);
		
		os = new FileOutputStream("d:\\3.xlsx");
		ExcelUtil.export2Excel(list,"测试", os, true,"序号");
		
		os = new FileOutputStream("d:\\4.xlsx");
		ExcelUtil.export2Excel(list,"测试",os, true,null);
	}
	
	@Test
	public void testImport() throws FileNotFoundException{
		InputStream is = new FileInputStream("d:\\1.xls");
		List<UserVo> list = ExcelUtil.importFromExcel(is,null,UserVo.class,0);
		assertTrue(list.size()==2);
		
		for (UserVo userVo : list) {
			System.out.println(userVo);
		}
		
		System.out.println("==============================");
		list=null;
		is = new FileInputStream("d:\\2.xls");
		list = ExcelUtil.importFromExcel(is,null,UserVo.class,0);
		assertTrue(list.size()==2);
		
		for (UserVo userVo : list) {
			System.out.println(userVo);
		}
		
		System.out.println("==============================");
		list=null;
		is = new FileInputStream("d:\\3.xlsx");
		list = ExcelUtil.importFromExcel(is,null,UserVo.class,0);
		assertTrue(list.size()==2);
		
		for (UserVo userVo : list) {
			System.out.println(userVo);
		}
		
		System.out.println("==============================");
		list=null;
		is = new FileInputStream("d:\\4.xlsx");
		list = ExcelUtil.importFromExcel(is,null,UserVo.class,0);
		assertTrue(list.size()==2);
		
		for (UserVo userVo : list) {
			System.out.println(userVo);
		}
	}

}
```