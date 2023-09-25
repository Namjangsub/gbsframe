package com.dksys.biz.user.cr.cr21;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.cr.cr21.service.CR21Svc;
import com.dksys.biz.util.MessageUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Controller
@Transactional(rollbackFor = Exception.class)
@RequestMapping("/user/cr/cr21")
public class CR21Ctr {
	
	private Logger logger = LoggerFactory.getLogger(CR21Ctr.class);
	
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    CR21Svc cr21Svc;
    @PostMapping(value = "/selectPrjctVSResultChart") 
	public String selectSalesPlanHistList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> list = cr21Svc.selectPrjctVSResultChart(paramMap);
		model.addAttribute("list", list);
		  
		JSONObject data = new JSONObject();
		JSONObject objCols1 = new JSONObject();
		JSONObject objCols2 = new JSONObject();
		JSONObject objCols3 = new JSONObject();
		JSONArray arryCols = new JSONArray();
		JSONArray arryRows = new JSONArray();
		
		objCols1.put("type", "string");
		objCols2.put("type", "number");
		objCols2.put("label", "목표");
		objCols3.put("type", "number");
		objCols3.put("label", "실적");
		
		arryCols.add(objCols1);
		arryCols.add(objCols2);
		arryCols.add(objCols3);
		
		for(int i = 0; i < 12; i++) {
			//월
			JSONObject legend = new JSONObject();
			legend.put("v", (i+1)+"월");
			legend.put("f", null);
			
			//데이터 년월 세팅
			String yyyyMm = String.format("%d%02d", Integer.parseInt(paramMap.get("searchYear")), i+1);
					
			//목표
			JSONObject value1 = new JSONObject();
			value1.put("v", list.get(0).get("'"+yyyyMm+"'"));
			value1.put("f", null);
			//실적
			JSONObject value2 = new JSONObject();
			value2.put("v", list.get(1).get("'"+yyyyMm+"'"));
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
	    	
		return "jsonView";
    }
    
}	  