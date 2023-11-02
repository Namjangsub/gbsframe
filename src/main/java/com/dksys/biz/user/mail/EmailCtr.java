package com.dksys.biz.user.mail;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.dksys.biz.user.mail.service.EmailSvc;
import com.dksys.biz.util.MessageUtils;

@Controller
@RequestMapping("/mail")
public class EmailCtr {


	@Autowired
	MessageUtils messageUtils;
	
    @Autowired
	EmailSvc emailSvc;

	@Value("${spring.mailType}")
	private String mailType;
	
    @Value("${spring.fileAttached}")
    private String fileAttached;
	    
    //메일 전송 처리 컨트롤러 
    @PostMapping(value = "/sendMailSimple")
    public String sendMailSimple(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
    	try {
    		int count = 0;
    		if (mailType.equals("SpringFramework")) {
    			count = emailSvc.sendMailSimple(paramMap);
    		} else {
    			count = emailSvc.sendApiMailSimple(paramMap);
    		}
    		model.addAttribute("resultCode", 200);
    		model.addAttribute("resultMessage", messageUtils.getMessage("mailSendComplete"));
    	}catch(Exception e){
			model.addAttribute("resultCode", 500);
			model.addAttribute("resultMessage", messageUtils.getMessage("mailSendFail"));
    	}
    	return "jsonView";
    }        
	
	//메일 전송 처리 컨트롤러 
	@PostMapping(value = "/sendJoinMail")
	public String sendJoinMail(@RequestParam Map<String, String> paramMap, MultipartHttpServletRequest mRequest, ModelMap model) throws Exception {
		try {
			int count = 0;
    		if (mailType.equals("SpringFramework")) {
    			count = emailSvc.sendMailHtml(paramMap, mRequest);
    		} else {
    			count = emailSvc.sendApiMailHtml(paramMap, mRequest);
    		}
			model.addAttribute("resultCode", 200);
			model.addAttribute("resultMessage", messageUtils.getMessage("mailSendComplete"));
			model.addAttribute("ordrgNo", paramMap.get("ordrgNo"));
			model.addAttribute("shortUrl", paramMap.get("shortUrl"));
			model.addAttribute("chkCode", paramMap.get("chkCode"));
		}catch(Exception e){
			String errorMessage = e.getMessage() != null ? e.getMessage() : "알수 없는 오류 확인 필요";
			paramMap.put("errorYn", "Y");
			paramMap.put("errorText", errorMessage);
			try {
//				int count = emailSvc.updateMailError(paramMap);
				int count = emailSvc.insertMail(paramMap); //오류로그 남기기 Exception 발생시 rollback되어 초기등록된 자료가 없음~
				model.addAttribute("resultCode", 500);
				model.addAttribute("resultMessage", messageUtils.getMessage("mailSendFail"));
			}catch(Exception ex){	  
				model.addAttribute("resultCode", 900);
				model.addAttribute("resultMessage", ex.getMessage());
			}
		}
		return "jsonView";
    }
	//메일에 파일 첨부 여부 체크 
	@PostMapping(value = "/sendFilefileAttach")
	public String sendFilefileAttach(@RequestParam Map<String, String> paramMap, ModelMap model) throws Exception {
		model.addAttribute("resultCode", 200);
		model.addAttribute("resultMessage", messageUtils.getMessage("check"));
		model.addAttribute("fileAttached", fileAttached);
		return "jsonView";
    }                
}