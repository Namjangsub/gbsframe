package com.dksys.biz.user.wb.wb01;

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
import com.dksys.biz.user.wb.wb01.service.WB01Svc;
import com.dksys.biz.user.wb.wb02.service.WB02Svc;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/wb/wb01")
public class WB01Ctr {
	
	private Logger logger = LoggerFactory.getLogger(WB01Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    WB01Svc wb01Svc;
    
    @Autowired
    WB02Svc wb02Svc;

    //<!-- WBS 일정계획 등록 메인 화면 조회 리스트  -->
	  @PostMapping(value = "/selectWbsPlanList") 
	  public String selectWbsPlanList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  int totalCnt = wb01Svc.selectWbsPlanCount(paramMap); 
		  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		  model.addAttribute("paginationInfo", paginationInfo);
		  
  		  List<Map<String, String>> resultList = wb01Svc.selectWbsPlanList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	//<!-- WBS 일정계획 등록 메인 화면 조회 리스트  -->
	  @PostMapping(value = "/selectWbsPlanExcelList") 
	  public String selectWbsPlanExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
  		  List<Map<String, String>> resultList = wb01Svc.selectWbsPlanExcelList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	  
	 
	  //<!-- 관리번호(WBS계획번호) 조회 조건 팝업 리스트  -->
	  @PostMapping(value = "/selectWbsPlanNoList") 
	  public String selectWbsPlanNoList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsPlanNoList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- 수주번호 조회 조건 팝업 리스트  -->
	  @PostMapping(value = "/selectWbsSalesCodeList") 
	  public String selectWbsSalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsSalesCodeList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
		  
	  }
    
	  //<!-- 신작구분 조회 조건 리스트 Combobox  -->       
	  @PostMapping(value = "/selectWbsPlanDivList") 
	  public String selectWbsPlanDivList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsPlanDivList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- WBS Level1관련 공통코드 리스트 Combobox 처리 -->  
	  @PostMapping(value = "/selectWbsLevel1List") 
	  public String selectWbsLevel1List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsLevel1List(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- 설계난이도 Combobox 처리 -->  
	  @PostMapping(value = "/selectWbsDsgnDifList") 
	  public String selectWbsDsgnDifList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsDsgnDifList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- 생산난이도 Combobox 처리 -->  
	  @PostMapping(value = "/selectWbsPrdctnDifList") 
	  public String selectWbsPrdctnDifList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsPrdctnDifList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
		  
	  }
		  
	  //<!-- WBS Level2관련 공통코드 리스트 Combobox 처리 -->  
	  @PostMapping(value = "/selectWbsLevel2List") 
	  public String selectWbsLevel2List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsLevel2List(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
	  }
	  
	  
	  //<!-- WBS Level3관련 공통코드 리스트 Combobox 처리 -->  
	  @PostMapping(value = "/selectWbsLevel3List") 
	  public String selectWbsLevel3List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsLevel3List(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- WBS 계획상태 공통코드 리스트 Combobox 처리 -->  
	  @PostMapping(value = "/selectWbsPlanStsList") 
	  public String selectWbsPlanStsList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> codeInfoList = wb01Svc.selectWbsPlanStsList(paramMap);
		  model.addAttribute("codeInfoList", codeInfoList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- WBS 일정계획 등록 Level1 등록 팝업 화면 Level2 조회 리스트 처리 -->  
	  @PostMapping(value = "/selectWbsPopupLevel2PlanList") 
	  public String selectWbsPopupLevel2PlanList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb01Svc.selectWbsPopupLevel2PlanList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- WBS 일정계획 등록 Level2 등록 팝업 화면 Level2 조회 리스트 처리 -->  
	  @PostMapping(value = "/selectWbsPopupLevel3PlanList") 
	  public String selectWbsPopupLevel3PlanList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb01Svc.selectWbsPopupLevel3PlanList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- WBS 공유대상자 조회 리스트 처리 -->  
	  @PostMapping(value = "/selectWbsPopupSharngList") 
	  public String selectWbsPopupSharngList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb01Svc.selectWbsPopupSharngList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView"; 
		  
	  }
	  	  
	//<!-- WBS 공유대상자 삭제 처리 -->  
	 @DeleteMapping(value = "/deleteWbsSharngUser")
     public String deleteWbsSharngUser(@RequestBody Map<String, String> paramMap, ModelMap model) {
		wb01Svc.deleteWbsSharngUser(paramMap);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	return "jsonView";
     } 	
	 
	 //<!-- WBS일정계획 메인화면 삭제 시 삭제유무 체크  -->
	 @PostMapping(value = "/selectWbsPlanDeleteConfirmCount")
	 public String selectWbsPlanDeleteConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int result = wb01Svc.selectWbsPlanDeleteConfirmCount(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	 }

	 
	
	 //<!-- WBS Plan No Max값 가져오기  -->
	 @PostMapping(value = "/selectMaxWbsPlanNo")
	 public String selectMaxWbsPlanNo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int result = wb01Svc.selectMaxWbsPlanNo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	 }

