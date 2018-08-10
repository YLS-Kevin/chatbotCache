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
import com.yls.app.entity.DialogType;
import com.yls.app.entity.TerminalDialogInter;
import com.yls.app.entity.TerminalDialogMore;
import com.yls.app.entity.TerminalDialogStatic;
import com.yls.app.persistence.mapper.DialogCacheMapper;
import com.yls.app.repository.AIKey;
import com.yls.app.repository.impl.RedisApiImpl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 终端对话缓存类
 * 
 * @author huangsy
 * @date 2018年3月2日下午3:53:38
 */
@Service
@DisallowConcurrentExecution
public class PersonalAutoTalkExecutorJob implements Executor {

	private static Logger logger = Logger.getLogger(PersonalAutoTalkExecutorJob.class);

	@Resource
	private DialogCacheMapper dialogCacheMapper;

	@Resource
	private RedisApiImpl redisApiImpl;
	
	@Resource
	private JedisPool pool;
	
	private Jedis jedis;

	// @Value("#{chatbotCache.adminUUID}")
	// private String id_ac;

	private long count;
	
	@Override
	public void execute() {
		count = 0;
		logger.info("执行对话库缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("对话个数："+count+"，执行对话库缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");

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
			this.cacheDialogStatic(id_dt);
			this.cacheDialogInter(id_dt);
			this.cacheDialogMore(id_dt);
		}
		if(jedis!=null) {
			jedis.close();
		}
	}
	
	/**
	 * 把对应键值对推送到redis服务器
	 * @param key
	 * @param value
	 */
	private void push2Redis(String key, String value, long stamp) {
		count++;
//		long stamp = getStamp(AIKey.REDIS_KEY_TTL);
//		redisApiImpl.zadd(key, stamp, value);
//		redisApiImpl.expire(key, AIKey.REDIS_KEY_TTL);
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
//		System.out.println(calendar.getTime().getTime());
		calendar.add(Calendar.SECOND, after);
//		System.out.println(calendar.getTime().getTime());
		return calendar.getTime().getTime();
	}

	/**
	 * 缓存固定一轮对话
	 */
	private void cacheDialogStatic(String id_dt) {
		List<TerminalDialogStatic> dialogStatic = dialogCacheMapper.findTerminalDialogStatic(id_dt);
		for (TerminalDialogStatic di : dialogStatic) {

			String that = "";
			String type = "0";
			String template = di.getAnswer();

			// 添加scripts脚本
			String scripts = "";
			if (di.getScripts() != null && !"".equals(di.getScripts())) {
				scripts = di.getScripts();
			}
			String stype = "";
			if (di.getStype() != null && !"".equals(di.getStype())) {
				stype = di.getStype();
			}
			String respara = "";
			if (di.getRepara() != null && !"".equals(di.getRepara())) {
				respara = di.getRepara();
			}
			String sin = "";
			if (di.getSin() != null && !"".equals(di.getSin())) {
				sin = di.getSin();

			}
			String sinword = "";
			if (di.getSinword() != null && !"".equals(di.getSinword())) {
				sinword = di.getSinword();
			}

			Map<String, String> script = new HashMap<String, String>();
			script.put("scripts", scripts);
			script.put("stype", stype);
			script.put("respara", respara);
			script.put("sin", sin);
			script.put("sinword", sinword);

			List<String> patterns = this.StaticCombinPatterns(di);
			// 每个pattern缓存一条redis记录
			for (String pattern : patterns) {
				
				long stamp = getStamp(AIKey.REDIS_KEY_TTL);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("pattern", pattern);
				map.put("template", template);
				map.put("type", type);
				map.put("that", that);
				map.put("script", script);
				map.put("stamp", String.valueOf(stamp));
				
				//缓存到redis
				String key = AIKey.PERSONAL_ANSWER + ":" + id_dt + ":" + pattern;
				String value = new Gson().toJson(map);

				this.push2Redis(key, value, stamp);

			}
		}
	}

