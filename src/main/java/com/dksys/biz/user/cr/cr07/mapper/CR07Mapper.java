package com.dksys.biz.user.cr.cr07.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface CR07Mapper {
    // 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

    // 삭제 전 체크
    List<Map<String, Object>> delete_Chk(Map<String, String> paramMap);

    // 팝업 입력대상 검색
	List<Map<String, String>> select_insert_target_modal(Map<String, String> paramMap);

	// 수정화면 정보
	Map<String, String> select_cr07_Info(Map<String, String> paramMap);
	
	// 수정화면 상세정보
	List<Map<String, String>> select_cr07_Info_Dtl(Map<String, String> paramMap);
	
	// fileTrgtKey 생성
	int select_cr07_SeqNext(Map<String, String> paramMap);

    // IOKey 생성
	int select_cr07_Ioseq(Map<String, String> paramMap);

    // 관리번호 생성
    String select_cr07_Next_MNGM_NO(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_cr07(Map<String, String> paramMap);

    int insert_cr07_Dtl(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_cr07(Map<String, String> paramMap);

	int update_cr07_delete(Map<String, String> paramMap);

    int update_cr07_Dtl(Map<String, String> paramMap);

	//DATA DELETE
	int delete_cr07(Map<String, String> paramMap);

    int delete_cr07_Dtl(Map<String, String> paramMap);
    
    int delete_cr07_Dtl_All(Map<String, String> paramMap);
}
