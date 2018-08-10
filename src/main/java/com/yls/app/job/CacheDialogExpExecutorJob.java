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
import com.yls.app.entity.DialogExp;
import com.yls.app.persistence.mapper.DialogCacheMapper;
import com.yls.app.repository.AIKey;
import com.yls.app.repository.impl.RedisApiImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 缓存异常应答
 * @author huangsy
 * @date 2018年3月8日上午9:42:13
 */
@Service
@DisallowConcurrentExecution
public class CacheDialogExpExecutorJob implements Executor {

	private final static Logger logger = Logger.getLogger(CacheDialogExpExecutorJob.class);
	
	@Resource
	private DialogCacheMapper dialogCacheMapper;

	@Resource
	private RedisApiImpl redisApiImpl;
	
	@Resource
	private JedisPool pool;
	
	private Jedis jedis;

	@Override
	public void execute() {

		logger.info("执行异常应答库缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("执行异常应答库缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");
		
	}
	
	/**
	 * 处理方法
	 */
	private void handle() {
		jedis = pool.getResource();
		this.cacheDialogExp();
		if(jedis!=null) {
			jedis.close();
		}
	}
	
	/**
	 * 缓存终端异常应答
	 */
	private void cacheDialogExp() {
		List<DialogExp> dialogExps = dialogCacheMapper.findAllDialogExp();
		for(DialogExp dialogExp : dialogExps) {
			String cid = dialogExp.getCid();
			String answer = dialogExp.getAnswer();
			String stype = dialogExp.getStype();//异常类型，类型：1-无答案时，2-接口异常时，3-系统出错时
			
			long stamp = getStamp(AIKey.REDIS_KEY_TTL);
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("pattern", AIKey.EXP_ANSWER);
			map.put("template", answer);
			map.put("type", "0");
			map.put("that", "");
//			map.put("stamp", String.valueOf(stamp));

			String key = AIKey.EXP_ANSWER + ":" + cid + ":" + stype;
			String value = new Gson().toJson(map);
			
			this.push2Redis(key, value, stamp);
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
