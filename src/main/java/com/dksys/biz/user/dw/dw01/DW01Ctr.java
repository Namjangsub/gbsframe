package com.dksys.biz.user.dw.dw01;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.dw.dw01.service.DW01Svc;
import com.dksys.biz.util.MessageUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user/dw/dw01")
public class DW01Ctr {
	
	@Autowired
	MessageUtils messageUtils;

    @Autowired
    DW01Svc dw01Svc;

	// 도면 트리 리스트 조회
    @PostMapping("/selectDrawDocTreeList")
	public String selectDrawDocTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> docTreeList = dw01Svc.selectDrawDocTreeList(paramMap);
		model.addAttribute("docTreeList", docTreeList);
		return "jsonView";
	}

	//카테고리별 파일정보 리스트 조회
	@PostMapping("/selectDrawTreeFileList")
	public String selectDrawTreeFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = dw01Svc.selectDrawTreeFileListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> fileList = dw01Svc.selectDrawTreeFileList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }
}
