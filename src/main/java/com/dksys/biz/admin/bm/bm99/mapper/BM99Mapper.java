package com.dksys.biz.admin.bm.bm99.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BM99Mapper {
	
	int selectManualCount(Map<String, String> paramMap);
	
	Map<String, Object> selectManualInfo(Map<String, String> paramMap);
	
	int insertManual(Map<String, String> paramMap);
	
	int updateManual(Map<String, String> paramMap);
	
}
