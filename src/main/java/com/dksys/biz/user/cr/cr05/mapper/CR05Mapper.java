package com.dksys.biz.user.cr.cr05.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface CR05Mapper {
	// 수금 조회
	int selectClmnListCount(Map<String, String> paramMap);
	List<Map<String, String>> selectClmnList(Map<String, String> paramMap);
	
	// 수금정보 조회
	Map<String, String> select_cr05_Info(Map<String, String> paramMap);
	List<Map<String, String>> select_cr05_Info_Dtl(Map<String, String> paramMap);
	// 팝업 입력대상 검색
	List<Map<String, String>> select_insert_target_modal(Map<String, String> paramMap);
	// 수금번호 조회
	String select_cr05_clmnNo(Map<String, String> paramMap);
	// 수금유형 검색
	List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap);
	// 계좌번호 검색
	List<Map<String, Object>> selectBkacCd(Map<String, String> paramMap);
	//DATA INSERT
	int insert_cr05(Map<String, String> paramMap);
	int insert_cr05_Dtl(Map<String, String> paramMap);
	
	int select_cr05_SeqNext(Map<String, String> paramMap);
	//DATA UPDATE
	int update_cr05(Map<String, String> paramMap);
	int update_cr05_Dtl(Map<String, String> paramMap);
	//DATA DELETE
	int delete_cr05_Dtl_All(Map<String, String> paramMap);
	int delete_cr05(Map<String, String> paramMap);
	int delete_cr05_Dtl(Map<String, String> paramMap);
	
	

}
