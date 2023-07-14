package com.dksys.biz.user.cr.cr11.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CR11Mapper {

	// 그리드 카운트
		int grid1_selectCount(Map<String, String> paramMap);

		// 그리드 리스트
		List<Map<String, String>> grid1_selectList(Map<String, String> paramMap);
	
}