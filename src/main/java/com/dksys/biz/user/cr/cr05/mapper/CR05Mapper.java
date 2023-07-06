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
	
	int insert_cr05(Map<String, String> paramMap);
	
	int select_cr05_SeqNext(Map<String, String> paramMap);
	
	int update_cr05(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	int update_cr05_Dtl(Map<String, String> dtl);
	
	int delete_cr05_Dtl_All(Map<String, String> paramMap);
	int delete_cr05(Map<String, String> paramMap);
	int delete_cr05_Dtl(Map<String, String> paramMap);

}
