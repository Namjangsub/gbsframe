package com.dksys.biz.user.wb.wb07.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface WB07Svc {

	List<Map<String, String>> select_wb07_List(Map<String, Object> paramMap);

	int createActualUnconfirmed(Map<String, String> paramMap) throws Exception;
	int completeActualConfirmed(Map<String, String> paramMap) throws Exception;
	int resetActualUnconfirmed(Map<String, String> paramMap) throws Exception;
	int deleteActual(Map<String, String> paramMap) throws Exception;

	List<Map<String, String>> selectWb22d02ExtraInfoGrid(Map<String, Object> paramMap);
	Map<String, String> selectWb22d02ExtraInfo(Map<String, String> paramMap);
	Map<String, Object> updateWb22d02ExtraInfo(Map<String, String> paramMap) throws Exception;
	Map<String, Object> updateWb22d02ExtraInfoBatch(Map<String, Object> paramMap) throws Exception;

	

	int updateWbsLevel2ActGantt(Map<String, String> paramMap) throws Exception;

	int updateWbsRemarks(Map<String, String> paramMap);

	int updateWbsSchedule(Map<String, String> paramMap) throws Exception;

	
}
