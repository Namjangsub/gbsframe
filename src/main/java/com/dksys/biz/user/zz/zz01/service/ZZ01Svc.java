package com.dksys.biz.user.zz.zz01.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface ZZ01Svc {

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

}