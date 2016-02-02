package excel;

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
