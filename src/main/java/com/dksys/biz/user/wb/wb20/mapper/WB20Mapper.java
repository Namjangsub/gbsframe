package com.dksys.biz.user.wb.wb20.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB20Mapper {
	
	int selectToDoCount(Map<String, String> paramMap);

	List<Map<String, String>> selectToDoList(Map<String, String> paramMap);
	
	int toDoCfDtUpdate(Map<String, String> paramMap);
	
}