package com.dksys.biz.user.bm.bm11.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM11Mapper {
	
	int selectBmUprMstrCount(Map<String, String> paramMap);

	List<Map<String, String>> selectBmUprMstrList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectUprHsMstrList(Map<String, String> paramMap);

	int insertBmUprMstr(Map<String, String> paramMap);

	int UpdateBmUprMstr(Map<String, String> paramMap);
	
}
