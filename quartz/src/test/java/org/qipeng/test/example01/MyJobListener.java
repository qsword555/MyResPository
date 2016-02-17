package org.qipeng.test.example01;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

public class MyJobListener implements JobListener {

	@Override
	public String getName() {
		return "MyJobListener";
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		System.out.println("MyJobListener says: jobToBeExecuted.");   //即将被执行
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		System.out.println("Job1Listener says: jobExecutionVetoed.");  //job被否决
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		System.out.println("Job1Listener says: jobWasExecuted.");   //任务执行完毕，可以在此方法中继续scheduler job
	}

}
