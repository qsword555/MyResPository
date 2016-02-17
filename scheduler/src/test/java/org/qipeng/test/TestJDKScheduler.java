package org.qipeng.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.scheduling.support.DelegatingErrorHandlingRunnable;
import org.springframework.scheduling.support.TaskUtils;

public class TestJDKScheduler {
	
	@Test
	public void test01() throws InterruptedException{
		//单例线程
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		
		/*
		 * 对于下面这个例子，它的实际执行间隔是3秒，也就是说上一次方法执行完毕之后，立即执行下一次
		 */
		service.scheduleAtFixedRate(new Runnable() {
	            @Override
	            public void run() {
	                System.out.println("test01:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	                try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	            }
	        },1,2,TimeUnit.SECONDS);  
		
		Thread.sleep(8000);
	}
	
	@Test
	public void test02() throws InterruptedException{
		//创建一个线程池
		ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
		
		/*
		 * 对于下面这个例子，它的实际执行间隔是5秒，也就是说方法执行完毕之后，再等待2秒执行下一次
		 */
		service.scheduleWithFixedDelay(new Runnable() {
	            @Override
	            public void run() {
	                System.out.println("test02:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	                try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	            }
	        },1,2,TimeUnit.SECONDS);   
		
		Thread.sleep(8000);
	}
	
	@Test
	public void test03() throws ParseException, InterruptedException{
		//每天执行
		String execTime = "12:30:00";
		
		long oneDay = 24*60*60*1000;
		
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Date curDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dayFormat.format(new Date())+" "+execTime);  
		
		long initDelay = curDate.getTime()-System.currentTimeMillis();
		initDelay = initDelay > 0 ? initDelay : oneDay + initDelay; 
		
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		
		service.scheduleAtFixedRate(new Runnable() {
	            @Override
	            public void run() {
	                System.out.println("test03:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	            }
	        },initDelay,oneDay,TimeUnit.MILLISECONDS);  
		
		
		Thread.sleep(120000);
	}
	
	@Test
	public void test04() throws InterruptedException{
		//单例线程
		ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
		
		//我们发现定时任务会被异常终止执行
		service.scheduleAtFixedRate(new Runnable() {
	            @Override
	            public void run() {
	            	System.err.println("test04:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	                throw new RuntimeException("定时任务出现异常");
	            }
	        },1,2,TimeUnit.SECONDS);  
		
		Thread.sleep(8000);
	}
	
	@Test
	public void test05() throws InterruptedException{
		//单例线程
		ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
		
		//异常已经被try...catch住了，定时任务就不会停止执行了
		WrapExceptionRunnable runnable = new WrapExceptionRunnable(new Runnable(){
			@Override
			public void run() {
				System.err.println("test05:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
                throw new RuntimeException("定时任务出现异常");
			}
		});
		
		service.scheduleAtFixedRate(runnable,1,2,TimeUnit.SECONDS);  
		
		Thread.sleep(8000);
	}
	
	//保证不会有Exception抛出到线程池的Runnable，防止因为异常的抛出而导致scheduled thread停止
    public static class WrapExceptionRunnable implements Runnable{

        private Runnable runnable;

        public WrapExceptionRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
    
    @Test
	public void test06() throws InterruptedException{
    	
		ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
		
		// 任何异常不会中断schedule执行, 由Spring TaskUtils的LOG_AND_SUPPRESS_ERROR_HANDLER進行处理
    	DelegatingErrorHandlingRunnable runnable = TaskUtils.decorateTaskWithErrorHandler(new Runnable(){
			@Override
			public void run() {
				System.err.println("test06:"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
                throw new RuntimeException("定时任务出现异常");
			}
    		
    	}, null, true);
		
		service.scheduleAtFixedRate(runnable,1,2,TimeUnit.SECONDS);  
		
		Thread.sleep(8000);
	}
    
}
