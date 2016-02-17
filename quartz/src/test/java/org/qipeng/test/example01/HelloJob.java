package org.qipeng.test.example01;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HelloJob implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("HelloJob " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}

}
