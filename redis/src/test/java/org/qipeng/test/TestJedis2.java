package org.qipeng.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class TestJedis2 {
	
	private static JedisPool pool;
	
	@BeforeClass
	public static void beforeClass(){
		JedisPoolConfig config = new JedisPoolConfig(); 
		
		config.setMaxIdle(20);  //最大空闲数
		config.setMinIdle(5);  //最小空弦数
		config.setMaxTotal(100);  //最大连接数
		config.setMaxWaitMillis(3000);  //获取连接时的最大等待毫秒数
		
		//创建连接时，检测连接是否成功
		config.setTestOnCreate(false);
		//使用连接时，检测连接是否成功 
		config.setTestOnBorrow(true);
		//返回连接时，检测连接是否成功  
		config.setTestOnReturn(false);
		
		String host = "127.0.0.1";
		int port = 6379;
		int timeout = 3000;
		
	    pool = new JedisPool(config,host,port,timeout); 
	    
	   //String password = "123";
	   //如果使用密码的话，需要在redis.conf配置文件中配置 requirepass qp123
	   //pool = new JedisPool(config,host,port, timeout,password); 
	    
	}

	@Test
	public void test01(){
		//实例化jedis
		Jedis jedis = pool.getResource();
		
		//存储键值对，成功返回"OK"
		String result = jedis.set("country","china");
		assertEquals("OK",result);
		
		//获取
		String country = jedis.get("country");
		assertEquals("china",country);
		
		//删除,返回删除条数
		Long val = jedis.del("country");
		assertEquals(new Long(1),val);
		
		//删除,返回删除条数
		val = jedis.del("country");
		assertEquals(new Long(0),val);
		
		//获取，未获取到，返回null
		country = jedis.get("country");
		assertNull(country);
		
		//关闭jedis
		jedis.close();
	}
	
}
