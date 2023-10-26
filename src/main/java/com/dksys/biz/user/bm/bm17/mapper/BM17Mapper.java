package com.dksys.biz.user.bm.bm17.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM17Mapper {
	
	//결재선리스트
	int selectMessageTemplListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectMessageTemplList(Map<String, String> paramMap);

	List<Map<String, String>> selectMessageTemplExcelList(Map<String, String> paramMap);

	String selectMaxFileTrgtKey(Map<String, String> paramMap);	
		
	//결재선 등록, 수정, 삭제
	int insertMessageTempl(Map<String, String> paramMap);
	
	int updateMessageTempl(Map<String, String> paramMap);
	
	int deleteMessageTempl(Map<String, String> paramMap);	
	
	//테스트 버튼 등록
	int insertKakaoMessage(Map<String, String> paramMap);
	
	Map<String, Object> selectMessageTemplInfo(Map<String, String> paramMap);
}
