package com.dksys.biz.user.pm.pm06;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.user.pm.pm06.service.PM06Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/pm/pm06")
public class PM06Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  PM06Svc pm06Svc;

  // 작업일보 리스트 조회
  @PostMapping(value = "/selectTripRptList")
  public String selectTripRptList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = pm06Svc.selectTripRptListCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = pm06Svc.selectTripRptList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }
  
}