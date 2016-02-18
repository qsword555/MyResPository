package org.qipeng.dynamictask.support;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DynamicExec1 {
	
	public void test01(){
		System.out.println("DynamicExec1:test01 " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	public void test02(){
		System.err.println("DynamicExec1:test02 " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
}
