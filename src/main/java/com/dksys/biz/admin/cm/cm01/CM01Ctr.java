package com.dksys.biz.admin.cm.cm01;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dksys.biz.admin.cm.cm01.service.CM01Svc;
import com.dksys.biz.util.MessageUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/cm/cm01")
public class CM01Ctr {

	@Autowired
	MessageUtils messageUtils;
	
    @Autowired
    CM01Svc cm01Svc;
    
    // 권한리스트 조회
    @PostMapping("/selectAuthInfo")
    public String selectAuthInfo(ModelMap model) {
    	List<Map<String, String>> authList = cm01Svc.selectAuthList();
    	model.addAttribute("authList", authList);
        return "jsonView";
    }
    
    // 권한등록
    @PostMapping("/insertAuth")
    public String insertAuth(HttpServletRequest request, @RequestBody Map<String, String> param, ModelMap model) {
    	param.put("pgmId", request.getRequestURI());
    	cm01Svc.insertAuth(param);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
    	return "jsonView";
    }
    
    // 권한삭제
    @DeleteMapping("/deleteAuth")
    public String deleteAuth(@RequestBody Map<String, String> param, ModelMap model) {
    	cm01Svc.deleteAuth(param);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
    	return "jsonView";
    }
    
    // 권한수정
    @PutMapping("/updateAuth")
    public String updateAuth(HttpServletRequest request, @RequestBody Map<String, String> param, ModelMap model) {
    	param.put("pgmId", request.getRequestURI());
    	cm01Svc.updateAuth(param);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
    }
    
    // 권한롤수정
    @PutMapping("/updateAuthRole")
    public String updateAuthRole(@RequestBody Map<String, String> param, ModelMap model) {
    	cm01Svc.updateAuthRole(param);
    	model.addAttribute("resultCode", 200);
    	model.addAttribute("resultMessage", messageUtils.getMessage("update"));
    	return "jsonView";
    }

	// 즐겨찾기 메뉴등록 및 삭제 하기
	@PostMapping("/favoritesMenuControl")
	public String favoritesMenuControl(HttpServletRequest request, @RequestBody Map<String, String> param, ModelMap model) {
//		param.put("pgmId", request.getRequestURI());
		param.put("pgmId", "MENU");
		String jobType = param.get("jobType").toString();
		int result = 0;
		try {
			if (jobType.equals("+")) {
				int totalCount = cm01Svc.selectFavoritesMenuCount(param);
				if (totalCount > 18) {
					model.addAttribute("resultCode", 500);
					model.addAttribute("resultMessage", "즐겨찾기 메뉴는 19개까지 가능합니다.");

				} else {
					result = cm01Svc.insertFavoritesMenu(param);
					model.addAttribute("resultCode", 200);
					model.addAttribute("resultMessage", messageUtils.getMessage("insert"));
				}
			} else {
				result = cm01Svc.deleteFavoritesMenu(param);
				model.addAttribute("resultCode", 200);
				model.addAttribute("resultMessage", messageUtils.getMessage("delete"));
			}
		} catch (Exception e) {
			// 즐겨찾기 관련 오류는 무시하기 (중복오류, 삭제오류 무시)
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", "오류 로그확인 바랍니다.");
		}
		return "jsonView";
	}
}