package com.dksys.biz.user.bm.bm13.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface BM13Mapper {
	
	//결재선리스트
	int selectApprovalListCount(Map<String, String> paramMap);

	List<Map<String, String>> selectApprovalList(Map<String, String> paramMap);
	
}