package com.dksys.biz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import com.dksys.biz.admin.cm.cm06.service.CM06Svc;
import com.dksys.biz.rest.url.service.UrlService;
import com.dksys.biz.util.WebClientUtil;
import org.springframework.core.io.ResourceLoader;

import javax.servlet.http.Cookie;
@Controller
public class HomeController {

    @Autowired
    CM01Svc cm01Svc;

	@Autowired
	CM06Svc cm06Svc;
	
    @Autowired
    private UrlService urlService;
    
    @Autowired
    WebClientUtil webClientUtil;
    
    public enum FormFactor { DESKTOP, PHONE, TABLET, UNKNOWN }

    // 삼성: 폰(S/A/M), 태블릿(T/X)
    private static final Pattern SAMSUNG_TABLET = Pattern.compile("\\bSM-[TX]\\w+", Pattern.CASE_INSENSITIVE);
    private static final Pattern SAMSUNG_PHONE  = Pattern.compile("\\bSM-[SAM]\\w+", Pattern.CASE_INSENSITIVE);
    // 레노버/화웨이/샤오미/아이패드
    private static final Pattern LENOVO_TB      = Pattern.compile("\\bLenovo TB-\\w+", Pattern.CASE_INSENSITIVE);
    private static final Pattern HUAWEI_PAD     = Pattern.compile("\\b(MediaPad|MatePad)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern XIAOMI_PAD     = Pattern.compile("\\b(Mi ?Pad|Mipad|Pad \\d)\\b", Pattern.CASE_INSENSITIVE);

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
			return "redirect:"+longUrl;
		}
		return  "redirect:/";
	}

	// 웰컴 페이지
    @GetMapping("/")
    public String welcome(HttpServletRequest request) {
        FormFactor ff = detectFormFactor(request);
//    	Device device = DeviceUtils.getCurrentDevice(request);
    	String redirectUrl = "";
        switch (ff) {
        case DESKTOP:
            redirectUrl = "/static/index.html";
            break;
        case TABLET:
            redirectUrl = "/static/tablet/index.html";
            break;
        case PHONE:
            redirectUrl = "/static/mobile/index.html";
            break;
        default:
            // 미확정이면 DeviceUtils/UA가 이미 반영되어 있으므로 모바일로 폴백
            redirectUrl = "/mobile/index.html";
        }
    	return "redirect:"+redirectUrl;
    }

    /** -------- 판별 로직: CH/쿠키 → DeviceUtils → UA 휴리스틱 -------- */
    public FormFactor detectFormFactor(HttpServletRequest req) {
        // 0) 필터가 선행해 둔 판정값 우선 사용 (앞서 드린 DeviceFormFactorFilter)
        Object attr = req.getAttribute("detectedFormFactor");
        if (attr instanceof String) {
            String s = ((String) attr).toLowerCase();
            if (s.startsWith("desktop")) return FormFactor.DESKTOP;
            if (s.startsWith("tablet"))  return FormFactor.TABLET;
            if (s.startsWith("phone"))   return FormFactor.PHONE;
        }

        // 1) 프런트 쿠키(form_factor=phone|tablet) 우선
        String cookieFF = readCookie(req, "form_factor");
        if ("tablet".equalsIgnoreCase(cookieFF)) return FormFactor.TABLET;
        if ("phone".equalsIgnoreCase(cookieFF) || "mobile".equalsIgnoreCase(cookieFF)) return FormFactor.PHONE;

        // 2) Client Hints 직접 해석 (들어오는 경우)
        String chModel  = stripQuotes(req.getHeader("Sec-CH-UA-Model"));   // 예: SM-S928N
        String chMobile = req.getHeader("Sec-CH-UA-Mobile");                // "?1" or "?0"
        String vpw      = req.getHeader("Viewport-Width");                  // CSS px
        if ("?0".equals(chMobile)) return FormFactor.DESKTOP;               // 데스크톱 확정

        if (isTabletModel(chModel)) return FormFactor.TABLET;
        if (isPhoneModel(chModel))  return FormFactor.PHONE;

        // Viewport-Width 기준(600dp 이상이면 태블릿)
        try {
            if (vpw != null) {
                double dp = Double.parseDouble(vpw);
                if (dp >= 600) return FormFactor.TABLET;
                if (dp > 0)    return FormFactor.PHONE;
            }
        } catch (Exception ignore) {}

        // 3) spring-mobile-device (있으면)
        try {
            org.springframework.mobile.device.Device device =
                    org.springframework.mobile.device.DeviceUtils.getCurrentDevice(req);
            if (device != null) {
                if (device.isNormal()) return FormFactor.DESKTOP;
                if (device.isTablet()) return FormFactor.TABLET;
                if (device.isMobile()) return FormFactor.PHONE;
            }
        } catch (Throwable ignore) { /* 라이브러리 미사용 환경 대응 */ }

        // 4) UA 휴리스틱(최후)
        String ua = req.getHeader("User-Agent");
        if (ua != null) {
            if (isTabletModel(ua) || ua.contains("iPad")) return FormFactor.TABLET;
            if (isPhoneModel(ua)) return FormFactor.PHONE;
            if (ua.contains("Android") && !ua.contains("Mobile")) return FormFactor.TABLET; // 약함
            if (ua.matches(".*(Windows|Macintosh|X11|Linux).*")) return FormFactor.DESKTOP;
        }
        return FormFactor.UNKNOWN;
    }

    private static boolean isTabletModel(String s) {
        if (s == null) return false;
        return SAMSUNG_TABLET.matcher(s).find()
                || LENOVO_TB.matcher(s).find()
                || HUAWEI_PAD.matcher(s).find()
                || XIAOMI_PAD.matcher(s).find()
                || s.contains("iPad");
    }
    private static boolean isPhoneModel(String s) {
        if (s == null) return false;
        return SAMSUNG_PHONE.matcher(s).find();
    }
    private static String stripQuotes(String s) {
        if (s == null) return null;
        return (s.startsWith("\"") && s.endsWith("\"")) ? s.substring(1, s.length()-1) : s;
    }
    private static String readCookie(HttpServletRequest req, String name) {
        Cookie[] cs = req.getCookies(); if (cs == null) return null;
        for (Cookie c : cs) if (name.equals(c.getName())) return c.getValue();
        return null;
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
//		List<Map<String, Object>> accessList = cm01Svc.selectMenuAuth(authArray, param);
		List<Map<String, Object>> accessList = cm01Svc.selectMenuAuthNew(authArray, param);

		model.addAttribute("accessList", accessList);
    	JSONArray jsonArray = new JSONArray();

		JSONObject json = new JSONObject();
    	for (Map<String, Object> map : accessList) {

			if (map.get("menuType").toString().equals("HTML")) {
				try {
					String sValue = map.get("menuUrl").toString();
					int lastDotIndex = sValue.lastIndexOf('.');
					if (lastDotIndex != -1) {
						sValue = sValue.substring(sValue.lastIndexOf('/') + 1, lastDotIndex);
					} else {
						sValue = sValue.substring(sValue.lastIndexOf('/') + 1);
					}
					json.put(sValue.toString(), map.get("saveYn").toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
    	}
		jsonArray.put(json);
    	model.addAttribute("accessJSON", jsonArray.toString());
    	return "jsonView";
    }

	// 사용자별 접근 가능한 메뉴정보
	@PostMapping("/selectMenuAuthFromUser")
	public String selectMenuAuthFromUser(@RequestBody Map<String, Object> param, Model model) {
		String[] authArray = { "AUTH000" };
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("userId", param.get("userId").toString());
		Map<String, String> UserInfo = cm06Svc.selectUserInfo(paramMap);

		authArray = UserInfo.get("authInfo").toString().split(",");
				
		List<Map<String, Object>> accessList = cm01Svc.selectMenuAuthNew(authArray, param);
		model.addAttribute("accessList", accessList);
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

	// 접근 가능한 메뉴정보
	@PostMapping("/selectCheckAuthority")
	public String selectCheckAuthority(@RequestBody Map<String, Object> param, Model model) {
		List<Map<String, Object>> accessList = cm01Svc.selectCheckAuthority(param);

		model.addAttribute("accessList", accessList);
		return "jsonView";
	}

}