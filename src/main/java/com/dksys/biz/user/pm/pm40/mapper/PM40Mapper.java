package com.dksys.biz.user.pm.pm40.mapper;

import java.util.List;
import java.util.Map;

public interface PM40Mapper {

	
		// 그리드 카운트
		int selectMainGridListCount(Map<String, String> paramMap);
			// 그리드 리스트
		List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);
			
		List<Map<String, String>> selectYearWorkMainList(Map<String, String> paramMap);
		
		//DATA INSERT
		int insert_pm40(Map<String, String> paramMap);
		
		//DATA UPDATE
		int update_pm40(Map<String, String> paramMap);

		//DATA DELETE
		int delete_pm40(Map<String, String> paramMap);

		String select_pm40_Next_MNGM_NO(Map<String, String> paramMap);
		
		int select_gochal_count(Map<String, String> paramMap);
		
		Map<String, String> select_pm40_Info(Map<String, String> paramMap);
		
		List<Map<String, String>> selectSignResUserlst(Map<String, String> paramMap);
		
		List<Map<String, String>> selectWorkPrtList(Map<String, String> paramMap);
		int select_result_count(Map<String, String> paramMap);
		int insert_pm40_p02(Map<String, String> paramMap);
		int insertWbsApprovalList(Map<String, String> sharngMap);
		int insertWbsSharngList(Map<String, String> sharngMap);
		int deleteWbsSharngList(Map<String, String> paramMap);
		List<Map<String, String>> deleteWbsSharngListChk(Map<String, String> paramMap);
}
