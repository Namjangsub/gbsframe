package com.dksys.biz.admin.cm.cm25;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.admin.cm.cm25.service.CM25Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/admin/cm/cm25")
public class CM25Ctr {

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CM25Svc cm25Svc;

    @PostMapping(value = "/selectExpendList")
    public String selectExpendList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int totalCnt = cm25Svc.selectExpendListCount(paramMap);
        model.addAttribute("paginationInfo", new PaginationInfo(paramMap, totalCnt));
        model.addAttribute("result", cm25Svc.selectExpendList(paramMap));
        return "jsonView";
    }

    @PostMapping(value = "/selectExpendInfo")
    public String selectExpendInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
        model.addAttribute("result", cm25Svc.selectExpendInfo(paramMap));
        return "jsonView";
    }

    @PostMapping(value = "/selectUploadFileList")
    public String selectUploadFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cm25Svc.selectUploadFileList(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    @PostMapping(value = "/insertExpend")
    public String insertExpend(@RequestParam Map<String, String> paramMap,
            MultipartHttpServletRequest mRequest, ModelMap model) {
        try {
            if (cm25Svc.insertExpend(paramMap, mRequest) > 0) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
                model.addAttribute("expendNo", paramMap.get("expendNo"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch (Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }
        return "jsonView";
    }

    @PostMapping(value = "/updateExpend")
    public String updateExpend(@RequestParam Map<String, String> paramMap,
            MultipartHttpServletRequest mRequest, ModelMap model) {
        try {
            if (cm25Svc.updateExpend(paramMap, mRequest) > 0) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("update"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch (Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }
        return "jsonView";
    }

    @PostMapping(value = "/deleteExpend")
    public String deleteExpend(@RequestBody Map<String, String> paramMap, ModelMap model) {
        try {
            if (cm25Svc.deleteExpend(paramMap) > 0) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch (Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }
        return "jsonView";
    }
}
