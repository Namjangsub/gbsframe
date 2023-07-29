package com.dksys.biz.user.mail.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface EmailSvc {
	
	int sendGmailSimple(Map<String, String> paramMap) throws Exception;

	int sendGmailHtml(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	int sendMailSimple(Map<String, String> paramMap) throws Exception;
	
	int sendMailHtml(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	public int selectMailCount(Map<String, String> paramMap);

	public List<Map<String, String>> selectMailList(Map<String, String> paramMap);

	public int insertMail(Map<String, String> paramMap);

	public int updateMailError(Map<String, String> paramMap);
	
	public int deleteMail(Map<String, String> paramMap);	
}