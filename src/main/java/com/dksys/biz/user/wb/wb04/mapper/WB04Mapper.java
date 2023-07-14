package com.dksys.biz.user.wb.wb04.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB04Mapper {
	
	List<Map<String, String>> selectWbsLeftSalesCodeTreeList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanTreeList(Map<String, String> paramMap);
	
    List<Map<String, String>> selectGanttList(Map<String, String> paramMap);

}