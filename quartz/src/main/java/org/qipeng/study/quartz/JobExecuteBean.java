package org.qipeng.study.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JobExecuteBean {
	
	public void executeByCronTriggerJob(){
		System.out.println("executeByCronTriggerJob " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
	public void executeBySimpleTriggerJob(){
		System.err.println("executeBySimpleTriggerJob " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
