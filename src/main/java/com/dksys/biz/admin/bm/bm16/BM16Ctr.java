package com.dksys.biz.admin.bm.bm16;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.admin.bm.bm16.service.BM16Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/bm/bm16")
public class BM16Ctr {
	
	@Autowired
	MessageUtils messageUtils;
	
	@Autowired
	BM16Svc bm16Svc;
	
	// 프로젝트 리스트 조회
    @PostMapping("/selectPrjctList")
    public String selectPrjctList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    	int totalCnt = bm16Svc.selectPrjctCount(paramMap);
    	PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    	model.addAttribute("paginationInfo", paginationInfo);
    	
    	List<Map<String, String>> PrjctList = bm16Svc.selectPrjctList(paramMap);
    	model.addAttribute("PrjctList", PrjctList);
        return "jsonView";
    }

}