package org.qipeng.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.qipeng.memcached.support.SpyMemcachedClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

@ContextConfiguration(locations={
		"classpath:context-spymemcached.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSpringSpyMemcached {

	@Resource
	private MemcachedClient memcachedClient;
	
	@Resource
	private SpyMemcachedClient spyMemcachedClient;
	
	@Test
	public void test01() throws InterruptedException, ExecutionException {
		// 一般常用的操作方法
		// set,get,replace,add,delete
		// 使用add添加，如果memcache中已经存在,则不添加，返回false
		// 使用set添加，如果存在就覆盖，不存在就添加

		OperationFuture<Boolean> result = memcachedClient.set("key1", 60, "value1");
		assertTrue(result.get());

		String val = (String) memcachedClient.get("key1");
		assertEquals(val, "value1");

		result = memcachedClient.replace("key2", 60, "value2");
		assertFalse(result.get());

		result = memcachedClient.replace("key1", 60, "value2");
		assertTrue(result.get());

		val = (String) memcachedClient.get("key1");
		assertEquals(val, "value2");

		result = memcachedClient.set("key1", 10, "value3");
		assertTrue(result.get());

		val = (String) memcachedClient.get("key1");
		assertEquals(val, "value3");

		result = memcachedClient.delete("key2");
		assertFalse(result.get());

		result = memcachedClient.delete("key1");
		assertTrue(result.get());

		val = (String) memcachedClient.get("key1");
		assertNull(val);

		result = memcachedClient.add("key1", 5, "value1");
		assertTrue(result.get());

		result = memcachedClient.add("key1", 5, "value2");
		assertFalse(result.get());

		Thread.sleep(5000);

		val = (String) memcachedClient.get("key1");
		assertNull(val);
	}
	
	@Test
	public void test02() throws IOException, InterruptedException, ExecutionException{
		String name = spyMemcachedClient.get("name");
		assertNull(name);
		
		spyMemcachedClient.set("name",20,"zhangsan");
		
		name = spyMemcachedClient.get("name");
		assertEquals(name, "zhangsan");
		
		spyMemcachedClient.delete("name");
		spyMemcachedClient.delete("name");
		name = spyMemcachedClient.get("name");
		assertNull(name);
		
		boolean result = spyMemcachedClient.safeSet("name",10,"lisi");
		assertTrue(result);
		
		name = spyMemcachedClient.get("name");
		assertEquals(name, "lisi");
		
		result = spyMemcachedClient.safeDelete("namex");
		assertFalse(result);
		
		result = spyMemcachedClient.safeDelete("name");
		assertTrue(result);
		
		spyMemcachedClient.set("name1",20,"xxx");
		spyMemcachedClient.set("name2",20,"yyy");
		spyMemcachedClient.set("name3",20,"zzz");
		
		result = spyMemcachedClient.flush();
		assertTrue(result);
		
		name = spyMemcachedClient.get("name1");
		assertNull(name);
		name = spyMemcachedClient.get("name2");
		assertNull(name);
		name = spyMemcachedClient.get("name3");
		assertNull(name);
		
		result = spyMemcachedClient.safeSet("name1",20,"xxx");
		assertTrue(result);
		result = spyMemcachedClient.safeSet("name2",20,"yyy");
		assertTrue(result);
		result = spyMemcachedClient.safeSet("name3",20,"zzz");
		assertTrue(result);
		
		Map<String,String> map = spyMemcachedClient.getBulk("name1","name2","name3");
		assertTrue(map.size()==3);
		
		map = spyMemcachedClient.getBulk("name1","name2","name4");
		assertTrue(map.size()==2);
		
		result = spyMemcachedClient.flush();
		assertTrue(result);
		
		String key = "counter";

		/*
		 * 如果key存在，则 为key的值+1，不存在则初始化为1
		 */
		long val = spyMemcachedClient.incr(key,1,1);
		assertEquals(1L,val);
		
		// 注意counter的实际类型是String
		String str = spyMemcachedClient.get(key);
		assertEquals("1",str);
		
		val = spyMemcachedClient.incr(key,5, 1);
		assertEquals(6,val);
		
		str = spyMemcachedClient.get(key);
		assertEquals("6",str);
		
		Future<Long> sum = spyMemcachedClient.asyncIncr(key,1);
		assertEquals(new Long(7),sum.get());
		
		str = spyMemcachedClient.get(key);
		assertEquals("7",str);

		val = spyMemcachedClient.decr(key,2,1);
		assertEquals(5,val);
		
		str = spyMemcachedClient.get(key);
		assertEquals("5",str);
		
		sum = spyMemcachedClient.asyncDecr(key,5);
		assertEquals(new Long(0),sum.get());
		
		str = spyMemcachedClient.get(key);
		assertEquals("0",str);
		
		
	}
	
}
