package com.dksys.biz.user.cr.cr12.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface CR12Mapper {
    // 그리드 카운트
	int select_cr12_Count(Map<String, String> paramMap);

	// 그리드 리스트
	List<Map<String, String>> select_cr12_List(Map<String, String> paramMap);
}
