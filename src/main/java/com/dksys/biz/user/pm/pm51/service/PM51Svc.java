package com.dksys.biz.user.pm.pm51.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM51Svc {

	int selectTripReqListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectTripReqList(Map<String, String> paramMap);

	Map<String, Object> selectTripReqDtl(Map<String, String> paramMap);

	int insertTripReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int updateTripReq(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int deleteTripReq(Map<String, String> paramMap) throws Exception;

	int updateTripReqPayDone(Map<String, String> paramMap) throws Exception;

	int updateTripReqPayCancel(Map<String, String> paramMap) throws Exception;

	List<Map<String, String>> selectSignResUserlstInit(Map<String, String> paramMap);

	int selectTripRptListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectTripRptList(Map<String, String> paramMap);

	List<Map<String, String>> selectTripReqForRpt(Map<String, String> paramMap);

	Map<String, Object> selectTripRptDtl(Map<String, String> paramMap);

	int insertTripRpt(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int updateTripRpt(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;

	int deleteTripRpt(Map<String, String> paramMap) throws Exception;

}
