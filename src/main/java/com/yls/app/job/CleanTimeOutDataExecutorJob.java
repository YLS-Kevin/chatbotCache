/**
 * 
 */
package com.yls.app.job;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.yls.app.entity.ClientDialogType;
import com.yls.app.entity.DWordGroup;
import com.yls.app.entity.DialogExp;
import com.yls.app.entity.DialogMan;
import com.yls.app.entity.DialogType;
import com.yls.app.entity.TerminalDialogInter;
import com.yls.app.entity.TerminalDialogMore;
import com.yls.app.entity.TerminalDialogStatic;
import com.yls.app.entity.Word;
import com.yls.app.persistence.mapper.ClientCacheMapper;
import com.yls.app.persistence.mapper.DialogCacheMapper;
import com.yls.app.persistence.mapper.WordCacheMapper;
import com.yls.app.repository.AIKey;
import com.yls.app.repository.impl.RedisApiImpl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 清除过期redis缓存
 * @author huangsy
 * @date 2018年3月17日下午1:00:32
 */
@Service
@DisallowConcurrentExecution
public class CleanTimeOutDataExecutorJob implements Executor {
	
	private final static Logger logger = Logger.getLogger(CleanTimeOutDataExecutorJob.class);
	
	@Resource
	private DialogCacheMapper dialogCacheMapper;
	
	@Resource
	private ClientCacheMapper clientCacheMapper;
	
	@Resource
	private WordCacheMapper wordCacheMapper;

	@Resource
	private RedisApiImpl redisApiImpl;
	
	@Resource
	private JedisPool pool;
	
	private Jedis jedis;

	@Override
	public void execute() {

		logger.info("清除过期缓存数据====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("清除过期缓存数据====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");
		
	}
	
	/**
	 * 处理方法
	 */
	private void handle() {
		jedis = pool.getResource();
		// 清除过期对话
//		List<DialogType> dialogTypes = dialogCacheMapper.findAllDialogType();
//		for (DialogType dt : dialogTypes) {
//			String id_dt = dt.getId();
//			this.cleanStaticDialog(id_dt);
//			this.cleanInterDialog(id_dt);
//			this.cleanMoreDialog(id_dt);
//			// 清除句式缓存
//			this.cleanSentence(id_dt);
//		}
		
		// 清除过期异常回复
//		this.cleanDialogExp();
		
		// 清除终端应用对话类型参照表
//		this.cleanClientDialogType();
		
		//清除动态词类型
		this.cleanDyna();
		
		if(jedis!=null) {
			jedis.close();
		}
	}
	
	/**
	 * 清除动态词类型
	 */
	private void cleanDyna() {
		List<Account> accounts = wordCacheMapper.findAllAccount();
		for(Account account : accounts) {
			String id = account.getId();
			String key = AIKey.DYNA + ":" + id;
			this.clean(key);
		}
	}
	
	/**
	 * 清理句式
	 */
	private void cleanSentence(String id_dt) {
		List<DialogMan> dialogMans = dialogCacheMapper.findAllDialogMan(id_dt);
		for(DialogMan dialogMan : dialogMans) {
			
			String dt = dialogMan.getId_dt();
			List<String> patterns = this.sentenceCombinPatterns(dialogMan);
			for(String pattern : patterns) {
				String[] keywords = pattern.split("\\(@\\)");
				for(String keyword : keywords) {
					String key = AIKey.SENTENCE + ":" + dt + ":" + keyword;
					this.clean(key);
				}
			}
		}
	}
	
	/**
	 * 清除终端应用对话类型参照表
	 */
	private void cleanClientDialogType() {
		List<ClientDialogType> clientDialogTypes = clientCacheMapper.findAllClientDialogType();
		for(ClientDialogType clientDialogType : clientDialogTypes) {
			String clientId = clientDialogType.getCid();
			String dialogType = clientDialogType.getId_dt();
			String key = AIKey.CLIENT_DIALOGTYPE + ":" + clientId;

			this.clean(key);
		}
	}
	
	/**
	 * 清除过期异常回复
	 */
	private void cleanDialogExp() {
		
		List<DialogExp> dialogExps = dialogCacheMapper.findAllDialogExp();
		for(DialogExp dialogExp : dialogExps) {
			String cid = dialogExp.getCid();
			String stype = dialogExp.getStype();//异常类型，类型：1-无答案时，2-接口异常时，3-系统出错时
			
			String key = AIKey.EXP_ANSWER + ":" + cid + ":" + stype;
			
			this.clean(key);
		}
		
	}
	
	/**
	 * 清除多轮对话
	 * @param id_dt
	 */
	private void cleanMoreDialog(String id_dt) {
		List<TerminalDialogMore> dialogMore = dialogCacheMapper.findTerminalDialogMore(id_dt);
		for (TerminalDialogMore dm : dialogMore) {
			this.cleanFirst(dm, id_dt);
			this.cleanNext(dm, id_dt);
		}
		
	}
	
	/**
	 * 清除多伦对话第一轮对话
	 * 
	 * @param dm
	 */
	private void cleanFirst(TerminalDialogMore dm, String id_dt) {
		List<String> patterns = this.FirstMoreCombinPatterns(dm);
		for (String pattern : patterns) {
			
			String key = AIKey.PERSONAL_ANSWER + ":" + id_dt + ":" + pattern;
			this.clean(key);

		}
	}
	
	/**
	 * 清除多伦对话下一轮对话
	 * 
	 * @param dm
	 */
	private void cleanNext(TerminalDialogMore dm, String id_dt) {
		List<String> patterns = this.NextMoreCombinPatterns(dm);
		for (String pattern : patterns) {
			
			String key = AIKey.PERSONAL_ANSWER + ":" + id_dt + ":" + pattern;
			this.clean(key);
			
		}
	}
	
	/**
	 * 清除接口对话
	 * @param id_dt
	 */
	private void cleanInterDialog(String id_dt) {
		
		List<TerminalDialogInter> dialogInter = dialogCacheMapper.findTerminalDialogInter(id_dt);
		for (TerminalDialogInter di : dialogInter) {
			List<String> patterns = this.InterCombinPatterns(di);
			for (String pattern : patterns) {
				
				//缓存到redis
				String key = AIKey.PERSONAL_ANSWER + ":" + id_dt + ":" + pattern;
				this.clean(key);
			}
		}
		
	}
	
	/**
	 * 清除固定一轮对话
	 * @param id_dt
	 */
	private void cleanStaticDialog(String id_dt) {
		
		List<TerminalDialogStatic> dialogStatic = dialogCacheMapper.findTerminalDialogStatic(id_dt);
		for (TerminalDialogStatic di : dialogStatic) {
			List<String> patterns = this.StaticCombinPatterns(di);
			for (String pattern : patterns) {
				
				//缓存到redis
				String key = AIKey.PERSONAL_ANSWER + ":" + id_dt + ":" + pattern;
				this.clean(key);

			}
		}
		
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
	 * 拼接pattern
	 * @param tdi
	 * @return
	 */
	private List<String> sentenceCombinPatterns(DialogMan tdi) {

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
	 * 清除过期缓存
	 * @param key
	 */
	private void clean(String key) {
		
		long stamp = new Date().getTime();
//		redisApiImpl.zremrangeByScore(key, 0, stamp);
		jedis.zremrangeByScore(key, 0, stamp);
		
	}
	
}
