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
	
	@PostMapping("/selectOrdrsListPop")
	public String selectOrdrsListPop(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectOrdrsListPopCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, Object>> ordrsList = cr02Svc.selectOrdrsListPop(param);
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
	
	@PostMapping(value = "/ItemDivEtc")
	public String getItemDivEtc(@RequestBody Map<String, String> param, ModelMap model) {
		String itemDivEtc = cr02Svc.selectItemDivEtc(param);
		model.addAttribute("itemDivEtc", itemDivEtc);
		return "jsonView";
	}

	@PostMapping(value = "/insertOrdrs")
	public String insertOrdrs(@RequestParam Map<String, String> param, MultipartHttpServletRequest mRequest, ModelMap model) {

		try {
			int rtnInt = cr02Svc.selectOrdrsKey(param);
			if(rtnInt == 0) {
				Map<String, String> rtnResult = cr02Svc.insertOrdrs(param,mRequest);
				model.addAttribute("ordrsNo", rtnResult.get("ordrsNo").toString());
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		    }else {
		    	model.addAttribute("resultCode", 900);
		    	model.addAttribute("resultMessage", "이미 등록된 건양수주번호가 있습니다.");
		    }
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}


		return "jsonView";
	}


	@PostMapping(value = "/updateOrdrs")
	public String updateOrdrs(@RequestParam Map<String, String> param,MultipartHttpServletRequest mRequest, ModelMap model) {

		try {
			cr02Svc.updateOrdrs(param,mRequest);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("update"));
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}




		return "jsonView";
	}

	@PostMapping(value = "/updateOrdrsPmntPlanProcess")
	public String updateOrdrsPmntPlanProcess(@RequestParam Map<String, String> param,MultipartHttpServletRequest mRequest, ModelMap model) {

		try {
			cr02Svc.updateOrdrsPmntPlanProcess(param,mRequest);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("update"));
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}




		return "jsonView";
	}

	@DeleteMapping(value = "/deleteOrdrs")
	public String deleteOrdrs(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {

		try {
			cr02Svc.deleteOrdrs(paramMap);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		}catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getLocalizedMessage());
		}
		return "jsonView";

	}


	@PostMapping("/selectOrdrsPlanHis")
	public String selectOrdrsPlanHis(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectOrdrsPlanHisCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, Object>> ordrsPlanHisList = cr02Svc.selectOrdrsPlanHis(param);
		model.addAttribute("ordrsPlanHisList", ordrsPlanHisList);
		return "jsonView";
	}
	
	  //SalesCode Search modal폼 사용 조회
	  @PostMapping(value = "/selectWbsLeftSalesCodeTreeList") 
	  public String selectWbsLeftSalesCodeTreeList(@RequestBody Map<String, String> param, ModelMap model) {		  
		  List<Map<String, Object>> fileList = cr02Svc.selectWbsLeftSalesCodeTreeList(param);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
	  }	 	
	//SalesCode Search사용 조회
	  @PostMapping(value = "/selectItemSalesCodeTreeList") 
	  public String selectItemSalesCodeTreeList(@RequestBody Map<String, String> param, ModelMap model) {		  
		  List<Map<String, Object>> fileList = cr02Svc.selectItemSalesCodeTreeList(param);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
	  }

	//복사기능 호출
	@PostMapping(value = "/callCopyOrdrs") 
	public String callCopyOrdrs(@RequestBody Map<String, String> paramMap, ModelMap model) {	  
		cr02Svc.callCopyOrdrs(paramMap);
		model.addAttribute("paramMap", paramMap);
		return "jsonView";
	}

	
	@PostMapping("/selectNoSalesCdOrdrsListPop")
	public String selectNoSalesCdOrdrsListPop(@RequestBody Map<String, String> param, ModelMap model) {
		int totalCnt = cr02Svc.selectNoSalesCdOrdrsListPopCount(param);
		PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, Object>> ordrsList = cr02Svc.selectNoSalesCdOrdrsListPop(param);
		model.addAttribute("ordrsList", ordrsList);
		return "jsonView";
	}
	
	  @PostMapping(value = "/selectJunmooApproval") 
	  public String selectJunmooApproval(@RequestBody Map<String, String> param, ModelMap model) {		  
		  int JunmooApproval = cr02Svc.selectJunmooApproval(param);
		  model.addAttribute("JunmooApproval", JunmooApproval);
		  return "jsonView";
	  }
	  
		//설비원가정보 삭제    
		@DeleteMapping(value = "/deleteOrdrsDetail")
		public String deleteOrdrsDetail(@RequestBody Map<String, String> param, ModelMap model) {
	    	try {			
	    		cr02Svc.deleteOrdrsDetail(param);
				model.addAttribute("resultCode", 200);
	    		model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
	    	}catch(Exception e) {
		    	 model.addAttribute("resultCode", 500);
		 		 model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
	    	}	    	
			return "jsonView";
		}
		
	  //SalesCode Search modal폼 사용 조회
	  @PostMapping(value = "/selectOrdrsDetails") 
	  public String selectOrdrsDetails(@RequestBody Map<String, String> param, ModelMap model) {		  
		  List<Map<String, Object>> fileList = cr02Svc.selectOrdrsDetails(param);
		  model.addAttribute("fileList", fileList);
		  return "jsonView";
	  }
	
}