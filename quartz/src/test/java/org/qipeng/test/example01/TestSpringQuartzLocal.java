package org.qipeng.test.example01;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={
		"classpath:context-quartz-local.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSpringQuartzLocal {

	@Test
	public void test01() throws InterruptedException{
		
		Thread.sleep(60000);
		
	}
	
}
