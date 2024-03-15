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

    	JSONObject data = new JSONObject();
    	JSONObject objCols1 = new JSONObject();
    	JSONObject objCols2 = new JSONObject();
    	JSONObject objCols3 = new JSONObject();
    	JSONArray arryCols = new JSONArray();
    	JSONArray arryRows = new JSONArray();

    	objCols1.put("type", "string");
    	objCols2.put("type", "number");
    	objCols2.put("label", "장애");
    	objCols3.put("type", "number");
    	objCols3.put("label", "일정지연");
//ERR_CNT, ORDRG_ID_NM, NORMAL_CNT,  AFT_ERR_CNT
    	arryCols.add(objCols1);
    	arryCols.add(objCols2);
    	arryCols.add(objCols3);
    	try {
	    	for(int i = 0; i < result.size(); i++) {
	    		JSONObject legend = new JSONObject();
	    		legend.put("v", result.get(i).get("ordrgIdNm"));
	    		legend.put("f", null);
	    		JSONObject value1 = new JSONObject();
	    		value1.put("v", result.get(i).get("errCnt"));
	    		value1.put("f", null);
	    		JSONObject value2 = new JSONObject();
	    		value2.put("v", result.get(i).get("aftErrCnt"));
	    		value2.put("f", null);
	    		
	    		JSONArray cValueArry = new JSONArray();
	    		cValueArry.add(legend);
	    		cValueArry.add(value1);
	    		cValueArry.add(value2);
	    		
	    		JSONObject cValueObj = new JSONObject();
	    		cValueObj.put("c", cValueArry);
	    		
	    		arryRows.add(cValueObj);
	    	}
    	}catch(Exception e){
    	    System.out.println("결과 자료변환 중 오류 발생");
        }
        
    	data.put("cols", arryCols);
    	data.put("rows", arryRows);
    	model.addAttribute("chartData", data);
	    	
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
