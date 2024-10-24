package com.dksys.biz.user.pm.pm40;

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
import com.dksys.biz.user.pm.pm40.service.PM40Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm40")
public class PM40Ctr {

	
	@Autowired
    MessageUtils messageUtils;

	
	  @Autowired
	  PM40Svc pm40svc;
	
	  
	  // 작업일보 리스트 조회
	  @PostMapping(value = "/selectYearWorkMainList")
	  public String selectYearWorkMainList(@RequestBody Map<String, String> paramMap, ModelMap model) {
		  List<Map<String, String>>  result = pm40svc.selectYearWorkMainList(paramMap);
	      model.addAttribute("result", result);
	      return "jsonView";
	  }
	  
	  
	//리스트 조회
		@PostMapping(value = "/selectMainGridList")
		public String grid1_selectList(@RequestBody Map<String, String> paramMap, ModelMap model) {
			int totalCnt = pm40svc.selectMainGridListCount(paramMap);
			PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
			model.addAttribute("paginationInfo", paginationInfo);
			List<Map<String, String>> result = pm40svc.selectMainGridList(paramMap);
			model.addAttribute("result", result);
			return "jsonView";
		}  
	  
		@PostMapping(value = "/insert_pm40")
		public String insert_pm40(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
			try {
				int result = pm40svc.insert_pm40(paramMap, mRequest);
				if (result != 0 && result != 7 ) {
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
				}
				else if (result != 0 && result == 7) {
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", "해당 월의 고찰은 등록 되어 있습니다.");
				}
				else {
					model.addAttribute("resultCode", 500);
					model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
				};
			}catch(Exception e){
				model.addAttribute("resultCode", 900);
				model.addAttribute("resultMessage", e.getMessage());
			}
			return "jsonView";
		}
	
		//UPDATE
		@PostMapping(value = "/update_pm40")
		public String update_pm40(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
			try {
				if (pm40svc.update_pm40(paramMap, mRequest) != 0 ) {
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
		
		//DELETE
		@PutMapping(value = "/delete_pm40")
		public String delete_pm40(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
			try {
				int result = pm40svc.delete_pm40(paramMap);
				//if (pm40svc.delete_pm40(paramMap) != 0 ) {
				if (result != 0 && result != 7) {
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
				} else if (result != 0 && result == 7) {
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", "해당 월의 팀장 고찰 등록 되어 있어 삭제가 불가합니다.");
				}
				else {
					model.addAttribute("resultCode", 500);
					model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
				};
			}catch(Exception e){
				model.addAttribute("resultCode", 900);
				model.addAttribute("resultMessage", e.getMessage());
			}
		  	return "jsonView";
		}
		
		//정보 조회
		@PostMapping(value = "/select_pm40_Info")
		public String select_pm02_Info(@RequestBody Map<String, String> paramMap, ModelMap model) {
			Map<String, String> result = pm40svc.select_pm40_Info(paramMap);
			model.addAttribute("result", result);
			return "jsonView";
		}
		
}
