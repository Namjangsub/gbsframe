package com.dksys.biz.user.sm.sm19.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface SM19Mapper {
    // 그리드 카운트
	int select_sm19_Count(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> select_sm19_List(Map<String, String> paramMap);

    // 창고검색
	List<Map<String, Object>> selectWhCd(Map<String, String> paramMap);

	// 수불유형
	List<Map<String, Object>> selectinoutDiv(Map<String, String> paramMap);
}
