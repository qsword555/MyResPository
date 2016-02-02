package excel;

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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMarried() {
		return married;
	}

	public void setMarried(boolean married) {
		this.married = married;
	}

	public char getSex() {
		return sex;
	}

	public void setSex(char sex) {
		this.sex = sex;
	}

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public java.util.Date getUtilDate() {
		return utilDate;
	}

	public void setUtilDate(java.util.Date utilDate) {
		this.utilDate = utilDate;
	}

	public java.sql.Date getSqlDate() {
		return sqlDate;
	}

	public void setSqlDate(java.sql.Date sqlDate) {
		this.sqlDate = sqlDate;
	}

	public java.util.Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(java.util.Calendar calendar) {
		this.calendar = calendar;
	}

	public long getBaseLongDate() {
		return baseLongDate;
	}

	public void setBaseLongDate(long baseLongDate) {
		this.baseLongDate = baseLongDate;
	}

	public Long getLongDate() {
		return longDate;
	}

	public void setLongDate(Long longDate) {
		this.longDate = longDate;
	}

	public java.sql.Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(java.sql.Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return "UserVo [id=" + id + ", name=" + name + ", married=" + married + ", sex=" + sex + ", money=" + money
				+ ", age=" + age + ", utilDate=" + utilDate + ", sqlDate=" + sqlDate + ", calendar=" + calendar
				+ ", baseLongDate=" + baseLongDate + ", longDate=" + longDate + ", timeStamp=" + timeStamp + "]";
	}
	
}
