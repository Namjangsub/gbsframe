package com.dksys.biz;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.dksys.biz.admin.cm.cm01.service.CM01Svc;
import com.dksys.biz.rest.url.service.UrlService;
import com.dksys.biz.util.WebClientUtil;

@Controller
public class HomeController {

    @Autowired
    CM01Svc cm01Svc;
	
    @Autowired
    private UrlService urlService;
    
    @Autowired
    WebClientUtil webClientUtil;
    
    @GetMapping("/rest/get")
    public String get(Model model) {
    	String result = webClientUtil.get("http://asset2.dongkuk.com/cmn/getHeadInfo.json?orgCd=UNC50011431&comOrgCd=UNC");
    	model.addAttribute("result", result);
    	return "jsonView";
    }

	// 단축URL처리
	@PostMapping("/s/redirectChkCode")
	public String redirectChkCode(HttpServletRequest request,  Model model){
		String longUrl = request.getParameter("longUrl").toString();
		String chkCode= urlService.shortUrlPromissChkCode(longUrl);
		if(chkCode.equals(request.getParameter("chkCode").toString())) {
			model.addAttribute("resultCode", 200);
	        model.addAttribute("resultText", "코드확인완료!");
		} else {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultText", "비밀코드 확인 바랍니다.");
		}
		return "jsonView";
	}
	
	// 단축URL처리
	@GetMapping("/s/{shortUrl}")
	public String redirect(HttpServletRequest request, @PathVariable String shortUrl){
		String longUrl= urlService.getLongUrlByshortUrl(shortUrl);
		if(longUrl != null){
			return "redirect:"+"http://"+longUrl;
		}
		return  "redirect:/";
	}

	// 웰컴 페이지
    @GetMapping("/")
    public String welcome(HttpServletRequest request) {
    	Device device = DeviceUtils.getCurrentDevice(request);
    	String redirectUrl = "";
    	if(device.isNormal()) {
    		redirectUrl = "/static/index.html";
    	}else {
    		redirectUrl = "/static/mobile/index.html";
    	}
    	return "redirect:"+redirectUrl;
    }
	
	// 권한 오류 시 요청처리
    @GetMapping("/noAuth")
    public String noAuth(Model model) {
    	throw new RuntimeException("토큰정보가 유효하지 않습니다.");
    }
    
    // 접근 가능한 메뉴정보
    @PostMapping("/selectMenuAuth")
    public String selectMenuAuth(@RequestBody Map<String, Object> param, Model model) {
    	String[] authArray = {"AUTH000"};
    	authArray = param.get("authInfo") != null ? param.get("authInfo").toString().split(",") : authArray;
    	List<Map<String, Object>> accessList = cm01Svc.selectMenuAuth(authArray);
    	model.addAttribute("accessList", accessList);
    	JSONArray jsonArray = new JSONArray();
    	
    	for (Map<String, Object> map : accessList) {
    		JSONObject json = new JSONObject();
			try {
				String sValue = map.get("menuUrl").toString();
				int lastDotIndex = sValue.lastIndexOf('.');
				if (lastDotIndex != -1) {
		            sValue = sValue.substring(sValue.lastIndexOf('/') + 1, lastDotIndex);
		        } else {
		            sValue = sValue.substring(sValue.lastIndexOf('/') + 1);
		        }
				json.put("m", sValue);
				json.put("s", map.get("saveYn"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
    		jsonArray.put(json);
    	}
    	model.addAttribute("accessJSON", jsonArray.toString());
    	return "jsonView";
    }
    
    // 접근 가능한 메뉴정보
    @PostMapping("/selectSubMenuAuth")
    public String selectSubMenuAuth(@RequestBody Map<String, Object> param, Model model) {
    	String[] authArray = {"AUTH000"};
    	String upMenuId = "";
    	authArray = param.get("authInfo") != null ? param.get("authInfo").toString().split(",") : authArray;
    	upMenuId = param.get("upMenuId") != null ? param.get("upMenuId").toString() : "";
    	List<Map<String, Object>> accessSubList = cm01Svc.selectSubMenuAuth(authArray, upMenuId);
    	model.addAttribute("accessSubList", accessSubList);
    	return "jsonView";
    }
}