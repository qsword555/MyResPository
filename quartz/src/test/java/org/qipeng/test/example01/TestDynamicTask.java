package org.qipeng.test.example01;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qipeng.dynamictask.bean.TaskDefinition;
import org.qipeng.dynamictask.service.DynamicTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={
		"classpath:context-basic.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestDynamicTask {
	
	@Autowired
	private DynamicTaskService dynamicTaskService;
	
	@Before
	public void before(){
		dynamicTaskService.deleteAll();
	}
	
	public void save(){
		TaskDefinition td = new TaskDefinition();
		td.setId(1L);
		td.setBeanClass("org.qipeng.dynamictask.support.DynamicExec1");
		td.setMethodName("test01");
		td.setStatus(true);
		td.setCron("*/1 * * * * ?");
		td.setName("任务1");
		dynamicTaskService.addTask(td);
		
		td = new TaskDefinition();
		td.setId(2L);
		td.setBeanClass("org.qipeng.dynamictask.support.DynamicExec1");
		td.setMethodName("test02");
		td.setStatus(false);
		td.setCron("*/2 * * * * ?");
		td.setName("任务2");
		dynamicTaskService.addTask(td);
		
		td = new TaskDefinition();
		td.setId(3L);
		td.setBeanName("dynamicExec2");
		td.setMethodName("test03");
		td.setStatus(true);
		td.setCron("*/3 * * * * ?");
		td.setName("任务3");
		dynamicTaskService.addTask(td);
		
		td = new TaskDefinition();
		td.setId(4L);
		td.setBeanName("dynamicExec2");
		td.setMethodName("test04");
		td.setStatus(false);
		td.setCron("*/4 * * * * ?");
		td.setName("任务3");
		dynamicTaskService.addTask(td);
		
	}
	
	@Test
	public void testSave() throws InterruptedException{
		save();
		Thread.sleep(9100);
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==2);
	}
	
	@Test
	public void testStart() throws InterruptedException{
		save();
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==2);
		
		dynamicTaskService.startTask(2L);
		
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==3);
		
		Thread.sleep(12000);
	}
	
	@Test
	public void testStop() throws InterruptedException{
		save();
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==2);
		
		dynamicTaskService.stopTask(1L);
		
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==1);
		
		Thread.sleep(7000);
	}
	
	@Test
	public void testDelete() throws InterruptedException{
		save();
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==2);
		
		dynamicTaskService.delete(1L,2L);
		
		Thread.sleep(12000);
		
		assertTrue(dynamicTaskService.getAllTaskCount()==2);
		assertTrue(dynamicTaskService.getStartTaskCount()==1);
		
	}
	
	@Test
	public void testDeleteAll() throws InterruptedException{
		save();
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==2);
		
		dynamicTaskService.deleteAll();
		
		Thread.sleep(5000);
		
		assertTrue(dynamicTaskService.getAllTaskCount()==0);
		assertTrue(dynamicTaskService.getStartTaskCount()==0);
		
	}
	
	@Test
	public void testUpdate() throws InterruptedException{
		save();
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==2);
		
		dynamicTaskService.stopTask(3L);
		
		Thread.sleep(5000);
		
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==1);
		
		TaskDefinition td = dynamicTaskService.get(1L);
		td.setCron("*/3 * * * * ?");
		dynamicTaskService.updateTask(td);
		
		Thread.sleep(7000);
		
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==1);
		
		td = dynamicTaskService.get(1L);
		td.setCron("0 15 10 * * ?");
		dynamicTaskService.updateTask(td);
		
	}
	
	@Test
	public void testExec2() throws InterruptedException{
		save();
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==2);
		
		dynamicTaskService.immediatelyExecute(1L);
		dynamicTaskService.immediatelyExecute(2L);
		dynamicTaskService.immediatelyExecute(3L);
		dynamicTaskService.immediatelyExecute(4L);
		
		assertTrue(dynamicTaskService.getAllTaskCount()==4);
		assertTrue(dynamicTaskService.getStartTaskCount()==2);
		
		Thread.sleep(5000);
	}
	
	
}
