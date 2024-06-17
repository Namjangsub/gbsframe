package com.dksys.biz.user.pm.pm04;

import java.util.HashMap;
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

import com.dksys.biz.user.pm.pm04.service.PM04Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm04")
public class PM04Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  PM04Svc pm04Svc;

  // 작업일보 리스트 조회
  @PostMapping(value = "/selectDailyWorkMainList")
  public String selectDailyWorkMainList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    List<Map<String, String>> result = pm04Svc.selectDailyWorkMainList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  	//팀원 검색
	@PostMapping(value = "/select_all_name")
	public String select_all_name(@RequestBody Map<String, String> paramMap, ModelMap model) {
		List<Map<String, String>> result = pm04Svc.select_all_name(paramMap);
		model.addAttribute("result", result);
		return "jsonView";
	}
}