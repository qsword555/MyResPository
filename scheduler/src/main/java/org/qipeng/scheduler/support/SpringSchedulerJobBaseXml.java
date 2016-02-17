package org.qipeng.scheduler.support;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SpringSchedulerJobBaseXml {
	
	//使用fixed-delay
	public void testScheduler01(){
		System.out.println("testScheduler01-"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	//使用fixed-rate
	public void testScheduler02(){
		System.err.println("testScheduler02-"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	//使用cron表达式
	public void testScheduler03(){
		System.out.println("testScheduler03-"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
}
