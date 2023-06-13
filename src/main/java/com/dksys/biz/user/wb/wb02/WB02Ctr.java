package com.dksys.biz.user.wb.wb02;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.exc.LogicException;
import com.dksys.biz.util.MessageUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.dksys.biz.user.wb.wb02.service.WB02Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb02")
public class WB02Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB02Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB02Svc wb02Svc;

     //<!-- WBS 일정계획 등록 메인 화면 조회 리스트  -->
	  @PostMapping(value = "/selectWbsRsltsPlanList") 
	  public String selectWbsRsltsPlanList(@RequestBody Map<String, String> paramMap, ModelMap model) {

		  int totalCnt = wb02Svc.selectWbsRsltsPlanListCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
  		  List<Map<String, String>> resultList = wb02Svc.selectWbsRsltsPlanList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	  }
	  
	  
	  //<!-- WBS 일정계획 등록 메인 화면 조회 엑셀 리스트  -->
	  @PostMapping(value = "/selectWbsRsltsPlanExcelList") 
	  public String selectWbsRsltsPlanExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		  List<Map<String, String>> resultList = wb02Svc.selectWbsRsltsPlanExcelList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	  }
	  
	//<!-- WBS 일정계획 실적 메인 화면 조회 엑셀 리스트  -->
	  @PostMapping(value = "/selectWbsRsltsResultExcelList1") 
	  public String selectWbsRsltsResultExcelList1(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		  List<Map<String, String>> resultList = wb02Svc.selectWbsRsltsResultExcelList1(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	  }
	  
	  
	//<!-- WBS 일정실적 등록 메인 화면 조회 리스트  -->
	  @PostMapping(value = "/selectWbsRsltsResultList1") 
	  public String selectWbsRsltsResultList1(@RequestBody Map<String, String> paramMap, ModelMap model) {

		  int totalCnt = wb02Svc.selectWbsRsltsResultCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
  		  List<Map<String, String>> resultList = wb02Svc.selectWbsRsltsResultList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
	  }
	  
	  
	  
	  //<!-- 실적 마스터 테이블 조회  --> 
	  @PostMapping(value = "/selectWbsRsltsMasterList") 
	  public String selectWbsRsltsMasterList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectWbsRsltsMasterList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- 실적 상세 테이블 조회  -->  
	  @PostMapping(value = "/selectWbsRsltsDetailList") 
	  public String selectWbsRsltsDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectWbsRsltsDetailList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- 공유 테이블 조회  -->  
	  @PostMapping(value = "/selectRsltsSharngList") 
	  public String selectRsltsSharngList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectRsltsSharngList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- 결재 테이블 조회  -->  
	  @PostMapping(value = "/selectRsltsApprovalList") 
	  public String selectRsltsApprovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectRsltsApprovalList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- 사용자 정보 테이블 조회  -->  
	  @PostMapping(value = "/selectRsltsMemberList") 
	  public String selectRsltsMemberList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectRsltsMemberList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	  
	  //<!-- 공유, 결재선 조회  -->     
	  @PostMapping(value = "/selectApprovalList") 
	  public String selectApprovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectApprovalList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	 
	  //<!-- /* 파일업로드를 위한 FielTreeKey Max값 조회 */ -->  
	  @PostMapping(value = "/selectMaxTrgtKey") 
	  public String selectMaxTrgtKey(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  int result = wb02Svc.selectMaxTrgtKey(paramMap);
		  model.addAttribute("result", result); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- /* WBS 데이터 중복 체크 조회 */ -->  
	  @PostMapping(value = "/selectWbsPlanChk")
	  public String selectWbsPlanChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int result = wb02Svc.selectWbsPlanChk(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	  }
	  
	  
	  
	  @PostMapping(value = "/wbsLevel1RsltsInsert")
      public String wbsLevel1RsltsInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {		
		
		List<Map<String, String>> detailChk = wb02Svc.deleteWbsRsltsDetailChk(paramMap);
		if (detailChk.size() > 0) {
			wb02Svc.deleteWbsRsltsDetail(paramMap);	
		}
		
		List<Map<String, String>> sharngChk = wb02Svc.deleteWbsSharngListChk(paramMap);
		if (sharngChk.size() > 0) {
			wb02Svc.deleteWbsSharngList(paramMap);	
		}
		
		List<Map<String, String>> approvalChk = wb02Svc.deleteWbsApprovalListChk(paramMap);
		if (approvalChk.size() > 0) {
			wb02Svc.deleteWbsApprovalList(paramMap);	
		}
		
		wb02Svc.wbsLevel1RsltsInsert(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	return "jsonView";
     }

	  //<!-- WBS 실적관리 메인화면에서 선택한 실적에 대한 계획정보 세부 조회  -->     
	  @PostMapping(value = "/selectWbsPlanInfoSelect") 
	  public String deleteWbsRsltsDetailChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb02Svc.selectWbsPlanInfoSelect(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  } 
	  
	  @PutMapping(value = "/wbsLevel1RsltsUpdate")
      public String wbsLevel1RsltsUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
	    List<Map<String, String>> detailChk = wb02Svc.deleteWbsRsltsDetailChk(paramMap);
	    if (detailChk.size() > 0) {
			wb02Svc.deleteWbsRsltsDetail(paramMap);	
	    }
		
	    List<Map<String, String>> sharngChk = wb02Svc.deleteWbsSharngListChk(paramMap);
	    if (sharngChk.size() > 0) {
			wb02Svc.deleteWbsSharngList(paramMap);	
	    }
		
	    List<Map<String, String>> approvalChk = wb02Svc.deleteWbsApprovalListChk(paramMap);
	    if (approvalChk.size() > 0) {
			wb02Svc.deleteWbsApprovalList(paramMap);	
	    }		
		  
		wb02Svc.wbsLevel1RsltsUpdate(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
     }
	  
	 //<!-- WBS Plan No Max값 가져오기  -->
	 @PostMapping(value = "/selectMaxWbsPlanNo")
	 public String selectMaxWbsPlanNo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = wb02Svc.selectMaxWbsPlanNo(paramMap);
		model.addAttribute("result", result); 
	    return "jsonView"; 
	 } 
	  
	   // <!-- WBS 실적 상위공정 유무체크  -->
	  @PostMapping(value = "/selectWbsPlanConfirmCount") 
	  public String selectWbsPlanConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> result = wb02Svc.selectWbsPlanConfirmCount(paramMap);
		  model.addAttribute("result", result); 
		  return "jsonView"; 
		  
	  }
	  
	 //<!-- WBS일정실적 메인화면 삭제 시 삭제유무 체크  -->
	 @PostMapping(value = "/selectWbsPlanDeleteConfirmCount")
	 public String selectWbsPlanDeleteConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int result = wb02Svc.selectWbsPlanDeleteConfirmCount(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	 }
	 
	 @DeleteMapping(value = "/deleteWbsPlanlist")
     public String deleteWbsPlanlist(@RequestBody Map<String, String> paramMap, ModelMap model) {
		wb02Svc.deleteWbsPlanlist(paramMap);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	return "jsonView";
     } 
	 
	 @PutMapping(value = "/wbsPlanStsCodeUpdate")
     public String wbsPlanStsCodeUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
        wb02Svc.wbsPlanStsCodeUpdate(paramMap);
   	    model.addAttribute("resultCode", 200);
   	    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
   	 return "jsonView";
     }
	 
	 
	 
	 @PutMapping(value = "/updateWbsPlanLockYnLvl1")
     public String updateWbsPlanLockYnLvl1(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
        wb02Svc.updateWbsPlanLockYnLvl1(paramMap);
   	    model.addAttribute("resultCode", 200);
   	    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
   	 return "jsonView";
     }

	 
	 
	 @PutMapping(value = "/updateWbsPlanLockYnLvl2")
     public String updateWbsPlanLockYnLvl2(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
        wb02Svc.updateWbsPlanLockYnLvl2(paramMap);
   	    model.addAttribute("resultCode", 200);
   	    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
   	 return "jsonView";
     }

	 
	 
	 @PutMapping(value = "/updateWbsPlanLockYnLvl3")
     public String updateWbsPlanLockYnLvl3(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
        wb02Svc.updateWbsPlanLockYnLvl3(paramMap);
   	    model.addAttribute("resultCode", 200);
   	    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
   	 return "jsonView";
     }
	 
	 
	 
	 
	 
	 
	 @PutMapping(value = "/updateWbsPlanCloseYn")
     public String updateWbsPlanCloseYn(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
        wb02Svc.updateWbsPlanCloseYn(paramMap);
        //wb02Svc.updateWbsRsltsMasterCloseYn(paramMap);
        //wb02Svc.updateWbsRsltsDetailCloseYn(paramMap);
   	    model.addAttribute("resultCode", 200);
   	    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
   	 return "jsonView";
     }

	 
	/*
	 * @PutMapping(value = "/updateWbsRsltsMasterCloseYn") public String
	 * updateWbsRsltsMasterCloseYn(@RequestParam Map<String, String> paramMap,
	 * MultipartHttpServletRequest mRequest, ModelMap model) {
	 * wb02Svc.updateWbsRsltsMasterCloseYn(paramMap);
	 * model.addAttribute("resultCode", 200); model.addAttribute("resultMessage",
	 * messageUtils.getMessage("update")); return "jsonView"; }
	 * 
	 * 
	 * 
	 * @PutMapping(value = "/updateWbsRsltsDetailCloseYn") public String
	 * updateWbsRsltsDetailCloseYn(@RequestParam Map<String, String> paramMap,
	 * MultipartHttpServletRequest mRequest, ModelMap model) {
	 * wb02Svc.updateWbsPlanCloseYn(paramMap); model.addAttribute("resultCode",
	 * 200); model.addAttribute("resultMessage", messageUtils.getMessage("update"));
	 * return "jsonView"; }
	 */
	 
	 
	 
	 

	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
		 
}