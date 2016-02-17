package org.qipeng.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.qipeng.scheduler.dynamic.DynamicTaskService;
import org.qipeng.scheduler.dynamic.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={
		"classpath:context-basic.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSpringDynamicTask {
	
	
	@Autowired
	private DynamicTaskService dynamicTaskService;
	
	@Test
	public void before(){
		dynamicTaskService.deleteAll();
	}
	
	@Test
	public void testAddTask() throws InterruptedException{
		TaskDefinition bean = new TaskDefinition();
		bean.setId(1L);
		bean.setName("job1");
		bean.setStatus(Boolean.TRUE);
		bean.setBeanName("springDynamicJob");
		bean.setMethodName("testJob01");
		bean.setCron("*/2 * * * * ?");
		bean.setDescription("第一个测试定时任务！");
		dynamicTaskService.addTask(bean);
		
		Thread.sleep(5000);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(1),dynamicTaskService.getStartTaskCount());
		assertNotNull(dynamicTaskService.get(1L));
		
		Thread.sleep(5000);
	}
	
	@Test
	public void testUpdateTask() throws InterruptedException{
		TaskDefinition bean = new TaskDefinition();
		bean.setId(1L);
		bean.setName("job1");
		bean.setStatus(Boolean.TRUE);
		bean.setBeanName("springDynamicJob");
		bean.setMethodName("testJob01");
		bean.setCron("*/3 * * * * ?");
		bean.setDescription("第一个测试定时任务！");
		dynamicTaskService.addTask(bean);
		
		Thread.sleep(9000);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(1),dynamicTaskService.getStartTaskCount());
		
		TaskDefinition def = dynamicTaskService.get(1L);
		assertNotNull(def);
		
		def.setCron("*/1 * * * * ?");
		dynamicTaskService.updateTask(def);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(1),dynamicTaskService.getStartTaskCount());
		
		Thread.sleep(5000);
	}
	
	@Test
	public void testDelete() throws InterruptedException{
		TaskDefinition bean = new TaskDefinition();
		bean.setId(1L);
		bean.setName("job1");
		bean.setStatus(Boolean.TRUE);
		bean.setBeanName("springDynamicJob");
		bean.setMethodName("testJob01");
		bean.setCron("*/1 * * * * ?");
		bean.setDescription("第一个测试定时任务！");
		dynamicTaskService.addTask(bean);
		
		bean = new TaskDefinition();
		bean.setId(2L);
		bean.setName("job2");
		bean.setStatus(Boolean.TRUE);
		bean.setBeanName("springDynamicJob");
		bean.setMethodName("testJob02");
		bean.setCron("*/2 * * * * ?");
		bean.setDescription("第二个测试定时任务！");
		dynamicTaskService.addTask(bean);
		
		assertEquals(new Long(2),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(2),dynamicTaskService.getStartTaskCount());
		
		for(TaskDefinition tk : dynamicTaskService.findAll()){
			System.out.println(tk);
		}
		
		Thread.sleep(10000);
		
		dynamicTaskService.delete(1L,2L);
		
		Thread.sleep(5000);
		
		assertEquals(new Long(0),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(0),dynamicTaskService.getStartTaskCount());
	}
	
	@Test
	public void testStartTask() throws InterruptedException{
		TaskDefinition bean = new TaskDefinition();
		bean.setId(1L);
		bean.setName("job1");
		bean.setStatus(Boolean.FALSE);
		bean.setBeanName("springDynamicJob");
		bean.setMethodName("testJob01");
		bean.setCron("*/1 * * * * ?");
		bean.setDescription("第一个测试定时任务！");
		dynamicTaskService.addTask(bean);
		
		Thread.sleep(3000);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(0),dynamicTaskService.getStartTaskCount());
		
		TaskDefinition def = dynamicTaskService.get(1L);
		assertNotNull(def);
		
		dynamicTaskService.startTask(1L);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(1),dynamicTaskService.getStartTaskCount());
		
		Thread.sleep(2000);
	}
	
	@Test
	public void testStopTask() throws InterruptedException{
		TaskDefinition bean = new TaskDefinition();
		bean.setId(1L);
		bean.setName("job1");
		bean.setStatus(Boolean.TRUE);
		bean.setBeanName("springDynamicJob");
		bean.setMethodName("testJob01");
		bean.setCron("*/1 * * * * ?");
		bean.setDescription("第一个测试定时任务！");
		dynamicTaskService.addTask(bean);
		
		Thread.sleep(3000);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(1),dynamicTaskService.getStartTaskCount());
		
		TaskDefinition def = dynamicTaskService.get(1L);
		assertNotNull(def);
		
		dynamicTaskService.stopTask(1L);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(0),dynamicTaskService.getStartTaskCount());
		
		Thread.sleep(2000);
	}
	
	@Test
	public void testExecTask() throws InterruptedException{
		TaskDefinition bean = new TaskDefinition();
		bean.setId(1L);
		bean.setName("job1");
		bean.setStatus(Boolean.FALSE);
		bean.setBeanName("springDynamicJob");
		bean.setMethodName("testJob01");
		bean.setCron("*/3 * * * * ?");
		bean.setDescription("第一个测试定时任务！");
		dynamicTaskService.addTask(bean);
		
		Thread.sleep(3000);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(0),dynamicTaskService.getStartTaskCount());
		
		dynamicTaskService.immediatelyExecute(1L);
		dynamicTaskService.immediatelyExecute(1L);
		
		Thread.sleep(2000);
		
		dynamicTaskService.startTask(1L);
		
		assertEquals(new Long(1),dynamicTaskService.getAllTaskCount());
		assertEquals(new Long(1),dynamicTaskService.getStartTaskCount());
		
		dynamicTaskService.immediatelyExecute(1L);
		
		Thread.sleep(6000);
		
		dynamicTaskService.immediatelyExecute(1L);
		
	}
}
