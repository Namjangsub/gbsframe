package com.dksys.biz.admin.cm.cm16;

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

import com.dksys.biz.admin.cm.cm16.service.CM16Svc;
import com.dksys.biz.cmn.vo.PaginationInfo;
import com.dksys.biz.util.MessageUtils;
import com.dksys.biz.util.ObjectUtil;



@Controller
@RequestMapping("/admin/cm/cm16")
public class CM16Ctr {
    
    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CM16Svc cm16Svc;

    // 문제현황 리스트 조회
    @PostMapping(value = "/selectItoaIssueList")
    public String selectItoaIssueList(@RequestBody Map<String, String> paramMap, ModelMap model) {

        paramMap.put("reqId", ObjectUtil.sqlInCodeGen(paramMap.get("reqId")));
        int totalCnt = cm16Svc.selectItoaIssueListCount(paramMap); 
        PaginationInfo paginationInfo = new PaginationInfo(paramMap, totalCnt);
        model.addAttribute("paginationInfo", paginationInfo);
        List<Map<String, Object>> result = cm16Svc.selectItoaIssueList(paramMap);
        model.addAttribute("result", result);

        List<Map<String, String>> resultReqId = cm16Svc.select_reqId_code(paramMap);
        model.addAttribute("resultReqId", resultReqId);
        
        return "jsonView";  
    }

     // 문제현황 정보 조회
    @PostMapping(value = "/selectItoaIssueInfo")
    public String selectItoaIssueInfo(@RequestBody Map<String, String> paramMap, ModelMap model) {
        Map<String, String> result = cm16Svc.selectItoaIssueInfo(paramMap);
        model.addAttribute("result", result);
        paramMap.put("fileTrgtKey", result.get("fileTrgtKey"));
        paramMap.put("reqId", result.get("reqId"));
        return "jsonView";
    }

    @PostMapping(value = "/selectConfirmCount")
    public String selectConfirmCount(@RequestBody Map<String, String> paramMap, ModelMap model) {
        int result = cm16Svc.selectConfirmCount(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    // 업로드파일 정보 조회
    @PostMapping(value = "/selectUploadFileList")
    public String selectUploadFileList(@RequestBody Map<String, String> paramMap, ModelMap model) {
        List<Map<String, String>> result = cm16Svc.selectUploadFileList(paramMap);
        model.addAttribute("result", result);
        return "jsonView";
    }

    // 문제현황 등록
    @PostMapping(value = "/itoaInsertIssue")
    public String itoaInsertIssue(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
        try {
            if(cm16Svc.itoaInsertIssue(paramMap, mRequest) !=0 ) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch(Exception e) {
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }        
        return "jsonView";
    }

    // 문제현황 수정
    @PostMapping(value = "/itoaUpdateIssue")
    public String itoaUpdateIssue(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
        try {
            if(cm16Svc.itoaUpdateIssue(paramMap, mRequest) !=0 ) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("update"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch(Exception e) {
            model.addAttribute("resultCode", 900);
			model.addAttribute("resultMessage", e.getMessage());
        }

        return "jsonView";
    }

    // 문제현황 삭제
    @PostMapping(value = "itoaDeleteIssue")
    public String itoaDeleteIssue(@RequestBody Map<String, String> paramMap, ModelMap model) throws Exception {
        try {
            if (cm16Svc.itoaDeleteIssue(paramMap) != 0 ) {
                model.addAttribute("resultCode", 200);
                model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
            } else {
                model.addAttribute("resultCode", 500);
                model.addAttribute("resultMessage", messageUtils.getMessage("fail"));
            }
        } catch(Exception e){
            model.addAttribute("resultCode", 900);
            model.addAttribute("resultMessage", e.getMessage());
        }

        return "jsonView";
    }

    public void inputFieldExistCheck(@RequestBody Map<String, String> paramMap, ModelMap model) {
        model.addAttribute("resultCode", 200);
        model.addAttribute("resultMessage", "");

        if (paramMap.get("ordCoCd")==null ||"".equals(paramMap.get("ordCoCd"))) {
            paramMap.put("ordCoCd", paramMap.get("coCd"));
        }
    }
}
