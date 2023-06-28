package com.dksys.biz.user.bm.bm10.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BM10Mapper {
	// 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);
	
	// 그리드 카운트
	int ProdModal_selectCount(Map<String, String> paramMap);
	
	// 팝업 그리드 리스트
	List<Map<String, String>> ProdModal_selectList(Map<String, String> paramMap);
	
	// 수정화면 정보
	Map<String, String> select_bm10_Info(Map<String, String> paramMap);
	
	// fileTrgtKey 생성
	int select_bm10_SeqNext(Map<String, String> paramMap);
	
	// DATA INSERT
	int insert_bm10(Map<String, String> paramMap);
	
	// DATA UPDATE
	int update_bm10(Map<String, String> paramMap);
	
	// DATA DELETE
	int delete_bm10(Map<String, String> paramMap);
}