	  @PostMapping(value = "/insertWbsSharngUser")
      public String insertWbsSharngUser(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		wb01Svc.insertWbsSharngUser(paramMap);
		wb01Svc.insertToDoList(paramMap);
		//model.addAttribute("shipSeq", paramMap.get("shipSeq"));
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	return "jsonView";
     }	  

	  
	  @PostMapping(value = "/wbsLevel1PlanInsert")
      public String wbsLevel1PlanInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
	    List<Map<String, String>> sharngChk = wb01Svc.deleteWbsSharngListChk(paramMap);
		if (sharngChk.size() > 0) {
			wb01Svc.deleteWbsSharngList(paramMap);	
		}
		wb01Svc.wbsLevel1PlanInsert(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	return "jsonView";
     }
	 
	  @PutMapping(value = "/wbsLevel1PlanUpdate")
      public String wbsLevel1PlanUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		List<Map<String, String>> sharngChk = wb01Svc.deleteWbsSharngListChk(paramMap);
		if (sharngChk.size() > 0) {
				wb01Svc.deleteWbsSharngList(paramMap);	
		}
		wb01Svc.wbsLevel1PlanUpdate(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
     }
	  
	  //<!-- WBS 1,2Level 정보 조회 리스트  -->  
	  @PostMapping(value = "/selectWbsInfoList") 
	  public String selectWbsInfoList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> resultList = wb01Svc.selectWbsInfoList(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView";  
	  }
	  
	  @PostMapping(value = "/wbsLevel2PlanInsert")
      public String wbsLevel2PlanInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
	    List<Map<String, String>> sharngChk = wb01Svc.deleteWbsSharngListChk(paramMap);
	    if (sharngChk.size() > 0) {
		   wb01Svc.deleteWbsSharngList(paramMap);	
	    }
		wb01Svc.wbsLevel2PlanInsert(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	return "jsonView";
     }
	 
	  @PutMapping(value = "/wbsLevel2PlanUpdate")
      public String wbsLevel2PlanUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		List<Map<String, String>> sharngChk = wb01Svc.deleteWbsSharngListChk(paramMap);
		if (sharngChk.size() > 0) {
				wb01Svc.deleteWbsSharngList(paramMap);	
		}
		wb01Svc.wbsLevel2PlanUpdate(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
     }

