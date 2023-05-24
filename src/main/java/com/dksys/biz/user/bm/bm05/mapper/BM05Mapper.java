package com.dksys.biz.user.bm.bm05.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM05Mapper {
	
	int selectBmMstrCount(Map<String, String> paramMap);

	List<Map<String, String>> selectBmMstrList(Map<String, String> paramMap);

	String insertBmMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);

	int updateBmMstr(Map<String, String> paramMap, MultipartHttpServletRequest mRequest);
	
}
