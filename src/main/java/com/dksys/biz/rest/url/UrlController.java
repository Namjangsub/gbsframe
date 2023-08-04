package com.dksys.biz.rest.url;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dksys.biz.rest.url.service.UrlService;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/api/Shortening")
    public String Shortening(@RequestBody Map<String, String> paramMap, Model model) {
    	String longUrl = paramMap.get("longUrl");
    	Map<String, String> returnUrl = urlService.generateShortUrl(paramMap);
        model.addAttribute("longUrl", longUrl);
        model.addAttribute("shortUrl", returnUrl.get("shortUrl"));
        model.addAttribute("chkCode", returnUrl.get("chkCode"));
        model.addAttribute("resultCode", 200);
    	return "jsonView";
    }
    
	// 단축URL chkCode 검증
    @PostMapping("/api/shortUrlPromissChkCode")
	public String shortUrlPromissChkCode(@RequestBody Map<String, String> paramMap, Model model){
    	String shortUrl = paramMap.get("longUrl");
		String chkCode= urlService.shortUrlPromissChkCode(shortUrl);
		if(chkCode.equals(paramMap.get("chkCode").toString())) {
			model.addAttribute("resultCode", 200);
	        model.addAttribute("resultText", "코드확인완료!");
		} else {
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultText", "비밀코드 확인 바랍니다.");
		}
		return "jsonView";
	}
}