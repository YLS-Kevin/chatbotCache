/**
 * 
 */
package com.yls.app.job;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.yls.app.entity.DialogMan;
import com.yls.app.entity.DialogType;
import com.yls.app.persistence.mapper.DialogCacheMapper;
import com.yls.app.repository.AIKey;
import com.yls.app.repository.impl.RedisApiImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author huangsy
 * @date 2018年3月29日下午4:57:03
 */
@Service
@DisallowConcurrentExecution
public class CacheSentenceExecutorJob implements Executor {

	private final static Logger logger = Logger.getLogger(CacheSentenceExecutorJob.class);
	
	@Resource
	private DialogCacheMapper dialogCacheMapper;

	@Resource
	private RedisApiImpl redisApiImpl;
	
	@Resource
	private JedisPool pool;
	
	private Jedis jedis;
	
	private long count;

	@Override
	public void execute() {
		
		count = 0;
		logger.info("执行句式缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("句式个数：" + count + "，执行句式缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");
		
	}
	
	/**
	 * 处理方法
	 */
	private void handle() {
		jedis = pool.getResource();
		// 缓存每一种类型的对话库
		List<DialogType> dialogTypes = dialogCacheMapper.findAllDialogType();
		for (DialogType dt : dialogTypes) {
			String id_dt = dt.getId();
			this.cacheSentence(id_dt);
		}
		if(jedis!=null) {
			jedis.close();
		}
	}
	
	/**
	 * 缓存句式
	 */
	private void cacheSentence(String id_dt) {
		List<DialogMan> dialogMans = dialogCacheMapper.findAllDialogMan(id_dt);
		for(DialogMan dialogMan : dialogMans) {
			String dt = dialogMan.getId_dt();
			List<String> patterns = this.sentenceCombinPatterns(dialogMan); 
			for(String pattern : patterns) {
				
				//对话id
				String id_d = dialogMan.getId_d();
				//统一时间戳
				long stamp = getStamp(AIKey.REDIS_KEY_TTL);
				//缓存固定对话和接口对话
				Map<String, String> map = new HashMap<String, String>();
				map.put("sentence", pattern);
				map.put("id_d", id_d);
				String value = new Gson().toJson(map);
				
				String[] keywords = pattern.split("\\(@\\)");
				for(String keyword : keywords) {
					String key = AIKey.SENTENCE + ":" + dt + ":" + keyword;
					this.push2Redis(key, value, stamp);
				}
			}
		}
	}
	
	/**
	 * 拼接pattern
	 * @param tdi
	 * @return
	 */
	private List<String> sentenceCombinPatterns(DialogMan tdi) {
//		long startTime = System.currentTimeMillis();
		String k1type = tdi.getAword1type();
		String k2type = tdi.getAword2type();
		String k3type = tdi.getAword3type();
		String k4type = tdi.getAword4type();
		String k5type = tdi.getAword5type();

		String k1 = tdi.getAword1();
		String k2 = tdi.getAword2();
		String k3 = tdi.getAword3();
		String k4 = tdi.getAword4();
		String k5 = tdi.getAword5();

		String aword1near = tdi.getAword1near();
		String aword2near = tdi.getAword2near();
		String aword3near = tdi.getAword3near();
		String aword4near = tdi.getAword4near();
		String aword5near = tdi.getAword5near();

		List<String> near1list = new ArrayList<String>();
		if (k1type != null && !"".equals(k1type)) {
			if ("1".equals(k1type)) {
				if (k1 != null && !"".equals(k1)) {
					if (aword1near != null && !"".equals(aword1near)) {
						String[] near1 = aword1near.split("\\|");
						near1list = new ArrayList<String>(Arrays.asList(near1));
						near1list.add(k1);
					} else {
						near1list.add(k1);
					}
				}
			} else if ("2".equals(k1type)) {
				String dyna1 = tdi.getAword1dyna();
				if (dyna1 != null && !"".equals(dyna1)) {
					near1list.add(dyna1);
				}
			}

		}

		List<String> near2list = new ArrayList<String>();
		if (k2type != null && !"".equals(k2type)) {
			if ("1".equals(k2type)) {
				if (k2 != null && !"".equals(k2)) {
					if (aword2near != null && !"".equals(aword2near)) {
						String[] near2 = aword2near.split("\\|");
						near2list = new ArrayList<String>(Arrays.asList(near2));
						near2list.add(k2);
					} else {
						near2list.add(k2);
					}
				}
			} else if ("2".equals(k2type)) {
				String dyna2 = tdi.getAword2dyna();
				if (dyna2 != null && !"".equals(dyna2)) {
					near2list.add(dyna2);
				}
			}

		}

		List<String> near3list = new ArrayList<String>();
		if (k3type != null && !"".equals(k3type)) {
			if ("1".equals(k3type)) {
				if (k3 != null && !"".equals(k3)) {
					if (aword3near != null && !"".equals(aword3near)) {
						String[] near3 = aword3near.split("\\|");
						near3list = new ArrayList<String>(Arrays.asList(near3));
						near3list.add(k3);
					} else {
						near3list.add(k3);
					}
				}
			} else if ("2".equals(k3type)) {
				String dyna3 = tdi.getAword3dyna();
				if (dyna3 != null && !"".equals(dyna3)) {
					near3list.add(dyna3);
				}
			}

		}

		List<String> near4list = new ArrayList<String>();
		if (k4type != null && !"".equals(k4type)) {
			if ("1".equals(k4type)) {
				if (k4 != null && !"".equals(k4)) {
					if (aword4near != null && !"".equals(aword4near)) {
						String[] near4 = aword4near.split("\\|");
						near4list = new ArrayList<String>(Arrays.asList(near4));
						near4list.add(k4);
					} else {
						near4list.add(k4);
					}
				}
			} else if ("2".equals(k4type)) {
				String dyna4 = tdi.getAword4dyna();
				if (dyna4 != null && !"".equals(dyna4)) {
					near4list.add(dyna4);
				}
			}

		}

		List<String> near5list = new ArrayList<String>();
		if (k5type != null && !"".equals(k5type)) {
			if ("1".equals(k5type)) {
				if (k5 != null && !"".equals(k5)) {
					if (aword5near != null && !"".equals(aword5near)) {
						String[] near5 = aword5near.split("\\|");
						near5list = new ArrayList<String>(Arrays.asList(near5));
						near5list.add(k5);
					} else {
						near5list.add(k5);
					}
				}
			} else if ("2".equals(k5type)) {
				String dyna5 = tdi.getAword5dyna();
				if (dyna5 != null && !"".equals(dyna5)) {
					near5list.add(dyna5);
				}
			}

		}

		List<List<String>> trees = new ArrayList<List<String>>();
		if (near1list != null && near1list.size() > 0) {
			trees.add(near1list);
		}
		if (near2list != null && near2list.size() > 0) {
			trees.add(near2list);
		}
		if (near3list != null && near3list.size() > 0) {
			trees.add(near3list);
		}
		if (near4list != null && near4list.size() > 0) {
			trees.add(near4list);
		}
		if (near5list != null && near5list.size() > 0) {
			trees.add(near5list);
		}
		ArrayList<String> result = new ArrayList<String>();
		this.manyTree(trees, 0, "", result);
//		logger.info("拼接句式耗时：" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");
		return result;

	}
	
	/**
	 * 多叉树遍历组合pattern
	 * 
	 * @param trees
	 * @param i
	 * @param temp
	 * @param result
	 */
	private void manyTree(List<List<String>> trees, int i, String temp, List<String> result) {
		if (trees.size() == i) {
			result.add(temp);
			return;
		} else {
			List<String> root = trees.get(i);
			for (String leaf : root) {
				manyTree(trees, i + 1, temp + leaf + AIKey.SEPARATOR, result);
			}
		}

	}
	
	/**
	 * 把对应键值对推送到redis服务器
	 * @param key
	 * @param value
	 */
	private void push2Redis(String key, String value, long stamp) {
		count++;
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
