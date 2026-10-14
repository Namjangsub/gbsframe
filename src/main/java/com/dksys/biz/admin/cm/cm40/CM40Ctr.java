package com.dksys.biz.admin.cm.cm40;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.admin.cm.cm40.service.CM40Svc;

@Controller
@RequestMapping("/admin/cm/cm40")
public class CM40Ctr {

    @Autowired
    CM40Svc cm40Svc;

    @PostMapping("/selectFileOpenStatus")
    public String selectFileOpenStatus(ModelMap model) {
        model.addAllAttributes(cm40Svc.getFileOpenStatus());
        // 현재 사용자의 점유해제 가능 여부(서버에서 계산). 기존 키/동작은 그대로 유지
        model.addAttribute("canClose", cm40Svc.isCloseAuthorizedForCurrentUser());
        return "jsonView";
    }

    // 점유해제(열린 핸들 강제 닫기) 요청
    @PostMapping("/closeOpenFile")
    public String closeOpenFile(@RequestBody Map<String, String> param, ModelMap model) {
        model.addAllAttributes(cm40Svc.closeOpenFile(param));
        return "jsonView";
    }
}
