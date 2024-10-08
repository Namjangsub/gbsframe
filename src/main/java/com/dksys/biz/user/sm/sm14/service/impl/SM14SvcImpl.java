package com.dksys.biz.user.sm.sm14.service.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dksys.biz.util.ExceptionThrower;
import com.dksys.biz.user.sm.sm14.mapper.SM14Mapper;
import com.dksys.biz.user.sm.sm14.service.SM14Svc;
import com.dksys.biz.admin.cm.cm08.service.CM08Svc;
import com.dksys.biz.admin.cm.cm15.service.CM15Svc;

@Service
@Transactional(rollbackFor = Exception.class)
public class SM14SvcImpl implements SM14Svc {

	@Autowired
	SM14Mapper sm14Mapper;	
	
	@Autowired
	SM14Svc sm14Svc;	
	
	@Autowired
	CM15Svc cm15Svc;
	
	@Autowired
	CM08Svc cm08Svc;	

	@Autowired
	ExceptionThrower thrower;


	@Override
	public int selectPurchaseListCount(Map<String, String> paramMap) {
		return sm14Mapper.selectPurchaseListCount(paramMap);
	}
	

	@Override
	public List<Map<String, String>> selectPurchaseList(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
		if( paramMap.get("union").equals("1") ) {
			result = sm14Mapper.selectPurchaseListUnion(paramMap);
		} else {
			result = sm14Mapper.selectPurchaseList(paramMap);			
		}
		return result;		
	}	
		
	@Override
	public List<Map<String, String>> selectPurchaseExcelList(Map<String, String> paramMap) {
		return sm14Mapper.selectPurchaseExcelList(paramMap);
	}	

	@Override
	public List<Map<String, String>> selectClntPurchaseList(Map<String, String> paramMap) {		
		return sm14Mapper.selectClntPurchaseList(paramMap);
	}	

	@Override
	public List<Map<String, String>> selectClntPurchaseDetailList(Map<String, String> paramMap) {		
		return sm14Mapper.selectClntPurchaseDetailList(paramMap);
	}	
			

	@Override
	public String selectMaxPchsNo(Map<String, String> paramMap) {
		return sm14Mapper.selectMaxPchsNo(paramMap);
	}		
		  
	/* 입고 등록시 발주 list */
	@Override
	public List<Map<String, String>> selectPurchaseDetailList(Map<String, String> paramMap) {
		return sm14Mapper.selectPurchaseDetailList(paramMap);
	}			
	
