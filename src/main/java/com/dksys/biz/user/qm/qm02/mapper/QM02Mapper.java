package com.dksys.biz.user.qm.qm02.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface QM02Mapper {
    // 그리드 카운트
	int select_grid_Count(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);

    // 창고 코드 검색
    List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);

    //그리드 검색
	List<Map<String, String>> select_stock_modal(Map<String, String> paramMap);

	//그리드 검색
	List<Map<String, String>> select_all_modal(Map<String, String> paramMap);
		
	//그리드 검색
	List<Map<String, String>> select_zupiter_modal(Map<String, String> paramMap);
		
	// 수정화면 정보
	Map<String, String> select_qm02_Info(Map<String, String> paramMap);
	
	// 해당 월에 데이터 확인
	int select_gochal_count(Map<String, String> paramMap);
	
	// 해당 월에  결과 데이터 확인
	int select_result_count(Map<String, String> paramMap);
		
	// fileTrgtKey 생성
	int select_qm02_SeqNext(Map<String, String> paramMap);

    // IOKey 생성
	int select_sm07_Ioseq(Map<String, String> paramMap);

    // 관리번호 생성
    String select_qm02_Next_MNGM_NO(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_qm02(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_qm02(Map<String, String> paramMap);

	//DATA DELETE
	int delete_qm02(Map<String, String> paramMap);

	//DATA INSERT
	int insert_qm02_p02(Map<String, String> paramMap);
}
