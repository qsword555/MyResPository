package org.qipeng.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@ContextConfiguration(locations={
		"classpath:context-redis.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJedis3 {
	
	@Resource
	private JedisPool pool;

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
