package org.qipeng.test.vo;

import java.io.Serializable;
import java.util.Date;

public class UserVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5281073343925409753L;

	private int id;
	
	private String name;
	
	private Date birthday;

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

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
}
