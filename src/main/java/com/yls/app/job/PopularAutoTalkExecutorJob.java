/**
 * 
 */
package com.yls.app.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.yls.app.entity.Dialog;
import com.yls.app.entity.DialogCache;
import com.yls.app.entity.DialogInter;
import com.yls.app.entity.DialogMore;
import com.yls.app.entity.DialogStatic;
import com.yls.app.persistence.mapper.DialogCacheMapper;
import com.yls.app.repository.AIKey;
import com.yls.app.repository.impl.RedisApiImpl;

/**
 * 通用对话缓存类
 * @author huangsy
 * @date 2018年2月26日上午9:03:08
 */
@Service
@DisallowConcurrentExecution
public class PopularAutoTalkExecutorJob implements Executor {

	private static Logger logger = Logger.getLogger(PopularAutoTalkExecutorJob.class);
	
	@Resource
	private DialogCacheMapper dialogCacheMapper;

	@Resource
	private RedisApiImpl redisApiImpl;
	
	@Value("#{chatbotCache.adminUUID}")
	private String id_ac;

	@Override
	public void execute() {

		logger.info("执行通用对话库缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("执行通用对话库缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");

	}

	/**
	 * 处理方法
	 */
	private void handle() {
//		this.insert();
//		this.findDialog();
//		this.cacheAutoTalk();
		this.cacheDialogStatic();
		this.cacheDialogInter();
		this.cacheDialogMore();
	}
	
	/**
	 * 缓存固定一轮对话
	 */
	private void cacheDialogStatic() {
		List<DialogStatic> dialogStatic = dialogCacheMapper.findDialogStatic(id_ac);
		for(DialogStatic di : dialogStatic) {
			String that = "";
			String type = "0";
			String template = di.getAnswer();
			//拼接pattern字段
			String pattern = "";
			String k1 = di.getAword1();
			String k2 = di.getAword2();
			String k3 = di.getAword3();
			String k4 = di.getAword4();
			String k5 = di.getAword5();
			if(k1!=null && !"".equals(k1)) {
				pattern = pattern + k1 + AIKey.SEPARATOR;
			}
			if(k2!=null && !"".equals(k2)) {
				pattern = pattern + k2 + AIKey.SEPARATOR;
			}
			if(k3!=null && !"".equals(k3)) {
				pattern = pattern + k3 + AIKey.SEPARATOR;
			}
			if(k4!=null && !"".equals(k4)) {
				pattern = pattern + k4 + AIKey.SEPARATOR;
			}
			if(k5!=null && !"".equals(k5)) {
				pattern = pattern + k5 + AIKey.SEPARATOR;
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put("pattern", pattern);
			map.put("template", template);
			map.put("type", type);
			map.put("that", that);
			String key = AIKey.POPULAR_ANSWER + ":" + pattern;
			String value = new Gson().toJson(map);
//			System.out.println(key+"-->"+value);
			redisApiImpl.sadd(key, value);
		}
	}
	
	/**
	 * 缓存接口对话
	 */
	private void cacheDialogInter() {
		List<DialogInter> dialogInter = dialogCacheMapper.findDialogInter(id_ac);
		for(DialogInter di : dialogInter) {
			String that = "";
			String type = "1";
			String url = di.getUrl();
			
			//拼接pattern字段
			String pattern = "";
			String k1type = di.getAword1type();
			String k2type = di.getAword2type();
			String k3type = di.getAword3type();
			String k4type = di.getAword4type();
			String k5type = di.getAword5type();
			
			if("1".equals(k1type)) {
				String k1 = di.getAword1();
				if(k1!=null && !"".equals(k1)) {
					pattern = pattern + k1 + AIKey.SEPARATOR;
				}
			} else if("2".equals(k1type)) {
				String k1dyna = di.getAword1dyna();
				if(k1dyna!=null && !"".equals(k1dyna)) {
					pattern = pattern + k1dyna + AIKey.SEPARATOR;
				}
			}
			
			if("1".equals(k2type)) {
				String k2 = di.getAword2();
				if(k2!=null && !"".equals(k2)) {
					pattern = pattern + k2 + AIKey.SEPARATOR;
				}
			} else if("2".equals(k2type)) {
				String k2dyna = di.getAword2dyna();
				if(k2dyna!=null && !"".equals(k2dyna)) {
					pattern = pattern + k2dyna + AIKey.SEPARATOR;
				}
			}
			
			if("1".equals(k3type)) {
				String k3 = di.getAword3();
				if(k3!=null && !"".equals(k3)) {
					pattern = pattern + k3 + AIKey.SEPARATOR;
				}
			} else if("2".equals(k3type)) {
				String k3dyna = di.getAword3dyna();
				if(k3dyna!=null && !"".equals(k3dyna)) {
					pattern = pattern + k3dyna + AIKey.SEPARATOR;
				}
			}
			
			if("1".equals(k4type)) {
				String k4 = di.getAword4();
				if(k4!=null && !"".equals(k4)) {
					pattern = pattern + k4 + AIKey.SEPARATOR;
				}
			} else if("2".equals(k4type)) {
				String k4dyna = di.getAword4dyna();
				if(k4dyna!=null && !"".equals(k4dyna)) {
					pattern = pattern + k4dyna + AIKey.SEPARATOR;
				}
			}
			
			if("1".equals(k5type)) {
				String k5 = di.getAword5();
				if(k5!=null && !"".equals(k5)) {
					pattern = pattern + k5 + AIKey.SEPARATOR;
				}
			} else if("2".equals(k5type)) {
				String k5dyna = di.getAword5dyna();
				if(k5dyna!=null && !"".equals(k5dyna)) {
					pattern = pattern + k5dyna + AIKey.SEPARATOR;
				}
			}
			
			//拼接template
			String template = "";
			String pk = "";//url名值对
			String para1 = di.getAword1para();
			String para2 = di.getAword2para();
			String para3 = di.getAword3para();
			String para4 = di.getAword4para();
			String para5 = di.getAword5para();
			
			//.........判断接口参数是固定还是变化
			
			if(para1!=null && !"".equals(para1)) {
				if(k1type!=null && "1".equals(k1type)) {
					para1 = para1 + "=" + di.getAword1();
					pk = para1;
				} else if(k1type!=null && "2".equals(k1type)) {
					para1 = para1 + "=" + di.getAword1dyna();
					pk = para1;
				}
			}
			if(para2!=null && !"".equals(para2)) {
				if(k2type!=null && "1".equals(k2type)) {
					para2 = para2 + "=" + di.getAword2();
					pk = pk + "&" + para2;
				} else if(k2type!=null && "2".equals(k2type)) {
					para2 = para2 + "=" + di.getAword2dyna();
					pk = pk + "&" + para2;
				}
			}
			if(para3!=null && !"".equals(para3)) {
				if(k3type!=null && "1".equals(k3type)) {
					para3 = para3 + "=" + di.getAword3();
					pk = pk + "&" + para3;
				} else if(k3type!=null && "2".equals(k3type)) {
					para3 = para3 + "=" + di.getAword3dyna();
					pk = pk + "&" + para3;
				}
			}
			if(para4!=null && !"".equals(para4)) {
				if(k4type!=null && "1".equals(k4type)) {
					para4 = para4 + "=" + di.getAword4();
					pk = pk + "&" + para4;
				} else if(k4type!=null && "2".equals(k4type)) {
					para4 = para4 + "=" + di.getAword4dyna();
					pk = pk + "&" + para4;
				}
			}
			if(para5!=null && !"".equals(para5)) {
				if(k5type!=null && "1".equals(k5type)) {
					para5 = para5 + "=" + di.getAword5();
					pk = pk + "&" + para5;
				} else if(k5type!=null && "2".equals(k5type)) {
					para5 = para5 + "=" + di.getAword5dyna();
					pk = pk + "&" + para5;
				}
			}
			template = url + "?" + pk;
			
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("pattern", pattern);
			map.put("template", template);
			map.put("type", type);
			map.put("that", that);
			String key = AIKey.POPULAR_ANSWER + ":" + pattern;
			String value = new Gson().toJson(map);
//			System.out.println(key+"-->"+value);
			redisApiImpl.sadd(key, value);
		}
	}
	
	/**
	 * 缓存多伦对话
	 */
	private void cacheDialogMore() {
		List<DialogMore> dialogMore = dialogCacheMapper.findDialogMore(id_ac);
		for(DialogMore dm : dialogMore) {
			this.saveFirst(dm, id_ac);
			this.saveNext(dm, id_ac);
		}
	}
	
	/**
	 * 保存多伦对话下一轮对话
	 * @param dm
	 */
	private void saveNext(DialogMore dm, String id_ac) {
		String pattern = "";
		String template = "";
		String type = "0";
		String that = "";
		
		//拼接that字段
		that = dm.getAnswer();
		
		//拼接pattern字段
//		String k1type = dm.getNext_aword1type();
//		String k2type = dm.getNext_aword2type();
//		String k3type = dm.getNext_aword3type();
//		String k4type = dm.getNext_aword4type();
//		String k5type = dm.getNext_aword5type();
//		if("1".equals(k1type)) {
//			String k1 = dm.getNext_aword1();
//			if(k1!=null && !"".equals(k1)) {
//				pattern = pattern + k1 + AIKey.SEPARATOR;
//			}
//		} else if("2".equals(k1type)) {
//			String k1dyna = dm.getNext_aword1dyna();
//			if(k1dyna!=null && !"".equals(k1dyna)) {
//				pattern = pattern + k1dyna + AIKey.SEPARATOR;
//			}
//		}
//		if("1".equals(k2type)) {
//			String k2 = dm.getNext_aword2();
//			if(k2!=null && !"".equals(k2)) {
//				pattern = pattern + k2 + AIKey.SEPARATOR;
//			}
//		} else if("2".equals(k2type)) {
//			String k2dyna = dm.getNext_aword2dyna();
//			if(k2dyna!=null && !"".equals(k2dyna)) {
//				pattern = pattern + k2dyna + AIKey.SEPARATOR;
//			}
//		}
//		if("1".equals(k3type)) {
//			String k3 = dm.getNext_aword3();
//			if(k3!=null && !"".equals(k3)) {
//				pattern = pattern + k3 + AIKey.SEPARATOR;
//			}
//		} else if("2".equals(k3type)) {
//			String k3dyna = dm.getNext_aword3dyna();
//			if(k3dyna!=null && !"".equals(k3dyna)) {
//				pattern = pattern + k3dyna + AIKey.SEPARATOR;
//			}
//		}
//		if("1".equals(k4type)) {
//			String k4 = dm.getNext_aword4();
//			if(k4!=null && !"".equals(k4)) {
//				pattern = pattern + k4 + AIKey.SEPARATOR;
//			}
//		} else if("2".equals(k4type)) {
//			String k4dyna = dm.getNext_aword4dyna();
//			if(k4dyna!=null && !"".equals(k4dyna)) {
//				pattern = pattern + k4dyna + AIKey.SEPARATOR;
//			}
//		}
//		if("1".equals(k5type)) {
//			String k5 = dm.getNext_aword5();
//			if(k5!=null && !"".equals(k5)) {
//				pattern = pattern + k5 + AIKey.SEPARATOR;
//			}
//		} else if("2".equals(k5type)) {
//			String k5dyna = dm.getNext_aword5dyna();
//			if(k5dyna!=null && !"".equals(k5dyna)) {
//				pattern = pattern + k5dyna + AIKey.SEPARATOR;
//			}
//		}
		String iword1type = dm.getIword1type();
		if("1".equals(iword1type)) {
			String iword1 = dm.getIword1();
			if(iword1!=null && !"".equals(iword1)) {
				pattern = pattern + iword1 + AIKey.SEPARATOR;
			}
		} else if("2".equals(iword1type)) {
			String iword1dyna = dm.getIword1dyna();
			if(iword1dyna!=null && !"".equals(iword1dyna)) {
				pattern = pattern + iword1dyna + AIKey.SEPARATOR;
			}
		}
		
		//拼接type字段
		if(dm.getNext_url()!=null && !"".equals(dm.getNext_url())) {
			type = "1";
		}
		
		//拼接template字段
		String answer = dm.getNext_answer();
		String url = dm.getNext_url();
		//固定类型
		if(answer!=null) {
			template = answer;
		}
		//接口类型
		if(url!=null) {
			String k1type = dm.getNext_aword1type();
			String k2type = dm.getNext_aword2type();
			String k3type = dm.getNext_aword3type();
			String k4type = dm.getNext_aword4type();
			String k5type = dm.getNext_aword5type();
			
			String pk = "";//url名值对
			String para1 = dm.getNext_aword1para();
			String para2 = dm.getNext_aword2para();
			String para3 = dm.getNext_aword3para();
			String para4 = dm.getNext_aword4para();
			String para5 = dm.getNext_aword5para();
			if(para1!=null && !"".equals(para1)) {
				if(k1type!=null && "1".equals(k1type)) {
					para1 = para1 + "=" + dm.getNext_aword1();
					pk = para1;
				} else if(k1type!=null && "2".equals(k1type)) {
					para1 = para1 + "=" + dm.getNext_aword1dyna();
					pk = para1;
				}
			}
			if(para2!=null && !"".equals(para2)) {
				if(k2type!=null && "1".equals(k2type)) {
					para2 = para2 + "=" + dm.getNext_aword2();
					pk = pk + "&" + para2;
				} else if(k2type!=null && "2".equals(k2type)) {
					para2 = para2 + "=" + dm.getNext_aword2dyna();
					pk = pk + "&" + para2;
				}
			}
			if(para3!=null && !"".equals(para3)) {
				if(k3type!=null && "1".equals(k3type)) {
					para3 = para3 + "=" + dm.getNext_aword3();
					pk = pk + "&" + para3;
				} else if(k3type!=null && "2".equals(k3type)) {
					para3 = para3 + "=" + dm.getNext_aword3dyna();
					pk = pk + "&" + para3;
				}
			}
			if(para4!=null && !"".equals(para4)) {
				if(k4type!=null && "1".equals(k4type)) {
					para4 = para4 + "=" + dm.getNext_aword4();
					pk = pk + "&" + para4;
				} else if(k4type!=null && "2".equals(k4type)) {
					para4 = para4 + "=" + dm.getNext_aword4dyna();
					pk = pk + "&" + para4;
				}
			}
			if(para5!=null && !"".equals(para5)) {
				if(k5type!=null && "1".equals(k5type)) {
					para5 = para5 + "=" + dm.getNext_aword5();
					pk = pk + "&" + para5;
				} else if(k5type!=null && "2".equals(k5type)) {
					para5 = para5 + "=" + dm.getNext_aword5dyna();
					pk = pk + "&" + para5;
				}
			}
			template = url + "?" + pk;
		}
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("pattern", pattern);
		map.put("template", template);
		map.put("type", type);
		map.put("that", that);
		String key = AIKey.POPULAR_ANSWER + ":" + pattern;
		String value = new Gson().toJson(map);
//		System.out.println(key+"-->"+value);
		redisApiImpl.sadd(key, value);
	}
	
	/**
	 * 保存多伦对话第一轮对话
	 * @param dm
	 */
	private void saveFirst(DialogMore dm, String id_ac) {
		
		String pattern = "";
		String template = "";
		String type = "0";
		String that = "";
		
		//拼接type字段
		if(dm.getUrl()!=null && !"".equals(dm.getUrl())) {
			type = "1";
		}
		//拼接that字段
		that = "";
		//拼接pattern字段
		String k1type = dm.getAword1type();
		String k2type = dm.getAword2type();
		String k3type = dm.getAword3type();
		String k4type = dm.getAword4type();
		String k5type = dm.getAword5type();
		if("1".equals(k1type)) {
			String k1 = dm.getAword1();
			if(k1!=null && !"".equals(k1)) {
				pattern = pattern + k1 + AIKey.SEPARATOR;
			}
		} else if("2".equals(k1type)) {
			String k1dyna = dm.getAword1dyna();
			if(k1dyna!=null && !"".equals(k1dyna)) {
				pattern = pattern + k1dyna + AIKey.SEPARATOR;
			}
		}
		if("1".equals(k2type)) {
			String k2 = dm.getAword2();
			if(k2!=null && !"".equals(k2)) {
				pattern = pattern + k2 + AIKey.SEPARATOR;
			}
		} else if("2".equals(k2type)) {
			String k2dyna = dm.getAword1dyna();
			if(k2dyna!=null && !"".equals(k2dyna)) {
				pattern = pattern + k2dyna + AIKey.SEPARATOR;
			}
		}
		if("1".equals(k3type)) {
			String k3 = dm.getAword3();
			if(k3!=null && !"".equals(k3)) {
				pattern = pattern + k3 + AIKey.SEPARATOR;
			}
		} else if("2".equals(k3type)) {
			String k3dyna = dm.getAword3dyna();
			if(k3dyna!=null && !"".equals(k3dyna)) {
				pattern = pattern + k3dyna + AIKey.SEPARATOR;
			}
		}
		if("1".equals(k4type)) {
			String k4 = dm.getAword4();
			if(k4!=null && !"".equals(k4)) {
				pattern = pattern + k4 + AIKey.SEPARATOR;
			}
		} else if("2".equals(k4type)) {
			String k4dyna = dm.getAword4dyna();
			if(k4dyna!=null && !"".equals(k4dyna)) {
				pattern = pattern + k4dyna + AIKey.SEPARATOR;
			}
		}
		if("1".equals(k5type)) {
			String k5 = dm.getAword5();
			if(k5!=null && !"".equals(k5)) {
				pattern = pattern + k5 + AIKey.SEPARATOR;
			}
		} else if("2".equals(k5type)) {
			String k5dyna = dm.getAword5dyna();
			if(k5dyna!=null && !"".equals(k5dyna)) {
				pattern = pattern + k5dyna + AIKey.SEPARATOR;
			}
		}
		//拼接template字段
		String answer = dm.getAnswer();
		String url = dm.getUrl();
		//固定类型
		if(answer!=null) {
			template = answer;
		}
		//接口类型
		if(url!=null) {
			String pk = "";//url名值对
			String para1 = dm.getAword1para();
			String para2 = dm.getAword2para();
			String para3 = dm.getAword3para();
			String para4 = dm.getAword4para();
			String para5 = dm.getAword5para();
			if(para1!=null && !"".equals(para1)) {
				para1 = para1 + "=" + dm.getAword1dyna();
				pk = para1;
			}
			if(para2!=null && !"".equals(para2)) {
				para2 = para2 + "=" + dm.getAword2dyna();
				pk = pk + "&" + para2;
			}
			if(para3!=null && !"".equals(para3)) {
				para3 = para3 + "=" + dm.getAword3dyna();
				pk = pk + "&" + para3;
			}
			if(para4!=null && !"".equals(para4)) {
				para4 = para4 + "=" + dm.getAword4dyna();
				pk = pk + "&" + para4;
			}
			if(para5!=null && !"".equals(para5)) {
				para5 = para5 + "=" + dm.getAword5dyna();
				pk = pk + "&" + para5;
			}
			template = url + "?" + pk;
		}
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("pattern", pattern);
		map.put("template", template);
		map.put("type", type);
		map.put("that", that);
		String key = AIKey.POPULAR_ANSWER + ":" + pattern;
		String value = new Gson().toJson(map);
//		System.out.println(key+"-->"+value);
		redisApiImpl.sadd(key, value);
	}
	
	/**
	 * 读取数据库数据缓存到redis
	 */
	private void cacheAutoTalk() {
		try {
			List<DialogCache> dialogs = dialogCacheMapper.findAll();
			for(DialogCache dialog : dialogs) {
				Map<String, String> map = this.formateDialogCache(dialog);
				String key = AIKey.POPULAR_ANSWER + ":" + map.get("pattern");
				String value = new Gson().toJson(map);
				System.out.println(key+"-->"+value);
				redisApiImpl.sadd(key, value);
			}

		} catch (Exception e) {
			logger.error("缓存通用对话库出错", e);
		}
	}
	
	/**
	 * 格式化从数据库读入的数据
	 * @param dialogCache
	 * @return
	 */
	private Map<String, String> formateDialogCache(DialogCache dialogCache) {
		
		//拼接pattern字段
		String pattern = "";
		if("1".equals(dialogCache.getK1type())) {//1表示固定类型关键词
			String key1 = dialogCache.getK1();
			if(key1!=null && !"".equals(key1)) {
				pattern = pattern + key1 + AIKey.SEPARATOR;
			}
		} else if("2".equals(dialogCache.getK1type())) {
			String key1 = dialogCache.getK1dyna();
			if(key1!=null && !"".equals(key1)) {
				pattern = pattern + key1 + AIKey.SEPARATOR;
			}
		}
		if("1".equals(dialogCache.getK2type())) {//1表示固定类型关键词
			String key2 = dialogCache.getK2();
			if(key2!=null && !"".equals(key2)) {
				pattern = pattern + key2 + AIKey.SEPARATOR;
			}
		} else if("2".equals(dialogCache.getK1type())) {
			String key2 = dialogCache.getK2dyna();
			if(key2!=null && !"".equals(key2)) {
				pattern = pattern + key2 + AIKey.SEPARATOR;
			}
		}
		if("1".equals(dialogCache.getK3type())) {//1表示固定类型关键词
			String key3 = dialogCache.getK3();
			if(key3!=null && !"".equals(key3)) {
				pattern = pattern + key3 + AIKey.SEPARATOR;
			}
		} else if("2".equals(dialogCache.getK1type())) {
			String key3 = dialogCache.getK3dyna();
			if(key3!=null && !"".equals(key3)) {
				pattern = pattern + key3 + AIKey.SEPARATOR;
			}
		}
		if("1".equals(dialogCache.getK4type())) {//1表示固定类型关键词
			String key4 = dialogCache.getK4();
			if(key4!=null && !"".equals(key4)) {
				pattern = pattern + key4 + AIKey.SEPARATOR;
			}
		} else if("2".equals(dialogCache.getK1type())) {
			String key4 = dialogCache.getK4dyna();
			if(key4!=null && !"".equals(key4)) {
				pattern = pattern + key4 + AIKey.SEPARATOR;
			}
		}
		if("1".equals(dialogCache.getK5type())) {//1表示固定类型关键词
			String key5 = dialogCache.getK5();
			if(key5!=null && !"".equals(key5)) {
				pattern = pattern + key5 + AIKey.SEPARATOR;
			}
		} else if("2".equals(dialogCache.getK1type())) {
			String key5 = dialogCache.getK5dyna();
			if(key5!=null && !"".equals(key5)) {
				pattern = pattern + key5 + AIKey.SEPARATOR;
			}
		}
		
		//拼接that字段
		String that = "";
		String that_answer = dialogCache.getThat_answer();
		if(that_answer!=null && !"".equals(that_answer)) {
			that = that_answer;
		}
//		String thatkey1 = dialogCache.getThat_k1();
//		String thatkey2 = dialogCache.getThat_k2();
//		String thatkey3 = dialogCache.getThat_k3();
//		String thatkey4 = dialogCache.getThat_k4();
//		String thatkey5 = dialogCache.getThat_k5();
//		if(thatkey1!=null && !"".equals(thatkey1)) {
//			that = that + thatkey1 + AIKey.SEPARATOR;
//		}
//		if(thatkey2!=null && !"".equals(thatkey2)) {
//			that = that + thatkey2 + AIKey.SEPARATOR;
//		}
//		if(thatkey3!=null && !"".equals(thatkey3)) {
//			that = that + thatkey3 + AIKey.SEPARATOR;
//		}
//		if(thatkey4!=null && !"".equals(thatkey4)) {
//			that = that + thatkey4 + AIKey.SEPARATOR;
//		}
//		if(thatkey5!=null && !"".equals(thatkey5)) {
//			that = that + thatkey5 + AIKey.SEPARATOR;
//		}
		
		//拼接template字段
		String template = "";
		String answer = dialogCache.getAnswer();
		String url = dialogCache.getUrl();
		//固定类型
		if(answer!=null) {
			template = answer;
		}
		//接口类型
		if(url!=null) {
			String pk = "";//url名值对
			String para1 = dialogCache.getK1para();
			String para2 = dialogCache.getK2para();
			String para3 = dialogCache.getK3para();
			String para4 = dialogCache.getK4para();
			String para5 = dialogCache.getK5para();
			if(para1!=null && !"".equals(para1)) {
				para1 = para1 + "=" + dialogCache.getK1dyna();
				pk = para1;
			}
			if(para2!=null && !"".equals(para2)) {
				para2 = para2 + "=" + dialogCache.getK2dyna();
				pk = pk + "&" + para2;
			}
			if(para3!=null && !"".equals(para3)) {
				para3 = para3 + "=" + dialogCache.getK3dyna();
				pk = pk + "&" + para3;
			}
			if(para4!=null && !"".equals(para4)) {
				para4 = para4 + "=" + dialogCache.getK4dyna();
				pk = pk + "&" + para4;
			}
			if(para5!=null && !"".equals(para5)) {
				para5 = para5 + "=" + dialogCache.getK5dyna();
				pk = pk + "&" + para5;
			}
			template = url + "?" + pk;
		}
		
		//拼接type字段
		String type = "0";
		if(url!=null) {
			type = "1";
		}
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("pattern", pattern);
		map.put("template", template);
		map.put("type", type);
		map.put("that", that);
		
		return map;
	}
	
	private void findAllDialog() {
		try {
			List<Dialog> dialogs = dialogCacheMapper.findAllDialog();
			System.out.println(dialogs);
		} catch(Exception e) {
			logger.error("查询对话出错", e);
		}
	}
	
	private void insert() {
		try{
			Dialog entity = new Dialog();
			entity.setId(UUID.randomUUID().toString().replaceAll("-", ""));
			entity.setId_dt("123123");
			entity.setAtype(2);
			entity.setState(1);
			entity.setSort(1);
			entity.setRemarks("");
			entity.setCreate_by("");
			entity.setCreate_date(new Date());
			entity.setUpdate_by("");
			entity.setUpdate_date(new Date());
			entity.setDel_flag("0");
			dialogCacheMapper.insertDialog(entity);
		} catch(Exception e) {
			logger.error("插入出错", e);
		}
	}

}
