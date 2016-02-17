package org.qipeng.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qipeng.test.vo.UserVo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SerializationUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

@ContextConfiguration(locations={
		"classpath:context-redis.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestJedis4 {
	
	@Resource
	private JedisPool jedisPool;
	
	private Jedis jedis;
	
	@Before
	public void before(){
		jedis = jedisPool.getResource();
	}
	
	@After
	public void after(){
		jedis.close();
	}
	
	@Test
	public void testBase() throws InterruptedException{
		//清空
		jedis.flushDB();
		
		//set的时候key如果存在，会直接覆盖原来的数据
		String result = jedis.set("name","Tom");
		assertEquals("OK",result);
		
		result = jedis.set("name","Jetty");
		assertEquals("OK",result);
		assertEquals("Jetty",jedis.get("name"));
		
		jedis.flushDB();

		//XX代表覆盖key如果存在则覆盖，NX代表如果存在就不插入
		//EX代表时间单位是秒,PX代表时间单位是毫秒
		jedis.set("name","张三","XX","PX",2000);
		Thread.sleep(2101);
		assertNull(jedis.get("name"));
		
		jedis.flushDB();
		
		jedis.set("name","张三");
		result=jedis.set("name","李四","XX");
		assertEquals("OK",result);
		assertEquals("李四",jedis.get("name"));
		
		jedis.flushDB();
		
		jedis.set("name","张三");
		result=jedis.set("name","李四","NX");
		assertNull(result);
		assertEquals("张三",jedis.get("name"));
		
		jedis.flushDB();
		
		//setex 单位秒
		result=jedis.setex("name",2,"ttt");
		assertEquals("OK",result);
		Thread.sleep(2101);
		assertNull(jedis.get("name"));
		
		jedis.flushDB();
		
		//key存在append到已经有的value之后 ，不存在 等同于jedis.set("name","cat");
		jedis.append("name","tom");
		assertEquals("tom",jedis.get("name"));
		
		jedis.append("name","cat");
		assertEquals("tomcat",jedis.get("name"));
		
		jedis.flushDB();
		
		//mset相当于 jedis.set("name1","aaa"); jedis.set("name2","bbb"); 
		jedis.mset("name1", "aaa", "name2", "bbb"); 
		List<String> list = jedis.mget("name1","name2");
		assertEquals(2,list.size());
		
		long val = jedis.del("name1","name2");  //删除多个
		assertEquals(2,val);
		
	}
	
	@Test
	public void testKey() throws InterruptedException{
		//清空数据 
		String result = jedis.flushDB();
		assertEquals("OK",result);
		
		//判断key是否存在
		assertFalse(jedis.exists("key"));
		jedis.set("key", "values");  
		assertTrue(jedis.exists("key"));
		
		//为key设置失效时间
		jedis.expire("key",2);
		Thread.sleep(2100);
		assertNull(jedis.get("key"));
		
		jedis.flushDB();
		jedis.set("key", "values");  
		//expireAt(key,longtime)可以使key在指定的一个时间点失效
		jedis.expireAt("key", System.currentTimeMillis()+1000000);
		
		//获取该数据的存活时间
		assertTrue(jedis.ttl("key")>999);
		
		//取消带有存活时间的key，变为永久
		//返回1代表处理成功，返回0代表处理失败（包括key不存在，和key本身就是持久的）
		assertEquals(new Long(1),jedis.persist("key"));
		assertEquals(new Long(0),jedis.persist("key"));
		assertEquals(new Long(0),jedis.persist("key1"));
		
		jedis.flushDB();
		
		//对key的模糊查询
		jedis.mset("key1","val1","key2","val2","ke3","va3");
		Set<String> set = jedis.keys("*");
		assertEquals(3,set.size());
		
		set = jedis.keys("key*");
		assertEquals(2,set.size());
		
		//重命名一个key，如果newkey存在，将会被覆盖
		result = jedis.rename("key1", "newKey");  
		assertEquals("OK",result);
		assertEquals("val1",jedis.get("newKey"));
		
		jedis.randomKey();//随机生成一个String的key
		jedis.randomBinaryKey();//随机生成一个byte[]的key
		
	}
	
	@Test
	public void testGet(){
		jedis.flushDB();  //清空当前DB
		 // 获取并更改数据  
        jedis.set("foo", "foo update");  
        String result = jedis.getSet("foo", "foo modify"); 
        assertEquals("foo update",result);
        assertEquals("foo modify",jedis.get("foo"));
        // 截取value的值    ,-1代表截取到末尾（不回改变原来的value）
        assertEquals("modify",jedis.getrange("foo",4,-1));
        assertEquals("foo modify",jedis.get("foo"));
        
        jedis.flushAll();//清空所有DB
	}
	

	@Test
	public void testObject(){
		//测试javabean类型的存储，需要对javabean进行序列化和反序列化,可以使用Spring提供的SerializationUtils来完成
		UserVo vo = new UserVo();  //需要实现Serializable接口
		vo.setId(1);
		vo.setName("张三");
		vo.setBirthday(new Date());
		
		//使用Jedis操作：key、value都需要转成byte[]字节数组。
		String key = "user."+vo.getId();
		String result = jedis.set(key.getBytes(),SerializationUtils.serialize(vo));
		assertEquals("OK",result);
		
		UserVo vo2 = (UserVo) SerializationUtils.deserialize(jedis.get(key.getBytes()));
		System.out.println(vo2.getId()+" "+vo2.getName()+" "+vo2.getBirthday());
		
		long num = jedis.del(key.getBytes());
		assertEquals(1,num);
	}
	
	@Test
	public void testList(){
		// 开始前，先移除所有的内容  
        jedis.del("messages");  
        jedis.rpush("messages", "Hello how are you?");  //右边入队
        jedis.lpush("messages", "Fine thanks. I'm having fun with redis.");  //左边入队
        jedis.rpush("messages", "I should look into this NOSQL thing ASAP");  //右边入队
        
        //获取长度
        assertEquals(new Long(3),jedis.llen("messages"));
        
        // 取出所有数据jedis.lrange是按范围取出，  
        // 第一个是key，第二个是起始位置，第三个是结束位置-1表示到末尾
        List<String> values = jedis.lrange("messages", 0, -1);  
        assertTrue(values.get(0).contains("Fine"));
        assertTrue(values.get(1).contains("Hello"));
        assertTrue(values.get(2).contains("I should"));
        
        assertTrue(jedis.rpop("messages").contains("I should"));  //右边出栈
        assertTrue(jedis.lpop("messages").contains("Fine"));  //左边出栈
        
        assertEquals(new Long(1),jedis.llen("messages"));  //出栈了两个，还剩下一个
        
        jedis.flushDB();
        
        jedis.lpush("lists", "1");  
        jedis.rpush("lists", "3");  
        jedis.rpush("lists", "4");  
        jedis.lpush("lists", "2");  
        
        //排序
        values = jedis.sort("lists");
        assertEquals("1",values.get(0));
        assertEquals("2",values.get(1));
        assertEquals("3",values.get(2));
        assertEquals("4",values.get(3));
        
        //修改列表中单个值  
        String result = jedis.lset("lists",3, "5");
        assertEquals("OK",result);
        
        //获取列表指定下标的值
        assertEquals("5",jedis.lindex("lists",3));
        
        // 删除列表指定下标的值  (返回删除的数据的条数)
        assertEquals(new Long(1),jedis.lrem("lists", 0, "1"));
        
        // 对list进行剪裁,剪掉0-1（包含），也就是删掉2
        result = jedis.ltrim("lists",0,1);  
        assertEquals("OK",result);
        
        assertEquals(new Long(2),jedis.llen("lists"));
        
        // 整个列表值  
        values = jedis.lrange("lists", 0, -1);
        assertEquals("2",values.get(0));
        assertEquals("3",values.get(1));
	}
	
	@Test
	public void testSet(){
		jedis.flushDB();
		
		//添加元素，返回添加的数量
	    jedis.sadd("fruit", "apple");  
	    jedis.sadd("fruit", "pear", "watermelon");   //可一次添加多个
	    long val = jedis.sadd("fruit", "apple");        //元素不能重复
	    assertEquals(0,val);
	    
	    //集合的长度使用scard获取
	    assertEquals(new Long(3),jedis.scard("fruit"));
	    
	    //遍历集合
	    Set<String> fruit = jedis.smembers("fruit"); 
	    assertTrue(fruit.contains("apple"));
	    assertTrue(fruit.contains("pear"));
	    assertTrue(fruit.contains("watermelon"));
	    
	    //移除元素(返回移除的数量，可以移除多个)
	    val = jedis.srem("fruit","watermelon"); 
	    assertEquals(1,val);
	    
	    //是否包含
	    assertFalse(jedis.sismember("fruit","watermelon"));
	    
	    //集合的交运算(sinter)、差集(sdiff)、并集(sunion)
	    jedis.sadd("num1", "1", "2","3");
	    jedis.sadd("num2", "1", "2","4");
	    jedis.sadd("num3", "1", "2","3","4","5");
	    
	    Set<String> sunion = jedis.sunion("num1", "num2");
	    assertEquals(4,sunion.size());
	    assertTrue(sunion.contains("1"));
	    assertTrue(sunion.contains("2"));
	    assertTrue(sunion.contains("3"));
	    assertTrue(sunion.contains("4"));
	    
	    Set<String> sinter = jedis.sinter("num1", "num2");
	    assertEquals(2,sinter.size());
	    assertTrue(sinter.contains("1"));
	    assertTrue(sinter.contains("2"));
	    
	    Set<String> sdiff = jedis.sdiff("num3", "num2");  //注意元素多的一定在前面
	    assertEquals(2,sdiff.size());
	    assertTrue(sdiff.contains("3"));
	    assertTrue(sdiff.contains("5"));
	    
	    //随机出栈一个元素
	    assertNotNull(jedis.spop("num3"));
	    assertEquals(new Long(4),jedis.scard("num3"));
	    
	}
	
	@Test
	public void testHashMap(){
		jedis.flushAll();
		
		Map<String, String>  map = new HashMap<String, String>();  
		map.put("key1","value1");
		map.put("key2","value2");
		map.put("key3","value3");
		
		String result = jedis.hmset("testMap",map);
		assertEquals("OK",result);
		
		//根据key集合获取value集合
	    List<String> values = jedis.hmget("testMap", "key1", "key2","key3");
	    assertEquals(3,values.size());
	    
	    assertEquals(new Long(3),jedis.hlen("testMap"));  //获得map的长度
	    
	    assertFalse(jedis.hexists("testMap","key4"));   //指定key的map是否包含
	    
	    Set<String> set = jedis.hkeys("testMap");   //获取key集合，set集合
	    assertEquals(3,set.size());
	    assertTrue(set.contains("key1"));
	    assertTrue(set.contains("key2"));
	    assertTrue(set.contains("key3"));
	    
	    values = jedis.hvals("testMap");       //获取value集合，list集合
	    assertEquals(3,values.size());
	    assertTrue(values.contains("value1"));
	    assertTrue(values.contains("value2"));
	    assertTrue(values.contains("value3"));
	    
	    assertEquals(new Long(1),jedis.hdel("testMap", "key2"));  //删除指定key的map中的元素
	    assertEquals(new Long(2),jedis.hlen("testMap"));  //获得map的长度
	    
	    Map<String,String> returnMap = jedis.hgetAll("testMap");  //获取map
	    assertEquals(2,returnMap.size());
	}
	
	
	 @Test
	 public void testUnUsePipeline() { 
		 	jedis.flushDB();  
	        long start = System.currentTimeMillis();  
	  
	        for (int i = 0; i < 10000; i++) {  
	            jedis.set("age1" + i, i + "");  
	            jedis.get("age1" + i);// 每个操作都发送请求给redis-server  
	        }  
	        long end = System.currentTimeMillis();  
	  
	        //unuse pipeline cost:893ms
	        long unUseCostTime = end - start;
	        System.out.println("unuse pipeline cost:" + unUseCostTime + "ms");  
	        
	        jedis.flushDB();  
	        
	        start = System.currentTimeMillis();  
	        
	        //使用管道将命令打包一起发
	        Pipeline p = jedis.pipelined();  
	        for (int i = 0; i < 10000; i++) {  
	            p.set("age2" + i, i + "");  
	            p.get("age2" + i);  
	        }  
	        p.sync();// 这段代码获取所有的response  
	        
	        end = System.currentTimeMillis();  
	        
	        //use pipeline cost:277ms
	        long useCostTime = end - start;
	        System.out.println("use pipeline cost:" + useCostTime + "ms");  
	        
	        assertTrue(unUseCostTime>useCostTime);
	 }  
	 
	 @Test
	 public void testSortedSet(){
		 jedis.flushDB();  
		 
		 //根据第二个参数进行排序，元素仍然不可重复，新的会覆盖旧的
		 jedis.zadd("testSrotSet",1.01,"aaa");
		 jedis.zadd("testSrotSet",1.02,"bbb");
		 jedis.zadd("testSrotSet",1.03,"aaa");   //将会覆盖原来的权重
		 jedis.zadd("testSrotSet",1.04,"ccc");
		 
		 Map<String,Double> map = new HashMap<String,Double>();
		 map.put("ddd",1.01);
		 map.put("eee",1.05);
		 
		 //添加元素方式二
		 jedis.zadd("testSrotSet", map);
		 
		 assertEquals(new Long(5),jedis.zcard("testSrotSet"));  //获得集合长度
		 
		 assertEquals(new Double(1.03),jedis.zscore("testSrotSet", "aaa"));//获得权重
		 
		 //获得[1.02,1.03]这个权值内的元素的个数，闭区间
		 assertEquals(new Long(2),jedis.zcount("testSrotSet",1.02, 1.03));
		
		 //遍历集合
		 Set<String> zrange = jedis.zrange("testSrotSet", 0,-1);
		 assertEquals(5,zrange.size());
		 
		 //倒着遍历集合
		 Set<String> zrevrange = jedis.zrevrange("testSrotSet", 0,1);
		 assertTrue(zrevrange.contains("eee"));
		 assertTrue(zrevrange.contains("ccc"));
		 
		 //遍历权重在[]闭区间内的元素
		 Set<String> zrangeByScore = jedis.zrangeByScore("testSrotSet",1.01,1.03);
		 assertEquals(3,zrangeByScore.size());
		 
		 //删除元素
		 assertEquals(new Long(1),jedis.zrem("testSrotSet","aaa"));
		 
		 //前开后闭，只会删除1.04
		 assertEquals(new Long(1),jedis.zremrangeByScore("testSrotSet",1.03,1.04));
		 
		 
	 }
	
}
