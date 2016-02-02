package org.qipeng.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

@ContextConfiguration(locations={
		"classpath:context-basic.xml",
		"classpath:context-xmemcached.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSpringXMemcached {
	
	@Resource
	private MemcachedClient memcachedClient;

	@Test
	public void test01() throws TimeoutException, InterruptedException, MemcachedException{
		boolean result = memcachedClient.set("key1",60,"value1");
		assertTrue(result);
		
		String val = memcachedClient.get("key1");
		assertEquals(val,"value1");
		
		result = memcachedClient.replace("key2",60,"value2");
		assertFalse(result);
		
		result = memcachedClient.replace("key1",60,"value2");
		assertTrue(result);
		
		val = memcachedClient.get("key1");
		assertEquals(val,"value2");
		
		result = memcachedClient.set("key1",10,"value3");
		assertTrue(result);
		
		val = memcachedClient.get("key1");
		assertEquals(val,"value3");
		
		result = memcachedClient.delete("key2");
		assertFalse(result);
		
		result = memcachedClient.delete("key1");
		assertTrue(result);
		
		val = memcachedClient.get("key1");
		assertNull(val);
		
		result = memcachedClient.add("key1",5,"value1");
		assertTrue(result);
		
		result = memcachedClient.add("key1",5,"value2");
		assertFalse(result);
		
		Thread.sleep(5000);
		
		val = memcachedClient.get("key1");
		assertNull(val);
	}
	
	@Test
	public void test02() throws TimeoutException, InterruptedException, MemcachedException{
		
		memcachedClient.delete("total");
		
		/*
		 * 如果key存在，则加2，否则初始化一个值0(默认值)
		 */
		long val = memcachedClient.incr("total",2);
		assertEquals(0L,val);
		
		// 注意counter的实际类型是String
		String str = memcachedClient.get("total");
		assertEquals("0",str);
		
		val = memcachedClient.incr("total", 5);
		assertEquals(5,val);
		
		str = memcachedClient.get("total");
		assertEquals("5",str);
		
		val = memcachedClient.decr("total", 3);
		assertEquals(2,val);
		
		str = memcachedClient.get("total");
		assertEquals("2",str);
	}
	
}
