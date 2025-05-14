package com.dksys.biz.user.sm.sm30;

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

import com.dksys.biz.user.qm.qm01.service.QM01Svc;
import com.dksys.biz.user.sm.sm30.service.SM30Svc;
import com.dksys.biz.user.wb.wb20.service.WB20Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/sm/sm30")
public class SM30Ctr {

	@Autowired
    MessageUtils messageUtils;

	@Autowired
	WB20Svc wb20Svc;

	@Autowired
	SM30Svc sm30Svc;

	@Autowired
	QM01Svc qm01Svc;

	// PJT 정보 조회
	@PostMapping(value = "/selectPjtInfo")
	public String selectPjtInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = sm30Svc.selectPjtInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// PJT별 대금 입금/지급 정보 상세 조회
	@PostMapping(value = "/selectPjtInfoDetail")
	public String selectPjtInfoDetail(@RequestBody Map<String, String> paramMap, ModelMap model) {
	  List<Map<String, String>> result = sm30Svc.selectPjtInfoDetail(paramMap);
	  model.addAttribute("result", result);
	  return "jsonView";
	}

	// 메입 대금 지급 실적 엑셀업로드 전 Check
	@PostMapping("/select_sm21_chk")
	public String select_sm21_chk(@RequestBody Map<String, Object> paramMap,ModelMap model) {
		try {
			List<Map<String, String>> result = sm30Svc.select_sm21_chk(paramMap);
			if (!result.isEmpty()) {
				model.addAttribute("result", result);
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", "Upload 적용 가능합니다.");
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail")); 
			}
		} catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}

		return "jsonView";
	}

	// 매입 대금 지급 실적 엑셀업로드
	@PostMapping("/insert_sm21_excelUpload")
    public String insert_sm21_excelUpload(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
    	try {
    		int excelData = sm30Svc.insert_sm21_excelUpload(paramMap);
    		if (excelData > 0) {
    			model.addAttribute("rtnStr", excelData);
    			model.addAttribute("resultCode", 200);
    			model.addAttribute("resultMessage", "적용 되었습니다.");
    		} else {
    			model.addAttribute("resultCode", 500);
    			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));    			
    		}
    	}catch(Exception e) {
    		model.addAttribute("resultCode", 900);
    		model.addAttribute("resultMessage", e.getMessage());
    	}
    	return "jsonView";
    }
	
	// 매입대금 지급 결재요청 등록
	@PostMapping(value = "/insert_sm30")
    public String insert_sm30(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
        try {
            if(sm30Svc.insert_sm30(paramMap, mRequest) !=0 ) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
                model.addAttribute("fileTrgtKey", paramMap.get("fileTrgtKey"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch(Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }        
        return "jsonView";
    }

	// 매입대금 지급 결재요청 기본 정보 조회
	@PostMapping(value = "/select_sm30_info")
	public String select_sm30_info(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = sm30Svc.select_sm30_info(paramMap);
		model.addAttribute("result", result);

		paramMap.put("reqNo", paramMap.get("fileTrgtKey"));
		// 결재정보 확인
		// 결재정보 확인 최종 Y, N 만 가져옴.
		Map<String, String> approval = sm30Svc.selectApprovalUserChk(paramMap);
		model.addAttribute("approval", approval);
		return "jsonView";
	}

	// 매입대금 지급 결재요청 리스트 정보 조회
	@PostMapping(value = "/sm30_pop_grid1_selectList")
	public String sm30_pop_grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = sm30Svc.sm30_pop_grid1_selectList(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 매입대금 지급 결재 요청 기본 정보 삭제
	@PutMapping(value = "/delete_all_sm30")
	public String delete_all_sm30(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			int result = sm30Svc.delete_all_sm30(paramMap);
			if (result > 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
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

	// 매입대금 지급 결재 요청 기본 리스트 삭제
	@PutMapping(value = "/delete_sm30_detail")
	public String delete_sm30_detail(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			int result = sm30Svc.delete_sm30_detail(paramMap);
			if (result > 0) {
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

	// 결재여부 유저정보 반환
	@PostMapping(value = "/selectApprovalUserChk")
	public String selectApprovalUserChk(@RequestBody Map<String, String> paramMap, ModelMap model) {
		Map<String, String> result = sm30Svc.selectApprovalUserChk(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	// 결재보류 업데이트
	@PostMapping(value = "/updateApprovalHold")
	public String updateApprovalHold(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)
			throws Exception {
		try {
			if (sm30Svc.updateApprovalHold(paramMap, mRequest) != 0) {
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

	// 결재자 추가
	@PostMapping(value = "/updateShareUser")
    public String updateShareUser(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
        try {
            if(sm30Svc.updateShareUser(paramMap, mRequest) !=0 ) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch(Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }        
        return "jsonView";
    }
}
