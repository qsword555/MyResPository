package org.qipeng.dynamictask.support;

import org.qipeng.dynamictask.bean.TaskDefinition;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

public class CollectJob extends QuartzJobBean {

	private ApplicationContext applicationContext;

	/**
	 * 从SchedulerFactoryBean注入的applicationContext.
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private TaskDefinition taskDefinition;

	public TaskDefinition getTaskDefinition() {
		return taskDefinition;
	}

	public void setTaskDefinition(TaskDefinition taskDefinition) {
		this.taskDefinition = taskDefinition;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		try {

			MethodInvoker methodInvoker = new MethodInvoker();

			methodInvoker.setTargetMethod(taskDefinition.getMethodName());

			Object bean = null;
			if (!StringUtils.isEmpty(taskDefinition.getBeanName())) {
				bean = applicationContext.getBean(taskDefinition.getBeanName());
			} else {
				bean = applicationContext.getAutowireCapableBeanFactory()
						.createBean(Class.forName(taskDefinition.getBeanClass()));
			}

			methodInvoker.setTargetObject(bean);
			methodInvoker.prepare();
			methodInvoker.invoke();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
