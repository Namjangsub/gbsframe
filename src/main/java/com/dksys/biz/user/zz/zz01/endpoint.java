package com.dksys.biz.user.zz.zz01;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class endpoint {
    
	@PostMapping("/endPointApiInfomation")
	public String endPointApiInfomation (@RequestParam Map<String, String> paramMap,
                                              HttpServletRequest servletRequest, ModelMap model) {
		
		String clientIp = getClientIP(servletRequest);;
		String clientURI = servletRequest.getRequestURI();
		StringBuffer clientURL = servletRequest.getRequestURL();
		String clientMethod = servletRequest.getMethod();
		
	    String clientCharacterEncoding = servletRequest.getCharacterEncoding();
	    String clientContentType = servletRequest.getContentType();
	    Locale clientLocale = servletRequest.getLocale();
	    
	    Enumeration<String> headerNames = servletRequest.getHeaderNames();
	    Map<String, String> clientHeaders = new HashMap<>();
	    while (headerNames.hasMoreElements()) {
	  	    String headerName = headerNames.nextElement();
	  	    String headerValue = servletRequest.getHeader(headerName);
	    	clientHeaders.put(headerName, headerValue);
	    }
	    
//	    Cookie[] clientCookies = servletRequest.getCookies();
//	    HttpSession clientSession = servletRequest.getSession();
//	    RequestDispatcher requestDispatcher = servletRequest.getRequestDispatcher("/endPointApiInfomation");
//	    HttpSession session = servletRequest.getSession();
	
		model.addAttribute("clientIp", clientIp);
		model.addAttribute("clientURI", clientURI);
		model.addAttribute("clientURL", clientURL);
		model.addAttribute("clientMethod", clientMethod);
//		model.addAttribute("headerNames", headerNames);
//		model.addAttribute("clientHeaders", clientHeaders);
//		model.addAttribute("clientCookies", clientCookies);
//		model.addAttribute("clientSession", clientSession);
		model.addAttribute("clientCharacterEncoding", clientCharacterEncoding);
		model.addAttribute("clientContentType", clientContentType);
		model.addAttribute("clientLocale", clientLocale);
		model.addAttribute("ua", clientHeaders.get("sec-ch-ua"));
		model.addAttribute("mobile", clientHeaders.get("sec-ch-ua-mobile"));
		model.addAttribute("platform", clientHeaders.get("sec-ch-ua-platform"));
		model.addAttribute("user-agent", clientHeaders.get("user-agent"));
		model.addAttribute("referer", clientHeaders.get("referer"));
//		model.addAttribute("requestDispatcher", requestDispatcher);
//		model.addAttribute("session", session);
		
		model.addAttribute("resultCode", 200);
		model.addAttribute("resultMessage", "처리완료!");
			
		return "jsonView";
	}
	
	public static String getClientIP(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");

	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("x-real-ip");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("x-original-forwarded-for");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("Proxy-Client-IP");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("WL-Proxy-Client-IP");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("HTTP_X_FORWARDED");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("HTTP_CLIENT_IP");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("HTTP_FORWARDED_FOR");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("HTTP_FORWARDED");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("HTTP_VIA");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getHeader("REMOTE_ADDR");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
	        ip = request.getRemoteAddr();
	    }

	    return ip;
	}
  
}
