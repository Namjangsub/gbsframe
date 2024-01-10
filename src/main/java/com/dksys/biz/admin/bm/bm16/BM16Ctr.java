package com.dksys.biz.admin.bm.bm16;

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

import com.dksys.biz.admin.bm.bm16.service.BM16Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Controller
@RequestMapping("/admin/bm/bm16")
public class BM16Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  BM16Svc bm16Svc;

//   프로젝트 리스트 조회
  @PostMapping(value = "/selectPrjctList")
  public String selectPrjctList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = bm16Svc.selectPrjctCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = bm16Svc.selectPrjctList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  // 프로젝트 정보 조회
  @PostMapping(value = "/selectPrjctInfo")
  public String selectPrjctInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    Map<String, String> result = bm16Svc.selectPrjctInfo(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
  // 대상설비 리스트 조회
  @PostMapping("/selectPrdtList")
  public String selectPrdtList(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> prdtList = bm16Svc.selectPrdtList(paramMap);
	  model.addAttribute("prdtList", prdtList);
	  return "jsonView";
  }

  // 아이템 정보조회
  @PostMapping("/selectItemList")
  public String selectItemList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    List<Map<String, String>> itemList = bm16Svc.selectItemList(paramMap);
    model.addAttribute("itemList", itemList);
    return "jsonView";
  }

  @PostMapping(value = "/selectConfirmCount")
  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int result = bm16Svc.selectConfirmCount(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  @PostMapping(value = "/insertPrjct")
  public String insertPrjct(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (bm16Svc.insertPrjct(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultDBError", ((SQLException) e.getCause()).getSQLState());
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
  }

  @PostMapping(value = "/updatePrjct")
  public String updatePrjct(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (bm16Svc.updatePrjct(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultDBError", ((SQLException) e.getCause()).getSQLState());
			model.addAttribute("resultMessage", e.getMessage());
		}
	  	return "jsonView";
  }

  @PutMapping(value = "/deletePrjct")
  public String deletePrjct(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (bm16Svc.deletePrjct(paramMap) != 0 ) {
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


  // 프로젝트 이슈정보 조회
  @PostMapping(value = "/selectPrjctIssueInfo")
  public String selectPrjctIssueInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    Map<String, String> result = bm16Svc.selectPrjctIssueInfo(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
    
  @PostMapping(value = "/insertPrjctIssue")
  public String insertPrjctIssue(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  try {
		  if (bm16Svc.insertPrjctIssue(paramMap, mRequest) != 0 ) {
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
  
  @PostMapping(value = "/updatePrjctIssue")
  public String updatePrjctIssue(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  try {
		  if (bm16Svc.updatePrjctIssue(paramMap, mRequest) != 0 ) {
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
  
  // 수주목표 물량 그래프용
  @PostMapping("/selectPrjctOrderBillChart")
  public String selectPrjctOrderBillChart(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> result = bm16Svc.selectPrjctOrderBillChart(paramMap);
	  model.addAttribute("result", result);

//	   	List<Map<String, String>> selectPalBillSalesPrftChart = fi02Svc.selectPalBillSalesPrftChart(paramMap);
    	JSONObject data = new JSONObject();
    	JSONObject objCols1 = new JSONObject();
    	JSONObject objCols2 = new JSONObject();
    	JSONObject objCols3 = new JSONObject();
    	JSONObject objCols4 = new JSONObject();
    	JSONObject objCols5 = new JSONObject();
    	JSONObject objCols6 = new JSONObject();
    	JSONObject objCols7 = new JSONObject();
    	JSONArray arryCols = new JSONArray();
    	JSONArray arryRows = new JSONArray();

    	objCols1.put("type", "string");
    	objCols2.put("type", "number");
    	objCols2.put("label", "해외수주");
    	objCols3.put("type", "number");
    	objCols3.put("label", "국내수주");
    	objCols4.put("type", "number");
    	objCols4.put("label", "신규수주");
    	objCols5.put("type", "number");
    	objCols5.put("label", "목표합계");
    	objCols6.put("type", "number");
    	objCols6.put("label", "수주완료");
    	objCols7.put("type", "number");
    	objCols7.put("label", "수주.수주목표누계");

    	arryCols.add(objCols1);
    	arryCols.add(objCols2);
    	arryCols.add(objCols3);
    	arryCols.add(objCols4);
    	arryCols.add(objCols5);
    	arryCols.add(objCols6);
    	arryCols.add(objCols7);
    	for(int i = 0; i < result.size(); i++) {
    		JSONObject legend = new JSONObject();
    		legend.put("v", result.get(i).get("yyyymm"));
    		legend.put("f", null);
    		JSONObject value1 = new JSONObject();
    		value1.put("v", result.get(i).get("inpexp02"));
    		value1.put("f", null);
    		JSONObject value2 = new JSONObject();
    		value2.put("v", result.get(i).get("inpexp01"));
    		value2.put("f", null);
    		JSONObject value3 = new JSONObject();
    		value3.put("v", result.get(i).get("inpexp03"));
    		value3.put("f", null);
    		JSONObject value4 = new JSONObject();
    		value4.put("v", result.get(i).get("ordrsPlanAmt"));
    		value4.put("f", null);
    		JSONObject value5 = new JSONObject();
    		value5.put("v", result.get(i).get("ordrsAmt"));
    		value5.put("f", null);
    		JSONObject value6 = new JSONObject();
    		value6.put("v", result.get(i).get("mmTot"));
    		value6.put("f", null);
    		
    		JSONArray cValueArry = new JSONArray();
    		cValueArry.add(legend);
    		cValueArry.add(value1);
    		cValueArry.add(value2);
    		cValueArry.add(value3);
    		cValueArry.add(value4);
    		cValueArry.add(value5);
    		cValueArry.add(value6);
    		
    		JSONObject cValueObj = new JSONObject();
    		cValueObj.put("c", cValueArry);
    		
    		arryRows.add(cValueObj);
    	}
    	
    	data.put("cols", arryCols);
    	data.put("rows", arryRows);
    	model.addAttribute("chartData1", data);
	  return "jsonView";
  }

  //프로젝트 대일정 조회
  @PostMapping("/selectPrjctPlanList")
  public String selectPrjctPlanList(@RequestBody Map<String, String> param, ModelMap model) {
	  
		String[] wbsArray = param.get("wbsCode").split(",");  //수주확정:WBSCODE01,목표원가/PFU배포:WBSCODE02,설계완료:WBSCODE03....
		String wbscode = "";
		for (int i = 0; i < wbsArray.length; i++) {

			String[] roleArray = wbsArray[i].split(":");   //"수주확정:WBSCODE01
			if ((wbsArray.length -1) == i ) {
				wbscode += "'" + roleArray[0] +"' AS " + roleArray[1];
			}
			else {
				wbscode += "'" + roleArray[0] +"' AS " + roleArray[1] +  ",";
			}
		}
		param.put("wbscode", wbscode);
	  
      int totalCnt = bm16Svc.selectPrjctPlanCount(param);
      PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
      model.addAttribute("paginationInfo", paginationInfo);

      List<Map<String, Object>> planList = bm16Svc.selectPrjctPlanList(param);
      model.addAttribute("planList", planList);
      return "jsonView";
  }
  
 //TODO LIST 프로젝트 이슈정보 조회
 @PostMapping(value = "/selectTodoIssueInfo")
 public String selectTodoIssueInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
   Map<String, String> result = bm16Svc.selectTodoIssueInfo(paramMap);
   model.addAttribute("result", result);
   return "jsonView";
 }
 
 //헤더 그리드 코드 검색
 @PostMapping(value = "/select_wbs_code")
 public String select_wbs_code(@RequestBody Map<String, String> paramMap, ModelMap model) {
	List<Map<String, String>> result = bm16Svc.select_wbs_code(paramMap);
	model.addAttribute("result", result);
	return "jsonView";
 }
  
}