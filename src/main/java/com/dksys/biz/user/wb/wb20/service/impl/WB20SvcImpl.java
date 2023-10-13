package com.dksys.biz.user.wb.wb20.service.impl;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.wb.wb20.mapper.WB20Mapper;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB20SvcImpl implements WB20Svc {
    
    @Autowired
    WB20Mapper wb20Mapper;

    
    @Autowired
    WB20Svc wb20Svc;
    

    @Autowired
    ExceptionThrower thrower;
    
	
	@Override
	public int selectToDoCount(Map<String, String> paramMap) {	
		return wb20Mapper.selectToDoCount(paramMap);
	}

	@Override
	public List<Map<String, String>> selectToDoList(Map<String, String> paramMap) {
		return wb20Mapper.selectToDoList(paramMap);
	}
	
	public int toDoCfDtUpdate(Map<String, String> paramMap) {
		int result = wb20Mapper.toDoCfDtUpdate(paramMap);
		return result;
	}

	
	public int updateRsltsApproval(Map<String, String> paramMap) {
		int result = wb20Mapper.updateRsltsApproval(paramMap);
		return result;
	}
	
	@Override
	public List<Map<String, String>> selectApprovalChk(Map<String, String> paramMap) {
		return wb20Mapper.selectApprovalChk(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectTodoDivList(Map<String, String> paramMap) {
		return wb20Mapper.selectTodoDivList(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectApprovalYnList(Map<String, String> paramMap) {
		return wb20Mapper.selectApprovalYnList(paramMap);
	}
	

	public int updateRsltsQmApproval(Map<String, String> paramMap) {
		int result = wb20Mapper.updateRsltsQmApproval(paramMap);		
		/*
		 * List<Map<String, String>> todoMaxChk =
		 * wb20Mapper.selectApprovalMaxTodoKeyChk(paramMap); if (todoMaxChk.toString()
		 * == paramMap.get("fileTrgtKey")) { wb20Mapper.updateQmQeqst(paramMap); }
		 */
				 
		return result;
	}
		
	public int updateQmMobileApproval(Map<String, String> paramMap) {
		int result = wb20Mapper.updateQmMobileApproval(paramMap);
		return result;
	}
	
	@Override
	public List<Map<String, String>> selectGetDeptList(Map<String, String> paramMap) {
		return wb20Mapper.selectGetDeptList(paramMap);
	}	
	
	/* 공통결재 라인 read */
	@Override
	public List<Map<String, String>> selectApprovalList(Map<String, String> paramMap) {
		return wb20Mapper.selectApprovalList(paramMap);
	}	

	/* 공통결재 라인 insert */
	@Override
	public int insertApprovalLine(Map<String, String> paramMap) {

		int result = 0;	    
		result += wb20Mapper.updateApprovalLine(paramMap);		
	    	    		  		
		return result;
	}		
	
	  // 결재라인 싱글 셀렉트 read	
	  @Override
	  public List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap) {
	    return wb20Mapper.selectSignResUserlst(paramMap);
	  }

	  @Override
	  public List<Map<String, String>> selectSignResUserlstInit(Map<String, String> paramMap) {
	    return wb20Mapper.selectSignResUserlstInit(paramMap);
	  }	  
	  
	  //결재라인 부서명등 select 	  
	  @Override
	  public List<Map<String, String>> selectShareUserInfo(Map<String, String> paramMap) {
	    return wb20Mapper.selectShareUserInfo(paramMap);
	  }	
	  

	  @Override	  
	  public String selectmaxTodoKey(Map<String, String> paramMap) {
		  return wb20Mapper.selectmaxTodoKey(paramMap);
	  }		  

	  //wb20 결재 insert
	  @Override
	  public int insertTodoMaster(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("approvalArr"), dtlMap);
		
		int result = 0;
		String maxTodoKey = "";		
		if( paramMap.containsKey("approvalArr") ) {		
			//수정시 삭제된 부분만 처리가 불가하여 전체 삭제후 저장
			wb20Mapper.deleteAllTodoMaster(detailMap.get(0));
			
			//결제라인 insert			
			for(Map<String, String> dtl : detailMap) {
				//입력, 수정 
				if( dtl.get("todoKey").equals("") ) {
					maxTodoKey = wb20Svc.selectmaxTodoKey(dtl);
					dtl.put("todoKey", maxTodoKey);
				}
				result += wb20Mapper.insertTodoMaster(dtl);				
			}		
		}				
		return result;
	}  	 
	  
	  //wb20 todo 삭제
	  @Override
	  public int deleteTodoMaster(Map<String, String> param) {
		  return wb20Mapper.deleteTodoMaster(param);
	  }
}