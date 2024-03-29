package com.dksys.biz.user.sm.sm03;

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
import com.dksys.biz.user.sm.sm03.service.SM03Svc;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/sm/sm03")
public class SM03Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM03Svc sm03Svc;	

	// 매입관리 입고 조회
	@PostMapping(value = "/selectWareHousingList")
	public String selectWareHousingList(@RequestBody Map<String, String> paramMap, ModelMap model) {

		paramMap.put("clntPjt", ObjectUtil.sqlInCodeGen(paramMap.get("clntPjt")));
		paramMap.put("mngIdCd", ObjectUtil.sqlInCodeGen(paramMap.get("mngIdCd")));
		int totalCnt = sm03Svc.selectWareHousingListCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm03Svc.selectWareHousingList(paramMap);
		model.addAttribute("result", result);
		

	   	List<Map<String, String>> resultPrjct = sm03Svc.select_prjct_code(paramMap);
	   	model.addAttribute("resultPrjct", resultPrjct);

	   	List<Map<String, String>> resultMngId = sm03Svc.select_mngId_code(paramMap);
	   	model.addAttribute("resultMngId", resultMngId);
	   	
		return "jsonView";
	}
	
	
	// 발주상세 조회
	@PostMapping(value = "/selectWareHousingDetailList")
	public String selectWareHousingDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> resultList = sm03Svc.selectWareHousingDetailList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}	
	
	//발주 등록
    @PostMapping(value = "/insertWareHousing")
    public String insertWareHousing(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm03Svc.insertWareHousing(paramMap, mRequest) != 0 ) {
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
    
    // 입고정보 - 수정 팝업
  	@PostMapping(value = "/selectWareHousingDetailInfo")
  	public String selectWareHousingDetailInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
  		List<Map<String, String>> resultList = sm03Svc.selectWareHousingDetailInfo(paramMap);
  		model.addAttribute("resultList", resultList);
  		return "jsonView";
  	} 
    
	//발주 수정
    @PostMapping(value = "/updateWareHousing")
    public String updateWareHousing(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm03Svc.updateWareHousing(paramMap, mRequest) != 0 ) {
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

	//발주 수정
    @PostMapping(value = "/updateWareHousingDetail")
    public String updateWareHousingDetail(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
  		try {
  			if (sm03Svc.updateWareHousingDetail(paramMap, mRequest) != 0 ) {
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

    
	//발주 detail 삭제    
	@DeleteMapping(value = "/deleteWareHousingDetail")
	public String deleteWareHousingDetail(@RequestBody Map<String, String> param, ModelMap model) {
		sm03Svc.deleteWareHousingDetail(param);
		model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		return "jsonView";
	} 
	
	//입고내역중 입고번호에 해당하는 내역 Direct 삭제  (마스터 삭제후 모든 상세내역 삭제처리 함.)    
	@DeleteMapping(value = "/deleteWareHousingInno")
	public String deleteWareHousingInno(@RequestBody Map<String, String> param, ModelMap model) {
		sm03Svc.deleteWareHousingInno(param);
		model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		return "jsonView";
	} 

    //헤더 multi select 코드 검색
    @PostMapping(value = "/select_prjct_code")
    public String select_prjct_code(@RequestBody Map<String, String> paramMap, ModelMap model) {
   	List<Map<String, String>> result = sm03Svc.select_prjct_code(paramMap);
   	model.addAttribute("result", result);
   	return "jsonView";
    }

    //헤더 multi select 코드 검색
    @PostMapping(value = "/select_mngId_code")
    public String select_mngId_code(@RequestBody Map<String, String> paramMap, ModelMap model) {
   	List<Map<String, String>> result = sm03Svc.select_mngId_code(paramMap);
   	model.addAttribute("result", result);
   	return "jsonView";
    }
}