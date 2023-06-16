package com.dksys.biz.user.sm.sm05.service;

import java.util.List;
import java.util.Map;

public interface SM05Svc {

	int selectIoCount(Map<String, String> paramMap);

	List<Map<String, String>> selectIoList(Map<String, String> paramMap);

//	int selectPchsDetailCount(Map<String, String> paramMap);

//	List<Map<String, String>> selectPchsDetail(Map<String, String> paramMap);

	Map<String, String> selectIoCostInfo(Map<String, String> paramMap);

//	int selectConfirmCount(Map<String, String> paramMap);
//
//	int insertPchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
//
//	int updatePchsCost(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
//
//	int deletePchsCost(Map<String, String> paramMap) throws Exception;

}