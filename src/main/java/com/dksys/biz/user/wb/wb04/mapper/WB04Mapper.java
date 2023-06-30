package com.dksys.biz.user.wb.wb04.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Mapper
public interface WB04Mapper {
	int selectWbsPlanCount(Map<String, String> paramMap);

	List<Map<String, String>> selectWbsPlanNoList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsSalesCodeList(Map<String, String> paramMap);
	
	int selectWbsPlanCloseYnNTreeListCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanCloseYnNTreeList(Map<String, String> paramMap);
	
	List<Map<String, String>> selectWbsPlanCloseYnNTreeListSub(Map<String, String> paramMap);
}