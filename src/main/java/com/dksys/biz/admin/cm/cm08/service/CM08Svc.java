package com.dksys.biz.admin.cm.cm08.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CM08Svc {

	public int uploadFile(String fileTrgtTyp, String fileTrgtKey, MultipartHttpServletRequest mRequest);

	int uploadTreeFile(String fileTrgtTyp,Map<String, String> paramMap,MultipartHttpServletRequest mRequest);

	int copyTreeFile(String fileTrgtTyp, Map<String, String> paramMap, MultipartHttpServletRequest mRequest);


	public List<Map<String, String>> selectFileList(Map<String, String> paramMap);
	
	public void setDisposition(HttpServletRequest request, HttpServletResponse response, String fileName);

	public Map<String, String> selectFileInfo(String fileKey);

	public int deleteFile(String fileKey);

	public String excelDownload(List<Map<String, String>> resultList, String string);

	public int selectTreeFileCount(Map<String, String> paramMap);
	
	public List<Map<String, String>> selectTreeFileList(Map<String, String> paramMap);

	public List<Map<String, String>> selectTreeFileModule(Map<String, String> paramMap);



	int selectConfirmCount(Map<String, String> paramMap);
	
	int moveFile(Map<String, String> paramMap);

	int deleteFileCall(Map<String, String> paramMap);

}