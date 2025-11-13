package com.dksys.biz.admin.cm.cm12;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.admin.cm.cm12.service.CM12Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/cm/cm12")
public class CM12Ctr {
	@Autowired
	MessageUtils messageUtils;
    
    @Autowired
    CM12Svc cm12Svc;
	
    @PostMapping(value = "/selectSolarLunarEventHolidaysList")
	public String selectSolarLunarEventHolidaysList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        if (paramMap.get("calDivCd").equals("A")) {
            paramMap.put("calDivCd", "S");
        	List<Map<String, String>> resultSolar = cm12Svc.selectSolarLunarEventHolidaysList(paramMap);
        	model.addAttribute("resultSolar", resultSolar);
        	
            paramMap.put("calDivCd", "L");
            List<Map<String, String>> resultLumar = cm12Svc.selectSolarLunarEventHolidaysList(paramMap);
            model.addAttribute("resultLumar", resultLumar);

            paramMap.put("calDivCd", "E");
            List<Map<String, String>> resultEnent = cm12Svc.selectSolarLunarEventHolidaysList(paramMap);
            model.addAttribute("resultEnent", resultEnent);
        } else {
            List<Map<String, String>> resultList = cm12Svc.selectSolarLunarEventHolidaysList(paramMap);
            model.addAttribute("resultList", resultList);
    	}
    	return "jsonView";
    }
	


    // 파일정보 트리 리스트 조회
    @PostMapping("/selectDocFileTreeList")
    public String selectDocFileTreeList(@RequestBody Map<String, String> param, ModelMap model) {
    	List<Map<String, String>> docTreeList = cm12Svc.selectDocFileTreeList(param);
    	model.addAttribute("docTreeList", docTreeList);
    	return "jsonView";
    }

    //카테고리별 파일정보 리스트 조회
    @PostMapping("/selectDocCustTreeFileList")
    public String selectDocTreeFileList(@RequestBody Map<String, String> param, ModelMap model) {
    	int totalCnt = cm12Svc.selectDocCustTreeFileCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> fileList = cm12Svc.selectDocCustTreeFileList(param);
    	model.addAttribute("fileList", fileList);
        return "jsonView";
    }
}