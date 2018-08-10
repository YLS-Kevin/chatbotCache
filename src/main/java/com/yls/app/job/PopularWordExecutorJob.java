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
 * @date 2018年3月5日上午9:23:05
 */
@Service
@DisallowConcurrentExecution
public class PopularWordExecutorJob implements Executor {
	
	private static Logger logger = Logger.getLogger(PopularWordExecutorJob.class);
	
	@Resource
	private WordCacheMapper wordCacheMapper;
	
	@Value("#{hanlp.root}")
	private String rootPath;
	
	@Value("#{hanlp.CoreDictionaryPath}")
	private String coreDictionaryPath;
	
	@Override
	public void execute() {

		logger.info("执行词库缓存====");
		long startTime = System.currentTimeMillis();

		this.handle();

		logger.info("执行词库缓存====结束" + ((System.currentTimeMillis() - startTime) * 0.001) + "s ");

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
		
		//生成核心词典文件
		String path = rootPath + coreDictionaryPath;
		String path_temp = path + "_temp";
		File file = new File(path_temp);
		if(!file.exists()) {
			if(!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("缓存词库-创建核心词库字典文件出错", e);
			}
		}
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			List<Word> core = wordCacheMapper.findCoreWords();
			List<Word> user = wordCacheMapper.findUserWords();
			List<Word> common = wordCacheMapper.findCommonWords();
			for(Word wo : core) {
				String wname = wo.getWname();
				String wx = wo.getWx();
				int wften = wo.getWften();
				String wx2 = wo.getWx2();
				int wften2 = wo.getWften2();
				String line = wname + " " + wx + " " + wften;
				//如果有词性2则拼接上
				if(wx2!=null && !"".equals(wx2)) {
					line = line + " " + wx2 + " " + wften2;
				}
				line = line + "\n";
				bw.write(line);
			}
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
			for(Word wo : common) {
				String wname = wo.getWname();
				String wx = wo.getWx();
				int wften = wo.getWften() + AIKey.WORD_FREQUENCY;
				String wx2 = wo.getWx2();
				int wften2 = wo.getWften2() + AIKey.WORD_FREQUENCY;
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
			logger.error("缓存词库出错", e);
		}
		//缓存词典成bin文件
//		new CoreDictionary();
		
		//用tempbin文件替换bin文件
		File bin = new File(path + ".bin");
		File tempbin = new File(path_temp + ".bin");
		if(tempbin.exists()) {
			bin.delete();
			tempbin.renameTo(bin);
		}
	}
	
}
