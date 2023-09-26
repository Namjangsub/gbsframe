package com.dksys.biz.user.qm.qm06;

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

import com.dksys.biz.user.qm.qm06.service.QM06Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/qm/qm06")
public class QM06Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  QM06Svc qm06Svc;

// 발주 및 출장 요청서 리스트 조회
  @PostMapping(value = "/selectQualityReqList")
  public String selectQualityReqList(@RequestBody Map<String, String> paramMap, ModelMap model) {
//    int totalCnt = qm06Svc.selectQualityReqCount(paramMap);
//    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
//    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> result = qm06Svc.selectQualityReqList(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

}
