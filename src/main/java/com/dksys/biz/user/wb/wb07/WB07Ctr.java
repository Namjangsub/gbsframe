package com.dksys.biz.user.wb.wb07;

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

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.wb.wb07.service.WB07Svc;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;

@Controller
@RequestMapping("/user/wb/wb07")
public class WB07Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    WB07Svc wb07svc;


    //리스트 조회
	@PostMapping(value = "/select_wb07_List")
	public String select_wb07_List(@RequestBody Map<String, Object> paramMap, ModelMap model) {
		List<Map<String, String>> result = wb07svc.select_wb07_List(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	


	@PostMapping(value = "/updateWbsRemarks")
    public String updateWbsRemarks(@RequestBody Map<String, String> paramMap,  ModelMap model) throws Exception {
  		try {
  			if (wb07svc.updateWbsRemarks(paramMap) != 0 ) {
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


	@PostMapping(value = "/updateWbsSchedule")
    public String updateWbsSchedule(@RequestBody Map<String, String> paramMap,  ModelMap model) throws Exception {
  		try {
  			if (wb07svc.updateWbsSchedule(paramMap) != 0 ) {
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

	@PostMapping(value = "/createActualUnconfirmed")
	public String createActualUnconfirmed(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (wb07svc.createActualUnconfirmed(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/completeActualConfirmed")
	public String completeActualConfirmed(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (wb07svc.completeActualConfirmed(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/resetActualUnconfirmed")
	public String resetActualUnconfirmed(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (wb07svc.resetActualUnconfirmed(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("update"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}

	@PostMapping(value = "/deleteActual")
	public String deleteActual(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		try {
			if (wb07svc.deleteActual(paramMap) != 0) {
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			} else {
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
			}
		} catch (Exception e) {
			model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
		}
		return "jsonView";
	}
	@PostMapping(value = "/selectWb22d02ExtraInfoGrid")
	public String selectWb22d02ExtraInfoGrid(@RequestBody Map<String, Object> paramMap, ModelMap model) {
		List<Map<String, String>> result = wb07svc.selectWb22d02ExtraInfoGrid(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/updateWb22d02ExtraInfoBatch")
	public String updateWb22d02ExtraInfoBatch(@RequestBody Map<String, Object> paramMap, ModelMap model) throws Exception {
		Map<String, Object> result = wb07svc.updateWb22d02ExtraInfoBatch(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}

	@PostMapping(value = "/updateWb22d02ExtraInfo")
	public String updateWb22d02ExtraInfo(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
		Map<String, Object> result = wb07svc.updateWb22d02ExtraInfo(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
	
}