/**
 * 
 */
package com.yls.app.persistence.mapper;

import java.util.List;
import com.yls.app.entity.ClientDialogType;
import com.yls.app.entity.DialogType2;

/**
 * @author huangsy
 * @date 2018年3月7日上午9:10:37
 */
public interface ClientCacheMapper {

	List<ClientDialogType> findAllClientDialogType();
	
	List<DialogType2> findAllDialogTypeV2();
	
	List<DialogType2> findAllDialogTypeSelfV2();
	
}
