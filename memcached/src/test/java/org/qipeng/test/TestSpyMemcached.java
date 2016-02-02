package org.qipeng.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

public class TestSpyMemcached {

	private static MemcachedClient memcachedClient;

	@BeforeClass
	public static void beforeClass() {
		ConnectionFactoryBuilder connectionFactoryBuilder = new ConnectionFactoryBuilder();
		connectionFactoryBuilder.setProtocol(Protocol.TEXT); // 使用二进制
		connectionFactoryBuilder.setOpTimeout(1000); // 超时时间（默认2500）
		connectionFactoryBuilder.setLocatorType(Locator.CONSISTENT); // 默认是ARRAY_MOD

		// 初始化MemcachedClient
		try {
			memcachedClient = new MemcachedClient(connectionFactoryBuilder.build(),
					AddrUtil.getAddresses("127.0.0.1:11211 10.128.202.183:11211"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

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
	public void test02() throws InterruptedException, ExecutionException{
		
		memcachedClient.delete("total");
		
		/*
		 * 如果key已经存在，则为key的值+1
		 * 如果key不存在，则为key初始化一个值1
		 */
		long val = memcachedClient.incr("total",1,1);
		assertEquals(1,val);
		
		// 注意counter的实际类型是String
		String str = (String) memcachedClient.get("total");
		assertEquals("1",str);
		
		val = memcachedClient.incr("total",3,-1);
		assertEquals(4,val);
		
		str = (String) memcachedClient.get("total");
		assertEquals("4",str);
		
		Future<Long> fVal = memcachedClient.asyncIncr("total",2);
		assertEquals(new Long(6),fVal.get());
		
		str = (String) memcachedClient.get("total");
		assertEquals("6",str);
		
		val = memcachedClient.decr("total",4,-1);
		assertEquals(2,val);
		
		str = (String) memcachedClient.get("total");
		assertEquals("2",str);
		
		fVal = memcachedClient.asyncDecr("total",2);
		assertEquals(new Long(0),fVal.get());
		
		str = (String) memcachedClient.get("total");
		assertEquals("0",str);
	}

}
