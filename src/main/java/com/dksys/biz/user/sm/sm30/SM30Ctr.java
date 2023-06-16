package com.dksys.biz.user.sm.sm30;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.user.sm.sm30.service.SM30Svc;
import com.dksys.biz.util.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/sm/sm30")
public class SM30Ctr {
    @Autowired
    SM30Svc sm30svc;
    @Autowired
    MessageUtils messageUtils;
    @PostMapping("/selectBomList")
    public String selectBomList(@RequestBody Map<String, String> param, ModelMap model) {
        int totalCnt = sm30svc.selectBomCount(param);
        PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> resultList = sm30svc.selectBomList(param);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }
    @PostMapping("/selectBomListMatr")
    public String selectBomListMatr(@RequestBody Map<String, String> param, ModelMap model) {
        int totalCnt = sm30svc.selectBomCount(param);
        PaginationInfo paginationInfo = new PaginationInfo(param, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> resultList = sm30svc.selectBomListMatr(param);
        model.addAttribute("resultList", resultList);
        return "jsonView";
    }
    @PostMapping("/selectBomInfo")
    public String selectBomInfo(@RequestBody Map<String, String> param, ModelMap model) {

        Map<String, Object> result = sm30svc.selectBomInfo(param);
        model.addAttribute("result", result);
        return "jsonView";
    }
    @DeleteMapping(value = "/deleteBom")
    public String deleteBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
        model.addAttribute("resultCode", sm30svc.deleteBom(paramMap));
        model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
        return "jsonView";
    }
    @PostMapping(value = "/updateBom")
    public String updateBom(@RequestBody Map<String, String> paramMap, ModelMap model) {
        System.out.println(paramMap.get("detailArr"));
        try {
            Map<String, Object> updateBomMap =  sm30svc.updateBom(paramMap);
            model.addAttribute("resultCode", updateBomMap.get("resultCode"));
            model.addAttribute("resultMessage", messageUtils.getMessage("update"));
            model.addAttribute("param", updateBomMap );
        }catch(Exception e) {
            model.addAttribute("resultCode", 500);
            model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
        }
        return "jsonView";
    }
    

}
