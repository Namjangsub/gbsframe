package com.dksys.biz.user.sm.sm05.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface SM05Mapper {
    // 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

    // 창고 코드 검색
    List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);

    // 팝업 재고 검색
	List<Map<String, String>> select_stock_modal(Map<String, String> paramMap);

	// 수정화면 정보
	Map<String, String> select_sm05_Info(Map<String, String> paramMap);
	
	// 수정화면 상세정보
	List<Map<String, String>> select_sm05_Info_Dtl(Map<String, String> paramMap);
	
	// fileTrgtKey 생성
	int select_sm05_SeqNext(Map<String, String> paramMap);

    // IOKey 생성
	int select_sm05_Ioseq(Map<String, String> paramMap);

    // 관리번호 생성
    String select_sm05_Next_MNGM_NO(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_sm05(Map<String, String> paramMap);

    int insert_sm05_Dtl(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_sm05(Map<String, String> paramMap);

    int update_sm05_Dtl(Map<String, String> paramMap);

	//DATA DELETE
	int delete_sm05(Map<String, String> paramMap);

    int delete_sm05_Dtl(Map<String, String> paramMap);
    
    int delete_sm05_Dtl_All(Map<String, String> paramMap);
}
