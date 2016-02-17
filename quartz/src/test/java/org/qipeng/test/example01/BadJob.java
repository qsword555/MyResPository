package org.qipeng.test.example01;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

public class BadJob implements Job{

	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
	    JobDataMap dataMap = context.getJobDetail().getJobDataMap();

	    int denominator = dataMap.getInt("denominator");
	    
	    System.out.println("---" + jobKey + " executing at " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " with denominator " + denominator);
	    
	    try {
	    	//denominator会传过来一个0，所以会报错
	    	int  calculation = 4815 / denominator;
	    	System.out.println("---" + jobKey + " completed at " + new SimpleDateFormat("HH:mm:ss").format(new Date())+" calculation="+calculation);
	    } catch (Exception e) {
	      System.out.println("--- Error in job!");
	      JobExecutionException e2 = new JobExecutionException(e);

	      //将denominator的值修改，使job运行不再出错
	      dataMap.put("denominator", "1");

	      //设置为true表示重新执行出错的job，设置为false表示不重新此次，但是后面还是会按照Trigger的规则执行
	      //e2.setRefireImmediately(false);
	      
	      //设置为true表示此job上关联的所有的Trigger也不再执行
	      //e2.setUnscheduleAllTriggers(true);
	      
	      //设置为true表示不再执行此job的此Trigger
	      //e2.setUnscheduleFiringTrigger(true);
	      
	      throw e2;
	    }
	    
	}

}
