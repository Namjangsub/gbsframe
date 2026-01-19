package com.dksys.biz.user.pm.pm20.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface PM20Svc {
	List<Map<String, String>> selectList_pm20(Map<String, String> paramMap) throws Exception;

	Map<String, String> select_pm20m_Info(Map<String, String> paramMap);

	List<Map<String, String>> selectAgendaList(Map<String, String> paramMap);

	List<Map<String, String>> select_pm20_d02_List(Map<String, String> paramMap);

	int insert_update_agenda_title(Map<String, String> paramMap) throws Exception;

	int insert_update_agenda(Map<String, String> paramMap) throws Exception;

	int pm20_d02_update(Map<String, Object> paramMap) throws Exception;

	int pm20_d02_delete(Map<String, Object> paramMap) throws Exception;

	public int agUploadFile(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
}
