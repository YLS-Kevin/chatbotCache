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
import com.yls.app.entity.DialogType2;
import com.yls.app.persistence.mapper.ClientCacheMapper;
import com.yls.app.repository.AIKey;
import com.yls.app.repository.impl.RedisApiImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author huangsy
 * @date 2018年3月6日下午6:02:38
 */
@Service
@DisallowConcurrentExecution
public class CacheTerminalDialogTypeExecutorJob implements Executor {
	
	private final static Logger logger = Logger.getLogger(CacheTerminalDialogTypeExecutorJob.class);
	
	@Resource
	private ClientCacheMapper clientCacheMapper;

	@Resource
	private RedisApiImpl redisApiImpl;
	
	@Resource
	private JedisPool pool;
	
	private Jedis jedis;

	@Override
	public void execute() {

		logger.info("执行终端应用对话类型参照表缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("执行终端应用对话类型参照表缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");
		
	}
	
	/**
	 * 处理方法
	 */
	private void handle() {
		jedis = pool.getResource();
		this.cacheClientDialogType();
		this.cacheClientDialogTypeSelf();
		if(jedis!=null) {
			jedis.close();
		}
	}
	
	/**
	 * 缓存机器人拥有的对话库类型
	 */
	private void cacheClientDialogType() {
		List<DialogType2> clientDialogTypes = clientCacheMapper.findAllDialogTypeV2();
		for(DialogType2 clientDialogType : clientDialogTypes) {
			String cid = clientDialogType.getCid();
			String cid_m = clientDialogType.getCid_m();
			String dialogType = clientDialogType.getId_dt();
			String key = AIKey.CLIENT_DIALOGTYPE + ":" + cid + ":" + cid_m;
			
			String[] dts = dialogType.split(",");
			for(String dt : dts) {
				long stamp = getStamp(AIKey.REDIS_KEY_TTL);
				
				Map<String, String> map = new HashMap<String, String>();
				map.put("dialogType", dt);
				String value = new Gson().toJson(map);
				
				this.push2Redis(key, value, stamp);
			}
		}
	}

	/**
	 * 缓存机器人拥有的对话库类型，自定义对话库部分
	 */
	private void cacheClientDialogTypeSelf() {
		List<DialogType2> clientDialogTypes = clientCacheMapper.findAllDialogTypeSelfV2();
		for(DialogType2 clientDialogType : clientDialogTypes) {
			String cid = clientDialogType.getCid();
			String cid_m = clientDialogType.getCid_m();
			String dialogType = clientDialogType.getId_dt();
			String key = AIKey.CLIENT_DIALOGTYPE + ":" + cid + ":" + cid_m;
			
			long stamp = getStamp(AIKey.REDIS_KEY_TTL);
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("dialogType", dialogType);
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
