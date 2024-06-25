package com.dksys.biz.user.cr.cr19.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface CR19Mapper {
    // 그리드 카운트
	int select_cr19_Count(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> select_cr19_List(Map<String, String> paramMap);

	int select_cr19m02_Count(Map<String, String> paramMap);
	
	List<Map<String, String>> select_cr19m02_List(Map<String, String> paramMap);
	
	// 수주버전 검색
	List<Map<String, Object>> select_ordrsHistNo_List(Map<String, String> paramMap);

	//DATA SAVE
	// int save_cr19(Map<String, String> paramMap);

	void save_cr19(Map<String, String> param);

	void call_save_cr19(Map<String, String> param);

	void save_cr19_create_cr10(Map<String, String> param);
	//아래쪽 수금계획 확정 저장 (계산서, 수금)처리로 대체함 
//	void call_save_cr19_create_cr10(Map<String, String> paramMap);

	//수금계획 이전 자료 삭제처리 (계산서, 수금) 상세, 및 집계자료
	int delete_cr19_create_cr10_planBillClmnDtl(Map<String, String> paramMap);
	int delete_cr19_create_cr10_planBillClmnMst(Map<String, String> paramMap);
	//수금계획 확정 저장 (계산서, 수금)
	int save_cr19_create_cr10_planBill(Map<String, String> dtl);
	int save_cr19_create_cr10_planClmn(Map<String, String> dtl);
	//수금계획  (계산서, 수금) 집계자료 생성
	int save_cr19_create_cr10_planClmn_Maste(Map<String, String> paramMap);

	
}
