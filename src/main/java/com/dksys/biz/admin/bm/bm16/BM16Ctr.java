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
   // 요청으로부터 받은 JSON 형식의 데이터를 Map<String, String> 형태의 파라미터로 받음
    Map<String, String> tempParam = new HashMap<String, String>();
    // 메소드는 PDRT (Product) ITEM을 수정하거나 등록하는 기능을 수행
    //  임시 맵(tempParam)을 생성하고 요청으로 받은 파라미터를 복사
    tempParam.putAll(param);
    tempParam.put("prdtDt", "");
    //	 그런 다음 "prdtDt" 키에 빈 문자열 복사

    int count = bm16Svc.selectOneMasterCount(tempParam);
    //    tempParam을 사용하여 기존의 마스터 데이터(row) 개수를 조회

    if (count > 0) {
      bm16Svc.insertOneMaster(param);
      bm16Svc.updateOneDetail01(param);
	 /*만약 개수가 0보다 크다면, 기존에 등록된 설비(row)가 있으므로
	  * 마스터 데이터를 등록한 후에 ITEM detail을 자동으로 저장*/
    } else {
      bm16Svc.insertOneMaster(param);
      // 그렇지 않으면, 새로운 insertOneMaster 데이터를 등록
    }

    model.addAttribute("resultCode", 200);
    					// 결과 코드 , 정상
    model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    					// 결과 메세지
    return "jsonView";
	/*결과를 담은 ModelMap 객체에 결과 코드(resultCode)와 결과 메시지(resultMessage)를 추가하고 "jsonView"를
	 * 반환하여 JSON 형식의 응답을 생성*/
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