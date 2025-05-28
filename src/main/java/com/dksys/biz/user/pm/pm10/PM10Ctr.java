package com.dksys.biz.user.pm.pm10;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.pm.pm10.service.PM10Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm10")
public class PM10Ctr {

	@Autowired
	MessageUtils messageUtils;

	@Autowired
	PM10Svc pm10Svc;

	// 임팀장회의록 현황 조회
	@PostMapping(value = "/selectMnList")
	public String selectClntPurchaseList(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		
		List<Map<String, String>> result = pm10Svc.selectMnList(paramMap);
		model.addAttribute("result", result);
		// 임팀장회의록 참석자 조회
		List<Map<String, String>> d02List = pm10Svc.select_p10_d02_List(paramMap);
		model.addAttribute("d02List", d02List);
		return "jsonView";
	}

	// 메인
	@PostMapping("/pm10_main_update")
	public String pm10_main_update(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
		try {
			pm10Svc.pm10_main_update(param);
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
		} catch(Exception e) {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
		}
		return "jsonView";
    }


	// 주제
	@PostMapping(value = "/pm10_d01_update")
	public String pm10_d01_update(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
		try {
			if (pm10Svc.pm10_d01_update(param) != 0) {
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


	// 팀별내용
	@PostMapping(value = "/pm10_d03_update")
	public String pm10_d03_update(@RequestBody Map<String, String> param, ModelMap model) throws Exception {
		try {
			if (pm10Svc.pm10_d03_update(param) != 0) {
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



	// 임팀장회의록 삭제
	@PostMapping(value = "/deleteMn")
	public String deleteMn(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (pm10Svc.deleteMn(paramMap) != 0) {
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

	// // 정렬번호 업데이트
	@PostMapping(value = "/pm10_d01_sortNo_update")
	public String pm10_d01_sortNo_update(@RequestBody Map<String,Object> paramMap, ModelMap model) throws Exception {
		try {
			if (pm10Svc.pm10_d01_sortNo_update(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		} catch(Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 참석자 update
	@PostMapping("/pm10_d02_update")
	public String pm10_d02_update(@RequestBody Map<String,Object> paramMap, ModelMap model) throws Exception {
		try {
			if (pm10Svc.pm10_d02_update(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		} catch(Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	// 참석자 삭제
	@PostMapping(value = "/pm10_d02_delete")
	public String pm10_d02_delete(@RequestBody Map<String, Object> paramMap, ModelMap model) throws Exception {
		try {
			if (pm10Svc.pm10_d02_delete(paramMap) != 0) {
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

	// 파일업로드
	@PostMapping("/mnUploadFile")
	public String mnUploadFile(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
		try {
			if (pm10Svc.mnUploadFile(paramMap, mRequest) >= 0 ) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("save"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			};
		}catch(Exception e){
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", e.getMessage());
		}
		
		return "jsonView";
	}
}