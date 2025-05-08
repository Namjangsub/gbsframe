package com.dksys.biz.user.qm.qm01;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.qm.qm01.service.QM01Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/qm/qm01")
public class QM01Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  QM01Svc qm01Svc;

// 발주 및 출장 요청서 리스트 조회
  @PostMapping(value = "/selectQualityReqList")
  public String selectQualityReqList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = qm01Svc.selectQualityReqCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = qm01Svc.selectQualityReqList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
//발주 및 출장 요청서 팝업 리스트 조회
 @PostMapping(value = "/selectPurchaseListPop")
 public String selectPurchaseListPop(@RequestBody Map<String, String> paramMap, ModelMap model) {
   int totalCnt = qm01Svc.selectPurchaseListPopCount(paramMap);
   PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
   model.addAttribute("paginationInfo", paginationInfo);
   List<Map<String, String>> result = qm01Svc.selectPurchaseListPop(paramMap);
   model.addAttribute("result", result);
   return "jsonView";
 }
   

  //요청 정보 
  @PostMapping(value = "/selectQtyReqInfo")
  public String selectQtyReqInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
        Map<String, String> result = qm01Svc.selectQtyReqInfo(paramMap);
        model.addAttribute("result", result);

        // 결재정보 확인
        List<Map<String, String>> approval = qm01Svc.selectApprovalChk(paramMap);
        model.addAttribute("approval", approval);
        return "jsonView";
  }
  
//요청 정보2 
  @PostMapping(value = "/selectQtyReqRespInfo")
  public String selectQtyReqRespInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
        Map<String, String> result = qm01Svc.selectQtyReqRespInfo(paramMap);
        model.addAttribute("result", result);

        // 결재정보 확인 reqNo에 결과서 번호를 넣어야 함.
        List<Map<String, String>> approval = qm01Svc.selectApprovalChk(paramMap);
        model.addAttribute("approval", approval);
        return "jsonView";
  }
  
  @PostMapping(value = "/selectShareUserlst")
  public String selectShareUserlst(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = qm01Svc.selectShareUserCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = qm01Svc.selectShareUserlst(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
   
  @PostMapping(value = "/selectShareResUserlst")
  public String selectShareResUserlst(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = qm01Svc.selectShareUserResCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = qm01Svc.selectShareResUserlst(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  

  @PostMapping(value = "/selectSignResUserlst")
  public String selectSignResUserlst(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = qm01Svc.selectSignResCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = qm01Svc.selectSignResUserlst(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
 /*@PostMapping(value = "/selectSignUserInfo")
  public String selectSignUserInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	List<Map<String, String>> resultList = qm01Svc.selectSignUserInfo(paramMap);
    model.addAttribute("resultList", resultList);
    return "jsonView";
  }
 */
  
  @PostMapping(value = "/selectApprovalChk")
  public String selectApprovalChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> result = qm01Svc.selectApprovalChk(paramMap);
	  model.addAttribute("result", result);
	  return "jsonView";
  }

  @PostMapping(value = "/selectConfirmCount")
  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int result = qm01Svc.selectConfirmCount(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  @PostMapping(value = "/insertQualityReq")
  public String insertQualityReq(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			//insertQualityReq 메서드를 호출. 결과를 담은 Map<String, String>을 반환
			Map<String, String> rtnResult = qm01Svc.insertQualityReq(paramMap, mRequest);
			
			//결과처리
			//"reqNo" 키에 해당하는 값을 가져와 문자열로 변환
			String reqNo = rtnResult.get("reqNo").toString();
			//문자열을 정수로 변환
			int result =  Integer.parseInt(rtnResult.get("result"));
			
			if (result !=0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
				//model.addAttribute("resultData", paramMap);
				model.addAttribute("reqNo", reqNo);
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
  
  
  
  @PostMapping(value = "/insertQualityResp")
  public String insertQualityResp(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (qm01Svc.insertQualityResp(paramMap, mRequest) != 0 ) {
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
  
  
  @PostMapping(value = "/updateQualityReq")
  public String updateQualityReq(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (qm01Svc.updateQualityReq(paramMap, mRequest) != 0 ) {
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

  @PostMapping(value = "/updateQualityResp")
  public String updateQualityResp(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (qm01Svc.updateQualityResp(paramMap, mRequest) != 0 ) {
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

	@PostMapping(value = "/updateQualityResultComment")
	public String updateQualityResultComment(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)
			throws Exception {
		try {
			if (qm01Svc.updateQualityResultComment(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
  
  
  @PutMapping(value = "/deleteQualityReq")
  public String deleteQualityReq(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (qm01Svc.deleteQualityReq(paramMap) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
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

  
  @PostMapping(value = "/selectApprovalList")
  public String selectApprovalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	List<Map<String, String>> resultList = qm01Svc.selectApprovalList(paramMap);
    model.addAttribute("resultList", resultList);
    return "jsonView";
  }
  
  @PostMapping("/selectCodeMaxCount")
  public String selectCodeMaxCount(@RequestBody Map<String, String> param, ModelMap model) {
  	int result = qm01Svc.selectCodeMaxCount(param);
  	model.addAttribute("result", result);
      return "jsonView";
  }

  // 하위코드 리스트 조회
  @PostMapping("/selectMainCodeList")
  public String selectMainCodeList(@RequestBody Map<String, String> param, ModelMap model) {
  	List<Map<String, String>> childCodeList = qm01Svc.selectMainCodeList(param);
  	model.addAttribute("childCodeList", childCodeList);
      return "jsonView";
  }
  
//결재라인 부서명등 select 
  @PostMapping(value = "/selectShareUserInfo")
  public String selectShareUserInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	List<Map<String, String>> resultList = qm01Svc.selectShareUserInfo(paramMap);
    model.addAttribute("resultList", resultList);
    return "jsonView";
  }
  
  @PostMapping(value = "/updateReqStChk")
  public String updateReqStChk(@RequestBody Map<String, String> paramMap, ModelMap model){
           int result = qm01Svc.updateReqStChk(paramMap);
				if ( result != 0 ) {
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", result);				
				}
				else {
					model.addAttribute("resultCode", 500);
					model.addAttribute("resultMessage", "QM01_updateReqStChk가 실행되지 않았습니다.");
				}
	  	return "jsonView";
  }
  
  @PostMapping(value = "/updateCheckDept")
  public String updateCheckDept(@RequestBody Map<String, String> paramMap, ModelMap model){
	  	try {
			if (qm01Svc.updateCheckDept(paramMap) != 0 ) {
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

//미발주현황 조회
 @PostMapping(value = "/nonOrderStatusList")
 public String nonOrderStatusList(@RequestBody Map<String, String> paramMap, ModelMap model) {
   List<Map<String, String>> result = qm01Svc.nonOrderStatusList(paramMap);
   model.addAttribute("result", result);
   return "jsonView";
 }
}