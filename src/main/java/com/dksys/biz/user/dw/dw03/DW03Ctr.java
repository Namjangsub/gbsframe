package com.dksys.biz.user.dw.dw03;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.dw.dw03.service.DW03Svc;
import com.dksys.biz.util.MessageUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user/dw/dw03")
public class DW03Ctr {
	
	@Autowired
	MessageUtils messageUtils;

    @Autowired
    DW03Svc dw03Svc;

	// 도면 트리 리스트 조회
    @PostMapping("/selectDrawDocTreeList")
	public String selectDrawDocTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> docTreeList = dw03Svc.selectDrawDocTreeList(paramMap);
		model.addAttribute("docTreeList", docTreeList);
		return "jsonView";
	}

	//카테고리별 파일정보 리스트 조회
	@PostMapping("/selectDrawTreeFileList")
	public String selectDrawTreeFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = dw03Svc.selectDrawTreeFileListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> fileList = dw03Svc.selectDrawTreeFileList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }
}
