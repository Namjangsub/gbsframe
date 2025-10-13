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

	// 도면 이력 버전별 리스트 상세 조회
    @PostMapping("/select_dw03_detailList")
	public String select_dw03_detailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = dw03Svc.select_dw03_detailList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// SALES CODE별 도면 트리 리스트 조회
    @PostMapping("/selectSalesCdDrawDocTreeList")
	public String selectSalesCdDrawDocTreeList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> docTreeList = dw03Svc.selectSalesCdDrawDocTreeList(paramMap);
		model.addAttribute("docTreeList", docTreeList);
		return "jsonView";
	}


	// SALES CODE 하위 카테고리별 파일정보 리스트 조회
	@PostMapping("/selectSalesCdDrawFileList")
	public String selectSalesCdDrawFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = dw03Svc.selectSalesCdDrawFileListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> fileList = dw03Svc.selectSalesCdDrawFileList(paramMap);
		model.addAttribute("fileList", fileList);
		return "jsonView";
    }

	@PostMapping("/dwgFileDownInfo")
	public String dwgFileDownInfo(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		int selectFileDownAuthChk = dw03Svc.selectFileDownAuthChk(paramMap);
		Map<String, String> fileInfo = dw03Svc.dwgFileDownInfo(paramMap);
		model.addAttribute("fileInfo", fileInfo);
		model.addAttribute("selectFileDownAuthChk", selectFileDownAuthChk);
		return "jsonView";
	}
}
