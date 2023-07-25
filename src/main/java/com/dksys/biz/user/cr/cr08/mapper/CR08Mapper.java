package com.dksys.biz.user.cr.cr08.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface CR08Mapper {
    // 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

    // 팝업 입력대상 검색
	List<Map<String, String>> select_insert_target_modal(Map<String, String> paramMap);

	// 수정화면 정보
	Map<String, String> select_cr08_Info(Map<String, String> paramMap);
	
	// 수정화면 상세정보
	List<Map<String, String>> select_cr08_Info_Dtl(Map<String, String> paramMap);
	
	// fileTrgtKey 생성
	int select_cr08_SeqNext(Map<String, String> paramMap);

    // IOKey 생성
	int select_cr08_Ioseq(Map<String, String> paramMap);

    // 관리번호 생성
    String select_cr08_Next_MNGM_NO(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_cr08(Map<String, String> paramMap);

    int insert_cr08_Dtl(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_cr08(Map<String, String> paramMap);

    int update_cr08_Dtl(Map<String, String> paramMap);

	int update_cr08_Conf(Map<String, String> paramMap);

	int update_cr08_Del(Map<String, String> paramMap);

	//DATA DELETE
	int delete_cr08(Map<String, String> paramMap);

    int delete_cr08_Dtl(Map<String, String> paramMap);
    
    int delete_cr08_Dtl_All(Map<String, String> paramMap);
}
