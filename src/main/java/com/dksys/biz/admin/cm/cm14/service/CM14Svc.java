package com.dksys.biz.admin.cm.cm14.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface CM14Svc {

	public int insertBoard(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	public List<Map<String, String>> selectBoardList(Map<String, String> paramMap);

	public int selectBoardCount(Map<String, String> paramMap);

	public Map<String, Object> selectBoardInfo(Map<String, String> paramMap);

	public int updateBoard(Map<String, String> paramMap, MultipartHttpServletRequest mRequest)  throws Exception ;

	public int uploadFile(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	public List<String> selectBoardPopList();

	public int deleteBoard(Map<String, String> paramMap) throws Exception;

}