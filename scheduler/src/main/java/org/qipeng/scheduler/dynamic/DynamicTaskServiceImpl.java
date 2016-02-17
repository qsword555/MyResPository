package org.qipeng.scheduler.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;
 
 
@Service
@Transactional
public class DynamicTaskServiceImpl implements DynamicTaskService {
 
    private final Logger logger = LoggerFactory.getLogger(DynamicTaskServiceImpl.class);
    private Map<Long, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<Long, ScheduledFuture<?>>();
 
    @Autowired
    private TaskScheduler taskScheduler;
 
    @Autowired
    private TaskDefinitionDao dao;
 
    @Autowired
    private ApplicationContext applicationContext;
 
    @Override
    public void initTask() { // 系统启动后，自动加载任务
        logger.info("=============init task start===============");
 
        List<Long> ids = new ArrayList<>();
        
        for(TaskDefinition td : dao.findAll()){
        	if (Boolean.TRUE.equals(td.getStatus())) {
                ids.add(td.getId());
            }
        }
        
        if (!CollectionUtils.isEmpty(ids)) {
            start(ids.toArray(new Long[0]));
        }
 
        logger.info("=============init task end 初始化成功" + ids.size() + "条任务===============");
    }
 
    @Override
    public void addTask(TaskDefinition taskDefinition) {
    	taskDefinition = dao.save(taskDefinition);
        Long id = taskDefinition.getId();
        if (Boolean.TRUE.equals(taskDefinition.getStatus())) {
            start(id);
        }
    }
 
    @Override
    public void updateTask(TaskDefinition taskDefinition) {
 
        stop(taskDefinition.getId());
 
        dao.save(taskDefinition);
 
        if (Boolean.TRUE.equals(taskDefinition.getStatus())) {
            start(taskDefinition.getId());
        }
 
    }
 
    @Override
    public void delete(Long... ids) {
        stop(ids);
       
        for (Long id : ids) {
			dao.delete(id);
		}
        
    }
 
    @Override
    public Iterable<TaskDefinition> findAll() {
        return dao.findAll();
    }
 
    @Override
    public Long getAllTaskCount() {
        return dao.count();
    }
 
    @Override
    public Long getStartTaskCount() {
    	return dao.getCountStatusOk();
    }
 
    @Override
    public void startTask(Long... ids) {
        dao.updateStatus(true, ids);
        start(ids);
    }
 
    @Override
    public void stopTask(Long... ids) {
    	dao.updateStatus(false, ids);
        stop(ids);
    }
 
    private synchronized void stop(Long... taskDefinitionIds) {
        for (Long taskDefinitionId : taskDefinitionIds) {
            TaskDefinition td = dao.findOne(taskDefinitionId);
 
            if (td == null) {
                return;
            }
 
            try {
                ScheduledFuture<?> future = taskMap.get(taskDefinitionId);
                if (future != null) {
                    //这里的true表示如果定时任务在执行，立即中止，false则等待任务结束后再停止。
                    future.cancel(true);
                }
            } catch (Exception e) {
                logger.error("stop task error, task id:" + taskDefinitionId, e);
                e.printStackTrace();
            }
        }
    }
 
    private synchronized void start(Long... taskDefinitionIds) {
 
        for (Long taskDefinitionId : taskDefinitionIds) {
 
            TaskDefinition td = dao.findOne(taskDefinitionId);
 
            if (td == null) {
                return;
            }
 
            try {
                ScheduledFuture<?> future = taskScheduler.schedule(createTask(td), new CronTrigger(td.getCron()));
                taskMap.put(taskDefinitionId, future);
            } catch (Exception e) {
                logger.error("start task error, task id:" + taskDefinitionId, e);
                e.printStackTrace();
            }
        }
    }
 
    private Runnable createTask(TaskDefinition td) {
        final MethodInvoker methodInvoker = new MethodInvoker();
        final Long taskId = td.getId();
        try {
            methodInvoker.setTargetMethod(td.getMethodName());
            Object bean = null;
            if (!StringUtils.isEmpty(td.getBeanName())) {
                bean = applicationContext.getBean(td.getBeanName());
            } else {
                bean = applicationContext.getAutowireCapableBeanFactory().createBean(Class.forName(td.getBeanClass()));
            }
            methodInvoker.setTargetObject(bean);
            methodInvoker.prepare();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return new Runnable() {
            @Override
            public void run() {
                try {
                    methodInvoker.invoke();
                } catch (Exception e) {
                    logger.error("run dynamic task error, task id is:" + taskId, e);
                    e.printStackTrace();
                }
            }
        };
    }
 
    @Override
    public TaskDefinition get(Long id) {
        return dao.findOne(id);
    }
 
    @Override
    public void immediatelyExecute(Long id) {
        TaskDefinition td = dao.findOne(id);
        new Thread(createTask(td)).start();
    }

	@Override
	public void deleteAll() {
		List<Long> list = new ArrayList<>();
		for (TaskDefinition task : dao.findAll()) {
			list.add(task.getId());
		}
		
		stop(list.toArray(new Long[0]));
		
		dao.deleteAll();
		
	}
 
}