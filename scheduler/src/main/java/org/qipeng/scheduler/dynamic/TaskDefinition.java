package org.qipeng.scheduler.dynamic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="maintain_task_definition")
public class TaskDefinition {

	 	@Id
	    private Long id;
	 
	    @Column(name = "name")
	    private String name;
	 
	    //cron表达式
	    private String cron;
	 
	    //要执行的class类全名
	    @Column(name = "bean_class")
	    private String beanClass;
	 
	    //要执行的被Spring管理的bean的名字
	    @Column(name = "bean_name")
	    private String beanName;
	 
	    //要执行的bean的方法名
	    @Column(name = "method_name")
	    private String methodName;
	 
	    //任务状态，是停止状态还是可运行状态
	    private Boolean status = Boolean.FALSE;
	 
	    //任务描述
	    private String description;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCron() {
			return cron;
		}

		public void setCron(String cron) {
			this.cron = cron;
		}

		public String getBeanClass() {
			return beanClass;
		}

		public void setBeanClass(String beanClass) {
			this.beanClass = beanClass;
		}

		public String getBeanName() {
			return beanName;
		}

		public void setBeanName(String beanName) {
			this.beanName = beanName;
		}

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}

		public Boolean getStatus() {
			return status;
		}

		public void setStatus(Boolean status) {
			this.status = status;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	
}
