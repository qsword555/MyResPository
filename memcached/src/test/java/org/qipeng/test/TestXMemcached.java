package org.qipeng.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.BeforeClass;
import org.junit.Test;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.TextCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.transcoders.SerializingTranscoder;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class TestXMemcached {
	
	private static MemcachedClient memcachedClient;

	@BeforeClass
	public static void beforeClass(){
		//使用MemcachedClientBuilder构建MemcachedClient
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses("127.0.0.1:11211 10.128.202.183:11211"),    //memcached服务器地址
                new int[] { 1,2}        //权重，权重越高，该memcached节点存储的数据将越多
				);
		
		 //设置连接池大小，即客户端个数
        builder.setConnectionPoolSize(5);
        //宕机报警
        builder.setFailureMode(true);
        // 使用二进制文件
        builder.setCommandFactory(new TextCommandFactory());
        //builder.setCommandFactory(new BinaryCommandFactory());
        //设置一致性哈希
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        //设置转码器
        builder.setTranscoder(new SerializingTranscoder());
        
        try {
			memcachedClient = builder.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test01() throws TimeoutException, InterruptedException, MemcachedException{
		//一般常用的操作方法
		//set,get,replace,add,delete
		//使用add添加，如果memcache中已经存在,则不添加，返回false
		//使用set添加，如果存在就覆盖，不存在就添加
		
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
		 * 如果key存在，则加2，否则初始化一个值0
		 */
		long val = memcachedClient.incr("total",2,0);
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
