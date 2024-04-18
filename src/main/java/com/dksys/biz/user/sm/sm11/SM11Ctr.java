package com.dksys.biz.user.sm.sm11;

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

import com.dksys.biz.user.sm.sm11.service.SM11Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm11")
public class SM11Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  SM11Svc sm11Svc;

//   프로젝트 리스트 조회
  @PostMapping(value = "/selectContractList")
  public String selectPchsCostList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = sm11Svc.selectContractCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = sm11Svc.selectContractList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  // 프로젝트 정보 조회
  @PostMapping(value = "/selectContractInfo")
  public String selectContractInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	Map<String, String> result = sm11Svc.selectContractInfo(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  

  @PostMapping(value = "/insertContract")
  public String insertPchsCost(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm11Svc.insertContract(paramMap, mRequest) != 0 ) {
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

  @PostMapping(value = "/updateContract")
  public String updateContract(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (sm11Svc.updateContract(paramMap, mRequest) != 0 ) {
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

  @PutMapping(value = "/deleteContract")
  public String deleteContract(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (sm11Svc.deleteContract(paramMap) != 0 ) {
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

  //salesCode 리스트 조회
  @PostMapping(value = "/selectTurnKeySalesCodeList")
  public String selectTurnKeySalesCodeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    List<Map<String, String>> result = sm11Svc.selectTurnKeySalesCodeList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

	
	//메일발송상태 update
  @PostMapping(value = "/updateMailEtcPchsOrderConfirm")
  public String updateMailEtcPchsOrderConfirm(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
		try {
			if (sm11Svc.updateMailEtcPchsOrderConfirm(param) != 0 ) {
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
  
  @PostMapping(value = "/etcPchsOrderMasterReport")
  public String etcPchsOrderMasterReport(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (sm11Svc.etcPchsOrderMasterReport(paramMap, mRequest) != 0 ) {
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
  
//   외주관리 지급대상 리스트 조회
  @PostMapping(value = "/selectContractLPayList")
  public String selectContractLPayList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  int totalCnt = sm11Svc.selectContractLPayCount(paramMap);
	  PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
	  model.addAttribute("paginationInfo", paginationInfo);
	  List<Map<String, String>> result = sm11Svc.selectContractLPayList(paramMap);
	  model.addAttribute("result", result);
	  return "jsonView";
  }

  @PutMapping(value = "/createContractBill")
  public String createContractBill(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (sm11Svc.createContractBill(paramMap) != 0 ) {
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
  
  @PutMapping(value = "/deleteContractBill")
  public String deleteContractBill(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  try {
		  if (sm11Svc.deleteContractBill(paramMap) != 0 ) {
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
	  
}