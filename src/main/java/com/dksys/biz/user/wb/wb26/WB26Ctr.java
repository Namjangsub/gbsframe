package com.dksys.biz.user.wb.wb26;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.wb.wb26.service.WB26Svc;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/wb/wb26")
public class WB26Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    WB26Svc wb26svc;

    //리스트 조회
	@PostMapping(value = "/select_wb26_List")
	public String select_wb26_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = wb26svc.select_wb26_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = wb26svc.select_wb26_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/wb26save")
    public String wb26save(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb26svc.wb26save(paramMap, mRequest) != 0 ) {
  				model.addAttribute("resultCode", 200);
  			model.addAttribute("resultMessage", messageUtils.getMessage("save"));
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

	@PostMapping(value = "/update_wb26_confirmYn")
    public String update_wb26_confirmYn(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (wb26svc.update_wb26_confirmYn(paramMap) != 0 ) {
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

	@PostMapping(value = "/update_wb26")
    public String update_wb26(@RequestBody Map<String, String> paramMap,  ModelMap model) throws Exception {
  		try {
  			if (wb26svc.update_wb26(paramMap) != 0 ) {
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


	// // 수금유형 조회
	// @PostMapping(value = "/selectPmntmtdCd") 
	// public String selectPmntmtdCd(@RequestBody Map<String, String> paramMap, ModelMap model) {
	// 	List<Map<String, Object>> result = cr13svc.selectPmntmtdCd(paramMap);
	// 	model.addAttribute("result", result);
	// 	return "jsonView";
	// }


    //리스트 조회
	@PostMapping(value = "/select_wb2602_List")
	public String select_wb2602_List(@RequestBody Map<String, String> paramMap, ModelMap model) {

		paramMap.put("prdtGrp", ObjectUtil.sqlInCodeGen(paramMap.get("prdtGrp")));
		
		int totalCnt = wb26svc.select_wb2602_List_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = wb26svc.select_wb2602_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

    //리스트 조회
	@PostMapping(value = "/select_wb2603_List")
	public String select_wb2603_List(@RequestBody Map<String, String> paramMap, ModelMap model) {

		paramMap.put("prdtGrp", ObjectUtil.sqlInCodeGen(paramMap.get("prdtGrp")));
		
		int totalCnt = wb26svc.select_wb2603_List_Count(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = wb26svc.select_wb2603_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}


    //리스트 조회
	@PostMapping(value = "/select_wb2604_List")
	public String select_wb2604_List(@RequestBody Map<String, String> paramMap, ModelMap model) {

		paramMap.put("prdtGrp", ObjectUtil.sqlInCodeGen(paramMap.get("prdtGrp")));
		
		List<Map<String, String>> result = wb26svc.select_wb2604_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}


    //리스트 조회
	@PostMapping(value = "/select_wb2605_List")
	public String select_wb2605_List(@RequestBody Map<String, String> paramMap, ModelMap model) {

		paramMap.put("prdtGrp", ObjectUtil.sqlInCodeGen(paramMap.get("prdtGrp")));
		
		List<Map<String, String>> result = wb26svc.select_wb2605_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}


    //간트차트에서 일정계획 수립시 개인별 할당된 업무 메뉴 추가 리스트 조회
	@PostMapping(value = "/selectWbsTaskTempletGantList")
	public String selectWbsTaskTempletGantList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		
		List<Map<String, String>> result = wb26svc.selectWbsTaskTempletGantList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
}