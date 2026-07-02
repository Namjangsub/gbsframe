package com.dksys.biz.admin.cm.cm30;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.admin.cm.cm30.service.CM30Svc;
import com.dksys.biz.util.MessageUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/cm/cm30")
public class CM30Ctr {

	@Autowired
	MessageUtils messageUtils;

    @Autowired
    CM30Svc cm30Svc;

    // 사용자별 그리드 컬럼 설정 조회
    @PostMapping("/selectGridConfig")
    public String selectGridConfig(@RequestBody Map<String, String> param, ModelMap model) {
    	String columnConfig = cm30Svc.selectGridConfig(param);
    	model.addAttribute("columnConfig", columnConfig);
        return "jsonView";
    }

    // 사용자별 그리드 컬럼 설정 저장(upsert)
    @PostMapping("/saveGridConfig")
    public String saveGridConfig(HttpServletRequest request, @RequestBody Map<String, String> param, ModelMap model) {
    	param.put("reqPgm", request.getRequestURI());
    	cm30Svc.saveGridConfig(param);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("save"));
    	return "jsonView";
    }

    // 사용자별 그리드 컬럼 설정 삭제(기본값 복원)
    @DeleteMapping("/deleteGridConfig")
    public String deleteGridConfig(@RequestBody Map<String, String> param, ModelMap model) {
    	cm30Svc.deleteGridConfig(param);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	return "jsonView";
    }

}
