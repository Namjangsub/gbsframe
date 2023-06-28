package com.dksys.biz.user.bm.bm05.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM05Mapper {
	// 그리드 카운트
	int grid1_selectCount(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);

	// 팝업 그리드 리스트
	int MatModal_selectCount(Map<String, String> paramMap);
	List<Map<String, String>> MatModal_selectList(Map<String, String> paramMap);

	// 수정화면 정보
	Map<String, String> select_bm05_Info(Map<String, String> paramMap);

	// 자재코드 품번 조회
	List<Map<String, String>> selectMatrCd(Map<String, String> paramMap);
	
	// fileTrgtKey 생성
	int select_bm05_SeqNext(Map<String, String> paramMap);
	
	//DATA INSERT
	int insert_bm05(Map<String, String> paramMap);
	//int insertBmMstr(Map<String, String> paramMap);
	
	//DATA UPDATE
	int update_bm05(Map<String, String> paramMap);
	//int updateBmMstr(Map<String, String> paramMap);

	//DATA DELETE
	int delete_bm05(Map<String, String> paramMap);

	// 증복체크
	Map<String, String> selectMatrCdChk(Map<String, String> paramMap);

	// 삭제체크
	Map<String, String> deleteMatrCdChk(Map<String, String> paramMap);
}
