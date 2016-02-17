package org.qipeng.scheduler.dynamic;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component("springDynamicJob")
public class SpringDynamicJob {

	
	public void testJob01(){
		 System.out.println("testJob01:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	public void testJob02(){
		 System.err.println("testJob02:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	public void testJob03(){
		 System.out.println("testJob03:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	public void testJob04(){
		 System.err.println("testJob04:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
}
