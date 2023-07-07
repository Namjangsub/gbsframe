package com.dksys.biz.user.bm.bm11;

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

import com.dksys.biz.user.bm.bm11.service.BM11Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/bm/bm11")
public class BM11Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    BM11Svc bm11svc;

    //리스트 조회
	@PostMapping(value = "/grid1_selectList")
	public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = bm11svc.grid1_selectCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = bm11svc.grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	//정보 조회
	@PostMapping(value = "/select_bm11_Info")
	public String select_bm11_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = bm11svc.select_bm11_Info(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//상세정보 조회
	@PostMapping(value = "/select_bm11_Info_Dtl")
	public String select_bm11_Info_Dtl(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = bm11svc.select_bm11_Info_Dtl(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//INSERT
	@PostMapping(value = "/insert_bm11")
	public String insert_bm11(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			Map<String, String> DupChk_BM11 = bm11svc.DupChk_BM11M01(paramMap);
			if(DupChk_BM11 != null && DupChk_BM11.size() > 0) {
        		model.addAttribute("resultCode", 500);
        		model.addAttribute("resultMessage",DupChk_BM11.get("adptDt") + " : 동일 정보를 가진 단가가 있습니다.");
        	}else {
        	 	if (bm11svc.insert_bm11(paramMap, mRequest) != 0 ) {
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
	@PostMapping(value = "/update_bm11")
	public String update_bm11(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (bm11svc.update_bm11(paramMap, mRequest) != 0 ) {
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
	@PutMapping(value = "/delete_bm11")
	public String delete_bm11(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (bm11svc.delete_bm11(paramMap) != 0 ) {
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