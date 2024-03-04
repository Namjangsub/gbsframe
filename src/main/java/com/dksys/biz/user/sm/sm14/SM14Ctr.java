package com.dksys.biz.user.sm.sm14;

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

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm14.service.SM14Svc;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/sm/sm14")
public class SM14Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM14Svc sm14Svc;	

	// 매입관리 입고 조회
	@PostMapping(value = "/selectPurchaseList")
	public String selectPurchaseList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm14Svc.selectPurchaseListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm14Svc.selectPurchaseList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
		

	// 거래처별 매입 확정 집계 조회
	@PostMapping(value = "/selectClntPurchaseList")
	public String selectClntPurchaseList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm14Svc.selectClntPurchaseList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}	

	// 거래처별 매입 확정 상세 조회
	@PostMapping(value = "/selectClntPurchaseDetailList")
	public String selectClntPurchaseDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		paramMap.put("mngIdCd", ObjectUtil.sqlInCodeGen(paramMap.get("mngIdCd")));
		List<Map<String, String>> result = sm14Svc.selectClntPurchaseDetailList(paramMap);
		model.addAttribute("result", result);
		
	   	List<Map<String, String>> resultMngId = sm14Svc.select_mngId_code(paramMap);
	   	model.addAttribute("resultMngId", resultMngId);
	   	
		return "jsonView";
	}
		// 매입관리 발주 조회 엑셀
	@PostMapping(value = "/selectPurchaseExcelList")
	public String selectPurchaseExcelList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm14Svc.selectPurchaseExcelList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}	

	
	// 발주상세 조회
	@PostMapping(value = "/selectPurchaseDetailList")
	public String selectPurchaseDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm14Svc.selectPurchaseDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}	
	
	//매입확정관리 등록
    @PostMapping(value = "/insertPurchaseBillDetail")
    public String insertPurchase(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm14Svc.insertPurchaseBillDetail(paramMap) != 0 ) {
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
    
	//매입확정관리 선택등록
    @PostMapping(value = "/insertinsertPurchaseSel")
    public String insertinsertPurchaseSel(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm14Svc.insertinsertPurchaseSel(paramMap) != 0 ) {
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
    
	//매입 detail 삭제    
	@DeleteMapping(value = "/deletePurchaseDetail")
	public String deletePurchaseDetail(@RequestBody Map<String, String> param, ModelMap model) {
		sm14Svc.deletePurchaseDetail(param);
		model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		return "jsonView";
	} 

	//세금계산서발행여부 
	@PostMapping(value = "/updateBillYn")
    public String updateBillYnChk(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm14Svc.updateBillYn(paramMap) != 0 ) {
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

	//세금계산서발행여부 
	@PostMapping(value = "/updateBillSeqYn")
    public String updateBillSeqYn(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm14Svc.updateBillSeqYn(paramMap) != 0 ) {
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
	
	// 발주자재 조회
	@PostMapping(value = "/selectOrdrgMatList")
	public String selectOrdrgMatList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm14Svc.selectOrdrgMatList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}	

	// 매입관리 입고 조회 NEW
	@PostMapping(value = "/selectPurchaseListNew")
	public String selectPurchaseListNew(@RequestBody Map<String, String> paramMap, ModelMap model) {
		paramMap.put("clntPjt", ObjectUtil.sqlInCodeGen(paramMap.get("clntPjt")));
		paramMap.put("mngIdCd", ObjectUtil.sqlInCodeGen(paramMap.get("mngIdCd")));
		int totalCnt = sm14Svc.selectPurchaseListCountNew(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm14Svc.selectPurchaseListNew(paramMap);
		model.addAttribute("result", result);

	   	List<Map<String, String>> resultPrjct = sm14Svc.select_prjct_code(paramMap);
	   	model.addAttribute("resultPrjct", resultPrjct);

	   	List<Map<String, String>> resultMngId = sm14Svc.select_mngId_code(paramMap);
	   	model.addAttribute("resultMngId", resultMngId);
	   	
		return "jsonView";
	}
	

	// 매입관리 입고 조회 NEW--Nam 거래처별 집계처리 하단그리드 세부내용
	@PostMapping(value = "/sm14selectPurchaseListNew")
	public String sm14selectPurchaseListNew(@RequestBody Map<String, String> paramMap, ModelMap model) {
		paramMap.put("clntPjt", ObjectUtil.sqlInCodeGen(paramMap.get("clntPjt")));
		paramMap.put("mngIdCd", ObjectUtil.sqlInCodeGen(paramMap.get("mngIdCd")));
		int totalCnt = sm14Svc.sm14selectPurchaseListNewCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm14Svc.sm14selectPurchaseListNew(paramMap);
		model.addAttribute("result", result);

	   	List<Map<String, String>> resultPrjct = sm14Svc.select_prjct_code(paramMap);
	   	model.addAttribute("resultPrjct", resultPrjct);

	   	List<Map<String, String>> resultMngId = sm14Svc.select_mngId_code(paramMap);
	   	model.addAttribute("resultMngId", resultMngId);
	   	
		return "jsonView";
	}
	

	// 매입관리 입고 조회 NAM 240226
	@PostMapping(value = "/selectClntPurchaseInboundList")
	public String selectClntPurchaseInboundList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm14Svc.selectClntPurchaseInboundList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	// 발주/입고 조회 NEW
	@PostMapping(value = "/selectOrderDetailListNew")
	public String selectOrderDetailListNew(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm14Svc.selectOrderDetailListNew(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	// 발주/입고 조회 NEW
	@PostMapping(value = "/selectOrderDetailListNewNam")
	public String selectOrderDetailListNewNam(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm14Svc.selectOrderDetailListNewNam(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	// 매입 조회 NEW
	@PostMapping(value = "/selectPchsDetailListNew")
	public String selectPchsDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm14Svc.selectPchsDetailListNew(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//매입확정관리 등록
    @PostMapping(value = "/insertPurchaseBillDetailNew")
    public String insertPurchaseBillDetailNew(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
    	
  		try {
  			if (sm14Svc.insertPurchaseBillDetailNew(paramMap) != 0 ) {
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