	//매입확정 detail insert
	@Override
	public int insertPurchaseBillDetail(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		
		
		int result = 0;	    

	    //MAX ORDGR_NO - 최조 입력일 경우만
		String maxPchsNo = "";
		String pchsNo = "";	 		
		String preOrdrgNo = "";
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("maxPchsNo", maxPchsNo);
			pchsNo = dtl.get("pchsNo").toString();
			//뒤 5자리
			pchsNo = pchsNo.substring(pchsNo.length()-5, pchsNo.length());
			//같은 발주번호인지 비교를 위해서 쓴다.
			if( pchsNo.equals("00000") || dtl.get("actFlag").equals("I") ) {
				//최초 실행이거나 이전발주번호 현재 발주번호가 다를때 신규매입번호 select
				if( !preOrdrgNo.equals(dtl.get("ordrgNo")) || preOrdrgNo.equals("") ) {					
					maxPchsNo = sm14Svc.selectMaxPchsNo(paramMap);					
					preOrdrgNo = dtl.get("ordrgNo");			
				} else if( preOrdrgNo.equals(dtl.get("ordrgNo")) ){

				}
				dtl.put("maxPchsNo", maxPchsNo);
				//비용이 경우 그대로 insert 발주일경우 자재수만큼 insert
				if( dtl.get("isCost").equals("Y") ) {
		    		result += sm14Mapper.insertPurchaseBillDetail(dtl);	
				} else if( dtl.get("isCost").equals("N") ) {
		    		result += sm14Mapper.insertPurchaseBillDetailOrdrg(dtl);
				}
			}
			//전체 확정으로 변경 - 확정일자요 확정여부 일괄 update
			if( dtl.get("actFlag").equals("U") && !pchsNo.equals("") ) {
				dtl.put("maxPchsNo", dtl.get("pchsNo"));				
	    		result += sm14Mapper.updatePurchaseBillDetailOrdrg(dtl);			    		
			}							
		}
		return result;
	}
	
	//매입확정 detail 선택 insert
	@Override
	public int insertinsertPurchaseSel(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		
		
		int result = 0;	    
	    //MAX ORDGR_NO - 최조 입력일 경우만
		String maxPchsNo = "";
		String pchsNo = "";	 		
		String preOrdrgNo = "";
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			dtl.put("maxPchsNo", maxPchsNo);
			pchsNo = dtl.get("pchsNo").toString();
			//뒤 5자리
			pchsNo = pchsNo.substring(pchsNo.length()-5, pchsNo.length());
			//같은 발주번호인지 비교를 위해서 쓴다.
			if( pchsNo.equals("00000")  ) {
				//최초 실행이거나 이전발주번호 현재 발주번호가 다를때 신규매입번호 select
				if( !preOrdrgNo.equals(dtl.get("ordrgNo")) || preOrdrgNo.equals("") ) {					
					maxPchsNo = sm14Svc.selectMaxPchsNo(paramMap);					
					preOrdrgNo = dtl.get("ordrgNo");			
				} else if( preOrdrgNo.equals(dtl.get("ordrgNo")) ){

				}
				dtl.put("maxPchsNo", maxPchsNo);
		    	result += sm14Mapper.insertPurchaseBillDetailOrdrg(dtl);	
		    //수정일 경우
			} else if( !pchsNo.equals("00000")  ) {
				dtl.put("maxPchsNo", dtl.get("pchsNo").toString());
		    	result += sm14Mapper.updatePurchaseBillDetail(dtl);				
			}
			
		}
		return result;
	}	
	
	
	//매입확정 DETAIL 수정
	@Override
	public int updatePurchaseBillDetail(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		

		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    HashMap<String, String> param = new HashMap<>();
	    param.put("userId", paramMap.get("userId"));
	    param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
	    	
		int result = 0;	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			
    		//result += sm14Mapper.updatePurchaseBillDetail(dtl);	
    		
    		//TB_SM12M01 TB_SM12M01 UPDATE -- 사용안함
    		if( paramMap.get("cmpletYn").equals("Y") ) {
        		//sm14Mapper.updatePurchaseMaster(dtl);    			
    		}
		}			

	    
		return result;
	}	
	
	@Override
	public int deletePurchaseDetail(Map<String, String> param) {
		int result = 0;
		result = sm14Mapper.deletePurchaseDetail(param);

		String ordrgNo = param.get("ordrgNo");
		String orderPrefix = ordrgNo.substring(0, 4);
		if (orderPrefix.equals("COST")) { 		//기타매입인경우
			result += sm14Mapper.updateETCIpgoDataPurchaseBillNoClear(param);
		} else {
			result += sm14Mapper.updateIpgoDataPurchaseBillNoClear(param);
		}
		
		
		String rtnString = sm14Mapper.selectPurchaseDetailCount(param);
		if("0".equals(rtnString)) {
			result = sm14Mapper.deletePurchaseMaster(param);
		}
		
		return result;
	}	
	
	//세금계산서발행여부 
	@Override
	public int updateBillYn(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		

		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    HashMap<String, String> param = new HashMap<>();
	    param.put("userId", paramMap.get("userId"));
	    param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
	    	
		int result = 0;	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			
//			//billYn이 Y : 매입계산서 발행확인,  N : 계산서 발행 취소 처리
//			if("Y".equals(dtl.get("billYn"))) {
//				result += sm14Mapper.updateBillYn(dtl);	
//			}
//    		
			result += sm14Mapper.updateBillYn(dtl);	
    
		}			

		return result;
	}
	
	//세금계산서발행여부 
	@Override
	public int updateBillSeqYn(Map<String, String> paramMap) throws Exception {

		Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
		Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
		List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("makeArr"), dtlMap);		

		//---------------------------------------------------------------  
		//첨부 화일 처리 권한체크 시작 -->파일 업로드, 삭제 권한 없으면 Exception 처리 됨
	  	//   필수값 :  jobType, userId, comonCd
		//---------------------------------------------------------------  
	    HashMap<String, String> param = new HashMap<>();
	    param.put("userId", paramMap.get("userId"));
	    param.put("comonCd", paramMap.get("comonCd"));  //프로트엔드에 넘어온 화일 저장 위치 정보
	    	
		int result = 0;	    	    
	    //upate
		for(Map<String, String> dtl : detailMap) {
			dtl.put("userId", paramMap.get("userId"));
			dtl.put("pgmId", paramMap.get("pgmId"));
			dtl.put("creatId", paramMap.get("userId"));
			
			result += sm14Mapper.updateBillSeqYn(dtl);	
    
		}			

		return result;
	}
	
	
	/* 발주자재리스트 */
	@Override
	public List<Map<String, String>> selectOrdrgMatList(Map<String, String> paramMap) {
		return sm14Mapper.selectOrdrgMatList(paramMap);
	}		
	
	

	@Override
	public int sm14selectPurchaseListNewCount(Map<String, String> paramMap) {
		return sm14Mapper.sm14selectPurchaseListNewCount(paramMap);
	}
	
	@Override
	public List<Map<String, String>> sm14selectPurchaseListNew(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm14Mapper.sm14selectPurchaseListNew(paramMap);
		return result;		
	}		

	@Override
	public List<Map<String, String>> selectPurchaseListNew(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm14Mapper.selectPurchaseListNew(paramMap);
		return result;		
	}		
	
	@Override
	public List<Map<String, String>> selectClntPurchaseInboundList(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm14Mapper.selectClntPurchaseInboundList(paramMap);
		return result;		
	}	

	@Override
	public int selectPurchaseListCountNew(Map<String, String> paramMap) {
		return sm14Mapper.selectPurchaseListCountNew(paramMap);
	}
	
	@Override
	public List<Map<String, String>> selectOrderDetailListNew(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm14Mapper.selectOrderDetailListNew(paramMap);
		return result;		
	}	
	
	@Override
	public List<Map<String, String>> selectOrderDetailListNewNam(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		//발주+비용
			result = sm14Mapper.selectOrderDetailListNewNam(paramMap);
		return result;		
	}	

	@Override
	public List<Map<String, String>> selectPchsDetailListNew(Map<String, String> paramMap) {		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
			result = sm14Mapper.selectPchsDetailListNew(paramMap);
		return result;		
	}
	
	//매입확정 detail insert
	@Override
	public int insertPurchaseBillDetailNew(Map<String, String> paramMap) throws Exception {
			
		int result = 0;	    

		String maxPchsNo = "";

		if("".equals(paramMap.get("pchsNo")) || paramMap.get("pchsNo") == null ) {					
			maxPchsNo = sm14Mapper.selectMaxPchsNoNew(paramMap);		
			paramMap.put("maxPchsNo", maxPchsNo);
			result = sm14Mapper.insertPurchaseMaster(paramMap);
		}else {
			maxPchsNo = paramMap.get("pchsNo");	
			paramMap.put("maxPchsNo", maxPchsNo);
		}

		paramMap.put("cmpletYn", "Y");
		paramMap.put("pchsQty", "1");
		paramMap.put("matrCd", "");

		String ordrgNo = paramMap.get("ordrgNo");
		String orderPrefix = ordrgNo.substring(0, 4);
		if (orderPrefix.equals("COST")) {
			paramMap.put("inNo", ordrgNo);
		}
		//매입확정번호 및 자료 생성
		result = sm14Mapper.insertPurchaseBillDetailNew(paramMap);

		if (orderPrefix.equals("COST")) {
			result += sm14Mapper.updateETCIpgoDataPurchaseBillNo(paramMap);
		} else {
			//매입자료에 대한 처리부분임 (매입확정은 일반입고, 기타비용, 반풐이있음)
			Gson gsonDtl = new GsonBuilder().disableHtmlEscaping().create();
			Type dtlMap = new TypeToken<ArrayList<Map<String, String>>>() {}.getType();	    		
			List<Map<String, String>> detailMap = gsonDtl.fromJson(paramMap.get("ipgoArr"), dtlMap);		
			if (detailMap != null && !detailMap.isEmpty()) {
				//매입확정 선택된 입고자료에 대해 매입확정번호 설정함
				for(Map<String, String> dtl : detailMap) {
					dtl.put("userId", paramMap.get("userId"));
					dtl.put("pgmId", paramMap.get("pgmId"));
					dtl.put("creatId", paramMap.get("userId"));
					dtl.put("maxPchsNo", maxPchsNo);
					dtl.put("pchsSeq", paramMap.get("pchsSeq"));
		    		result += sm14Mapper.updateIpgoDataPurchaseBillNo(dtl);		//입고자료에 매입확정번호 설정	    		
				}			
			}
		}
			
		return result;
	}	
	  
	  //multi select 검색
	  @Override
	  public List<Map<String, String>> select_prjct_code(Map<String, String> paramMap) {
		 return sm14Mapper.select_prjct_code(paramMap);
	  }
	  
	  //multi select 검색
	  @Override
	  public List<Map<String, String>> select_mngId_code(Map<String, String> paramMap) {
		 return sm14Mapper.select_mngId_code(paramMap);
	  }
	
}