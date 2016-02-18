package org.qipeng.test.example01;

import org.qipeng.dynamictask.bean.TaskDefinition;
import org.qipeng.dynamictask.service.DynamicTaskService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestClusterClientA {

	public static void main(String[] args) throws InterruptedException {
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("context-basic.xml");
		System.out.println("clientA启动完毕，测试开始！");
		
		DynamicTaskService service = ctx.getBean(DynamicTaskService.class);
		service.deleteAll();
		
		TaskDefinition td = new TaskDefinition();
		td.setId(1L);
		td.setBeanClass("org.qipeng.dynamictask.support.DynamicExec1");
		td.setMethodName("test01");
		td.setStatus(true);
		td.setCron("0 58 8 ? * *");
		td.setName("任务1");
		service.addTask(td);
		
		td = new TaskDefinition();
		td.setId(2L);
		td.setBeanClass("org.qipeng.dynamictask.support.DynamicExec1");
		td.setMethodName("test02");
		td.setStatus(true);
		td.setCron("0 59 8 ? * *");
		td.setName("任务2");
		service.addTask(td);
		
		Thread.sleep(120000);
		
		
		ctx.close();
		
	}
	
}
