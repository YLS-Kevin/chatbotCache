package com.yls.app.startup;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author huangsy
 * 2017年12月29日17:44:47
 */
public class Bootstrap {
	
	private  static Logger logger = Logger.getLogger(Bootstrap.class);
	
	public void start() {
		logger.info("启动智能机器人redis缓存服务");
		//设置profile环境
		System.setProperty("spring.profiles.default", "prod");
		new ClassPathXmlApplicationContext("applicationContext.xml");
		
	}
	
	public static void main(String args[]) {
		Bootstrap boostrap = new Bootstrap();
		boostrap.start();
	}

}
