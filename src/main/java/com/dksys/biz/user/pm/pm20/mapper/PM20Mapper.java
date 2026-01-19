package com.dksys.biz.user.pm.pm20.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM20Mapper {
	List<Map<String, String>> selectList_pm20(Map<String, String> paramMap);

	Map<String, String> select_pm20m_Info(Map<String, String> paramMap);

	List<Map<String, String>> selectAgendaList(Map<String, String> paramMap);

	List<Map<String, String>> select_pm20_d02_List(Map<String, String> paramMap);

	String selectMaxFileTrgtKeySeq(Map<String, String> paramMap);
	
	int insert_update_agenda_title(Map<String, String> param);

	int pm20_main_update(Map<String, String> param);

	int pm20_d3_insert_update(Map<String, String> param);

	int pm20_d02_update(Map<String, Object> param);

	int pm20_d02_delete(Map<String, Object> param);

}
