package com.dksys.biz.user.sm.sm20.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SM20Mapper {
	List<Map<String, String>> sm20_main_grid1_selectList(Map<String, String> paramMap);

	List<Map<String, String>> sm20_main_grid2_selectList(Map<String, String> paramMap);

	List<Map<String, String>> sm20_main_grid3_selectList(Map<String, String> paramMap);

	int update_sm20_payYn(Map<String, String> param);
	
	int grid1_selectCount(Map<String, String> paramMap);
	
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

	// 팝업 입력대상 검색
	List<Map<String, String>> select_sm20_insert_target_modal(Map<String, String> paramMap);

	// 수정화면 정보
	Map<String, String> select_sm20_Info(Map<String, String> paramMap);

	// 수정화면 상세정보
	List<Map<String, String>> select_sm20_Info_Dtl(Map<String, String> paramMap);

	// fileTrgtKey 생성
	int select_sm20_SeqNext(Map<String, String> paramMap);

    // IOKey 생성
	int select_sm20_Ioseq(Map<String, String> paramMap);

    // 관리번호 생성
    String select_sm20_Next_MNGM_NO(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_sm20(Map<String, String> paramMap);

    int insert_sm20_Dtl(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_sm20(Map<String, String> paramMap);

    int update_sm20_Dtl(Map<String, String> paramMap);

	int update_sm20_Conf(Map<String, String> paramMap);

	int update_sm20_Del(Map<String, String> paramMap);

	//DATA DELETE
	int delete_sm20(Map<String, String> paramMap);

    int delete_sm20_Dtl(Map<String, String> paramMap);

	List<Map<String, String>> select_prjct_code(Map<String, String> paramMap);

	List<Map<String, String>> select_mngId_code(Map<String, String> paramMap);

	int update_sm20_payCompleteChke(Map<String, String> paramMap);
}




