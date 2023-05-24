package com.dksys.biz.admin.bm.bm16;

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

import com.dksys.biz.admin.bm.bm16.service.BM16Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/bm/bm16")
public class BM16Ctr {

  @Autowired
  MessageUtils messageUtils;

  @Autowired
  BM16Svc bm16Svc;

//   프로젝트 리스트 조회
  @PostMapping(value = "/selectPrjctList")
  public String selectPrjctList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = bm16Svc.selectPrjctCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);
    List<Map<String, String>> bm1601m01 = bm16Svc.selectPrjctList(paramMap);
    model.addAttribute("bm1601m01", bm1601m01);
    return "jsonView";
  }

  // 프로젝트 정보 조회
  @PostMapping(value = "/selectPrjctInfo")
  public String selectPrjctInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
    Map<String, String> result = bm16Svc.selectPrjctInfo(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  // 아이템 정보조회
  @PostMapping("/selectItemList")
  public String selectItemList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    List<Map<String, String>> itemList = bm16Svc.selectItemList(paramMap);
    model.addAttribute("itemList", itemList);
    return "jsonView";
  }

  // 대상설비 리스트 조회
  @PostMapping("/selectPrdtList")
  public String selectPrdtList(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int totalCnt = bm16Svc.selectPrdtCount(paramMap);
    PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
    model.addAttribute("paginationInfo", paginationInfo);

    List<Map<String, String>> prdtList = bm16Svc.selectPrdtList(paramMap);
    model.addAttribute("prdtList", prdtList);
    return "jsonView";
  }

  @PostMapping(value = "/selectConfirmCount")
  public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
    int result = bm16Svc.selectConfirmCount(paramMap);
    model.addAttribute("result", result);
    return "jsonView";
  }

  @PostMapping(value = "/insertPrjct")
  public String insertPrjct(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest,
      ModelMap model) {
    bm16Svc.insertPrjct(paramMap, mRequest);
    model.addAttribute("resultCode", 200);
    model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    return "jsonView";
  }

  @PostMapping(value = "/updatePrjct")
  public String updatePrjct(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest,
      ModelMap model) {
    bm16Svc.updatePrjct(paramMap, mRequest);
    model.addAttribute("resultCode", 200);
    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    return "jsonView";
  }

  @PutMapping(value = "/deletePrjct")
  public String deletePrjct(@RequestBody Map<String, String> param, ModelMap model) {
    bm16Svc.deletePrjct(param);
    model.addAttribute("resultCode", 200);
    model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    return "jsonView";
  }

  @DeleteMapping(value = "/deletePrjctDtl")
  public String deletePrjctDtl(@RequestBody List<Map<String, String>> paramList, ModelMap model) {
    try {
      bm16Svc.deletePrjctDtl(paramList);
      model.addAttribute("resultCode", 200);
      model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    } catch (Exception e) {
      model.addAttribute("resultCode", 500);
      model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
    }
    return "jsonView";
  }

//이하 PRDT 추가

//대상설비 중복 조회
  @PostMapping("/checkOverLapMaster")
  public String checkOverLapMaster(@RequestBody Map<String, String> param, ModelMap model) {
    int result = bm16Svc.selectOneMasterCount(param);
    if (result > 0) {
      model.addAttribute("resultCode", 500);
      model.addAttribute("resultMessage", messageUtils.getMessage("exist"));
    } else {
      model.addAttribute("resultCode", 200);
      model.addAttribute("resultMessage", messageUtils.getMessage("check"));
    }
    model.addAttribute("result", result);

    return "jsonView";
  }

//  // 리스트 조회
  @PostMapping("/checkOverLapDetail01")
  public String checkOverLapDetail01(@RequestBody Map<String, String> param, ModelMap model) {
    int result = bm16Svc.selectDetail01Count(param);
    if (result > 0) {
      model.addAttribute("resultCode", 500);
      model.addAttribute("resultMessage", messageUtils.getMessage("exist"));
    } else {
      model.addAttribute("resultCode", 200);
      model.addAttribute("resultMessage", messageUtils.getMessage("check"));
    }
    model.addAttribute("result", result);

    return "jsonView";
  }

  // PDRT ITEM 상세조회
  @PostMapping("/seletOneMaster")
  public String seletOneMaster(@RequestBody Map<String, String> param, ModelMap model) {
    Map<String, String> itemInfo = bm16Svc.seletOneMaster(param);
    model.addAttribute("itemInfo", itemInfo);
    return "jsonView";
  }

  // PDRT ITEM 수정 및 등록
  @PutMapping("/insertOneMaster")
  public String insertOneMaster(@RequestBody Map<String, String> param, ModelMap model) {

    Map<String, String> tempParam = new HashMap<String, String>();

    tempParam.putAll(param);
    tempParam.put("prdtDt", "");

    int count = bm16Svc.selectOneMasterCount(tempParam);
    // 기존 설비 row가 있을 시, ITEM detail 자동저장
    if (count > 0) {
      bm16Svc.insertOneMaster(param);
      bm16Svc.updateOneDetail01(param);

    } else {

      bm16Svc.insertOneMaster(param);

    }

    model.addAttribute("resultCode", 200);
    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    return "jsonView";
  }

//  // 중복 조회
//  @PostMapping("/checkOverLapMaster")
//  public String checkOverLapMaster(@RequestBody Map<String, String> param, ModelMap model) {
//    int result = bm16Svc.selectOneMasterCount(param);
//    if (result > 0) {
//      model.addAttribute("resultCode", 500);
//      model.addAttribute("resultMessage", messageUtils.getMessage("exist"));
//    } else {
//      model.addAttribute("resultCode", 200);
//      model.addAttribute("resultMessage", messageUtils.getMessage("check"));
//    }
//    model.addAttribute("result", result);
//
//    return "jsonView";
//  }

}