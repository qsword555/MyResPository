package excel;

import java.math.BigDecimal;

import org.junit.Test;

import excel.UserVo.MoneyConverter;

public class TestOthers {

	@Test
	public void test01(){
		
		MoneyConverter convert = new MoneyConverter();
		
		BigDecimal money = new BigDecimal("100000");
		
		String str = convert.convertToString(money);
		
		BigDecimal big = (BigDecimal) convert.convertFromString(str);
		
		System.out.println(str);
		
		System.out.println(big);
	}
	
}