	  @PostMapping(value = "/wbsLevel3PlanInsert")
      public String wbsLevel3PlanInsert(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
	    List<Map<String, String>> sharngChk = wb01Svc.deleteWbsSharngListChk(paramMap);
	    if (sharngChk.size() > 0) {
		   wb01Svc.deleteWbsSharngList(paramMap);	
	    }
		wb01Svc.wbsLevel3PlanInsert(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	return "jsonView";
     }
	 
	  @PutMapping(value = "/wbsLevel3PlanUpdate")
      public String wbsLevel3PlanUpdate(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
	    List<Map<String, String>> sharngChk = wb01Svc.deleteWbsSharngListChk(paramMap);
	    if (sharngChk.size() > 0) {
		   wb01Svc.deleteWbsSharngList(paramMap);	
	    }
		wb01Svc.wbsLevel3PlanUpdate(paramMap, mRequest);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
     }
	  
	  //<!-- /* 파일업로드를 위한 FielTreeKey Max값 조회 */ -->  
	  @PostMapping(value = "/selectMaxTrgtKey") 
	  public String selectMaxTrgtKey(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  int result = wb01Svc.selectMaxTrgtKey(paramMap);
		  model.addAttribute("result", result); 
		  return "jsonView"; 
		  
	  }
	  
	  //<!-- /* WBS 데이터 중복 체크 조회 */ -->  
	  @PostMapping(value = "/selectWbsPlanChk")
	  public String selectWbsPlanChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int result = wb01Svc.selectWbsPlanChk(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	  }
	  
	   //<!-- TO-DO 리스트에서 넘겨받은 조건에 따른 WBS PLAN 정보 조회  -->
	  //<!-- WBS 1,2Level 정보 조회 리스트  -->  
	  @PostMapping(value = "/selectWbsPlanInfoSelect") 
	  public String selectWbsPlanInfoSelect(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>> resultList = wb01Svc.selectWbsPlanInfoSelect(paramMap);
		  model.addAttribute("resultList", resultList); 
		  return "jsonView";  
	  }

	  //<!-- /* WBS 상위레벨 데이터 체크 */ -->  
	  @PostMapping(value = "/selectWbsPlanConfirmCount")
	  public String selectWbsPlanConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int result = wb01Svc.selectWbsPlanConfirmCount(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	  }
	  
	  //<!-- /* 파일트리 구분코드 조회 */ -->  
	  @PostMapping(value = "/selectFileCodeSelect")
	  public String selectFileCodeSelect(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> result = wb01Svc.selectFileCodeSelect(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	  }
	  
	  //<!-- 사용자 정보 테이블 조회  -->  
	  @PostMapping(value = "/selectPlanSharngList") 
	  public String selectPlanSharngList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  
		  List<Map<String, String>> resultList = wb01Svc.selectPlanSharngList(paramMap);
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
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  //<!-- 실적 마스터 테이블 조회  --> 
	  @PostMapping(value = "/selectWbsPlanTreeList") 
	  public String selectWbsPlanTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {		  
		int totalCnt = wb01Svc.selectWbsPlanTreeListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> fileList = wb01Svc.selectWbsPlanTreeList(paramMap);
    	model.addAttribute("fileList", fileList);
        return "jsonView";
		  
	  }
	  
	  @DeleteMapping(value = "/deleteWbsPlanlist")
	     public String deleteWbsPlanlist(@RequestBody Map<String, String> paramMap, ModelMap model) {
			wb01Svc.deleteWbsPlanlist(paramMap);
	    	model.addAttribute("resultCode", 200);
	    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
	    	return "jsonView";
	     } 	
	  
	  
	  @PostMapping(value = "/selectWbsPlanInfo") 
	  public String selectWbsPlanInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {		  
		  Map<String, String> result = wb01Svc.selectWbsPlanInfo(paramMap);
		  model.addAttribute("result", result); 
		  return "jsonView"; 
	  }
	  
	  @PostMapping(value = "/insertWbsPlan")
	  public String insertWbsPlan(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
			try {
				
				if (wb01Svc.insertWbsPlan(paramMap, mRequest) != 0 ) {
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
	  
	  @PostMapping(value = "/updateWbsPlan")
	  public String updatePchsCost(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		  	try {
				if (wb01Svc.updateWbsPlan(paramMap, mRequest) != 0 ) {
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