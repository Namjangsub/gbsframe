package com.dksys.biz.user.cr.cr10;

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

import com.dksys.biz.user.cr.cr10.service.CR10Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/cr/cr10")
public class CR10Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  CR10Svc cr10Svc;

    //Paging 조회
	@PostMapping(value = "/selectWbsIssuePageList")
	public String selectWbsIssuePageList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = cr10Svc.selectWbsIssuePageCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = cr10Svc.selectWbsIssuePageList(paramMap);
		model.addAttribute("resultList", result);
		return "jsonView";
	}

	// BOM내역상세 조회
	@PostMapping(value = "/selectBomDetailList")
	public String selectBomDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectBomDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	//Bom 리스트
	@PostMapping("/selectBuyBomList")
	public String selectBuyBomList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectBuyBomList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}

	// 자재 리스트 조회
	@PostMapping("/selectBomMatrList")
	public String selectBomMatrList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = cr10Svc.selectBomMatrList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	// Bom 자재 Map 조회
	@PostMapping(value = "/selectBomMatrInfo")
		public String selectBomMatrInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = cr10Svc.selectBomMatrInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
  
	@PostMapping(value = "/insertBom")
	public String insertBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (cr10Svc.insertBom(paramMap, mRequest) != 0 ) {
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

	@PostMapping(value = "/updateBom")
	public String updateBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.updateBom(paramMap, mRequest) != 0 ) {
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

	@PutMapping(value = "/deleteBomAll")
	public String deleteBomAll(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.deleteBomAll(paramMap) != 0 ) {
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

	@PostMapping(value = "/insertBomMatr")
	public String insertBomMatr(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  try {
		  if (cr10Svc.insertBomMatr(paramMap, mRequest) != 0 ) {
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
  
	@PostMapping(value = "/updateBomMatr")
	public String updateBomMatr(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  try {
		  if (cr10Svc.updateBomMatr(paramMap, mRequest) != 0 ) {
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

	@PutMapping(value = "/deleteBomMatr")
	public String deleteBomMatr(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (cr10Svc.deleteBomMatr(paramMap) != 0 ) {
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
	
	  
	@PostMapping(value = "/insertCopyBom")
	public String insertCopyBom(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (cr10Svc.insertCopyBom(paramMap, mRequest) != 0 ) {
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
  
}