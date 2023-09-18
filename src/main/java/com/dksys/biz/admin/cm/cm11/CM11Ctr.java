package com.dksys.biz.admin.cm.cm11;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.admin.cm.cm11.service.CM11Svc;
import com.dksys.biz.util.MessageUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Controller
@RequestMapping("/user/cm/cm11")
public class CM11Ctr {
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    CM11Svc cm11Svc;
	
    @PostMapping(value = "/selectDashBoardList")
	public String selectDashBoardList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	String searchDttm = cm11Svc.selectSearchDttm();
    	model.addAttribute("searchDttm", searchDttm);
    	
    	List<Map<String, String>> resultList1 = cm11Svc.selectPrjectDashList(paramMap);
    	model.addAttribute("resultList1", resultList1);

    	JSONObject data = new JSONObject();
    	JSONObject objCols1 = new JSONObject();
    	JSONObject objCols2 = new JSONObject();
    	JSONObject objCols3 = new JSONObject();
    	JSONArray arryCols = new JSONArray();
    	JSONArray arryRows = new JSONArray();

    	objCols1.put("type", "string");
    	objCols2.put("type", "number");
    	objCols2.put("label", "계획");
    	objCols3.put("type", "number");
    	objCols3.put("label", "실적");

    	arryCols.add(objCols1);
    	arryCols.add(objCols2);
    	arryCols.add(objCols3);
    	for(int i = 0; i < resultList1.size(); i++) {
    		JSONObject legend = new JSONObject();
    		legend.put("v", resultList1.get(i).get("rowTit"));
    		legend.put("f", null);
    		JSONObject value1 = new JSONObject();
    		value1.put("v", resultList1.get(i).get("planAmt"));
    		value1.put("f", null);
    		JSONObject value2 = new JSONObject();
    		value2.put("v", resultList1.get(i).get("ordrsAmt"));
    		value2.put("f", null);
    		
    		JSONArray cValueArry = new JSONArray();
    		cValueArry.add(legend);
    		cValueArry.add(value1);
    		cValueArry.add(value2);
    		
    		JSONObject cValueObj = new JSONObject();
    		cValueObj.put("c", cValueArry);
    		
    		arryRows.add(cValueObj);
    	}
    	
    	data.put("cols", arryCols);
    	data.put("rows", arryRows);
    	model.addAttribute("chartData1", data);
    	
    	List<Map<String, String>> resultList2 = cm11Svc.selectClientTaxDashList(paramMap);
    	model.addAttribute("resultList2", resultList2);
    	
    	data = new JSONObject();
    	objCols1 = new JSONObject();
    	objCols2 = new JSONObject();
    	objCols3 = new JSONObject();
    	arryCols = new JSONArray();
    	arryRows = new JSONArray();

    	objCols1.put("type", "string");
    	objCols2.put("type", "number");
    	objCols2.put("label", "계획");
    	objCols3.put("type", "number");
    	objCols3.put("label", "실적");

    	arryCols.add(objCols1);
    	arryCols.add(objCols2);
    	arryCols.add(objCols3);
    	for(int i = 0; i < resultList2.size(); i++) {
    		JSONObject legend = new JSONObject();
    		legend.put("v", resultList2.get(i).get("clntNm"));
    		legend.put("f", null);
    		JSONObject value1 = new JSONObject();
    		value1.put("v", resultList2.get(i).get("planAmt"));
    		value1.put("f", null);
    		JSONObject value2 = new JSONObject();
    		value2.put("v", resultList2.get(i).get("salesAmt"));
    		value2.put("f", null);
    		
    		JSONArray cValueArry = new JSONArray();
    		cValueArry.add(legend);
    		cValueArry.add(value1);
    		cValueArry.add(value2);
    		
    		JSONObject cValueObj = new JSONObject();
    		cValueObj.put("c", cValueArry);
    		
    		arryRows.add(cValueObj);
    	}
    	
    	data.put("cols", arryCols);
    	data.put("rows", arryRows);
    	model.addAttribute("chartData2", data);
    	
    	
    	List<Map<String, String>> resultList3 = cm11Svc.selectClientPchsDashList(paramMap);
    	model.addAttribute("resultList3", resultList3);
    	data = new JSONObject();
    	objCols1 = new JSONObject();
    	objCols2 = new JSONObject();
    	arryCols = new JSONArray();
    	arryRows = new JSONArray();

