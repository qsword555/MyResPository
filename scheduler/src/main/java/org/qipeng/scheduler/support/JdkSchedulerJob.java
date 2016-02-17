package org.qipeng.scheduler.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.support.DelegatingErrorHandlingRunnable;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.StringUtils;

/**
 * 用JDK的ScheduledThreadPoolExecutor定时执行的任务。 相比Spring的Task NameSpace配置方便,
 * 不需要反射調用，并强化了退出超时控制.
 * 有两种使用方式：
 * 一种是每隔多久执行一次，需要设置initialDelay，period
 * 第二种是每天到了指定的时间执行，需要设置defTime
 *
 * @author peng.qi
 */
public class JdkSchedulerJob implements Runnable{
	
	private static Log logger = LogFactory.getLog(JdkSchedulerJob.class);
	
	private static long DAY = 24 * 60 * 60 * 1000;   //每天的毫秒数
	 
    private int initialDelay = 0;          //开始执行延时(单位秒)
 
    private int period = 0;               //两次执行的间隔时间(单位秒)
 
    private String defTime = null;          //定义的时间（格式HH:mm:ss）表示每天到了此时间就会执行
    
    private int shutdownTimeout = Integer.MAX_VALUE;       //shutdown后等待的时间(单位秒)
 
    private ScheduledExecutorService scheduledExecutorService;    //定时任务接口
    
    @PostConstruct
    public void init() throws Exception {
 
        // 任何异常不会中断schedule执行, 由Spring的TaskUtils的LOG_AND_SUPPRESS_ERROR_HANDLER進行处理
    	DelegatingErrorHandlingRunnable task = TaskUtils.decorateTaskWithErrorHandler(this, null, true);
 
        // 创建单线程的SechdulerExecutor
        scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor(Executors.defaultThreadFactory());
 
        if (!StringUtils.isEmpty(defTime) && this.defTime.matches("\\d{2}:\\d{2}:\\d{2}")) {
        	 //每天到了defTime就会执行
            long initDelay  = getTimeMillis(this.defTime) - System.currentTimeMillis();
            initDelay = initDelay > 0 ? initDelay : DAY + initDelay;
            scheduledExecutorService.scheduleAtFixedRate(task,initDelay, DAY, TimeUnit.MILLISECONDS);
            logger.info("初始化["+this.getClass().getSimpleName()+"]任务成功，任务将会在每天的["+defTime+"]执行");
        } else {
        	//每隔多长时间运行
            // 验证间隔时间有效性
            if(period<=0){
                throw new IllegalArgumentException("任务的间隔时间必须大于0");
            }
            scheduledExecutorService.scheduleAtFixedRate(task, initialDelay,
                    period, TimeUnit.SECONDS);
            logger.info("初始化["+this.getClass().getSimpleName()+"]任务成功，任务将会在["+initialDelay+"]后执行，每["+period+"]s执行一次！");
        }
 
    }
    
    @PreDestroy
    public void stop() {
    	 try {
    		 scheduledExecutorService.shutdownNow();
             if (!scheduledExecutorService.awaitTermination(shutdownTimeout, TimeUnit.SECONDS)) {
            	 logger.error("Pool did not terminated");
             }
         } catch (InterruptedException ie) {
             Thread.currentThread().interrupt();
         }
    }
    
    /**
     * 获取指定时间对应的毫秒数
     * @param time "HH:mm:ss"
     * @return
     */
    private long getTimeMillis(String time) {
        try {
        	SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        	SimpleDateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " "
                    + time);
            return curDate.getTime();
        } catch (ParseException e) {
        	logger.error(e);
        }
        return 0;
    }
    
    /**
     * 设置任务初始启动延时时间.
     */
	public void setInitialDelay(int initialDelay) {
		this.initialDelay = initialDelay;
	}

	 /**
     * 设置任务间隔时间,单位秒.
     */
	public void setPeriod(int period) {
		this.period = period;
	}

	 /**
     * 设置一个执行的时间
     * @param defTime 时间字符串，时间格式必须为HH:mm:ss
     *           
     */
	public void setDefTime(String defTime) {
		this.defTime = defTime;
	}

	/**
     * 设置normalShutdown的等待时间, 单位秒.
     */
	public void setShutdownTimeout(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}

	@Override
	public void run() {
		//任务执行的代码放到这里即可
		System.out.println("JdkSchedulerJob run at "+new SimpleDateFormat("HH:mm:ss").format(new Date()));
	}
    

}
