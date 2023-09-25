package com.dksys.biz.user.bm.bm14.service;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

public interface BM14Svc {

	List<Map<String, String>> selectBomTreeList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectBomlevelList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectBomAllLevelList(Map<String, String> paramMap);

	Map<String, String> selectBomTreInfo(Map<String, String> paramMap);
	
//	Map<String, String> checkBomRootSalesCdInfo(Map<String, String> paramMap);

	int checkBomId(Map<String, String> paramMap);

	int insertBomTree(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	  
	int moveBom(List<Map<String, String>> paramList);
	
	int deleteBom(Map<String, String> paramMap);
	
	int copyBomTree(List<Map<String, String>> paramList);
	
	int copyBomRootSalesCdTree(Map<String, String> paramList);

	List<Map<String, String>> getAllChildNodeInfos(Map<String, String> paramMap);
	
	int checkBomInfo(Map<String, String> paramMap);

	List<Map<String, String>> selectBomAllLevelTempList(Map<String, String> paramMap);

	String insertTempBom(Map<String, String> paramMap);

	Map<String, String> callDraftTempBom(Map<String, String> paramMap);

	List<Map<String, String>> selectBomAllEnterList(Map<String, String> paramMap);

	int selectBomAllEnterListCount(Map<String, String> paramMap);
	
}