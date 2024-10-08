package com.dksys.biz.user.fi.fi03.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface FI03Mapper {
    // 그리드 카운트
	int select_fi03_Count(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> select_fi03_List(Map<String, String> paramMap);

    // // 수금유형 검색
	// List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap);
}
