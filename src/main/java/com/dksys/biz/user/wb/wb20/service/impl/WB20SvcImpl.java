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
import com.dksys.biz.user.qm.qm01.mapper.QM01Mapper;
import com.dksys.biz.user.wb.wb20.mapper.WB20Mapper;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Service
@Transactional(rollbackFor = Exception.class)
public class WB20SvcImpl implements WB20Svc {
    
    @Autowired
    WB20Mapper wb20Mapper;

    @Autowired
    WB24Mapper wb24Mapper;
    
    @Autowired
    QM01Mapper qm01Mapper;

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
	public List<Map<String, String>> selectGetApprovalList(Map<String, String> paramMap) {
		return wb20Mapper.selectGetApprovalList(paramMap);
	}	

	/* 공통결재 라인 insert */
	@Override
	public Map<String, String> insertApprovalLine(Map<String, String> paramMap) {

		int result = 0;	    
		result += wb20Mapper.updateApprovalLine(paramMap);	
		
		String tempReqNo = paramMap.get("todoNo");
		//TODODIV2020:발주 및 출장 요청 상태코드 바꾸기
		if ("TODODIV2020".equals(paramMap.get("todoDiv2CodeId"))) {
			//발주요청서 진행상태 변경 처리
			//REQ_ST: REQST01 --> REQST02 로 상태 변경처
			//result += qm01Mapper.updateReqStChk(paramMap);
			paramMap.put("reqNo",    tempReqNo);
			result += qm01Mapper.updateReqSt(paramMap);

		//TODODIV2030:발주 및 출장 요청 결과자료 상태코드 바꾸기
		} else if ("TODODIV2030".equals(paramMap.get("todoDiv2CodeId"))) {
			//REQ_ST: REQST02 --> REQST03 로 상태 변경처리
			paramMap.put("reqNo",    "REQ" + tempReqNo.substring(3, 10));
			result += qm01Mapper.updateReqStRslt(paramMap);

		//TODODIV2060:WBS이슈 발생에 대한 결재이면 이슈상태 변경처리
		} else if ("TODODIV2060".equals(paramMap.get("todoDiv2CodeId"))) {
			//ISS_STS: ISSSTS01 --> ISSSTS02 로 상태 변경처리
			result += wb24Mapper.updateWbsIssueStChk(paramMap);

		//TODODIV2090:WBS조치 이슈조치결과 담당팀장 위험도 평가내역 수정 처리
		} else if ("TODODIV2090".equals(paramMap.get("todoDiv2CodeId"))) {
			if (paramMap.containsKey("actDngEval")) {
				String value = paramMap.get("actDngEval");
				if (value != null && !value.isEmpty()) {
					result += wb24Mapper.updateWbsIssueResultEvaluate(paramMap);
				} 
			} 
		}
	    	
		//최종결재 완료시 알림톡 발송 대상인지 확인 
		Map<String, String> resultMap = wb20Mapper.selectTodoFinalYn(paramMap);
		resultMap.put("resultCount", Integer.toString(result));
		
		return resultMap;
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
					maxTodoKey = wb20Mapper.selectmaxTodoKey(dtl);
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
		  int result = wb20Mapper.deleteTodoMaster(param);
		  	  result = wb20Mapper.updateTodoMasterSanctnSn(param); 
		  return  result;
	  }
	  
	  
	  @Override
		public List<Map<String, String>> selectMobileTodoSelect(Map<String, String> paramMap) {
			return wb20Mapper.selectMobileTodoSelect(paramMap);
		}
	  
	  @Override
	  public Map<String, String> selectTodoFinalYn(Map<String, String> paramMap) {
		  return wb20Mapper.selectTodoFinalYn(paramMap);
	  }	  
		
	  @Override
	  public int updateApprovalCancle(Map<String, String> paramMap) {
		return wb20Mapper.updateApprovalCancle(paramMap);
	  } 

		@Override
		public int M08selectToDoCount(Map<String, String> paramMap) {	
			return wb20Mapper.M08selectToDoCount(paramMap);
		}

		@Override
		public List<Map<String, String>> M08selectToDoList(Map<String, String> paramMap) {
			return wb20Mapper.M08selectToDoList(paramMap);
		}
		
}