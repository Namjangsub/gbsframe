package com.dksys.biz.admin.cm.cm15;

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

import com.dksys.biz.admin.cm.cm15.service.CM15Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/cm/cm15")
public class CM15Ctr {
	
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	CM15Svc cm15Svc;

    
    // 파일권한 저장
	@PutMapping(value = "/updateFileAuth")
    public String updateFileAuth(@RequestBody Map<String, Object> param, ModelMap model) {
		try {
			int result = cm15Svc.updateFileAuth(param);
	    	model.addAttribute("resultCode", 200);
	    	model.addAttribute("updateCount", result);
	    	model.addAttribute("resultMessage", messageUtils.getMessage("save"));
		}catch (Exception e) {
			model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
    	return "jsonView"; 
    }
	
    // 파일트리 권한 정보 리스트 조회
    @PostMapping(value = "/selectTreeAuthList")
    public String selectTreeFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = cm15Svc.selectTreeAuthCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> fileList = cm15Svc.selectTreeAuthList(paramMap);
    	model.addAttribute("fileList", fileList);
        return "jsonView";
    }

    @DeleteMapping(value = "/deleteFileAuthInfo")
    public String deleteFileAuthInfo(@RequestBody Map<String, Object> paramMap, ModelMap model){
		try {
	    	int result = cm15Svc.deleteFileAuthInfo(paramMap);
	    	model.addAttribute("deleteCount", result);
	    	model.addAttribute("resultCode", 200);
	    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
    		model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
    	return "jsonView";
    }

    // 파일트리 사용자 권한 정보 리스트 조회
    @PostMapping(value = "/selectTreeAuthUserList")
    public String selectTreeAuthUserList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	List<Map<String, String>> userList = cm15Svc.selectTreeAuthUserList(paramMap);
    	model.addAttribute("userList", userList);
        return "jsonView";
    }
}