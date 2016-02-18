package org.qipeng.dynamictask.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.qipeng.dynamictask.bean.TaskDefinition;
import org.qipeng.dynamictask.dao.IBaseDao;
import org.qipeng.dynamictask.support.CollectJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class DynamicTaskServiceImpl implements DynamicTaskService{
	
	@Autowired
	private IBaseDao baseDao;
	
	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
	 
    private static final String JOB_PREFIX = "JOB_";
 
    private static final String TRIGGER_PREFIX = "trigger_";
 
    private Class<? extends Job> JOB_EXEC_CLASS = CollectJob.class;
	
	@Override
	public void initTask() {
		System.out.println("======================================");
        System.out.println("执行Batch任务初始化操作开始");
        System.out.println("======================================");
        
        try {
			List<TaskDefinition> jobs = baseDao.findListByHql("from TaskDefinition");
			List<Long> list = new ArrayList<>();
			for (TaskDefinition bean : jobs) {
			        JobDetail jobDetail = JobBuilder.newJob(JOB_EXEC_CLASS)
			                .withIdentity(JOB_PREFIX+bean.getId(), DEFAULT_GROUP)
			                .build();
			        jobDetail.getJobDataMap().put("taskDefinition", bean);

			        CronTrigger trigger = TriggerBuilder.newTrigger()
			                .withIdentity(TRIGGER_PREFIX+bean.getId(), DEFAULT_GROUP)
			                .withSchedule(CronScheduleBuilder.cronSchedule(bean.getCron()))
			                .build();
			        scheduler.scheduleJob(jobDetail, trigger);
			        
			        if(bean.getStatus()){
			        	list.add(bean.getId());
			        	
			        }
			        
			}
			stopTask(list.toArray(new Long[0]));
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
        
        System.out.println("======================================");
        System.out.println("执行Batch任务初始化操作结束");
        System.out.println("======================================");
		
	}
	

	@Override
	public void addTask(TaskDefinition taskDefinition) {
		 Long pk = (Long) baseDao.save(taskDefinition);
		 
         JobDetail jobDetail = JobBuilder.newJob(JOB_EXEC_CLASS)
                 .withIdentity(JOB_PREFIX+pk, DEFAULT_GROUP)
                 .build();
         jobDetail.getJobDataMap().put("taskDefinition", taskDefinition);

         CronTrigger trigger = TriggerBuilder.newTrigger()
                 .withIdentity(TRIGGER_PREFIX+pk, DEFAULT_GROUP)
                 .withSchedule(CronScheduleBuilder.cronSchedule(taskDefinition.getCron()))
                 .build();
         try {
			scheduler.scheduleJob(jobDetail, trigger);
			
			if(!taskDefinition.getStatus()){
				stopTask(taskDefinition.getId());
			}
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateTask(TaskDefinition taskDefinition) {
		try {
			baseDao.update(taskDefinition);
			JobKey jobKey = new JobKey(JOB_PREFIX+taskDefinition.getId(),DEFAULT_GROUP);
			TriggerKey triggerKey = new TriggerKey(TRIGGER_PREFIX+taskDefinition.getId(), DEFAULT_GROUP);
			CronTrigger newTrigger = TriggerBuilder.newTrigger()
			        .withIdentity(TRIGGER_PREFIX+taskDefinition.getId(), DEFAULT_GROUP)
			        .withSchedule(CronScheduleBuilder.cronSchedule(taskDefinition.getCron()))
			        .build();
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			jobDetail.getJobDataMap().put("taskDefinition", taskDefinition);
			scheduler.rescheduleJob(triggerKey, newTrigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(Long... ids) {
		if(ids!=null && ids.length>0){
			List<TriggerKey> triggerKeys = new ArrayList<>();
			for (Long id : ids) {
				TriggerKey key = new TriggerKey(TRIGGER_PREFIX+id, DEFAULT_GROUP);
				triggerKeys.add(key);
			}
			try {
				scheduler.unscheduleJobs(triggerKeys);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
			
			Map<String,Object> alias = new HashMap<>();
			alias.put("ids",ids);
			baseDao.updateByHql("delete from TaskDefinition where id in:ids",alias);
		}
		
	}

	@Override
	public List<TaskDefinition> findAll() {
		return baseDao.findAll(TaskDefinition.class);
	}

	@Override
	public Long getAllTaskCount() {
		return baseDao.getCount("from TaskDefinition");
	}

	@Override
	public Long getStartTaskCount() {
		return baseDao.getCount("from TaskDefinition where status=?",true);
	}

	@Override
	public void startTask(Long... ids) {
		try {
			if(ids!=null && ids.length>0){
				Map<String,Object> alias = new HashMap<>();
				alias.put("status", true);
				alias.put("ids",ids);
				baseDao.updateByHql("update TaskDefinition set status=:status where id in:ids", alias);
				for (Long id : ids) {
					 JobKey jobKey = new JobKey(JOB_PREFIX+id,DEFAULT_GROUP);
			         TriggerKey triggerKey = new TriggerKey(TRIGGER_PREFIX+id, DEFAULT_GROUP);
			         scheduler.resumeJob(jobKey);
			         scheduler.resumeTrigger(triggerKey);
				}
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopTask(Long... ids) {
		try {
			if(ids!=null && ids.length>0){
				for (Long id : ids) {
					 JobKey jobKey = new JobKey(JOB_PREFIX+id,DEFAULT_GROUP);
			         TriggerKey triggerKey = new TriggerKey(TRIGGER_PREFIX+id, DEFAULT_GROUP);
			         scheduler.pauseJob(jobKey);
			         scheduler.pauseTrigger(triggerKey);
					
				}
				Map<String,Object> alias = new HashMap<>();
				alias.put("status", false);
				alias.put("ids",ids);
				baseDao.updateByHql("update TaskDefinition set status=:status where id in:ids", alias);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public TaskDefinition get(Long id) {
		return baseDao.get(TaskDefinition.class, id);
	}

	@Override
	public void immediatelyExecute(Long id) {
		
		 try {
			 MethodInvoker methodInvoker = new MethodInvoker();
			 
			 TaskDefinition task = baseDao.get(TaskDefinition.class, id);
			 methodInvoker.setTargetMethod(task.getMethodName());
			 
			 Object bean = null;
			 if (!StringUtils.isEmpty(task.getBeanName())) {
			     bean = applicationContext.getBean(task.getBeanName());
			 } else {
			     bean = applicationContext.getAutowireCapableBeanFactory().createBean(Class.forName(task.getBeanClass()));
			 }
			 
			 methodInvoker.setTargetObject(bean);
			 methodInvoker.prepare();
			 methodInvoker.invoke();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void deleteAll() {
		
		List<TaskDefinition> list = baseDao.findAll(TaskDefinition.class);
		List<TriggerKey> triggerKeys = new ArrayList<>();
		for (TaskDefinition taskDefinition : list) {
			TriggerKey key = new TriggerKey(TRIGGER_PREFIX+taskDefinition.getId(), DEFAULT_GROUP);
			triggerKeys.add(key);
		}
		
		try {
			scheduler.unscheduleJobs(triggerKeys);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		
		baseDao.updateByHql("delete from TaskDefinition");
	}

}
