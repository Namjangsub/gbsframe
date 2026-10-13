package com.dksys.biz.admin.cm.cm40;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
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
        return "jsonView";
    }
}
