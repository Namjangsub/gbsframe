package com.dksys.biz.user.bm.bm05;

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

import com.dksys.biz.user.bm.bm05.service.BM05Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/bm/bm05")
public class BM05Ctr {

	@Autowired
	  MessageUtils messageUtils;
    
	@Autowired
	BM05Svc bm05Svc;

	//리스트 조회
	@PostMapping(value = "/grid1_selectList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = bm05Svc.grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = bm05Svc.grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//팝업 그리드 리스트
	@PostMapping(value = "/MatModal_selectList")
	public String MatModal_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = bm05Svc.MatModal_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = bm05Svc.MatModal_selectList(paramMap);
		model.addAttribute("MatModalList", result);
		return "jsonView";
	}

	//정보 조회
	@PostMapping(value = "/select_bm05_Info")
	public String select_bm05_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = bm05Svc.select_bm05_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//INSERT
	@PostMapping(value = "/insert_bm05")
	public String insert_bm05(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			Map<String, String> dupmatrCd = bm05Svc.selectMatrCdChk(paramMap);
			if(dupmatrCd != null && dupmatrCd.size() > 0) {
        		model.addAttribute("resultCode", 500);
        		model.addAttribute("resultMessage",dupmatrCd.get("matrCd") + " : 동일 정보를 가진 자재가 있습니다.");
        	}else {
        	 	if (bm05Svc.insert_bm05(paramMap, mRequest) != 0 ) {
        	 		model.addAttribute("resultCode", 200);
        	 		model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
        	 	} else {
        	 		model.addAttribute("resultCode", 500);
        	 		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
        	 	};
        	}
    	}catch(Exception e){
    		model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
	}
	
	//UPDATE
	@PostMapping(value = "/update_bm05")
	public String update_bm05(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (bm05Svc.update_bm05(paramMap, mRequest) != 0 ) {
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
	
	//DELETE
	@PutMapping(value = "/delete_bm05")
	public String delete_bm05(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			Map<String, String> delmatrCd = bm05Svc.deleteMatrCdChk(paramMap);
			if(delmatrCd != null && delmatrCd.size() > 0) {
        		model.addAttribute("resultCode", 500);
        		model.addAttribute("resultMessage",delmatrCd.get("matrCd") + " : 발주정보가 존재하여 삭제할 수 없습니다.");
        	}else {
        	 	if (bm05Svc.delete_bm05(paramMap) != 0 ) {
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
				} else {
					model.addAttribute("resultCode", 500);
					model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
				};
        	}
    	}catch(Exception e){
    		model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
	}
	


	//자재마스터 설계 BOM에서 형번/규격 검색용
	@PostMapping(value = "/BOM_selectMatrMnoList")
	public String BOM_selectMatrMnoList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = bm05Svc.BOM_selectMatrMnoList(paramMap);
		model.addAttribute("resultList", result);
		return "jsonView";
	}
}
