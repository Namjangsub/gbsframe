package com.dksys.biz.user.sm.sm02;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm02.service.SM02Svc;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/sm/sm02")
public class SM02Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM02Svc sm02Svc;

	// 매입관리 발주 조회
	@PostMapping(value = "/selectOrderList")
	public String selectPchsList(@RequestBody Map<String, String> paramMap, ModelMap model) {

//		//프로젝트 코드 배열을 쿼리 in함수용 파라메터로 변환작업  
//		String[] clntPjtArray = paramMap.get("clntPjt").split(",");  //'PRJCT98,PRJCT160,PRJCT159'
//		String clntPjt = "";
//		if (!("").equals(paramMap.get("clntPjt"))) {
//			clntPjt = "'";
//	        for (int i = 0; i < clntPjtArray.length; i++) {
//	        	clntPjt += clntPjtArray[i].trim(); // trim()을 사용하여 앞뒤 공백 제거
//	            if (i < clntPjtArray.length - 1) {
//	            	clntPjt += "','";
//	            }
//	        }
//	        clntPjt += "'";
//		}
//		paramMap.put("clntPjt", clntPjt);

		paramMap.put("clntPjt", ObjectUtil.sqlInCodeGen(paramMap.get("clntPjt")));
		paramMap.put("mngIdCd", ObjectUtil.sqlInCodeGen(paramMap.get("mngIdCd")));
		int totalCnt = sm02Svc.selectOrderListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm02Svc.selectOrderList(paramMap);
		model.addAttribute("result", result);
		

	   	List<Map<String, String>> resultPrjct = sm02Svc.select_prjct_code(paramMap);
	   	model.addAttribute("resultPrjct", resultPrjct);

	   	List<Map<String, String>> resultMngId = sm02Svc.select_mngId_code(paramMap);
	   	model.addAttribute("resultMngId", resultMngId);
	   	
		return "jsonView";
	}
	
	// BOM내역상세 조회
	@PostMapping(value = "/selectBomDetailList")
	public String selectBomDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectBomDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	// 발주상세 조회
	@PostMapping(value = "/selectOrderDetailList")
	public String selectOrderDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectOrderDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}	
	
	// 발주상세 팝업 조회
	@PostMapping(value = "/selectOrderDetailView")
	public String selectOrderDetailView(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectOrderDetailView(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}		
	
	//발주 등록
    @PostMapping(value = "/insertOrderMaster")
    public String insertOrderMaster(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.insertOrderMaster(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
  			} else {
  				model.addAttribute("resultCode", 500);
  				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
  			};
  		}catch(Exception e){
  			model.addAttribute("resultCode", 900);
  			model.addAttribute("resultMessage", e.getMessage());
  		}
  		return "jsonView";
    }	  
    
	//발주 수정
    @PostMapping(value = "/updateOrder")
    public String updateOrder(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.updateOrder(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
  			} else {
  				model.addAttribute("resultCode", 500);
  				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
  			};
  		}catch(Exception e){
  			model.addAttribute("resultCode", 900);
  			model.addAttribute("resultMessage", e.getMessage());
  		}
  		return "jsonView";
    }         

	//발주 수정
    @PostMapping(value = "/updateOrderDetail")
    public String updateOrderDetail(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.updateOrderDetail(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
  			} else {
  				model.addAttribute("resultCode", 500);
  				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
  			};
  		}catch(Exception e){
  			model.addAttribute("resultCode", 900);
  			model.addAttribute("resultMessage", e.getMessage());
  		}
  		return "jsonView";
    }    

    
	//발주 detail 삭제    
	@DeleteMapping(value = "/deleteOrderDetail")
	public String deleteOrderDetail(@RequestBody Map<String, String> param, ModelMap model) {
		sm02Svc.deleteOrderDetail(param);
		model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		return "jsonView";
	}
	
	//발주 등록시 현재환율 조회
	@PostMapping(value = "/selectCurrToday")
	public String selectCurrToday(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectCurrToday(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	//발주 등록시 구매단가 실시간 조회
	@PostMapping(value = "/selectCurrMatrUpr")
	public String selectCurrMatrUpr(@RequestBody Map<String, String> paramMap, ModelMap model) {
		String resultList = sm02Svc.selectCurrMatrUpr(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// 제한기준일자 조회
	@PostMapping(value = "/selectordrglimit")
	public String selectordrglimit(@RequestBody Map<String, String> paramMap, ModelMap model) {
		String resultList = sm02Svc.selectordrglimit(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	//발주삭제시 입고, 매입여부 체크
	@PostMapping(value = "/selectInPurchaseChk")
	public String selectInPurchaseChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm02Svc.selectInPurchaseChk(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}		
	
	//발주 detail 삭제    
	@DeleteMapping(value = "/deleteOrder")
	public String deleteOrder(@RequestBody Map<String, String> param, ModelMap model) {
		sm02Svc.deleteOrder(param);
		model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		return "jsonView";
	} 
	
	//메일발송상태 update
    @PostMapping(value = "/updateMailConfirm")
    public String updateMailConfirm(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.updateMailConfirm(param) != 0 ) {
  				model.addAttribute("resultCode", 200);
  				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
  			} else {
  				model.addAttribute("resultCode", 500);
  				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
  			};
  		}catch(Exception e){
  			model.addAttribute("resultCode", 900);
  			model.addAttribute("resultMessage", e.getMessage());
  		}
  		return "jsonView";
    }   	  
    
	//발주 일괄 결재 요청
    @PostMapping(value = "/updateOrderApprovalRequest")
    public String updateOrderApprovalRequest(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.updateOrderApprovalRequest(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
  				model.addAttribute("makeArr", paramMap.get("makeArr"));  //콜백에서 처리하기 위해 파라메터 전달함.
  			} else {
  				model.addAttribute("resultCode", 500);
  				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
  			};
  		}catch(Exception e){
  			model.addAttribute("resultCode", 900);
  			model.addAttribute("resultMessage", e.getMessage());
  		}
  		return "jsonView";
    } 

    //헤더 multi select 코드 검색
    @PostMapping(value = "/select_prjct_code")
    public String select_prjct_code(@RequestBody Map<String, String> paramMap, ModelMap model) {
   	List<Map<String, String>> result = sm02Svc.select_prjct_code(paramMap);
   	model.addAttribute("result", result);
   	return "jsonView";
    }

    //헤더 multi select 코드 검색
    @PostMapping(value = "/select_mngId_code")
    public String select_mngId_code(@RequestBody Map<String, String> paramMap, ModelMap model) {
   	List<Map<String, String>> result = sm02Svc.select_mngId_code(paramMap);
   	model.addAttribute("result", result);
   	return "jsonView";
    }
    
    @PostMapping(value = "/OrderMasterReport")
    public String OrderMasterReport(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm02Svc.OrderMasterReport(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
  			} else {
  				model.addAttribute("resultCode", 500);
  				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
  			};
  		}catch(Exception e){
  			model.addAttribute("resultCode", 900);
  			model.addAttribute("resultMessage", e.getMessage());
  		}
  		return "jsonView";
    }	  
 	  
    
	//발주 물품 당사 도착 확인 처리
  @PostMapping(value = "/arriveWareHousing")
  public String arriveWareHousing(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (sm02Svc.arriveWareHousing(paramMap) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
  }   
	  
  
//발주 물품 당사 도착 확인 처리
@PostMapping(value = "/detailArriveWareHousing")
public String detailArriveWareHousing(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
	try {
		if (sm02Svc.detailArriveWareHousing(paramMap) != 0 ) {
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("update"));
		} else {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		};
	}catch(Exception e){
		model.addAttribute("resultCode", 900);
		model.addAttribute("resultMessage", e.getMessage());
	}
	return "jsonView";
} 


}