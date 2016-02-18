package org.qipeng.test.example01;

import org.qipeng.dynamictask.bean.TaskDefinition;
import org.qipeng.dynamictask.service.DynamicTaskService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestClusterClientB {

	public static void main(String[] args) throws InterruptedException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("context-basic.xml");
		System.out.println("clientB启动完毕，测试开始！");
		
		
//		DynamicTaskService service = ctx.getBean(DynamicTaskService.class);
//		service.deleteAll();
		
//		DynamicTaskService service = ctx.getBean(DynamicTaskService.class);
//		
//		TaskDefinition td = new TaskDefinition();
//		td.setId(3L);
//		td.setBeanName("dynamicExec2");
//		td.setMethodName("test03");
//		td.setStatus(true);
//		td.setCron("0 51 8 ? * *");
//		td.setName("任务3");
//		service.addTask(td);
//		
//		td = new TaskDefinition();
//		td.setId(4L);
//		td.setBeanName("dynamicExec2");
//		td.setMethodName("test04");
//		td.setStatus(true);
//		td.setCron("*/8 * * * * ?");
//		td.setName("任务4");
//		service.addTask(td);
		
		Thread.sleep(120000);
		
		
		ctx.close();
	}
	
}
