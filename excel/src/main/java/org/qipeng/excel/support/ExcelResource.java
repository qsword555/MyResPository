package org.qipeng.excel.support;

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
