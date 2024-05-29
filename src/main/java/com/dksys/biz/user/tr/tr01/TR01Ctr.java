package com.dksys.biz.user.tr.tr01;

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

import com.dksys.biz.user.tr.tr01.service.TR01Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/tr/tr01")
public class TR01Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
 TR01Svc tr01Svc;

//   프로젝트 리스트 조회
  @PostMapping(value = "/selectTransList")
  public String selectTransList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = tr01Svc.selectTransCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = tr01Svc.selectTransList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  // 프로젝트 정보 조회
  @PostMapping(value = "/selectTransInfo")
  public String selectTransInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
	Map<String, String> result = tr01Svc.selectTransInfo(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  

  @PostMapping(value = "/insertTrans")
  public String insertTrans(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			if (tr01Svc.insertTrans(paramMap, mRequest) != 0 ) {
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

  @PostMapping(value = "/updateTrans")
  public String updateContract(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
	  	try {
			if (tr01Svc.updateTrans(paramMap, mRequest) != 0 ) {
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

  @PutMapping(value = "/deleteTrans")
  public String deleteTrans(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
	  	try {
			if (tr01Svc.deleteTrans(paramMap) != 0 ) {
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

}