package com.dksys.biz.user.sm.sm05;

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
import com.dksys.biz.user.sm.sm05.service.SM05Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm05")
public class SM05Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	SM05Svc sm05Svc;

	// 프로젝트 리스트 조회 - 폐기내역 조회  
	@PostMapping(value = "/select_sm05_ioList")
	public String select_sm05_ioList(@RequestBody Map<String, String> paramMap, ModelMap model) {		
		int totalCnt = sm05Svc.select_sm05_ioCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);		
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm05Svc.select_sm05_ioList(paramMap);		
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 프로젝트 리스트 조회 - 폐기상세 조회
	@PostMapping(value = "/select_sm05_ioDetailList")
	public String select_sm05_ioDetailList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		
		int totalCnt = sm05Svc.select_sm05_ioDetailCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);		
		model.addAttribute("paginationInfo", paginationInfo);
		List<Map<String, String>> result = sm05Svc.select_sm05_ioDetailList(paramMap);		
		model.addAttribute("result", result);
		return "jsonView";
	}

	//  폐기창고 재고정보 - 모달 
//	@PostMapping(value = "/selectIoOutWhList")
//	public String selectIoOutWhList(@RequestBody Map<String, String> paramMap, ModelMap model) {
//		int totalCnt = sm05Svc.selectIoOutWhCount(paramMap);
//		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
//		model.addAttribute("paginationInfo", paginationInfo);
//		List<Map<String, String>> result = sm05Svc.selectIoOutWhList(paramMap);
//		model.addAttribute("result", result);
//		return "jsonView";
//	}  
  
	//정보 조회
	@PostMapping(value = "/select_sm05_ioInfo")
	public String select_sm05_ioInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = sm05Svc.select_sm05_ioInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
	//폐기창고 selectbox 선택시 값 나타내기
	@PostMapping("/selectOutWhNm")
    public String selectOutWhNm(@RequestBody Map<String, String> param, ModelMap model) {
        List<Map<String, Object>> whList = sm05Svc.selectOutWhNm(param);
        model.addAttribute("whList", whList);
        return "jsonView";
    }
	
	// 폐기창고재고정보 - 모달창
	@PostMapping(value = "/select_sm05_ioModalList")
	public String select_sm05_ioModalList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		int totalCnt = sm05Svc.select_sm05_ioModalCount(paramMap);
		PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		
		List<Map<String, String>> resultList = sm05Svc.select_sm05_ioModalList(paramMap);
		model.addAttribute("resultList", resultList);
		return "jsonView";
	}
	
	//폐기창고 insert - 불출정보
    @PostMapping(value = "/insert_sm05")
    public String insert_sm05(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)  {
		try {
			if (sm05Svc.insert_sm05(paramMap, mRequest) != 0 ) {
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
    
    //폐기창고 insert - 폐기창고 재고정보
    @PostMapping(value = "/insert_sm05_IoInfo")
    public String insert_sm05_IoInfo(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)  {
		try {
			if (sm05Svc.insert_sm05_IoInfo(paramMap, mRequest) != 0 ) {
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

    //수정
	@PostMapping(value = "/update_sm05")
	public String update_sm05(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest,
			ModelMap model) throws Exception {
		try {
			if (sm05Svc.update_sm05(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
	
	@PostMapping(value = "/update_sm05_IoInfo")
	public String update_sm05_IoInfo(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest,
			ModelMap model) throws Exception {
		try {
			if (sm05Svc.update_sm05_IoInfo(paramMap, mRequest) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
	//삭제
	@PutMapping(value = "/delete_sm05M_io")
	public String delete_sm05M_io(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (sm05Svc.delete_sm05M_io(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
			;
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
	
	@PutMapping(value = "/delete_sm05D_io")
	public String delete_sm05D_io(@RequestBody Map<String, String> paramMap, ModelMap model) {
		try {
				sm05Svc.delete_sm05D_io(paramMap);
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
		} catch (Exception e) {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
	}
	
	
	// 프로젝트 정보 조회 - popup 부분
//	  @PostMapping(value = "/selectIoInfo")
//	  public String selectIoInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
//		Map<String, String> result = sm05Svc.selectIoInfo(paramMap);
//	    model.addAttribute("result", result);
//	    return "jsonView";
//	  }
//	@PostMapping(value = "/selectConfirmCount")
//	public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
//		int result = sm05Svc.selectConfirmCount(paramMap);
//		model.addAttribute("result", result);
//		return "jsonView";
//	}




}