package com.dksys.biz.user.im.im01;

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

import com.dksys.biz.admin.bm.bm01.mapper.BM01Mapper;
import com.dksys.biz.admin.bm.bm02.mapper.BM02Mapper;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr02.mapper.CR02Mapper;
import com.dksys.biz.user.im.im01.service.IM01Svc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/user/im/im01")
public class IM01Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    IM01Svc im01Svc;

    @Autowired
    BM02Mapper bm02Mapper;

    @Autowired
    CR02Mapper cr02Mapper;

    @Autowired
    BM01Mapper bm01Mapper;
  
    // 개선제안 리스트 조회
    @PostMapping(value = "/selectImprovementList")
    public String selectImprovementList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int totalCnt = im01Svc.selectImprovementListCount(paramMap);
        PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, String>> result = im01Svc.selectImprovementList(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    // 개선제안 정보 조회
    @PostMapping(value = "/selectImprovement")
    public String selectImprovement(@RequestBody Map<String, String> paramMap, ModelMap model) {
        Map<String, String> result = im01Svc.selectImprovement(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    @PostMapping(value = "/insertImprovement")
    public String insertImprovement(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)
            throws Exception {
        try {
            inputFieldExistCheck(paramMap, model);
        } catch (Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }
        if ((Integer.parseInt("222") == (int) model.get("resultCode"))) {
            try {
                if (im01Svc.insertImprovement(paramMap, mRequest) != 0) {
                    model.addAttribute("resultCode", 200);
                    model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
                    model.addAttribute("imprvmNo", paramMap.get("imprvmNo"));
                } else {
                    model.addAttribute("resultCode", 500);
                    model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
                }
                ;
            } catch (Exception e) {
                model.addAttribute("resultCode", 900);
                model.addAttribute("resultMessage", e.getMessage());
            }
        } else {
            // 입력 오류에 대한 추가 처리 내용
        }
        return "jsonView";
    }

    @PostMapping(value = "/updateImprovement")
    public String updateImprovement(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model)
            throws Exception {
        try {
            inputFieldExistCheck(paramMap, model);
        } catch (Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }
        if ((Integer.parseInt("222") == (int) model.get("resultCode"))) {
            try {
                if (im01Svc.updateImprovement(paramMap, mRequest) != 0) {
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
        }
        return "jsonView";
    }

    @PutMapping(value = "/deleteImprovement")
    public String deleteImprovement(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
        try {
            if (im01Svc.deleteImprovement(paramMap) != 0) {
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

    // 입력필드 오류체크
    public void inputFieldExistCheck(@RequestBody Map<String, String> paramMap, ModelMap model) {

        model.addAttribute("resultCode", 222);
        model.addAttribute("resultMessage", "");
        // 오더, salesCode용 coCd처리(ordCoCd 값이 없으면 기본 coCd값을 적용함
        if (paramMap.get("ordCoCd") == null || "".equals(paramMap.get("ordCoCd"))) {
            paramMap.put("ordCoCd", paramMap.get("coCd"));
        }

        int cnt = 0;
        // salescode 체크(salesCd)
        // salesCode가 있으면 salesCode에 있는 정보를 기준으로 재설정함
        if (paramMap.get("salesCd") == null || "".equals(paramMap.get("salesCd"))) {
            // 작업 내용에 따라 옵션 처리
        } else {
            cnt = cr02Mapper.salesCdExistValidationCheck(paramMap);
            if (cnt == 0) {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("salesCdNotFound"));
                return;
            } else {
                Map<String, String> result = cr02Mapper.salesCdSearchOrderInfo(paramMap);
                Object salesCdResult = result.get("salesCd");
                if (salesCdResult == null || "".equals(salesCdResult.toString())) {
                    model.addAttribute("resultCode", 500);
                    model.addAttribute("resultMessage", messageUtils.getMessage("salesCdNotFound"));
                    return;
                } else {
                    paramMap.put("clntCd", result.get("ordrsClntCd"));
                    paramMap.put("ordrsNo", result.get("ordrsNo"));
                    paramMap.put("prjctCd", result.get("clntPjt"));
                    paramMap.put("clntPjt", result.get("clntPjt"));
                    paramMap.put("eqpNm", result.get("eqpNm"));
                    paramMap.put("prdtCd", result.get("prdtCd"));
                    paramMap.put("itemDiv", result.get("itemDiv"));
                    paramMap.put("itemCd", result.get("itemDiv"));
                    return;
                }
            }
        }


        // 제품구분 체크(prdtCd)
        if (paramMap.get("prdtCd") == null || "".equals(paramMap.get("prdtCd"))) { // 입력값이 있는지 확인
            // 작업 내용에 따라 옵션 처리
        } else {
            cnt = bm01Mapper.prdtCdExistValidationCheck(paramMap);
            if (cnt == 0) {
//          Object prdtCdResult = result1.get("prdtCd");
//          if (prdtCdResult==null ||"".equals(prdtCdResult.toString())) {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("prdtCdNotFound"));
                return;
            }
        }
        // 아이템 체크(itemDiv)
        // Commbo Box로 프론트엔드에서 체크됨
    }

    @PostMapping(value = "/selectImprovementShareUserlst")
    public String selectImprovementShareUserlst(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = im01Svc.selectImprovementShareUserlst(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }
}