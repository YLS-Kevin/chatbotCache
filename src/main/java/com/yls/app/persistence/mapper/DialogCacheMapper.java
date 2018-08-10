/**
 * 
 */
package com.yls.app.persistence.mapper;

import java.util.List;
import com.yls.app.entity.Account;
import com.yls.app.entity.Dialog;
import com.yls.app.entity.DialogCache;
import com.yls.app.entity.DialogExp;
import com.yls.app.entity.DialogInter;
import com.yls.app.entity.DialogMan;
import com.yls.app.entity.DialogMore;
import com.yls.app.entity.DialogMoreAndOne2;
import com.yls.app.entity.DialogStatic;
import com.yls.app.entity.DialogType;
import com.yls.app.entity.TerminalDialogInter;
import com.yls.app.entity.TerminalDialogMore;
import com.yls.app.entity.TerminalDialogStatic;

/**
 * @author huangsy
 * @date 2018年2月26日上午9:19:43
 */
public interface DialogCacheMapper {

	void insertDialog(Dialog entity);
	
	List<DialogCache> findAll();
	
	List<DialogType> findAllDialogType();
	
	List<Dialog> findAllDialog();
	List<Account> findAllAccountWithout(String adminUUID);
	
	List<DialogStatic> findDialogStatic(String id_ac);
	List<DialogInter> findDialogInter(String id_ac);
	List<DialogMore> findDialogMore(String id_ac);
	
	List<TerminalDialogStatic> findTerminalDialogStatic(String id_dt);
	List<TerminalDialogInter> findTerminalDialogInter(String id_dt);
	List<TerminalDialogMore> findTerminalDialogMore(String id_dt);
	
	List<DialogExp> findAllDialogExp();
	
	List<DialogMan> findAllDialogMan(String id_dt);
	
	//对话2.0
	/**
	 * @param id_dt 对话类型id
	 * @param aptype 模胡查询为1，关键词查询为2
	 * @return
	 */
	List<DialogMoreAndOne2> findDialogMoreV2(String id_dt, int aptype);
	/**
	 * @param id_dt 对话类型id
	 * @param aptype 模胡查询为1，关键词查询为2
	 * @return
	 */
	List<DialogMoreAndOne2> findDialogOneV2(String id_dt, int aptype);
	
}
