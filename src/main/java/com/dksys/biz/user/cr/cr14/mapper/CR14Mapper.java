package com.dksys.biz.user.cr.cr14.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface CR14Mapper {
    // 그리드 카운트
	int select_cr14_Count(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> select_cr14_List(Map<String, String> paramMap);

    // // 수금유형 검색
	// List<Map<String, Object>> selectPmntmtdCd(Map<String, String> paramMap);
}
