package com.dksys.biz.user.bm.bm05.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM05Mapper {
	
	// 자재마스터 조회
	int selectBmMstrCount(Map<String, String> paramMap);
	List<Map<String, String>> selectBmMstrList(Map<String, String> paramMap);
	// 자재코드 품번 조회
	List<Map<String, String>> selectMatrCd(Map<String, String> paramMap);
	// 상세 입력
	int insertBmMstr(Map<String, String> paramMap);
	// 상세 수정
	int updateBmMstr(Map<String, String> paramMap);
	// 증복체크
	String selectMatrCdChk(Map<String, String> paramMap);
	// 자재마스터 팝업 조회
	int selectMatListCount(Map<String, String> paramMap);
	List<Map<String, String>> selectMatList(Map<String, String> paramMap);
	
}
