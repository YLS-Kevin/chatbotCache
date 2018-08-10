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
import com.yls.app.entity.DialogMoreAndOne2;
import com.yls.app.entity.DialogType;
import com.yls.app.persistence.mapper.DialogCacheMapper;
import com.yls.app.repository.AIKey;
import com.yls.app.repository.impl.RedisApiImpl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 对话缓存2.0-模糊匹配
 * @author huangsy
 * @date 2018年5月7日下午5:22:45
 */
@Service
@DisallowConcurrentExecution
public class CacheAwordExecutorJob implements Executor {
	
	private final static Logger logger = Logger.getLogger(CacheSentenceExecutorJob.class);
	
	@Resource
	private DialogCacheMapper dialogCacheMapper;

	@Resource
	private RedisApiImpl redisApiImpl;
	
	@Resource
	private JedisPool pool;
	
	private Jedis jedis;
	
	/**
	 * 模胡匹配
	 */
	private static final int aptype_aword = 1;

	private long count;

	@Override
	public void execute() {
		
		count = 0;
		logger.info("执行模糊匹配缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("模糊匹配对话个数："+count+"，执行模糊匹配缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");
		
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
			this.cacheAwordMore(id_dt);
			this.cacheDialogOne(id_dt);
		}
		if(jedis!=null) {
			jedis.close();
		}
	}
	
	/**
	 * 缓存单轮对话
	 * @param id_dt
	 */
	private void cacheDialogOne(String id_dt) {
		List<DialogMoreAndOne2> dialogOne2 = dialogCacheMapper.findDialogOneV2(id_dt, aptype_aword);
		for (DialogMoreAndOne2 dm : dialogOne2) {
			this.save(dm, id_dt);
		}
	}
	
	/**
	 * 缓存多伦对话
	 */
	private void cacheAwordMore(String id_dt) {
		List<DialogMoreAndOne2> dialogMore2 = dialogCacheMapper.findDialogMoreV2(id_dt, aptype_aword);
		for (DialogMoreAndOne2 dm : dialogMore2) {
			this.save(dm, id_dt);
		}
	}
	
	/**
	 * 保存多伦对话
	 * @param dm
	 */
	private void save(DialogMoreAndOne2 dm, String id_dt) {

		String template = "";
		String type = "0";
		String that = "";
		String pattern = "";

		// 拼接type字段
		if (dm.getUrl() != null && !"".equals(dm.getUrl())) {
			type = "1";
			if(dm.getIs_front_call() != null && !"".equals(dm.getIs_front_call())) {
				if("1".equals(dm.getIs_front_call())) {//是否是前端调用, 1:是, 0:否.   默认:0
					type = "2";
				}
			}
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
		if (url != null && !"".equals(url)) {
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
		
		//对话id
		String id_d = dm.getId();
		
		//拼接pattern字段
		String aword = dm.getAword();
		if(aword != null) {
			pattern = aword;
		}
		//创建key value 然后缓存到redis
		this.createPushKV(pattern, template, type, that, script, id_d, dm);

	}
	
	/**
	 * 创建key value 然后缓存到redis
	 * @param pattern
	 * @param template
	 * @param type 是否接口
	 * @param that
	 * @param script
	 * @param id_dt 对话类型
	 * @param dm
	 */
	private void createPushKV(String pattern, String template, String type, String that, Map<String, String> script, 
			String id_d, DialogMoreAndOne2 dm) {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pattern", pattern);
		map.put("template", template);
		map.put("type", type);
		map.put("that", that);
		map.put("script", script);
		map.put("id_d", id_d);
		map.put("is_contain_kw", dm.getIs_contain_kw());
		
		//对话类型id
		String id_dt = dm.getId_dt();
		//cid_m_id_dt不空，说明该对话是通用模块自定义对话触发对话
		String cid_m_id_dt = dm.getCid_m_id_dt();
		if(cid_m_id_dt!=null && !"".contentEquals(cid_m_id_dt)) {
			id_dt = cid_m_id_dt;
		}
		//mul_dialog_type为4说明是多伦入口主题，mul_dialog_type为5说明是多伦对话主题
		String mul_dialog_type = dm.getMul_dialog_type();
		String key = "";
		//如果是多伦对话主题，需要区分主题id
		if("5".equals(mul_dialog_type)) {
			//多伦对话主题id
			String id_ap = dm.getId_ap();
			map.put("id_ap", id_ap);
			key = AIKey.AWORD_MORENEXT + ":" + id_dt + ":" + id_ap + ":" + pattern; 
		} else if("4".equals(mul_dialog_type)) {
			//多伦入口主题id
			String id_ap = dm.getId_ap();
			map.put("id_ap", id_ap);
			key = AIKey.AWORD_MOREFIRST + ":" + id_dt + ":" + pattern;
		} else {
			map.put("id_ap", "");
			key = AIKey.AWORD_ONE + ":" + id_dt + ":" + pattern;
		}
		
		String value = new Gson().toJson(map);
		this.push2Redis(key, value);
	}
	
	/**
	 * 把对应键值对推送到redis服务器
	 * @param key
	 * @param value
	 */
	private void push2Redis(String key, String value) {
		count++;
		long stamp = this.getStamp(0);
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
