package com.dksys.biz.user.sm.sm01;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm01.service.SM01Svc;
import com.dksys.biz.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/sm/sm01")
public class SM01Ctr {
    @Autowired
    SM01Svc sm01svc;
    
    @Autowired
    MessageUtils messageUtils;
    
    @PostMapping("/selectBomList")
    public String selectBomList(@RequestBody Map<String, String> param, ModelMap model) {
        int totalCnt = sm01svc.selectBomCount(param);
        PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> resultList = sm01svc.selectBomList(param);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }
    @PostMapping("/selectBomListMatr")
    public String selectBomListMatr(@RequestBody Map<String, String> param, ModelMap model) {
        int totalCnt = sm01svc.selectBomCount(param);
        PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> resultList = sm01svc.selectBomListMatr(param);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }

    @PostMapping("/selectBomInfo")
    public String selectBomInfo(@RequestBody Map<String, String> param, ModelMap model) {

        Map<String, Object> result = sm01svc.selectBomInfo(param);
        model.addAttribute("result", result);
        return "jsonView";
    }
    
    @PostMapping("/selectBomDetailList")
    public String selectBomDetailList(@RequestBody Map<String, String> param, ModelMap model) {
        int totalCnt = sm01svc.selectBomDetailCount(param);
        System.out.println(totalCnt+" 총 로우");
        PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> estList = sm01svc.selectBomDetailList(param);
        model.addAttribute("estList", estList);
        return "jsonView";
    }
    
    @DeleteMapping(value = "/deleteBom")
    public String deleteBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
        model.addAttribute("resultCode", sm01svc.deleteBom(paramMap));
        model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
        return "jsonView";
    }
    
    @PostMapping(value = "/updateBom")
    public String updateBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
        System.out.println(paramMap.get("detailArr"));
        try {
            Map<String, Object> updateBomMap =  sm01svc.updateBom(paramMap);
            model.addAttribute("resultCode", updateBomMap.get("resultCode"));
            model.addAttribute("resultMessage", messageUtils.getMessage("update"));
            model.addAttribute("param", updateBomMap );
        }catch(Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
        }
        return "jsonView";
    }
    
    @PostMapping(value = "/insertBomDetail")
    public String insertBomDetailList(@RequestParam Map<String, String> paramMap, ModelMap model) {
        try {
            Map<String, Object> newBomMap  = sm01svc.insertBomDetailList(paramMap);
            System.out.println(newBomMap+"최종");
            model.addAttribute("resultCode", 200);
            model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
            model.addAttribute("param", newBomMap );
        }catch(Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", e.getLocalizedMessage());
        }
        return "jsonView";
    }
}
