package org.qipeng.test.example01;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestClusterClientB {

	public static void main(String[] args) {
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("context-quartz-cluster.xml");
		System.out.println("clientB启动完毕，等待job执行！");
		try {
			Thread.sleep(600000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ctx.close();
		
	}
	
}
