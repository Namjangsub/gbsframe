package com.dksys.biz.admin.bm.bm16.mapper;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BM16Mapper {
	
	int selectPrjctCount(Map<String, String> paramMap);
	
	List<Map<String, String>> selectPrjctList(Map<String, String> paramMap);

}