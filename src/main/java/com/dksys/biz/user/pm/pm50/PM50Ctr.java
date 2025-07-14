package com.dksys.biz.user.pm.pm50;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.pm.pm50.service.PM50Svc;
import com.dksys.biz.user.wb.wb24.mapper.WB24Mapper;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm50")
public class PM50Ctr {
	
	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM50Svc pm50Svc;

	@Autowired
	WB24Mapper wb24Mapper;

	// 출장자 사진파일 리스트 조회
	@PostMapping(value = "/select_pm50_List")
    public String select_pm50_List(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int totalCnt = pm50Svc.select_pm50_ListCount(paramMap); 
        PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> result = pm50Svc.select_pm50_List(paramMap);
        model.addAttribute("result", result);
        return "jsonView";  
    }

	//카테고리별 파일정보 리스트 조회
    @PostMapping("/selectSendFileList")
    public String selectFileList(@RequestBody Map<String, String> param, ModelMap model) {
    	int totalCnt = pm50Svc.selectSendFileCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> fileList = pm50Svc.selectSendFileList(param);
    	model.addAttribute("fileList", fileList);
        return "jsonView";
    }

	// 출장자 사진파일 작업정보
	@PostMapping("/select_pm50_Info")
	public String select_pm50_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> result = pm50Svc.select_pm50_Info(paramMap);
    	model.addAttribute("result", result);
		List<Map<String, String>> bfuRows = pm50Svc.selectBfuAllFileRows(paramMap);
    	model.addAttribute("bfuRows", bfuRows);
		paramMap.put("issNo", paramMap.get("fileTrgtKey"));
		List<Map<String, String>> appChk = wb24Mapper.actChk(paramMap);
		model.addAttribute("appChk", appChk);
    	return "jsonView";
    }

	// QR로 넘어온 SalesCd정보
	@PostMapping("/select_salesCd_info")
	public String select_salesCd_info(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	Map<String, String> result = pm50Svc.select_salesCd_info(paramMap);
    	model.addAttribute("result", result);
    	return "jsonView";
    }

	// 등록
	@PostMapping("/insert_pm50")
	public String insert_pm50(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm50Svc.insert_pm50(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
				model.addAttribute("fileTrgtKey", paramMap.get("fileTrgtKey"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		
		return "jsonView";
	}

	// 수정
	@PostMapping("/update_pm50")
	public String update_pm50(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm50Svc.update_pm50(paramMap, mRequest) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		
		return "jsonView";
	}

	// 문제등록 업데이트
	@PostMapping("/update_issue_pm50_d01")
	public String update_issue_pm50_d01(@RequestBody Map<String, Object> paramMap, ModelMap model) {
		try {
			if (pm50Svc.update_issue_pm50_d01(paramMap) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		
		return "jsonView";
	}

	// 문제등록 업데이트
	@PostMapping("/update_issue_reset_pm50_d01")
	public String update_issue_reset_pm50_d01(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
			if (pm50Svc.update_issue_reset_pm50_d01(paramMap) != 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		
		return "jsonView";
	}

	// 출장자 사진파일 리스트 삭제
	@PutMapping(value = "/delete_pm50")
	public String delete_pm50(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm50Svc.delete_pm50(paramMap) >= 0 ) {
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