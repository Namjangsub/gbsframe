package com.dksys.biz.user.pm.pm60.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PM60Mapper {

	List<Map<String, String>> selectTeamMemberList(Map<String, String> paramMap);

	List<Map<String, String>> selectRsvCalendar(Map<String, String> paramMap);

	List<Map<String, String>> selectRsvActualCalendar(Map<String, String> paramMap);

	List<Map<String, String>> selectVacationCalendar(Map<String, String> paramMap);

	Map<String, String> selectRsvDtl(Map<String, String> paramMap);

	int insertRsv(Map<String, String> paramMap);

	int updateRsv(Map<String, String> paramMap);

	int deleteRsv(Map<String, String> paramMap);

	List<Map<String, String>> selectRsvDateOverlapList(Map<String, String> paramMap);

	List<Map<String, String>> selectVndrList(Map<String, String> paramMap);

	int insertVndr(Map<String, String> paramMap);

	int updateVndr(Map<String, String> paramMap);

	int deleteVndr(Map<String, String> paramMap);

	List<Map<String, String>> selectRsvStatDaily(Map<String, String> paramMap);

}
