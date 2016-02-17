package org.qipeng.scheduler.support;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

@Component
public class SpringSchedulerJobBaseAnnotation {

	/*
	 * fixedDelay相当于jdk的ScheduledExecutorService的scheduleWithFixedDelay
	 * 方法执行完毕后再等待delay时间执行下一次（下例实际执行间隔为3）
	 */
	@Scheduled(initialDelay=1000,fixedDelay=2000)     
	public void testScheduler01(){
		System.out.println("testScheduler01-"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * fixedRate相当于jdk的ScheduledExecutorService的scheduleAtFixedRate
	 * 如果方法执行时间超过了定时任务时间间隔，立即执行下一次（下例实际执行间隔为5）
	 */
	@Scheduled(initialDelay=1000,fixedRate=2000)   
	public void testScheduler02(){
		System.err.println("testScheduler02-"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 基于cron表达式的，注意不支持initialDelay这个配置
	 * 感觉位于scheduleWithFixedDelay和scheduleAtFixedRate之间（下例实际执行间隔为4）
	 */
	@Scheduled(cron="*/2 * * * * ?")   
	public void testScheduler03(){
		System.err.println("testScheduler03-"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 此例演示了如何为一个方法增加多个定时触发器
	 */
	@Schedules({
		@Scheduled(cron="*/5 * * * * ?"),
		@Scheduled(initialDelay=10000,fixedRate=1000)
		})
	public void testScheduler04(){
		System.out.println("testScheduler04-"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
	
}
