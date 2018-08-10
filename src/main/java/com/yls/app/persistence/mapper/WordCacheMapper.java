/**
 * 
 */
package com.yls.app.persistence.mapper;

import java.util.List;

import com.yls.app.entity.Account;
import com.yls.app.entity.DWordGroup;
import com.yls.app.entity.Word;

/**
 * @author huangsy
 * @date 2018年3月5日上午9:16:46
 */
public interface WordCacheMapper {
	
	List<Word> findAutoin();
	
	List<Word> findAll();
	
	List<Word> findCoreWords();
	List<Word> findUserWords();
	List<Word> findCommonWords();
	
	List<Account> findAllAccount();
	
	List<DWordGroup> findAllWordTypeByAccountId(String id);

}