	/**
	 * 组合pattern
	 * 
	 * @param tds
	 * @return
	 */
	private List<String> StaticCombinPatterns(TerminalDialogStatic tds) {

		String k1 = tds.getAword1();
		String k2 = tds.getAword2();
		String k3 = tds.getAword3();
		String k4 = tds.getAword4();
		String k5 = tds.getAword5();

		String aword1near = tds.getAword1near();
		String aword2near = tds.getAword2near();
		String aword3near = tds.getAword3near();
		String aword4near = tds.getAword4near();
		String aword5near = tds.getAword5near();

		List<String> near1list = new ArrayList<String>();
		if (k1 != null && !"".equals(k1)) {
			if (aword1near != null && !"".equals(aword1near)) {
				String[] near1 = aword1near.split("\\|");
				near1list = new ArrayList<String>(Arrays.asList(near1));
				near1list.add(k1);
			} else {
				near1list.add(k1);
			}
		}
		List<String> near2list = new ArrayList<String>();
		if (k2 != null && !"".equals(k2)) {
			if (aword2near != null && !"".equals(aword2near)) {
				String[] near2 = aword2near.split("\\|");
				near2list = new ArrayList<String>(Arrays.asList(near2));
				near2list.add(k2);
			} else {
				near2list.add(k2);
			}
		}
		List<String> near3list = new ArrayList<String>();
		if (k3 != null && !"".equals(k3)) {
			if (aword3near != null && !"".equals(aword3near)) {
				String[] near3 = aword3near.split("\\|");
				near3list = new ArrayList<String>(Arrays.asList(near3));
				near3list.add(k3);
			} else {
				near3list.add(k3);
			}
		}
		List<String> near4list = new ArrayList<String>();
		if (k4 != null && !"".equals(k4)) {
			if (aword4near != null && !"".equals(aword4near)) {
				String[] near4 = aword4near.split("\\|");
				near4list = new ArrayList<String>(Arrays.asList(near4));
				near4list.add(k4);
			} else {
				near4list.add(k4);
			}
		}
		List<String> near5list = new ArrayList<String>();
		if (k5 != null && !"".equals(k5)) {
			if (aword5near != null && !"".equals(aword5near)) {
				String[] near5 = aword5near.split("\\|");
				near5list = new ArrayList<String>(Arrays.asList(near5));
				near5list.add(k5);
			} else {
				near5list.add(k5);
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
		manyTree(trees, 0, "", result);
		return result;

	}

	/**
	 * 组合pattern
	 * 
	 * @param tdi
	 * @return
	 */
	private List<String> InterCombinPatterns(TerminalDialogInter tdi) {

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
		manyTree(trees, 0, "", result);
		return result;

	}

	/**
	 * 组合pattern
	 * 
	 * @param tdm
	 * @return
	 */
	private List<String> FirstMoreCombinPatterns(TerminalDialogMore tdm) {

		String k1type = tdm.getAword1type();
		String k2type = tdm.getAword2type();
		String k3type = tdm.getAword3type();
		String k4type = tdm.getAword4type();
		String k5type = tdm.getAword5type();

		String k1 = tdm.getAword1();
		String k2 = tdm.getAword2();
		String k3 = tdm.getAword3();
		String k4 = tdm.getAword4();
		String k5 = tdm.getAword5();

		String aword1near = tdm.getAword1near();
		String aword2near = tdm.getAword2near();
		String aword3near = tdm.getAword3near();
		String aword4near = tdm.getAword4near();
		String aword5near = tdm.getAword5near();

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
				String dyna1 = tdm.getAword1dyna();
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
				String dyna2 = tdm.getAword2dyna();
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
				String dyna3 = tdm.getAword3dyna();
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
				String dyna4 = tdm.getAword4dyna();
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
				String dyna5 = tdm.getAword5dyna();
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
		manyTree(trees, 0, "", result);
		return result;
	}

	/**
	 * 组合pattern
	 * 
	 * @param tdm
	 * @return
	 */
	private List<String> NextMoreCombinPatterns(TerminalDialogMore tdm) {

		String iword1type = tdm.getIword1type();
		String iword1 = tdm.getIword1();
		String iword1near = tdm.getIword1near();

		List<String> near1list = new ArrayList<String>();
		if (iword1type != null && !"".equals(iword1type)) {
			if ("1".equals(iword1type)) {
				if (iword1 != null && !"".equals(iword1)) {
					if (iword1near != null && !"".equals(iword1near)) {
						String[] near1 = iword1near.split("\\|");
						near1list = new ArrayList<String>(Arrays.asList(near1));
						near1list.add(iword1);
					} else {
						near1list.add(iword1);
					}
				}
			} else if ("2".equals(iword1type)) {
				String iword1dyna = tdm.getIword1dyna();
				if (iword1dyna != null && !"".equals(iword1dyna)) {
					near1list.add(iword1dyna);
				}
			}
		}

		List<List<String>> trees = new ArrayList<List<String>>();
		if (near1list != null && near1list.size() > 0) {
			trees.add(near1list);
		}
		ArrayList<String> result = new ArrayList<String>();
		manyTree(trees, 0, "", result);
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
	 * 缓存接口对话
	 */
	private void cacheDialogInter(String id_dt) {
		List<TerminalDialogInter> dialogInter = dialogCacheMapper.findTerminalDialogInter(id_dt);
		for (TerminalDialogInter di : dialogInter) {
			String that = "";
			String type = "1";
			String url = di.getUrl();

			// 拼接pattern字段
			// String pattern = "";
			String k1type = di.getAword1type();
			String k2type = di.getAword2type();
			String k3type = di.getAword3type();
			String k4type = di.getAword4type();
			String k5type = di.getAword5type();

			// 拼接template
			String template = "";
			String pk = "";// url名值对
			String para1 = di.getAword1para();
			String para2 = di.getAword2para();
			String para3 = di.getAword3para();
			String para4 = di.getAword4para();
			String para5 = di.getAword5para();

			// 判断接口参数是固定还是变化

			if (para1 != null && !"".equals(para1)) {
				if (k1type != null && "1".equals(k1type)) {
					para1 = para1 + "=" + di.getAword1();
					pk = para1;
				} else if (k1type != null && "2".equals(k1type)) {
					para1 = para1 + "=" + di.getAword1dyna();
					pk = para1;
				}
			}
			if (para2 != null && !"".equals(para2)) {
				if (k2type != null && "1".equals(k2type)) {
					para2 = para2 + "=" + di.getAword2();
					pk = pk + "&" + para2;
				} else if (k2type != null && "2".equals(k2type)) {
					para2 = para2 + "=" + di.getAword2dyna();
					pk = pk + "&" + para2;
				}
			}
			if (para3 != null && !"".equals(para3)) {
				if (k3type != null && "1".equals(k3type)) {
					para3 = para3 + "=" + di.getAword3();
					pk = pk + "&" + para3;
				} else if (k3type != null && "2".equals(k3type)) {
					para3 = para3 + "=" + di.getAword3dyna();
					pk = pk + "&" + para3;
				}
			}
			if (para4 != null && !"".equals(para4)) {
				if (k4type != null && "1".equals(k4type)) {
					para4 = para4 + "=" + di.getAword4();
					pk = pk + "&" + para4;
				} else if (k4type != null && "2".equals(k4type)) {
					para4 = para4 + "=" + di.getAword4dyna();
					pk = pk + "&" + para4;
				}
			}
			if (para5 != null && !"".equals(para5)) {
				if (k5type != null && "1".equals(k5type)) {
					para5 = para5 + "=" + di.getAword5();
					pk = pk + "&" + para5;
				} else if (k5type != null && "2".equals(k5type)) {
					para5 = para5 + "=" + di.getAword5dyna();
					pk = pk + "&" + para5;
				}
			}

			if (url.contains("?")) {
				template = url + "&" + pk;
			} else {
				template = url + "?" + pk;
			}

			// 添加scripts脚本
			String scripts = "";
			if (di.getScripts() != null && !"".equals(di.getScripts())) {
				scripts = di.getScripts();
			}
			String stype = "";
			if (di.getStype() != null && !"".equals(di.getStype())) {
				stype = di.getStype();
			}
			String respara = "";
			if (di.getRepara() != null && !"".equals(di.getRepara())) {
				respara = di.getRepara();
			}
			String sin = "";
			if (di.getSin() != null && !"".equals(di.getSin())) {
				sin = di.getSin();

			}
			String sinword = "";
			if (di.getSinword() != null && !"".equals(di.getSinword())) {
				sinword = di.getSinword();
			}

			Map<String, String> script = new HashMap<String, String>();
			script.put("scripts", scripts);
			script.put("stype", stype);
			script.put("respara", respara);
			script.put("sin", sin);
			script.put("sinword", sinword);

			List<String> patterns = this.InterCombinPatterns(di);
			for (String pattern : patterns) {
				
				long stamp = getStamp(AIKey.REDIS_KEY_TTL);
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("pattern", pattern);
				map.put("template", template);
				map.put("type", type);
				map.put("that", that);
				map.put("script", script);
				map.put("stamp", String.valueOf(stamp));
				
				String key = AIKey.PERSONAL_ANSWER + ":" + id_dt + ":" + pattern;
				String value = new Gson().toJson(map);
				
				this.push2Redis(key, value, stamp);

			}
		}
	}

	/**
	 * 缓存多伦对话
	 */
	private void cacheDialogMore(String id_dt) {
		List<TerminalDialogMore> dialogMore = dialogCacheMapper.findTerminalDialogMore(id_dt);
		for (TerminalDialogMore dm : dialogMore) {
			this.saveFirst(dm, id_dt);
			this.saveNext(dm, id_dt);
		}
	}

	/**
	 * 保存多伦对话下一轮对话
	 * 
	 * @param dm
	 */
	private void saveNext(TerminalDialogMore dm, String id_dt) {
		// String pattern = "";
		String template = "";
		String type = "0";
		String that = "";

		// 拼接that字段
		that = dm.getAnswer();

		// 拼接type字段
		if (dm.getNext_url() != null && !"".equals(dm.getNext_url())) {
			type = "1";
		}

		// 拼接template字段
		String answer = dm.getNext_answer();
		String url = dm.getNext_url();
		// 固定类型
		if (answer != null) {
			template = answer;
		}
		// 接口类型
		if (url != null) {
			String k1type = dm.getNext_aword1type();
			String k2type = dm.getNext_aword2type();
			String k3type = dm.getNext_aword3type();
			String k4type = dm.getNext_aword4type();
			String k5type = dm.getNext_aword5type();

			String pk = "";// url名值对
			String para1 = dm.getNext_aword1para();
			String para2 = dm.getNext_aword2para();
			String para3 = dm.getNext_aword3para();
			String para4 = dm.getNext_aword4para();
			String para5 = dm.getNext_aword5para();
			if (para1 != null && !"".equals(para1)) {
				if (k1type != null && "1".equals(k1type)) {
					para1 = para1 + "=" + dm.getNext_aword1();
					pk = para1;
				} else if (k1type != null && "2".equals(k1type)) {
					para1 = para1 + "=" + dm.getNext_aword1dyna();
					pk = para1;
				}
			}
			if (para2 != null && !"".equals(para2)) {
				if (k2type != null && "1".equals(k2type)) {
					para2 = para2 + "=" + dm.getNext_aword2();
					pk = pk + "&" + para2;
				} else if (k2type != null && "2".equals(k2type)) {
					para2 = para2 + "=" + dm.getNext_aword2dyna();
					pk = pk + "&" + para2;
				}
			}
			if (para3 != null && !"".equals(para3)) {
				if (k3type != null && "1".equals(k3type)) {
					para3 = para3 + "=" + dm.getNext_aword3();
					pk = pk + "&" + para3;
				} else if (k3type != null && "2".equals(k3type)) {
					para3 = para3 + "=" + dm.getNext_aword3dyna();
					pk = pk + "&" + para3;
				}
			}
			if (para4 != null && !"".equals(para4)) {
				if (k4type != null && "1".equals(k4type)) {
					para4 = para4 + "=" + dm.getNext_aword4();
					pk = pk + "&" + para4;
				} else if (k4type != null && "2".equals(k4type)) {
					para4 = para4 + "=" + dm.getNext_aword4dyna();
					pk = pk + "&" + para4;
				}
			}
			if (para5 != null && !"".equals(para5)) {
				if (k5type != null && "1".equals(k5type)) {
					para5 = para5 + "=" + dm.getNext_aword5();
					pk = pk + "&" + para5;
				} else if (k5type != null && "2".equals(k5type)) {
					para5 = para5 + "=" + dm.getNext_aword5dyna();
					pk = pk + "&" + para5;
				}
			}
			if (url.contains("?")) {
				template = url + "&" + pk;
			} else {
				template = url + "?" + pk;
			}
		}

		// 添加scripts脚本
		String scripts = "";
		if (dm.getNext_scripts() != null && !"".equals(dm.getNext_scripts())) {
			scripts = dm.getNext_scripts();
		}
		String stype = "";
		if (dm.getNext_stype() != null && !"".equals(dm.getNext_stype())) {
			stype = dm.getStype();
		}
		String respara = "";
		if (dm.getNext_repara() != null && !"".equals(dm.getNext_repara())) {
			respara = dm.getNext_repara();
		}
		String sin = "";
		if (dm.getNext_sin() != null && !"".equals(dm.getNext_sin())) {
			sin = dm.getNext_sin();

		}
		String sinword = "";
		if (dm.getNext_sinword() != null && !"".equals(dm.getNext_sinword())) {
			sinword = dm.getNext_sinword();
		}

		Map<String, String> script = new HashMap<String, String>();
		script.put("scripts", scripts);
		script.put("stype", stype);
		script.put("respara", respara);
		script.put("sin", sin);
		script.put("sinword", sinword);

		List<String> patterns = this.NextMoreCombinPatterns(dm);
		for (String pattern : patterns) {
			
			long stamp = getStamp(AIKey.REDIS_KEY_TTL);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pattern", pattern);
			map.put("template", template);
			map.put("type", type);
			map.put("that", that);
			map.put("script", script);
			map.put("stamp", String.valueOf(stamp));
			String key = AIKey.PERSONAL_ANSWER + ":" + id_dt + ":" + pattern;
			String value = new Gson().toJson(map);
			
			this.push2Redis(key, value, stamp);
		}
	}

	/**
	 * 保存多伦对话第一轮对话
	 * 
	 * @param dm
	 */
	private void saveFirst(TerminalDialogMore dm, String id_dt) {

		String template = "";
		String type = "0";
		String that = "";

		// 拼接type字段
		if (dm.getUrl() != null && !"".equals(dm.getUrl())) {
			type = "1";
		}
		// 拼接that字段
		that = "";
		
		// 拼接template字段
		String answer = dm.getAnswer();
		String url = dm.getUrl();
		// 固定类型
		if (answer != null) {
			template = answer;
		}
		// 接口类型
		if (url != null) {
			String pk = "";// url名值对
			String para1 = dm.getAword1para();
			String para2 = dm.getAword2para();
			String para3 = dm.getAword3para();
			String para4 = dm.getAword4para();
			String para5 = dm.getAword5para();
			if (para1 != null && !"".equals(para1)) {
				para1 = para1 + "=" + dm.getAword1dyna();
				pk = para1;
			}
			if (para2 != null && !"".equals(para2)) {
				para2 = para2 + "=" + dm.getAword2dyna();
				pk = pk + "&" + para2;
			}
			if (para3 != null && !"".equals(para3)) {
				para3 = para3 + "=" + dm.getAword3dyna();
				pk = pk + "&" + para3;
			}
			if (para4 != null && !"".equals(para4)) {
				para4 = para4 + "=" + dm.getAword4dyna();
				pk = pk + "&" + para4;
			}
			if (para5 != null && !"".equals(para5)) {
				para5 = para5 + "=" + dm.getAword5dyna();
				pk = pk + "&" + para5;
			}
			if (url.contains("?")) {
				template = url + "&" + pk;
			} else {
				template = url + "?" + pk;
			}
		}

		// 添加scripts脚本
		String scripts = "";
		if (dm.getScripts() != null && !"".equals(dm.getScripts())) {
			scripts = dm.getScripts();
		}
		String stype = "";
		if (dm.getStype() != null && !"".equals(dm.getStype())) {
			stype = dm.getStype();
		}
		String respara = "";
		if (dm.getRepara() != null && !"".equals(dm.getRepara())) {
			respara = dm.getRepara();
		}
		String sin = "";
		if (dm.getSin() != null && !"".equals(dm.getSin())) {
			sin = dm.getSin();

		}
		String sinword = "";
		if (dm.getSinword() != null && !"".equals(dm.getSinword())) {
			sinword = dm.getSinword();
		}

		Map<String, String> script = new HashMap<String, String>();
		script.put("scripts", scripts);
		script.put("stype", stype);
		script.put("respara", respara);
		script.put("sin", sin);
		script.put("sinword", sinword);
		
		List<String> patterns = this.FirstMoreCombinPatterns(dm);
		for (String pattern : patterns) {
			
			long stamp = getStamp(AIKey.REDIS_KEY_TTL);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pattern", pattern);
			map.put("template", template);
			map.put("type", type);
			map.put("that", that);
			map.put("script", script);
			map.put("stamp", String.valueOf(stamp));
			String key = AIKey.PERSONAL_ANSWER + ":" + id_dt + ":" + pattern;
			String value = new Gson().toJson(map);

			this.push2Redis(key, value, stamp);

		}
	}

}