    	objCols1.put("type", "string");
    	objCols1.put("label", "거래처");
    	objCols2.put("type", "number");
    	objCols2.put("label", "실적");

    	arryCols.add(objCols1);
    	arryCols.add(objCols2);
    	for(int i = 0; i < resultList3.size(); i++) {
    		JSONObject legend = new JSONObject();
    		legend.put("v", resultList3.get(i).get("rowTit"));
    		legend.put("f", null);
    		JSONObject value1 = new JSONObject();
    		value1.put("v", Integer.parseInt(resultList3.get(i).get("pchsTot")));
    		value1.put("f", null);
    		
    		JSONArray cValueArry = new JSONArray();
    		cValueArry.add(legend);
    		cValueArry.add(value1);
    		
    		JSONObject cValueObj = new JSONObject();
    		cValueObj.put("c", cValueArry);
    		
    		arryRows.add(cValueObj);
    	}
    	
    	data.put("cols", arryCols);
    	data.put("rows", arryRows);
    	model.addAttribute("chartData3", data);
    	
    	List<Map<String, String>> resultList4 = cm11Svc.selectFacList(paramMap);
    	model.addAttribute("resultList4", resultList4);
    	data = new JSONObject();
    	objCols1 = new JSONObject();
    	objCols2 = new JSONObject();
    	objCols3 = new JSONObject();
    	arryCols = new JSONArray();
    	arryRows = new JSONArray();

    	objCols1.put("type", "string");
    	objCols1.put("label", "공장");
    	objCols2.put("type", "number");
    	objCols2.put("label", "계획");
    	objCols3.put("type", "number");
    	objCols3.put("label", "실적");

    	arryCols.add(objCols1);
    	arryCols.add(objCols2);
    	arryCols.add(objCols3);
    	for(int i = 0; i < resultList4.size(); i++) {
    		JSONObject legend = new JSONObject();
    		legend.put("v", resultList4.get(i).get("rowTit"));
    		legend.put("f", null);
    		JSONObject value1 = new JSONObject();
    		value1.put("v", resultList4.get(i).get("planAmt"));
    		value1.put("f", null);
    		JSONObject value2 = new JSONObject();
    		value2.put("v", resultList4.get(i).get("salesAmt"));
    		value2.put("f", null);
    		
    		JSONArray cValueArry = new JSONArray();
    		cValueArry.add(legend);
    		cValueArry.add(value1);
    		cValueArry.add(value2);
    		
    		JSONObject cValueObj = new JSONObject();
    		cValueObj.put("c", cValueArry);
    		
    		arryRows.add(cValueObj);
    	}
    	
    	data.put("cols", arryCols);
    	data.put("rows", arryRows);
    	model.addAttribute("chartData4", data);
    	
    	List<Map<String, String>> resultList5 = cm11Svc.selectMonthlyStat(paramMap);
    	model.addAttribute("resultList5", resultList5);
    	data = new JSONObject();
    	objCols1 = new JSONObject();
    	objCols2 = new JSONObject();
    	objCols3 = new JSONObject();
    	arryCols = new JSONArray();
    	arryRows = new JSONArray();

    	objCols1.put("type", "string");
    	objCols1.put("label", "월별");
    	objCols2.put("type", "number");
    	objCols2.put("label", "매출");
    	objCols3.put("type", "number");
    	objCols3.put("label", "매입");

    	arryCols.add(objCols1);
    	arryCols.add(objCols2);
    	arryCols.add(objCols3);
    	for(int i = 0; i < resultList5.size(); i++) {
    		JSONObject legend = new JSONObject();
    		legend.put("v", resultList5.get(i).get("trstDt"));
    		legend.put("f", null);
    		JSONObject value1 = new JSONObject();
    		value1.put("v", resultList5.get(i).get("salesTot"));
    		value1.put("f", null);
    		JSONObject value2 = new JSONObject();
    		value2.put("v", resultList5.get(i).get("pchsTot"));
    		value2.put("f", null);
    		
    		JSONArray cValueArry = new JSONArray();
    		cValueArry.add(legend);
    		cValueArry.add(value1);
    		cValueArry.add(value2);
    		
    		JSONObject cValueObj = new JSONObject();
    		cValueObj.put("c", cValueArry);
    		
    		arryRows.add(cValueObj);
    	}
    	
    	data.put("cols", arryCols);
    	data.put("rows", arryRows);
    	model.addAttribute("chartData5", data);
    	return "jsonView";
    }
	
}