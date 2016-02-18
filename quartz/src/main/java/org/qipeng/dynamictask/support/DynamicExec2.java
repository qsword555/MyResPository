package org.qipeng.dynamictask.support;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component("dynamicExec2")
public class DynamicExec2 {
	
	public void test03(){
		System.err.println("DynamicExec2:test03 " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	public void test04(){
		System.err.println("DynamicExec2:test04 " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	
}
