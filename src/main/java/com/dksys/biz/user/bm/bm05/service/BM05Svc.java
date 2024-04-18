package com.dksys.biz.user.bm.bm05.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface BM05Svc {

	// 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);
	
	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);
	
	// 팝업 그리드 리스트
	int MatModal_selectCount(Map<String, String> paramMap);
	List<Map<String, String>> MatModal_selectList(Map<String, String> paramMap);
	
	// 수정화면 정보
	Map<String, String> select_bm05_Info(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_bm05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA UPDATE
	int update_bm05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception;
	
	//DATA DELETE
	int delete_bm05(Map<String, String> paramMap) throws Exception;
	
	// 자재마스터 품번 중복 체크 조회
	Map<String, String> selectMatrCdChk(Map<String, String> paramMap);

	// 자재마스터 품번 삭제 체크 조회
	Map<String, String> deleteMatrCdChk(Map<String, String> paramMap);

	// 자재마스터 설계 BOM에서 형번/규격 검색용
	List<Map<String, String>> BOM_selectMatrMnoList(Map<String, String> paramMap);
}
