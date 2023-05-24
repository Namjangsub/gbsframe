package com.dksys.biz.user.cr.cr02;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr01.service.CR01Svc;
import com.dksys.biz.user.cr.cr02.service.CR02Svc;
import com.dksys.biz.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/user/cr/cr02")
public class CR02Ctr {

	@Autowired
	MessageUtils messageUtils;
	@Autowired
	CR01Svc cr01Svc;
	@Autowired
	CR02Svc cr02Svc;

	@PostMapping("/selectOrdrsList")
	public String selectOrdrsList(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectOrdrsCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);

		List<Map<String, Object>> ordrsList = cr02Svc.selectOrdrsList(param);
		model.addAttribute("ordrsList", ordrsList);
		return "jsonView";
	}

	@PostMapping(value = "/selectOrdrsInfo")
	public String selectOrdrsInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, Object> ordrsInfo = cr02Svc.selectOrdrsInfo(paramMap);
		model.addAttribute("ordrsInfo", ordrsInfo);
		return "jsonView";
	}
	@PostMapping(value = "/selectOrdrsWithEst")
	public String selectOrdrsWithEst(@RequestBody Map<String, String> params,ModelMap model) {
		model.addAttribute("info", cr02Svc.selectOrdrsWithEst(params));
		return "jsonView";
	}

	@PostMapping(value = "/selectClntInfo")
	public String selectClntInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, Object> estInfo = cr01Svc.selectEstInfo(paramMap);
		System.out.println(estInfo+"error");
		model.addAttribute("clntInfo", estInfo.get("estClntCd"));
		return "jsonView";
	}
	// ...

	@PostMapping(value = "/maxOrdrsNo")
	public String getMaxOrdrsNo(@RequestBody Map<String, String> param, ModelMap model) {
		String maxOrdrsNo = cr02Svc.selectMaxOrdrsNo(param);
		model.addAttribute("maxOrdrsNo", maxOrdrsNo);
		return "jsonView";
	}

	@PostMapping(value = "/insertOrdrs")
	public String insertOrdrs(@RequestParam Map<String, String> param, MultipartHttpServletRequest mRequest, ModelMap model) {
		cr02Svc.insertOrdrs(param,mRequest);

		return "jsonView";
	}


	@PostMapping(value = "/updateOrdrs")
	public String updateOrdrs(@RequestParam Map<String, String> param,MultipartHttpServletRequest mRequest, ModelMap model) {
		cr02Svc.updateOrdrs(param,mRequest);
		return "jsonView";
	}

	@DeleteMapping(value = "/deleteOrdrs")
	public String deleteOrdrs(@RequestBody Map<String, String> paramMap, ModelMap model) {
		cr02Svc.deleteOrdrs(paramMap);
		model.addAttribute("resultCode", 200);
		model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		return "jsonView";
	}
}