/**
 * 
 */
package com.yls.app.repository;

/**
 * @author huangsy
 * 20182018年1月31日上午11:45:30
 * 智能机器人语料库rediskey
 */
public class AIKey {
	
	/**
	 * 对话库2.0，模糊匹配-多伦入口
	 */
	public static final String AWORD_MOREFIRST = "ChatBot:AIKey:Aword:MoreFirst";
	
	/**
	 * 对话库2.0，模胡匹配-多伦对话中
	 */
	public static final String AWORD_MORENEXT = "ChatBot:AIKey:Aword:MoreNext";
	
	/**
	 * 对话库2.0，模胡匹配-单轮对话
	 */
	public static final String AWORD_ONE = "ChatBot:AIKey:Aword:One";
	
	/**
	 * 对话库2.0，关键词匹配-多伦入口
	 */
	public static final String DIALOG_MOREFIRST = "ChatBot:AIKey:Dialog:MoreFirst";
	
	/**
	 * 对话库2.0，关键词匹配-多伦对话中
	 */
	public static final String DIALOG_MORENEXT = "ChatBot:AIKey:Dialog:MoreNext";
	
	/**
	 * 对话库2.0，关键词匹配-单轮对话
	 */
	public static final String DIALOG_ONE = "ChatBot:AIKey:Dialog:One";
	
	/**
	 * 动态词
	 */
	public static final String DYNA = "ChatBot:AIKey:Dyna";
	
	/**
	 * 模糊句式
	 */
	public static final String AWORD = "ChatBot:AIKey:Aword";
	
	/**
	 * 句式
	 */
	public static final String SENTENCE = "ChatBot:AIKey:Sentence";
	
	/**
	 * 异常应答
	 */
	public static final String EXP_ANSWER = "ChatBot:AIKey:ExpAnswer";
	
	/**
	 * redis缓存数据过期时间，默认60秒
	 */
	public static final int REDIS_KEY_TTL = 60;
	
	/**
	 *  最大历史记录数
	 */
	public static final int MAX_HISTORY = 5;
	
	/**
	 * 终端对话库映射表
	 */
	public static final String CLIENT_DIALOGTYPE = "ChatBot:AIKey:Client_DialogType";
	
	/**
	 * 用户输入的词的默认词频
	 */
	public static final int WORD_FREQUENCY = 999999;
	
	/**
	 * 关键词分隔符
	 */
	public static final String SEPARATOR = "(@)";
	
	/**
	 * 时间词
	 */
	public static final String TIME = "(&t&)";
	
	/**
	 * 时间词
	 */
	public static final String SITUS = "(&ns&)";
	
	/**
	 * 随机回复
	 */
	public static final String RANDOM_ANSWER = "ChatBot:AIKey:Random:randomanswer";
	
	/**
	 * 通用语库
	 */
	public static final String POPULAR_ANSWER = "ChatBot:AIKey:Popular";
	
	/**
	 * 个性语库
	 */
	public static final String PERSONAL_ANSWER = "ChatBot:AIKey:Personal";
	
	/**
	 * 1字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_ONE = "keywordExtration:AIKey:One";
	
	/**
	 * 2字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_TWO = "keywordExtration:AIKey:Two";
	
	/**
	 * 3字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_THREE = "keywordExtration:AIKey:Three";
	
	/**
	 * 4字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_FOUR = "keywordExtration:AIKey:Four";
	
	/**
	 * 5字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_FIVE = "keywordExtration:AIKey:Five";
	
	/**
	 * 6字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_SIX = "keywordExtration:AIKey:Six";
	
	/**
	 * 7字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_SEVEN = "keywordExtration:AIKey:Seven";
	
	/**
	 * 8字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_EIGHT = "keywordExtration:AIKey:Eight";
	
	/**
	 * 9字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_NINE = "keywordExtration:AIKey:Nine";
	
	/**
	 * 10字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_TEN = "keywordExtration:AIKey:Ten";
	
	/**
	 * 11字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_ELEVEN = "keywordExtration:AIKey:Eleven";
	
	/**
	 * 12字词
	 */
	public static final String KEYWORDEXTRATION_AIKEY_TWELVE = "keywordExtration:AIKey:Twelve";

}
