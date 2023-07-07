package com.dksys.biz.user.bm.bm11.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM11Mapper {
    // 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

    // 수정화면 정보
	Map<String, String> select_bm11_Info(Map<String, String> paramMap);
	
	// 수정화면 상세정보
	List<Map<String, String>> select_bm11_Info_Dtl(Map<String, String> paramMap);
	
	// fileTrgtKey 생성
	int select_bm11_SeqNext(Map<String, String> paramMap);

	// 증복체크
	Map<String, String> DupChk_BM11M01(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_bm11(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_bm11(Map<String, String> paramMap);

	//DATA DELETE
	int delete_bm11(Map<String, String> paramMap);
}
