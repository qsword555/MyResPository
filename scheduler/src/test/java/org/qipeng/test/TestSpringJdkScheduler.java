package org.qipeng.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.qipeng.scheduler.support.ThreadUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={
		"classpath:context-jdk-scheduler.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSpringJdkScheduler {

	@Test
	public void test01(){
		ThreadUtil.sleep(60000);
	}
	
}
