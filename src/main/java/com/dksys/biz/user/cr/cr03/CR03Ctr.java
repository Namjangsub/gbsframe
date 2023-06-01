package com.dksys.biz.user.cr.cr03;

import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.cr.cr03.service.CR03Svc;
import com.dksys.biz.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/cr/cr03")
public class CR03Ctr {

    @Autowired
    MessageUtils messageUtils;
    
    @Autowired
    CR03Svc cr03svc;


    @PostMapping("/maxEst")
    public String showEstimationForm(@RequestBody Map<String, String> param, ModelMap model) {
        String maxEstNo = cr03svc.selectMaxEstNo(param);
        model.addAttribute("maxEstNo", maxEstNo);
        return "jsonView";
    }

    @PostMapping("/selectMaxEstDeg")
    public String selectMaxEstDeg(@RequestBody Map<String, String> param, ModelMap model) {
        String maxEstDeg = cr03svc.selectMaxEstDeg(param);
        model.addAttribute("maxEstDeg", maxEstDeg);
        return "jsonView";
    }

    @PostMapping("/selectEstList")
    public String selectEstList(@RequestBody Map<String, String> param, ModelMap model) {
        int totalCnt = cr03svc.selectEstCount(param);
        System.out.println(totalCnt+"총로우");
        PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
    
        List<Map<String, Object>> estList = cr03svc.selectEstList(param);
        model.addAttribute("estList", estList);
        return "jsonView";
    }
    @PostMapping("/selectEstListNotOrdrs")
    public String selectEstListNotOrdrs(@RequestBody Map<String, String> param, ModelMap model) {
        int totalCnt = cr03svc.selectEstCount(param);
        PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> estList = cr03svc.selectEstListNotOrdrs(param);
        model.addAttribute("estList", estList);
        return "jsonView";
    }
    
    @PostMapping(value = "/selectEstInfo")
    public String selectEstInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
            int totalCnt = cr03svc.selectEstDetailCount(paramMap);
            PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
            model.addAttribute("paginationInfo", paginationInfo);
            System.out.println(totalCnt+"총로우");
            Map<String, Object> estInfo = cr03svc.selectEstInfo(paramMap);
            model.addAttribute("estInfo", estInfo);
        return "jsonView";
    }
    
    @PostMapping(value = "/insertEst")
    public String insertEst(@RequestParam Map<String, String> paramMap , MultipartHttpServletRequest mRequest, ModelMap model) {
        try {
            Map<String, Object> newEstMap    = cr03svc.insertEst(paramMap, mRequest);
            model.addAttribute("resultCode", 200);
            model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
            model.addAttribute("param", newEstMap);
        }catch(Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", e.getLocalizedMessage());
        }
        return "jsonView";
    }
    @PostMapping(value = "/insertEstDeg")
    public String insertEstDeg(@RequestParam Map<String, String> paramMap , MultipartHttpServletRequest mRequest, ModelMap model) {
        try {
            Map<String, Object> newEstMap  = cr03svc.insertEstDeg(paramMap, mRequest);
            System.out.println(newEstMap+"최종");
            model.addAttribute("resultCode", 200);
            model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
            model.addAttribute("param", newEstMap );
        }catch(Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", e.getLocalizedMessage());
        }
        return "jsonView";
    }


    @PutMapping(value = "/updateEst")
    public String updateEst(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) {
        try {
            Map<String, Object> updateEstMap =  cr03svc.updateEst(paramMap, mRequest);
            model.addAttribute("resultCode", updateEstMap.get("resultCode"));
            model.addAttribute("resultMessage", messageUtils.getMessage("update"));
            model.addAttribute("param", updateEstMap );
        }catch(Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
        }
        return "jsonView";
    }
    
    @DeleteMapping(value = "/deleteEst")
    public String deleteEst(@RequestBody Map<String, String> paramMap, ModelMap model) {



        model.addAttribute("resultCode", cr03svc.deleteEst(paramMap));
        model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
        return "jsonView";
    }
}