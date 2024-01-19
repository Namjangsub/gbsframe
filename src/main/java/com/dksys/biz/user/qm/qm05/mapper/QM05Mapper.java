package com.dksys.biz.user.qm.qm05.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QM05Mapper {

	int selectMainGridListCount(Map<String, String> paramMap);
	
	//그리드 리스트
	List<Map<String, String>> selectMainGridList(Map<String, String> paramMap);

	List<Map<String, String>> select_dept_code(Map<String, String> paramMap);
}
