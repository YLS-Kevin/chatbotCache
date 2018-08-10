/**
 * 
 */
package com.yls.app.job;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.yls.app.entity.Account;
import com.yls.app.entity.DWordGroup;
import com.yls.app.persistence.mapper.WordCacheMapper;
import com.yls.app.repository.AIKey;
import com.yls.app.repository.impl.RedisApiImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author huangsy
 * @date 2018年5月11日上午11:09:25
 */
@Service
@DisallowConcurrentExecution
public class CacheDynaExecutorJob implements Executor {

	private final static Logger logger = Logger.getLogger(CacheDynaExecutorJob.class);

	@Resource
	private WordCacheMapper wordCacheMapper;

	@Resource
	private RedisApiImpl redisApiImpl;
	
	@Resource
	private JedisPool pool;
	
	private Jedis jedis;

	@Override
	public void execute() {

		logger.info("执行动态词类型缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("执行动态词类型缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");

	}

	/**
	 * 处理方法
	 */
	private void handle() {
		jedis = pool.getResource();
		this.cacheDyna();
		if(jedis!=null) {
			jedis.close();
		}
	}
	
	/**
	 * 缓存动态词
	 */
	private void cacheDyna() {
		
		List<Account> accounts = wordCacheMapper.findAllAccount();
		for(Account account : accounts) {
			String id = account.getId();
			
			List<DWordGroup> dWordGroups = wordCacheMapper.findAllWordTypeByAccountId(id);
			for(DWordGroup dWordGroup : dWordGroups) {
				String group_name = dWordGroup.getGroup_name();
				long stamp = this.getStamp(AIKey.REDIS_KEY_TTL);
				String key = AIKey.DYNA + ":" + id;
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("type", "(&"+group_name+"&)");
				map.put("stamp", String.valueOf(stamp));
				String value = new Gson().toJson(map);
				
				this.push2Redis(key, value, stamp);
			}
		}
	}
	
	/**
	 * 把对应键值对推送到redis服务器
	 * @param key
	 * @param value
	 */
	private void push2Redis(String key, String value, long stamp) {
		
		jedis.zadd(key, stamp, value);
//		jedis.expire(key, AIKey.REDIS_KEY_TTL);
		
	}
	
	/**
	 * 获取多少秒之后的时间戳
	 * @return
	 */
	private long getStamp(int after) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.SECOND, after);
		return calendar.getTime().getTime();
	}
	
}
