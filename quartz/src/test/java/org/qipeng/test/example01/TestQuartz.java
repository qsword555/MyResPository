package org.qipeng.test.example01;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Matcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.AnnualCalendar;
import org.quartz.impl.calendar.HolidayCalendar;
import org.quartz.impl.matchers.KeyMatcher;

public class TestQuartz {
	
	private SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	
	private Scheduler scheduler;
	
	@Before
	public void before(){
		try {
			scheduler = schedulerFactory.getScheduler();
			
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void after(){
		try {
			SchedulerMetaData metaData = scheduler.getMetaData();
			System.out.println("共运行了"+metaData.getNumberOfJobsExecuted()+"个job!");
			//等待job执行完毕后停止Scheduler
			scheduler.shutdown(true);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	//测试在指定的时间执行一次
	@Test
	public void testHelloWorld() throws SchedulerException, InterruptedException{
		
	    JobDetail job = JobBuilder.newJob(HelloJob.class).withIdentity("job1", "group1").build();

	    //下一分钟
	    Date runTime = DateBuilder.evenMinuteDate(new Date());
	    
	    Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").startAt(runTime).build();

	    scheduler.scheduleJob(job, trigger);
	    
	    Thread.sleep(60000);
	    
	}
	
	//SimpleTrigger
	@Test
	public void testSimpleTrigger() throws SchedulerException, InterruptedException{
		
		 JobDetail job = JobBuilder.newJob(HelloJob.class).withIdentity("job1", "group1").build();
		 
		 //立即开始执行，执行间隔1秒，重复执行5次（一共执行6次）    
		 //repeatForever()可以永远执行
		 Trigger trigger = TriggerBuilder.newTrigger()
				 .withIdentity("trigger1", "group1")
				 .startNow()
				 .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1).withRepeatCount(5))
				 .build();
		 
		 scheduler.scheduleJob(job, trigger);
		 
		 Thread.sleep(8000);
	}
	
	//CronTrigger
	@Test
	public void testCronTrigger() throws SchedulerException, InterruptedException{
		
		 JobDetail job = JobBuilder.newJob(HelloJob.class).withIdentity("job1", "group1").build();
		 
		 //不写什么时间开始的话，默认就是立即开始1次
		 Trigger trigger = TriggerBuilder.newTrigger()
				 .withIdentity("trigger1", "group1")
				 .withSchedule(CronScheduleBuilder.cronSchedule("*/2 * * * * ?"))
				 .build();
		 
		 scheduler.scheduleJob(job, trigger);
		 
		 Thread.sleep(8000);
	}
	
	//JobDatail
	@Test
	public void testJobDetail() throws SchedulerException, InterruptedException{
		
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("listData",Arrays.asList("red","green","blue"));       
		
		JobDetail job = JobBuilder.newJob(DataDetailJob.class).
				withIdentity("job1", "group1")
				.usingJobData(jobDataMap)          //usingJobData提供了很多重载函数
				.usingJobData("stringData","Hello")         
				.usingJobData("floatValue",3.141f) 
				.build();
		
		//不写什么时间开始的话，默认就是立即开始1次
		 Trigger trigger = TriggerBuilder.newTrigger()
				 .withIdentity("trigger1", "group1")
				 .withSchedule(CronScheduleBuilder.cronSchedule("*/5 * * * * ?"))
				 .build();
		
		 scheduler.scheduleJob(job, trigger);
		 
		 Thread.sleep(6000);
		
	}
	
	@Test
	public void testExecutionException1() throws SchedulerException, InterruptedException{
		
		Date startTime = DateBuilder.nextGivenSecondDate(null,3);
		
		JobDetail job = JobBuilder.newJob(BadJob.class).withIdentity("badJob1", "group1").usingJobData("denominator", "0").build();
		
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").startAt(startTime).build();
		
		scheduler.scheduleJob(job, trigger);
		
		Thread.sleep(7000);

	}
	
	@Test
	public void testExecutionException2() throws SchedulerException, InterruptedException{
		
		Date startTime = DateBuilder.nextGivenSecondDate(null,3);
		
		JobDetail job = JobBuilder.newJob(BadJob.class).withIdentity("badJob1", "group1").usingJobData("denominator", "0").build();
		
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1")
				.startAt(startTime)
				.withSchedule(CronScheduleBuilder.cronSchedule("*/2 * * * * ?"))
				.build();
		
		scheduler.scheduleJob(job, trigger);
		
		Thread.sleep(10000);

	}
	
	@Test
	public void testExecutionException3() throws SchedulerException, InterruptedException{
		
		Date startTime = DateBuilder.nextGivenSecondDate(null,5);
		
		JobDetail job1 = JobBuilder.newJob(BadJob.class).withIdentity("badJob1", "group1").usingJobData("denominator", "0").build();
		
		Trigger trigger1 = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1")
				.startNow()
				.withSchedule(CronScheduleBuilder.cronSchedule("*/2 * * * * ?"))
				.build();
		
		startTime = DateBuilder.nextGivenSecondDate(null,10);
		
		Trigger trigger2 = TriggerBuilder.newTrigger().withIdentity("trigger2", "group1")
				.startAt(startTime)
				.withSchedule(CronScheduleBuilder.cronSchedule("*/2 * * * * ?"))
				.build();
		
		scheduler.scheduleJob(job1, trigger1);
		scheduler.scheduleJob(job1, trigger2);
		
		Thread.sleep(15000);

	}
	
	@Test
	public void testCalendar() throws SchedulerException, InterruptedException{
		
		AnnualCalendar holidays = new AnnualCalendar();
		 
		Calendar today = new GregorianCalendar(2016,2,5);
		holidays.setDayExcluded(today, true);
		scheduler.addCalendar("holidays", holidays, false, false);
		
		JobDetail job = JobBuilder.newJob(HelloJob.class).withIdentity("badJob1", "group1")
				.build();
		
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 43 16 * * ?"))
				.modifiedByCalendar("holidays")
				.build();
		
		scheduler.scheduleJob(job, trigger);
		
		Thread.sleep(60000);
	} 
	
	@Test
	public void testHolidayCalendar() throws SchedulerException, InterruptedException{
		
		HolidayCalendar holidays = new HolidayCalendar();
		holidays.addExcludedDate(new Date());
		
		scheduler.addCalendar("holidays", holidays, false, false);
		
		JobDetail job = JobBuilder.newJob(HelloJob.class).withIdentity("badJob1", "group1")
				.build();
		
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 45 16 * * ?"))
				.modifiedByCalendar("holidays")
				.build();
		
		scheduler.scheduleJob(job, trigger);
		
		Thread.sleep(60000);
	} 
	
	@Test
	public void testJobListener() throws SchedulerException, InterruptedException{
		
		JobDetail job = JobBuilder.newJob(HelloJob.class).
				withIdentity("job1", "group1")
				.build();
		 
		 Date startTime = DateBuilder.nextGivenSecondDate(null,3);
		 //立即开始执行，执行间隔1秒，重复执行5次（一共执行6次）    
		 //repeatForever()可以永远执行
		 Trigger trigger = TriggerBuilder.newTrigger()
				 .withIdentity("trigger1", "group1")
				 .startAt(startTime)
				 .build();
		 
		 // Set up the listener
		 JobListener listener = new MyJobListener();
		 Matcher<JobKey> matcher = KeyMatcher.keyEquals(job.getKey());
		 scheduler.getListenerManager().addJobListener(listener, matcher);
		 
		 scheduler.scheduleJob(job, trigger);
		 
		 Thread.sleep(8000);
	}
	
}
