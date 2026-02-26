package com.dksys.biz.user.wb.wb07.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WB07Mapper {

	List<Map<String, String>> select_wb07_List(Map paramMap);


	Map<String, String> selectWb22d02ExtraInfo(Map<String, String> param);
	List<Map<String, String>> selectWb22d02ExtraInfoGrid(Map<String, Object> param);
	int updateWb22d02ExtraInfo(Map<String, String> param);
	int insertWb22d02ExtraInfo(Map<String, String> param);
	


	// 프론트엔드의 간트차트에서 수정되는 내용 처리 용
	int updateWbsRemarks(Map<String, String> param);
	int insertWbsScheduleHIST(Map<String, String> param);
	int updateWbsScheduleVersionUp(Map<String, String> param);
	int updateWbsSchedule(Map<String, String> param);
	int updateWbsScheduleVersionUpWbsCode(Map<String, String> param);
	

	int updateWbsLevel2MetaGantt_revisedFinishDt(Map<String, String> paramMap);
	int upsertWbsActualStart(Map<String, String> paramMap);
	String selectWbsActualFileTrgtKey(Map<String, String> paramMap);
	int updateWbsActualComplete(Map<String, String> paramMap);
	int updateWbsActualReset(Map<String, String> paramMap);
	
	// deleteActual 
	int deleteWbsActual(Map<String, String> paramMap);
	int deleteWbsActualPlanClear(Map<String, String> paramMap);
	
	int updateWbsActualLevel2Insert(Map<String, String> paramMap);
	
}
