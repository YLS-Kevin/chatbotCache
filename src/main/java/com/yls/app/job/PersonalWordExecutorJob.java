/**
 * 
 */
package com.yls.app.job;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.yls.app.entity.Word;
import com.yls.app.persistence.mapper.WordCacheMapper;
import com.yls.app.repository.AIKey;

/**
 * @author huangsy
 * @date 2018年3月21日下午3:24:12
 */
@Service
@DisallowConcurrentExecution
public class PersonalWordExecutorJob implements Executor {

	private static Logger logger = Logger.getLogger(PersonalWordExecutorJob.class);
	
	@Resource
	private WordCacheMapper wordCacheMapper;
	
	@Value("#{hanlp.root}")
	private String rootPath;
	
	@Value("#{chatbotCache.MyDictionaryPath}")
	private String myDictionaryPath;
	
	@Override
	public void execute() {

		logger.info("执行个性化词库缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("执行个性化词库缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");

	}

	/**
	 * 处理方法
	 */
	private void handle() {
		this.cacheWord();
	}

	/**
	 * 缓存词典库到本地txt文件，并生成对应的bin文件
	 */
	private void cacheWord() {
		
		//生成词典文件
		String temp = rootPath + myDictionaryPath;
		File tempfile = new File(temp);
		if(!tempfile.exists()) {
			if(!tempfile.getParentFile().exists()) {
				tempfile.getParentFile().mkdirs();
			}
			try {
				tempfile.createNewFile();
			} catch (IOException e) {
				logger.error("缓存词库-创建个性化词库字典文件出错", e);
			}
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempfile));
			List<Word> user = wordCacheMapper.findAutoin();
			for(Word wo : user) {
				String wname = wo.getWname();
				String wx = wo.getWx();
				int wften = AIKey.WORD_FREQUENCY;
				String wx2 = wo.getWx2();
				int wften2 = AIKey.WORD_FREQUENCY;
				String line = wname + " " + wx + " " + wften;
				//如果有词性2则拼接上
				if(wx2!=null && !"".equals(wx2)) {
					line = line + " " + wx2 + " " + wften2;
				}
				line = line + "\n";
				bw.write(line);
			}
			bw.flush();
		} catch (IOException e) {
			logger.error("缓存个性化词库出错", e);
		}
	}

}
