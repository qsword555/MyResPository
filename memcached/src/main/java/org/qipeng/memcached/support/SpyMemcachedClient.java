package org.qipeng.memcached.support;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;

import net.spy.memcached.MemcachedClient;

/**
 * 对SpyMemcachedClient进一步的封装
 * @author peng.qi
 *
 */
@SuppressWarnings("unchecked")
public class SpyMemcachedClient implements DisposableBean{

	private MemcachedClient memcachedClient;
	
	private long shutdownTimeout = 2500;

	private long updateTimeout = 2500;
	
	/**
	 * Get方法
	 * 如果未取到数据，则返回null
	 */
	public <T> T get(String key){
		return (T) memcachedClient.get(key);
		
	}
	
	/**
	 * GetBulk方法
	 * 如果未取到数据，则返回null
	 * 根据key，将key存在的数据封装为map并返回
	 */
	public <T> Map<String,T> getBulk(String ...keys){
		return (Map<String, T>) memcachedClient.getBulk(keys);
	}

	/**
	 * 异步Set方法, 不考虑执行结果.
	 */
	public void set(String key, int expiredTime, Object value) {
		memcachedClient.set(key, expiredTime, value);
	}
	
	/**
	 * 安全的Set方法, 保证在updateTimeout秒内返回执行结果, 否则返回false并取消操作.
	 */
	public boolean safeSet(String key, int expiration, Object value) {
		Future<Boolean> future = memcachedClient.set(key, expiration, value);
		try {
			return future.get(updateTimeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			future.cancel(false);
		}
		return false;
	}
	
	/**
	 * 异步 Delete方法, 不考虑执行结果.
	 */
	public void delete(String key) {
		memcachedClient.delete(key);
	}
	
	/**
	 * 安全的Delete方法, 保证在updateTimeout秒内返回执行结果, 否则返回false并取消操作.
	 */
	public boolean safeDelete(String key) {
		Future<Boolean> future = memcachedClient.delete(key);
		try {
			return future.get(updateTimeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			future.cancel(false);
		}
		return false;
	}
	
	/**
	 * Incr方法.（分布式计数器）
	 * 如果key存在，则为key的值增加by
	 * 如果key不存在，则为key初始化一个值defaultValue
	 */
	public long incr(String key, int by, long defaultValue) {
		return memcachedClient.incr(key, by, defaultValue);
	}

	/**
	 * Decr方法.（分布式计数器）
	 * 如果key存在，则为key的值减少by
	 * 如果key不存在，则为key初始化一个值defaultValue
	 */
	public long decr(String key, int by, long defaultValue) {
		return memcachedClient.decr(key, by, defaultValue);
	}

	/**
	 * 异步Incr方法, 不支持默认值, 若key不存在返回-1.
	 */
	public Future<Long> asyncIncr(String key, int by) {
		return memcachedClient.asyncIncr(key, by);
	}

	/**
	 * 异步Decr方法, 不支持默认值, 若key不存在返回-1.
	 */
	public Future<Long> asyncDecr(String key, int by) {
		return memcachedClient.asyncDecr(key, by);
	}
	
	/**
	 * 刷新memcached，会清空所有的缓存
	 * @return
	 */
	public boolean flush() {  
        try {
			return memcachedClient.flush().get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}  
        return false;
    }  
	
	/**
	 * 查看memcachedClient状态
	 * @param stream
	 * @throws IOException
	 */
	 public void printStats(OutputStream stream) throws IOException {  
	        Map<SocketAddress, Map<String, String>> statMap =   
	        		memcachedClient.getStats();  
	        if (stream == null) {  
	            stream = System.out;  
	        }  
	        StringBuffer buf = new StringBuffer();  
	        Set<SocketAddress> addrSet = statMap.keySet();  
	        Iterator<SocketAddress> iter = addrSet.iterator();  
	        while (iter.hasNext()) {  
	            SocketAddress addr = iter.next();  
	            buf.append(addr.toString() + "/n");  
	            Map<String, String> stat = statMap.get(addr);  
	            Set<String> keys = stat.keySet();  
	            Iterator<String> keyIter = keys.iterator();  
	            while (keyIter.hasNext()) {  
	                String key = keyIter.next();  
	                String value = stat.get(key);  
	                buf.append("  key=" + key + ";value=" + value + "/n");  
	            }  
	            buf.append("/n");  
	        }  
	        stream.write(buf.toString().getBytes());  
	        stream.flush();  
	    }  
	
	

	@Override
	public void destroy() throws Exception {
		if (memcachedClient != null) {
			memcachedClient.shutdown(shutdownTimeout, TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 * 获得原生的memcached进行操作
	 * @return
	 */
	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}

	public void setMemcachedClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}

	public void setUpdateTimeout(long updateTimeout) {
		this.updateTimeout = updateTimeout;
	}

	public void setShutdownTimeout(long shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}
	
}
