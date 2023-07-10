package com.dksys.biz.user.cr.cr05;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr05.service.CR05Svc;
import com.dksys.biz.util.MessageUtils;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
@RequestMapping("/user/cr/cr05")
public class CR05Ctr {

	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	CR05Svc cr05Svc;
	// 수금등록 조회
	@PostMapping(value = "/selectClmnList")
	public String selectClmnList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr05Svc.selectClmnListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr05Svc.selectClmnList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	// 수금정보 조회
	@PostMapping(value = "/selectClmnInfo")
	public String selectClmnInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr05Svc.selectClmnInfoCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr05Svc.selectClmnInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	
	// 수금번호 조회
	
	
	
	// 수금등록 입력
	@PostMapping(value = "/insert_cr05")
	public String insert_cr05(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if(cr05Svc.insert_cr05(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
	
	// 수금등록 수정
	@PostMapping(value = "/update_cr05")
	public String update_cr05(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if(cr05Svc.update_cr05(paramMap, mRequest) !=0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		
		return "jsonView";
	}
	
	// 수금등록 삭제
	@PutMapping(value = "/delete_cr05")
	public String delete_cr05(@RequestBody Map<String, String> paramMap, MultipartHttpServletRequest mRequset, ModelMap model) throws Exception {
		try {
			if(cr05Svc.delete_cr05(paramMap, mRequset) !=0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
}
