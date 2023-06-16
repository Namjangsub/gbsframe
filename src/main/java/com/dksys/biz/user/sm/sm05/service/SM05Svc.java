package com.dksys.biz.user.sm.sm05.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface SM05Svc {

	int selectPchsCount(Map<String, String> paramMap);

	List<Map<String, String>> selectPchsList(Map<String, String> paramMap);

	int selectPchsDetailCount(Map<String, String> paramMap);

	List<Map<String, String>> selectPchsDetail(Map<String, String> paramMap);

	Map<String, String> selectPchsCostInfo(Map<String, String> paramMap);

	int selectConfirmCount(Map<String, String> paramMap);

	int insertPchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int updatePchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int deletePchsCost(Map<String, String> paramMap) throws Exception;

}