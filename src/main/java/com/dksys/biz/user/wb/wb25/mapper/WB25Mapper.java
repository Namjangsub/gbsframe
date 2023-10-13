package com.dksys.biz.user.wb.wb25.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB25Mapper {
	
	int selectWbsTaskEvlCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsTaskEvlList(Map<String, String> paramMap);
		
	int selectTaskEvlSeqNext(Map<String, String> paramMap);
	
    int wbsTaskEvlInsert(Map<String, String> paramMap);	
	
	int wbsTaskEvlUpdate(Map<String, String> paramMap);
	
	int wbsTaskEvlCloseYnConfirm(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsTaskEvlResultList(Map<String, String> paramMap);
	
	
}