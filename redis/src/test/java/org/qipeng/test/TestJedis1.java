package org.qipeng.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import redis.clients.jedis.Jedis;

public class TestJedis1 {

	@Test
	public void test01(){
		//实例化jedis
		Jedis jedis = new Jedis("127.0.0.1",6379);
		
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
