package com.dksys.biz.user.pm.pm01;

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

import com.dksys.biz.admin.bm.bm01.mapper.BM01Mapper;
import com.dksys.biz.admin.bm.bm02.mapper.BM02Mapper;
import com.dksys.biz.admin.cm.cm05.mapper.CM05Mapper;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr02.mapper.CR02Mapper;
import com.dksys.biz.user.pm.pm01.service.PM01Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm01")
public class PM01Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  PM01Svc pm01Svc;

  @Autowired
  BM02Mapper bm02Mapper;

  @Autowired
  CR02Mapper cr02Mapper;

  @Autowired
  BM01Mapper bm01Mapper;

  @Autowired
  CM05Mapper cm05Mapper;
  
  // 작업일보 리스트 조회
  @PostMapping(value = "/selectDailyWorkList")
  public String selectDailyWorkList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = pm01Svc.selectDailyWorkCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = pm01Svc.selectDailyWorkList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
  // 작업일보 이슈 리스트 조회
  @PostMapping(value = "/selectIssueWorkList")
  public String selectIssueWorkList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = pm01Svc.selectIssueWorkCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = pm01Svc.selectIssueWorkList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  

  // 개인별작업일보현황(업무별)
  @PostMapping(value = "/selectWorkPrtList")
  public String selectWorkPrtList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    List<Map<String, String>> result = pm01Svc.selectWorkPrtList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
  // 개인별작업일보현황(수주별)
  @PostMapping(value = "/selectWorkOrdrsList")
  public String selectWorkOrdrsList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = pm01Svc.selectWorkOrdrsCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = pm01Svc.selectWorkOrdrsList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  // 작업일보 정보 조회
  @PostMapping(value = "/selectDailyWorkInfo")
  public String selectDailyWorkInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	Map<String, String> result = pm01Svc.selectDailyWorkInfo(paramMap);
    model.addAttribute("result", result);
    paramMap.put("workRptNo", result.get("workRptNo"));
    paramMap.put("workRptId", result.get("workRptId"));
    
    List<Map<String, String>> tripRows = pm01Svc.selectDailyWorkTripRows(paramMap);
    model.addAttribute("tripRows", tripRows);
    return "jsonView";
  }
  

  @PostMapping(value = "/selectConfirmCount")
  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int result = pm01Svc.selectConfirmCount(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  @PostMapping(value = "/insertDailyWork")
  public String insertDailyWork(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  try {
		  inputFieldExistCheck(paramMap, model);
	  } catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
	  }
	  if ((Integer.parseInt("222") == (int) model.get("resultCode"))) {
		  try {
				if (pm01Svc.insertDailyWork(paramMap, mRequest) != 0 ) {
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
	  } else {
		  //입력 오류에 대한 추가 처리 내용 
	  }
	  return "jsonView";
  }

  @PostMapping(value = "/updateDailyWork")
  public String updateDailyWork(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  try {
		  inputFieldExistCheck(paramMap, model);
	  } catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
	  }
	  if ((Integer.parseInt("222") == (int) model.get("resultCode"))) {
		  	try {
				if (pm01Svc.updateDailyWork(paramMap, mRequest) != 0 ) {
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
	  }
	  	return "jsonView";
  }

  @PutMapping(value = "/deleteDailyWork")
  public String deleteDailyWork(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (pm01Svc.deleteDailyWork(paramMap) != 0 ) {
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


  // 월 개인별 작업일보 집계 조회
  @PostMapping(value = "/selectMonthlyWorkList")
  public String selectMonthlyWorkList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    List<Map<String, String>> result = pm01Svc.selectMonthlyWorkList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
  // 입력필드 오류체크
  public void inputFieldExistCheck(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  
	  model.addAttribute("resultCode", 222);
	  model.addAttribute("resultMessage", ""); 
	  //오더, salesCode용 coCd처리(ordCoCd 값이 없으면 기본 coCd값을 적용함
	  if (paramMap.get("ordCoCd")==null ||"".equals(paramMap.get("ordCoCd"))) {
		  paramMap.put("ordCoCd", paramMap.get("coCd"));
	  }
	  
	  int cnt = 0; 
	  //salescode 체크(salesCd)
	  //salesCode가 있으면 salesCode에 있는 정보를 기준으로 재설정함 
	  if (paramMap.get("salesCd")==null ||"".equals(paramMap.get("salesCd"))) {
		  //작업 내용에 따라 옵션 처리
	  }	else {
		  cnt = cr02Mapper.salesCdExistValidationCheck(paramMap);
		  if (cnt == 0) {
			  	model.addAttribute("resultCode", 500);
			  	model.addAttribute("resultMessage", messageUtils.getMessage("salesCdNotFound"));
			 	return;
		  }	else {
			  Map<String, String> result = cr02Mapper.salesCdSearchOrderInfo(paramMap);
			  Object salesCdResult = result.get("salesCd");
			  if (salesCdResult==null ||"".equals(salesCdResult.toString())) {
				  	model.addAttribute("resultCode", 500);
				  	model.addAttribute("resultMessage", messageUtils.getMessage("salesCdNotFound"));
				 	return;
			  } else {
				  paramMap.put("clntCd", result.get("ordrsClntCd"));
				  paramMap.put("ordrsNo", result.get("ordrsNo"));
				  paramMap.put("prjctCd", result.get("clntPjt"));
				  paramMap.put("clntPjt", result.get("clntPjt"));
				  paramMap.put("eqpNm", result.get("eqpNm"));
				  paramMap.put("prdtCd", result.get("prdtCd"));
				  paramMap.put("itemDiv", result.get("itemDiv"));
				  paramMap.put("itemCd", result.get("itemDiv"));
				  return;
			  }
		  }  
	  }

	  //수주번호 체크 (ordrsNo)
	  if (paramMap.get("ordrsNo")==null ||"".equals(paramMap.get("ordrsNo"))) {
		  //작업 내용에 따라 옵션 처리
	  }	else {
		  cnt = cr02Mapper.orderNoExistValidationCheck(paramMap);
		  if (cnt == 0) {
					model.addAttribute("resultCode", 500);
					model.addAttribute("resultMessage", messageUtils.getMessage("ordrsNoNotFound"));
				 	return;
		  } else {
			  Map<String, String> result = cr02Mapper.selectOrdrsInfoToOrdrsNo(paramMap);
			  Object ordrsNoResult = result.get("ordrsNo");
			  if (ordrsNoResult==null ||"".equals(ordrsNoResult.toString())) {
			  } else {
				  paramMap.put("clntCd", result.get("ordrsClntCd"));
				  paramMap.put("clntPjt", result.get("clntPjt"));
				  paramMap.put("prjctCd", result.get("clntPjt"));
				  paramMap.put("clntNm", result.get("ordrsClntNm"));
			  }
		  }
	  }
		
	  //고객사 체크 (clntCd)
	  if (paramMap.get("clntNm")==null ||"".equals(paramMap.get("clntNm"))) {  //입력값이 있는지 확인
		  //작업 내용에 따라 옵션 처리
	  }	else {
		  cnt = bm02Mapper.selectClntExistCount(paramMap);
		  if (cnt == 0) {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("clntNotFound"));
			 	return;
		  }	  
	  } 
	  
	  //고객사pjt 체크(clntPjt)
	  //Commbo Box로 프론트엔드에서 체크됨
	  
	  //제품구분 체크(prdtCd)
	  if (paramMap.get("prdtCd")==null ||"".equals(paramMap.get("prdtCd"))) {  //입력값이 있는지 확인
		  //작업 내용에 따라 옵션 처리
	  }	else {
		  cnt = bm01Mapper.prdtCdExistValidationCheck(paramMap);
		  if (cnt == 0) {
//		  Object prdtCdResult = result1.get("prdtCd");
//		  if (prdtCdResult==null ||"".equals(prdtCdResult.toString())) {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("prdtCdNotFound"));
			 	return;
		  }	  
	  }
	  //아이템 체크(itemDiv)
	  //Commbo Box로 프론트엔드에서 체크됨
  }
  
  // 업로드파일 정보 조회
  @PostMapping(value = "/selectUploadFileList")
  public String selectUploadFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	List<Map<String, String>> result = pm01Svc.selectUploadFileList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
  
  // 작업일보 이슈 리스트 조회
  @PostMapping(value = "/selectAllIssueWorkList")
  public String selectAllIssueWorkList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = pm01Svc.selectAllIssueWorkListCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = pm01Svc.selectAllIssueWorkList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
  // 작업일보 리스트 조회
  @PostMapping(value = "/selectDailyWorkPrductList")
  public String selectDailyWorkPrductList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    List<Map<String, String>> result = pm01Svc.selectDailyWorkPrductList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

   	// 작업일보 이슈 리스트 (고객사)조회
	@PostMapping(value = "/selectNewAllIssueWorkList")
	public String selectNewAllIssueWorkList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = pm01Svc.selectNewAllIssueWorkListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = pm01Svc.selectNewAllIssueWorkList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
}