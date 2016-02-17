package org.qipeng.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.qipeng.scheduler.support.ThreadUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={
		"classpath:context-spring-scheduler-annotation.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSpringSchedulerBaseAnnotation {

	@Test
	public void test01(){
		ThreadUtil.sleep(20000);
	}
	
}
