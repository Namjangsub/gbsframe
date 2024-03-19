package com.dksys.biz.user.qm.qm07;

import java.util.HashMap;
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

import com.dksys.biz.user.cr.cr21.service.CR21Svc;
import com.dksys.biz.user.qm.qm07.service.QM07Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Controller
@RequestMapping("/user/qm/qm07")
public class QM07Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  QM07Svc qm07Svc;

// 발주 및 출장 요청서 리스트 조회
  @PostMapping(value = "/selectQualityReqSCDSTSList")
  public String selectQualityReqSCDSTSList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    int totalCnt = qm07Svc.selectQualityReqSCDSTSListCount(paramMap);
	    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
	    model.addAttribute("paginationInfo", paginationInfo);
	    List<Map<String, String>> result = qm07Svc.selectQualityReqSCDSTSList(paramMap);
		model.addAttribute("list", result);
	    	
		return "jsonView";
  }

//발주 및 출장 요청서 검색조건용 자료 조회
 @PostMapping(value = "/selectQualityReqSCDSTSOption")
 public String selectQualityReqSCDSTSOption(@RequestBody Map<String, String> paramMap, ModelMap model) {
	    List<Map<String, String>> resultDept = qm07Svc.selectQualityReqSCDSTSDept(paramMap);
		model.addAttribute("resultDept", resultDept);
	    List<Map<String, String>> resultClnt = qm07Svc.selectQualityReqSCDSTSClnt(paramMap);
		model.addAttribute("resultClnt", resultClnt);
	    List<Map<String, String>> resultPrjct = qm07Svc.selectQualityReqSCDSTSPrjct(paramMap);
		model.addAttribute("resultPrjct", resultPrjct);
		return "jsonView";
 }
  
}
